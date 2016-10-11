package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.jragnarok.JRagnarokConstants.DATE_FORMAT;
import static org.diverproject.jragnarok.JRagnarokUtil.binToHex;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.loginMessage;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Encrypt;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Salt;
import static org.diverproject.jragnarok.JRagnarokUtil.random;
import static org.diverproject.jragnarok.JRagnarokUtil.skip;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_CONNECT_INFO_CHANGED;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_EXE_HASHCHECK;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN2;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN3;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN4;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN_HAN;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN_PCBANG;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_REQ_CHAR_CONNECT;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_REQ_HASH;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_SSO_LOGIN_REQ;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.RequestCharConnectPacket;
import org.diverproject.jragnarok.packets.AcknologeHash;
import org.diverproject.jragnarok.packets.KeepAlivePacket;
import org.diverproject.jragnarok.packets.LoginHan;
import org.diverproject.jragnarok.packets.LoginMD5;
import org.diverproject.jragnarok.packets.LoginMD5Info;
import org.diverproject.jragnarok.packets.LoginMD5Mac;
import org.diverproject.jragnarok.packets.LoginPCBang;
import org.diverproject.jragnarok.packets.LoginPacket;
import org.diverproject.jragnarok.packets.LoginSingleSignOn;
import org.diverproject.jragnarok.packets.ReceivePacketIDPacket;
import org.diverproject.jragnarok.packets.RefuseLoginBytePacket;
import org.diverproject.jragnarok.packets.RefuseLoginIntPacket;
import org.diverproject.jragnarok.packets.ReponseCharConnectPacket;
import org.diverproject.jragnarok.packets.UpdateClientHashPacket;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.ServerState;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.login.structures.CharServerType;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.jragnarok.server.login.structures.ClientHash;
import org.diverproject.jragnarok.server.login.structures.ClientType;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.Time;
import org.diverproject.util.lang.HexUtil;
import org.diverproject.util.lang.IntUtil;

public class LoginClientService extends LoginServerService
{
	private LoginService login;
	private LoginLogService log;
	private LoginIpBanService ipban;
	private LoginCharacterService character;
	private AccountController accountController;

	public LoginClientService(LoginServer server)
	{
		super(server);
	}

	public void init() throws RagnarokException
	{
		log = getServer().getLogService();
		ipban = getServer().getIpBanService();
		login = getServer().getLoginService();
		character = getServer().getCharService();

		accountController = new AccountController(getConnection());
	}

	public FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logInfo("Conexão fechada (ip: %s).\n", fd.getAddressString());
				return false;
			}

			if (fd.getCache() == null)
			{
				if (getConfigs().getBool("ipban.enabled") && ipban.isBanned(fd.getAddressString()))
				{
					log("conexão recusada, ip não autorizado (ip: %s).\n", fd.getAddressString());

					log.addLoginLog(fd.getAddressString(), null, -3, "ip banned");
					skip(fd, false, 23);

					RefuseLoginBytePacket refuseLoginPacket = new RefuseLoginBytePacket();
					refuseLoginPacket.setResult(AuthResult.REJECTED_FROM_SERVER);
					refuseLoginPacket.setBlockDate("");
					refuseLoginPacket.send(fd);

					fd.close();
				}
			}

			LoginSessionData sd = new LoginSessionData(fd);

			ReceivePacketIDPacket packetReceivePacketID = new ReceivePacketIDPacket();
			packetReceivePacketID.receive(fd);

			short command = packetReceivePacketID.getPacketID();

			switch (command)
			{
				case PACKET_CA_CONNECT_INFO_CHANGED:
					keepAlive(fd);
					break;

				case PACKET_CA_EXE_HASHCHECK:
					updateClientHash(fd, sd);
					break;

				// Solicitação de acesso senha crua
				case PACKET_CA_LOGIN:
				case PACKET_CA_LOGIN_PCBANG:
				case PACKET_CA_LOGIN_HAN:
				case PACKET_CA_SSO_LOGIN_REQ:
				// Solicitação de acesso senha MD5
				case PACKET_CA_LOGIN2:
				case PACKET_CA_LOGIN3:
				case PACKET_CA_LOGIN4:
					return requestAuth(fd, sd, command);

				case PACKET_CA_REQ_HASH:
					parseRequestKey(fd, sd);
					break;

				case PACKET_CA_REQ_CHAR_CONNECT:
					requestCharConnect(fd, sd);
					break;

				default:
					String packet = HexUtil.parseInt(command, 4);
					String address = fd.getAddressString();
					logNotice("fim de conexão inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
					fd.close();
					return false;
			}

			return true;
		}
	};

	private void keepAlive(FileDescriptor fd)
	{
		KeepAlivePacket keepAlivePacket = new KeepAlivePacket();
		keepAlivePacket.receive(fd);
	}

	private void updateClientHash(FileDescriptor fd, LoginSessionData sd)
	{
		UpdateClientHashPacket updateClientHashPacket = new UpdateClientHashPacket();
		updateClientHashPacket.receive(fd);

		sd.setClientHash(new ClientHash());
		sd.getClientHash().set(updateClientHashPacket.getHashValue());
	}

	private void sentAuthResult(LoginSessionData sd, AuthResult result)
	{
		RefuseLoginBytePacket refuseLoginPacket = new RefuseLoginBytePacket();
		refuseLoginPacket.setResult(result);
		refuseLoginPacket.send(sd.getFileDecriptor());
	}

	private void parseRequestKey(FileDescriptor fd, LoginSessionData sd)
	{
		short md5KeyLength = (short) (12 + (random() % 4));
		String md5Key = md5Salt(md5KeyLength);

		AcknologeHash packet = new AcknologeHash();
		packet.setMD5KeyLength(md5KeyLength);
		packet.setMD5Key(md5Key);
		packet.send(fd);
	}

	private boolean requestAuth(FileDescriptor fd, LoginSessionData sd, short command)
	{
		boolean usingRawPassword = true;

		switch (command)
		{
			case PACKET_CA_LOGIN:
				LoginPacket loginPacket = new LoginPacket();
				loginPacket.receive(fd);
				sd.setVersion(loginPacket.getVersion());
				sd.setClientType(ClientType.parse(loginPacket.getClientType()));
				sd.setUsername(loginPacket.getUsername());
				sd.setPassword(loginPacket.getPassword());
				break;

			case PACKET_CA_LOGIN_PCBANG:
				LoginPCBang loginPCBang = new LoginPCBang();
				loginPCBang.receive(fd);
				sd.setVersion(loginPCBang.getVersion());
				sd.setClientType(ClientType.parse(loginPCBang.getClientType()));
				sd.setUsername(loginPCBang.getUsername());
				sd.setPassword(loginPCBang.getPassword());
				break;

			case PACKET_CA_LOGIN_HAN:
				LoginHan loginHan = new LoginHan();
				loginHan.receive(fd);
				sd.setVersion(loginHan.getVersion());
				sd.setClientType(ClientType.parse(loginHan.getClientType()));
				sd.setUsername(loginHan.getUsername());
				sd.setPassword(loginHan.getPassword());
				break;

			case PACKET_CA_SSO_LOGIN_REQ:
				LoginSingleSignOn loginSingleSignOn = new LoginSingleSignOn();
				loginSingleSignOn.receive(fd);
				sd.setVersion(loginSingleSignOn.getVersion());
				sd.setClientType(ClientType.parse(loginSingleSignOn.getClientType()));
				sd.setUsername(loginSingleSignOn.getUsername());
				sd.setPassword(loginSingleSignOn.getToken());
				break;

			case PACKET_CA_LOGIN2:
				LoginMD5 loginMD5 = new LoginMD5();
				loginMD5.receive(fd);
				sd.setVersion(loginMD5.getVersion());
				sd.setUsername(loginMD5.getUsername());
				sd.setPassword(loginMD5.getPassword());
				sd.setClientType(ClientType.parse(loginMD5.getClientType()));
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN3:
				LoginMD5Info loginMD5Info = new LoginMD5Info();
				loginMD5Info.receive(fd);
				sd.setVersion(loginMD5Info.getVersion());
				sd.setUsername(loginMD5Info.getUsername());
				sd.setPassword(loginMD5Info.getPassword());
				sd.setClientType(ClientType.parse(loginMD5Info.getClientType()));
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN4:
				LoginMD5Mac loginMD5Mac = new LoginMD5Mac();
				loginMD5Mac.receive(fd);
				sd.setVersion(loginMD5Mac.getVersion());
				sd.setUsername(loginMD5Mac.getUsername());
				sd.setPassword(loginMD5Mac.getPassword());
				sd.setClientType(ClientType.parse(loginMD5Mac.getClientType()));
				usingRawPassword = false;
				break;
		}

		if (usingRawPassword)
		{
			logNotice("solicitação de conexão de %s (ip: %s, version: %d)\n", sd.getUsername(), sd.getAddressString(), sd.getVersion());

			if (getConfigs().getBool("login.use_md5_password"))
				sd.setPassword(md5Encrypt(sd.getPassword()));

			sd.getPassDencrypt().setValue(0);
		}

		else
		{
			log("solicitação de conexão passdenc de %s (ip: %s, version: %d)\n", sd.getUsername(), sd.getAddressString(), sd.getVersion());

			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT);
			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT2);
			sd.setPassword(binToHex(sd.getPassword(), 16));
		}

		if (sd.getPassDencrypt().getValue() != 0 && getConfigs().getBool("login.use_md5_password"))
		{
			sentAuthResult(sd, AuthResult.REJECTED_FROM_SERVER);
			return false;
		}

		AuthResult result = login.mmoAuth(sd, false);

		if (result == AuthResult.OK)
		{
			authOk(sd);
			return true;
		}

		authFailed(sd, result);

		return false;
	}

	private void authOk(LoginSessionData sd)
	{
		
	}

	private void authFailed(LoginSessionData sd, AuthResult result)
	{
		if (getConfigs().getBool("log.login"))
		{
			if (IntUtil.interval(result.CODE, 0, 15))
				log.addLoginLog(sd.getAddressString(), sd, result.CODE, loginMessage(result.CODE));

			else if (IntUtil.interval(result.CODE, 99, 104))
				log.addLoginLog(sd.getAddressString(), sd, result.CODE, loginMessage(result.CODE-83));

			else
				log.addLoginLog(sd.getAddressString(), sd, result.CODE, loginMessage(22));
		}

		if (result.CODE == 0 || result.CODE == 1)
			ipban.addBanLog(sd.getAddressString());

		String blockDate = "";

		if (result == AuthResult.BANNED_UNTIL)
		{
//			try {

				Time unbanTime = new Time(System.currentTimeMillis() + 3600);//accountController.getBanTime(sd.getUsername());
				blockDate = unbanTime.toStringFormat(DATE_FORMAT);

//			} catch (RagnarokException e) {
//
//				logError("falha ao obter tempo de ban, enviando mensagem indefinida:\n");
//				logExeception(e);
//
//				blockDate = "Falha de conexão";
//
//			}
		}

		if (sd.getVersion() >= dateToVersion(20120000))
		{
			RefuseLoginIntPacket packet = new RefuseLoginIntPacket();
			packet.setBlockDate(blockDate);
			packet.setCode(result);
			packet.send(sd.getFileDecriptor());
		}

		else
		{
			RefuseLoginBytePacket packet = new RefuseLoginBytePacket();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(sd.getFileDecriptor());
		}
	}

	private void requestCharConnect(FileDescriptor fd, LoginSessionData sd)
	{
		RequestCharConnectPacket rccPacket = new RequestCharConnectPacket();
		rccPacket.receive(fd);

		sd.setUsername(rccPacket.getUsername());
		sd.setPassword(rccPacket.getPassword());

		if (getConfigs().getBool("login.user_md5_password"))
			sd.setPassword(md5Encrypt(sd.getPassword()));

		sd.getPassDencrypt().setValue(0);
		sd.setVersion(getConfigs().getInt("login.version"));

		String serverName = rccPacket.getServerName();
		int serverIP = rccPacket.getServerIP();
		short serverPort = rccPacket.getServerPort();
		short type = rccPacket.getType();
		short newValue = rccPacket.getNewValue();

		logInfo("conexão solicitada do servidor de personagens %s@%s (account: %s, pass: %s, ip: %s)", serverName, serverIP, sd.getUsername(), sd.getPassword(), fd.getAddressString());

		String message = format("charserver - %s@%s:%d", serverName, SocketUtil.socketIP(serverIP), serverPort);
		log.addLoginLog(fd.getAddressString(), sd, 100, message);

		AuthResult result = login.mmoAuth(sd, true);

		if (getServer().isState(ServerState.RUNNING) &&
			result == AuthResult.OK &&
			sd.getID() < getServer().getCharServers().size() &&
			fd.isConnected())
		{
			log("conexão do servidor de personagens '%s' aceita.\n", serverName);

			ClientCharServer server = new ClientCharServer(fd);
			server.setFileDecriptor(fd);
			server.setName(serverName);
			server.setServerIP(new InternetProtocol(serverIP));
			server.setPort(serverPort);
			server.setUsers((short) 0);
			server.setType(CharServerType.parse(type));
			server.setNewValue(newValue);
			getServer().getCharServers().add(server);

			fd.setParseListener(character.parse);
			fd.getFlag().setServer((byte) 1);

			ReponseCharConnectPacket packet = new ReponseCharConnectPacket();
			packet.setResult(AuthResult.OK);
			packet.send(fd);
		}

		else
		{
			logNotice("Conexão com o servidor de personagens '%s' RECUSADA.\n", serverName);

			ReponseCharConnectPacket packet = new ReponseCharConnectPacket();
			packet.setResult(AuthResult.REJECTED_FROM_SERVER);
			packet.send(fd);
		}
	}
}

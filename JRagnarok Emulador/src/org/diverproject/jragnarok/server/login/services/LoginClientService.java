package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.jragnarok.JRagnarokUtil.binToHex;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
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
import org.diverproject.jragnarok.packets.RefuseLoginPacket;
import org.diverproject.jragnarok.packets.UpdateClientHashPacket;
import org.diverproject.jragnarok.server.FileDecriptor;
import org.diverproject.jragnarok.server.FileDecriptorListener;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.ServerState;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.login.structures.CharServerType;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.jragnarok.server.login.structures.ClientHash;
import org.diverproject.jragnarok.server.login.structures.ClientType;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;
import org.diverproject.jragnarok.server.login.structures.Sex;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.lang.HexUtil;

public class LoginClientService extends LoginServerService
{
	private LoginService login;
	private LoginLogService log;
	private LoginIpBanService ipban;
	private LoginCharacterService character;

	public LoginClientService(LoginServer server)
	{
		super(server);

		log = server.getLogService();
		ipban = server.getIpBanService();
		login = server.getLoginService();
		character = server.getCharService();
	}

	public FileDecriptorListener parse = new FileDecriptorListener()
	{
		@Override
		public void onCall(FileDecriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logInfo("Conex�o fechada (ip: %s).\n", fd.getAddressString());
				return;
			}

			if (fd.getCache() == null)
			{
				if (getConfigs().getBool("ipban.enabled") && ipban.isBanned(fd.getAddressString()))
				{
					log("conex�o recusada, ip n�o autorizado (ip: %s).\n", fd.getAddressString());

					log.addLoginLog(fd.getAddressString(), null, -3, "ip banned");
					skip(fd, false, 23);

					RefuseLoginPacket refuseLoginPacket = new RefuseLoginPacket();
					refuseLoginPacket.setCode(RefuseLoginPacket.REJECTED_FROM_SERVER);
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

				// Solicita��o de acesso senha crua
				case PACKET_CA_LOGIN:
				case PACKET_CA_LOGIN_PCBANG:
				case PACKET_CA_LOGIN_HAN:
				case PACKET_CA_SSO_LOGIN_REQ:
				// Solicita��o de acesso senha MD5
				case PACKET_CA_LOGIN2:
				case PACKET_CA_LOGIN3:
				case PACKET_CA_LOGIN4:
					requestAuth(fd, sd, command);
					break;

				case PACKET_CA_REQ_HASH:
					parseRequestKey(fd, sd);
					break;

				case PACKET_CA_REQ_CHAR_CONNECT:
					requestCharConnect(fd, sd);
					break;

				default:
					String packet = HexUtil.parseInt(command, 4);
					String address = fd.getAddressString();
					logNotice("fim de conex�o inesperado (pacote: %s, ip: %s)", packet, address);
					fd.close();
			}
		}
	};

	private void keepAlive(FileDecriptor fd)
	{
		KeepAlivePacket keepAlivePacket = new KeepAlivePacket();
		keepAlivePacket.receive(fd);
	}

	private void updateClientHash(FileDecriptor fd, LoginSessionData sd)
	{
		UpdateClientHashPacket updateClientHashPacket = new UpdateClientHashPacket();
		updateClientHashPacket.receive(fd);

		sd.setClientHash(new ClientHash());
		sd.getClientHash().set(updateClientHashPacket.getHashValue());
	}

	private boolean requestAuth(FileDecriptor fd, LoginSessionData sd, short command)
	{
		boolean usingRawPassword = false;

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
			log("solicita��o de conex�o de %s (ip: %s, version: %d)", sd.getUsername(), sd.getAddressString(), sd.getVersion());

			if (getConfigs().getBool("login.use_md5_password"))
				sd.setPassword(md5Encrypt(sd.getPassword()));

			sd.getPassDencrypt().setValue(0);
		}

		else
		{
			log("solicita��o de conex�o passdenc de %s (ip: %s, version: %d)", sd.getUsername(), sd.getAddressString(), sd.getVersion());

			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT);
			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT2);
			sd.setPassword(binToHex(sd.getPassword(), 16));
		}

		if (sd.getPassDencrypt().getValue() != 0 && getConfigs().getBool("login.use_md5_password"))
		{
			sentAuthResult(sd, RefuseLoginPacket.REJECTED_FROM_SERVER);
			return false;
		}

		AuthResult result = login.mmoAuth(sd, false);

		if (result == AuthResult.OK)
			authOk(sd);
		else
			authFailed(sd, result);

		return true;
	}

	private void authOk(LoginSessionData sd)
	{
		
	}

	private void authFailed(LoginSessionData sd, AuthResult result)
	{

	}

	private void sentAuthResult(LoginSessionData sd, byte code)
	{
		RefuseLoginPacket refuseLoginPacket = new RefuseLoginPacket();
		refuseLoginPacket.setCode(code);
		refuseLoginPacket.send(sd.getFileDecriptor());
	}

	private void parseRequestKey(FileDecriptor fd, LoginSessionData sd)
	{
		short md5KeyLength = (short) (12 + (random() % 4));
		String md5Key = md5Salt(md5KeyLength);

		AcknologeHash packet = new AcknologeHash();
		packet.setMD5KeyLength(md5KeyLength);
		packet.setMD5Key(md5Key);
		packet.send(fd);
	}

	private void requestCharConnect(FileDecriptor fd, LoginSessionData sd)
	{
		RequestCharConnectPacket requestCharConnectPacket = new RequestCharConnectPacket();
		requestCharConnectPacket.receive(fd);

		sd.setUsername(requestCharConnectPacket.getUsername());
		sd.setPassword(requestCharConnectPacket.getPassword());

		if (getConfigs().getBool("login.user_md5_password"))
			sd.setPassword(md5Encrypt(sd.getPassword()));

		sd.getPassDencrypt().setValue(0);
		sd.setVersion(getConfigs().getInt("login.version"));

		String serverName = requestCharConnectPacket.getServerName();
		int serverIP = requestCharConnectPacket.getServerIP();
		short serverPort = requestCharConnectPacket.getServerPort();
		short type = requestCharConnectPacket.getType();
		short newValue = requestCharConnectPacket.getNewValue();

		logInfo("conex�o solicitada do servidor de personagens %s@%s (account: %s, pass: %s, ip: %s)", serverName, serverIP, sd.getUsername(), sd.getPassword(), fd.getAddressString());

		String message = format("charserver - %s@%s:%d", serverName, SocketUtil.socketIP(serverIP), serverPort);
		log.addLoginLog(fd.getAddressString(), sd, 100, message);

		AuthResult result = login.mmoAuth(sd, true);

		if (getServer().isState(ServerState.RUNNING) &&
			result == AuthResult.OK &&
			sd.getSex() == Sex.SERVER &&
			sd.getID() < getServer().getCharServers().size() &&
			fd.isConnected())
		{
			log("conex�o do servidor de personagens '%s' aceita.\n", serverName);

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
			fd.setFlag(FileDecriptor.FLAG_SERVER);

		}
	}
}

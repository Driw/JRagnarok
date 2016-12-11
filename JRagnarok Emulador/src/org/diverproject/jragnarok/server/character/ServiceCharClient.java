package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.MIN_CHARS;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_UNLIMITED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_ENABLED;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CHAR_SERVER_SELECTED;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.receive.AcknowledgePacket;
import org.diverproject.jragnarok.packets.response.NotifyAuth;
import org.diverproject.jragnarok.packets.response.PincodeSendState;
import org.diverproject.jragnarok.packets.response.PincodeSendState.PincodeState;
import org.diverproject.jragnarok.packets.response.RefuseEnter;
import org.diverproject.jragnarok.packets.response.SendAccountChars;
import org.diverproject.jragnarok.packets.response.SendAccountSlot;
import org.diverproject.jragnarok.packets.response.SendCharPageCount;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.character.control.OnlineCharControl;
import org.diverproject.jragnarok.server.character.structures.Character;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.jragnarok.server.character.structures.OnlineCharData;
import org.diverproject.jragnarok.server.common.NotifyAuthResult;
import org.diverproject.util.collection.Index;
import org.diverproject.util.lang.HexUtil;

/**
 * <h1>Serviço para Acesso dos Clientes</h1>
 *
 * <p>Esse serviço é a primeira fronteira do servidor para se comunicar com o cliente.
 * Nele será recebido uma nova conexão solicitada para com o servidor de personagem.
 * Após receber a conexão deverá redirecionar o cliente conforme o comando recebido.</p>
 *
 * <p>De acordo com o comando recebido, deverá redirecionar a outros serviços se necessário.
 * Fica sendo de sua responsabilidade garantir também o envio de pacotes para os clientes.
 * Podendo ainda ser necessário comunicar-se com outro serviço para auxiliá-lo.</p>
 *
 * @see AbstractCharService
 * @see ServiceCharServerAuth
 * @see ServiceCharLogin
 * @see OnlineCharControl
 *
 * @author Andrew
 */

public class ServiceCharClient extends AbstractCharService
{
	/**
	 * Instancia um novo serviço para recebimento de novos clientes no servidor.
	 * Para este serviço é necessário realizar chamados para iniciar e destruir.
	 * @param server servidor de personagem responsável por este serviço.
	 */

	public ServiceCharClient(CharServer server)
	{
		super(server);
	}

	/**
	 * Listener usado para receber novas conexões solicitadas com o servidor de personagem.
	 */

	public final FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			logDebug("parsing fd#%d.\n", fd.getID());

			CFileDescriptor cfd = (CFileDescriptor) fd;

			if (!fd.isConnected())
				return false;

			// Já foi autenticado, não deveria estar aqui
			if (fd.getFlag().is(FileDescriptor.FLAG_EOF) && parseAlreadyAuth(cfd))
				return true;

			return acknowledgePacket(cfd);
		}
	};

	/**
	 * Procedimento de chamado após a primeira análise do cliente em relação a sua sessão.
	 * Aqui o cliente será redirecionado corretamente conforme o tipo de comando solicitado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se deve manter a conexão ou false caso contrário.
	 */

	public boolean acknowledgePacket(CFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd, false);

		short command = packetReceivePacketID.getPacketID();

		switch (command)
		{
			case PACKET_CHAR_SERVER_SELECTED:
				return auth.parse(fd);

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logNotice("fim de conexão inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	/**
	 * Essa funcionalidade é chamada durante a análise de novos pacotes recebidos pelo cliente.
	 * Além disso a condição mínima para este chamado é que esteja em EOF (end of file).
	 * Deve verificar se o jogador (sessão) já foi autenticado e está online no sistema.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se foi autenticado ou false caso contrário (true fecha conexão).
	 */

	public boolean parseAlreadyAuth(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		// Client já autenticado
		if (sd != null && sd.isAuth())
		{
			OnlineCharData online = onlines.get(sd.getID());

			if (online != null && online.getFileDescriptor().getID() == fd.getID())
				online.setFileDescriptor(null);

			// Se não está em nenhum servidor deixar offline.
			if (online != null && online.getServer() == -1)
				onlines.remove(online);

			return true;
		}

		return false;
	}

	/**
	 * Notifica o cliente que houve algum problema após a autenticação do acesso.
	 * O acesso foi autenticado porém houve algum problema em liberar o acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da liberação do acesso para o cliente.
	 */

	public void sendNotifyResult(CFileDescriptor fd, NotifyAuthResult result)
	{
		logDebug("notificar resultado (fd: %d).\n", fd.getID());

		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Recusa a entrada de uma determinada sessão no servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado que será mostrado ao cliente.
	 */

	public void refuseEnter(CFileDescriptor fd, byte result)
	{
		logDebug("entrada rejeitada em (fd: %d).\n", fd.getID());

		RefuseEnter packet = new RefuseEnter();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Informa ao cliente que o sistema de código PIN está entrando no estado de:
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param sd dados da sessão do cliente com o servidor de personagem.
	 * @param state estado do qual o código PIN deve assumir.
	 */

	public void pincodeSendState(CFileDescriptor fd, PincodeState state)
	{
		logDebug("enviando estado de código pin (fd: %d).\n", fd.getID());

		CharSessionData sd = fd.getSessionData();
		sd.getPincode().genSeed();

		PincodeSendState packet = new PincodeSendState();
		packet.setPincodeSeed(sd.getPincode().getSeed());
		packet.setAccountID(sd.getID());
		packet.setState(state.CODE);
		packet.send(fd);
	}

	/**
	 * Envia todos os dados para a exibição da lista de personagens de uma conta de acordo com o cliente.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void sendCharList(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando lista de personagens de account#%d (fd: %d).\n", sd.getID(), fd.getID());

		if (sd.getVersion() >= dateToVersion(20130000))
		{
			sendAccountSlot(fd);
			sendAccountChars(fd);
			sendCharPageCount(fd);
		}

		else
			sendAccountChars(fd);

		if (sd.getVersion() >= dateToVersion(20060819))
			;
	}

	/**
	 * Envia ao cliente os dados relacionados a quantidade de slots disponíveis para personagens.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void sendAccountSlot(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados de slot de account#%d (fd: %d)", sd.getID(), fd.getID());

		SendAccountSlot packet = new SendAccountSlot();
		packet.setMinChars(b(MIN_CHARS));
		packet.setCharsVip(sd.getVip().getCharSlotCount());
		packet.setCharsBilling(sd.getVip().getCharBilling());
		packet.setCharsSlot(sd.getCharSlots());
		packet.setMaxChars(b(MAX_CHARS));
	}

	/**
	 * Envia todos os dados dos personagens da conta respectiva a sessão que foi estabelecida.
	 * Os dados são enviados ao cliente para que ele possa exibir a seleção do personagem.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	private void sendAccountChars(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados dos personagens de account#%d (fd: %d).\n", sd.getID(), fd.getID());

		for (int i = 0; i < MAX_CHARS; i++)
			sd.setCharData(null, i);

		if (sd.getVersion() >= dateToVersion(20100413))
		{
			try {

				Index<Character> characters = this.characters.list(sd.getID());

				SendAccountChars packet = new SendAccountChars();
				packet.setTotalSlots(MAX_CHARS);
				packet.setPremiumStartSlot(MIN_CHARS);
				packet.setPremiumEndSlot(b(MIN_CHARS + sd.getVip().getCharSlotCount()));
				packet.setDummyBeginBilling(b(0));
				packet.setFirstTime(0);
				packet.setSecondTime(0);
				packet.setDummyEndBilling(null);
				packet.setCode(0);
				packet.setCharMoveCount(sd.getCharactersMove());
				packet.setCharMoveEnabled(getConfigs().getBool(CHAR_MOVE_ENABLED));
				packet.setCharMoveUnlimited(getConfigs().getBool(CHAR_MOVE_UNLIMITED));
				packet.setCharacters(characters);
				packet.send(fd);

			} catch (RagnarokException e) {
				logError("falha ao carregar dados dos personagens (aid: %d):", sd.getID());
				logExeception(e);
			}
		}
	}

	/**
	 * Envia ao cliente a quantidade de páginas disponíveis para seleção de personagens.
	 * Cada página é composta por 3 slots para alocação de um único personagem no mesmo.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void sendCharPageCount(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		SendCharPageCount packet = new SendCharPageCount();
		packet.setCharSlots(sd.getCharSlots());
		packet.setPageCount(sd.getCharSlots() / 3);
		packet.send(fd);
	}

	// TODO chclif_char_delete2_ack
	// TODO chclif_char_delete2_accept_ack
	// TODO chclif_char_delete2_cancel_ack
	// TODO chclif_parse_char_delete2_req
	// TODO chclif_delchar_check
	// TODO chclif_parse_char_delete2_accept
	// TODO chclif_parse_char_delete2_cancel
	// TODO chclif_parse_maplogin
	// TODO chclif_parse_reqtoconnect
	// TODO chclif_parse_req_charlist
	// TODO chclif_parse_charselect
	// TODO chclif_parse_createnewchar
	// TODO chclif_refuse_delchar
	// TODO chclif_parse_delchar
	// TODO chclif_parse_reqrename
	// TODO charblock_timer
	// TODO chclif_block_character
	// TODO chclif_parse_ackrename
	// TODO chclif_ack_captcha
	// TODO chclif_reject
	// TODO chclif_parse_reqcaptcha
	// TODO chclif_parse_chkcaptcha

	public boolean isPincodeAllowed(String pincode)
	{
		// TODO pincode_allowed
		return false;
	}

	public void parsePincodeSetNew(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("definindo primeiro código PIN (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_setnew
	}

	public void parsePincodeChange(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("alterando código PIN existente (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_change
	}

	public void parsePincodeCheck(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("recebendo código PIN inserido (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_check
	}

	public void reqPincodeWindow(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("solicitar tela para digitar código PIN (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_reqpincode_window
	}

	public void sendPincodeState(CFileDescriptor fd, PincodeState state)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando código PIN paraa atuar em %s (fd: %d, aid: %d).\n", state, fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_pincode_sendstate
	}

	public void parseMoveCharSlot(CFileDescriptor fd)
	{
		// TODO chclif_parse_moveCharSlot
	}

	public void sendMoveCharSlotResult(CFileDescriptor fd)
	{
		// TODO chclif_moveCharSlotReply
	}
}

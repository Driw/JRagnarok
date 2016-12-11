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
 * <h1>Servi�o para Acesso dos Clientes</h1>
 *
 * <p>Esse servi�o � a primeira fronteira do servidor para se comunicar com o cliente.
 * Nele ser� recebido uma nova conex�o solicitada para com o servidor de personagem.
 * Ap�s receber a conex�o dever� redirecionar o cliente conforme o comando recebido.</p>
 *
 * <p>De acordo com o comando recebido, dever� redirecionar a outros servi�os se necess�rio.
 * Fica sendo de sua responsabilidade garantir tamb�m o envio de pacotes para os clientes.
 * Podendo ainda ser necess�rio comunicar-se com outro servi�o para auxili�-lo.</p>
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
	 * Instancia um novo servi�o para recebimento de novos clientes no servidor.
	 * Para este servi�o � necess�rio realizar chamados para iniciar e destruir.
	 * @param server servidor de personagem respons�vel por este servi�o.
	 */

	public ServiceCharClient(CharServer server)
	{
		super(server);
	}

	/**
	 * Listener usado para receber novas conex�es solicitadas com o servidor de personagem.
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

			// J� foi autenticado, n�o deveria estar aqui
			if (fd.getFlag().is(FileDescriptor.FLAG_EOF) && parseAlreadyAuth(cfd))
				return true;

			return acknowledgePacket(cfd);
		}
	};

	/**
	 * Procedimento de chamado ap�s a primeira an�lise do cliente em rela��o a sua sess�o.
	 * Aqui o cliente ser� redirecionado corretamente conforme o tipo de comando solicitado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se deve manter a conex�o ou false caso contr�rio.
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
				logNotice("fim de conex�o inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	/**
	 * Essa funcionalidade � chamada durante a an�lise de novos pacotes recebidos pelo cliente.
	 * Al�m disso a condi��o m�nima para este chamado � que esteja em EOF (end of file).
	 * Deve verificar se o jogador (sess�o) j� foi autenticado e est� online no sistema.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se foi autenticado ou false caso contr�rio (true fecha conex�o).
	 */

	public boolean parseAlreadyAuth(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		// Client j� autenticado
		if (sd != null && sd.isAuth())
		{
			OnlineCharData online = onlines.get(sd.getID());

			if (online != null && online.getFileDescriptor().getID() == fd.getID())
				online.setFileDescriptor(null);

			// Se n�o est� em nenhum servidor deixar offline.
			if (online != null && online.getServer() == -1)
				onlines.remove(online);

			return true;
		}

		return false;
	}

	/**
	 * Notifica o cliente que houve algum problema ap�s a autentica��o do acesso.
	 * O acesso foi autenticado por�m houve algum problema em liberar o acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da libera��o do acesso para o cliente.
	 */

	public void sendNotifyResult(CFileDescriptor fd, NotifyAuthResult result)
	{
		logDebug("notificar resultado (fd: %d).\n", fd.getID());

		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Recusa a entrada de uma determinada sess�o no servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado que ser� mostrado ao cliente.
	 */

	public void refuseEnter(CFileDescriptor fd, byte result)
	{
		logDebug("entrada rejeitada em (fd: %d).\n", fd.getID());

		RefuseEnter packet = new RefuseEnter();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Informa ao cliente que o sistema de c�digo PIN est� entrando no estado de:
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param sd dados da sess�o do cliente com o servidor de personagem.
	 * @param state estado do qual o c�digo PIN deve assumir.
	 */

	public void pincodeSendState(CFileDescriptor fd, PincodeState state)
	{
		logDebug("enviando estado de c�digo pin (fd: %d).\n", fd.getID());

		CharSessionData sd = fd.getSessionData();
		sd.getPincode().genSeed();

		PincodeSendState packet = new PincodeSendState();
		packet.setPincodeSeed(sd.getPincode().getSeed());
		packet.setAccountID(sd.getID());
		packet.setState(state.CODE);
		packet.send(fd);
	}

	/**
	 * Envia todos os dados para a exibi��o da lista de personagens de uma conta de acordo com o cliente.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
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
	 * Envia ao cliente os dados relacionados a quantidade de slots dispon�veis para personagens.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
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
	 * Envia todos os dados dos personagens da conta respectiva a sess�o que foi estabelecida.
	 * Os dados s�o enviados ao cliente para que ele possa exibir a sele��o do personagem.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
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
	 * Envia ao cliente a quantidade de p�ginas dispon�veis para sele��o de personagens.
	 * Cada p�gina � composta por 3 slots para aloca��o de um �nico personagem no mesmo.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
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

		logDebug("definindo primeiro c�digo PIN (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_setnew
	}

	public void parsePincodeChange(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("alterando c�digo PIN existente (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_change
	}

	public void parsePincodeCheck(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("recebendo c�digo PIN inserido (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_check
	}

	public void reqPincodeWindow(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("solicitar tela para digitar c�digo PIN (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_reqpincode_window
	}

	public void sendPincodeState(CFileDescriptor fd, PincodeState state)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando c�digo PIN paraa atuar em %s (fd: %d, aid: %d).\n", state, fd.getID(), sd.getID());

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

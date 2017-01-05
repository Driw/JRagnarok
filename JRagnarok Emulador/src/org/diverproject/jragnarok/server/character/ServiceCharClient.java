package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.DATE_FORMAT;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.MIN_CHARS;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_UNLIMITED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_ENABLED;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.character.toclient.TAG_CHARACTER_BLOCK_INFO;
import org.diverproject.jragnarok.packets.character.toclient.HC_Accept2;
import org.diverproject.jragnarok.packets.character.toclient.HC_AcceptEnterNeoUnion;
import org.diverproject.jragnarok.packets.character.toclient.HC_AckCharInfoPerPage;
import org.diverproject.jragnarok.packets.character.toclient.HC_BlockCharacter;
import org.diverproject.jragnarok.packets.character.toclient.HC_CharListNotify;
import org.diverproject.jragnarok.packets.character.toclient.HC_RefuseEnter;
import org.diverproject.jragnarok.packets.character.toclient.HC_SecondPasswordLogin;
import org.diverproject.jragnarok.packets.character.toclient.HC_SecondPasswordLogin.PincodeState;
import org.diverproject.jragnarok.packets.common.NotifyAuthResult;
import org.diverproject.jragnarok.packets.common.RefuseEnter;
import org.diverproject.jragnarok.packets.inter.SC_NotifyBan;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.character.entities.Character;
import org.diverproject.util.collection.Index;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;

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
 * @see OnlineMap
 * @see CharacterControl
 *
 * @author Andrew
 */

public class ServiceCharClient extends AbstractCharService
{
	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineMap onlines;

	/**
	 * Controle para gerenciar dados dos personagens.
	 */
	private CharacterControl characters;

	/**
	 * Instancia um novo servi�o para recebimento de novos clientes no servidor.
	 * Para este servi�o � necess�rio realizar chamados para iniciar e destruir.
	 * @param server servidor de personagem respons�vel por este servi�o.
	 */

	public ServiceCharClient(CharServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		onlines = getServer().getFacade().getOnlineMap();
		characters = getServer().getFacade().getCharacterControl();
	}

	@Override
	public void destroy()
	{
		onlines = null;
		characters = null;
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

		SC_NotifyBan packet = new SC_NotifyBan();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Recusa a entrada de uma determinada sess�o no servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param error resultado que ser� mostrado ao cliente.
	 */

	public void refuseEnter(CFileDescriptor fd, RefuseEnter error)
	{
		logDebug("entrada rejeitada em (fd: %d).\n", fd.getID());

		HC_RefuseEnter packet = new HC_RefuseEnter();
		packet.setError(error);
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

		HC_SecondPasswordLogin packet = new HC_SecondPasswordLogin();
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
			sendBlockCharacters(fd);
	}

	/**
	 * Envia ao cliente os dados relacionados a quantidade de slots dispon�veis para personagens.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void sendAccountSlot(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados de slot de account#%d (fd: %d).\n", sd.getID(), fd.getID());

		HC_Accept2 packet = new HC_Accept2();
		packet.setNormalSlots(sd.getCharSlots());
		packet.setPremiumSlots(sd.getVip().getCharSlotCount());
		packet.setBillingSlots(sd.getVip().getCharBilling());
		packet.setProducibleSlots(sd.getCharSlots());
		packet.setValidSlots(b(MAX_CHARS));
		packet.send(fd);
	}

	/**
	 * Envia todos os dados dos personagens da conta respectiva a sess�o que foi estabelecida.
	 * Os dados s�o enviados ao cliente para que ele possa exibir a sele��o do personagem.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void sendAccountChars(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados dos personagens de account#%d (fd: %d).\n", sd.getID(), fd.getID());

		for (int i = 0; i < MAX_CHARS; i++)
			sd.setCharData(null, i);

		if (sd.getVersion() >= dateToVersion(20100413))
		{
			try {

				Index<Character> characters = this.characters.list(sd.getID());

				HC_AcceptEnterNeoUnion packet = new HC_AcceptEnterNeoUnion();
				packet.setTotalSlots(MAX_CHARS);
				packet.setPremiumStartSlot(MIN_CHARS);
				packet.setPremiumEndSlot(b(MIN_CHARS + sd.getVip().getCharSlotCount()));
				packet.setCharMoveCount(sd.getCharactersMove());
				packet.setCharMoveEnabled(getConfigs().getBool(CHAR_MOVE_ENABLED));
				packet.setCharMoveUnlimited(getConfigs().getBool(CHAR_MOVE_UNLIMITED));
				packet.setCharacters(characters);
				packet.send(fd);

			} catch (RagnarokException e) {
				logError("falha ao carregar dados dos personagens (aid: %d):.\n", sd.getID());
				logException(e);
			}
		}
	}

	public void sendBlockCharacters(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		Queue<TAG_CHARACTER_BLOCK_INFO> blocks = new DynamicQueue<>();
		CharData data = null;

		for (int slot = 0; slot < MAX_CHARS; slot++)
		{
			if ((data = sd.getCharData(slot)) == null)
				continue;

			if (!data.getUnban().isNull())
			{
				TAG_CHARACTER_BLOCK_INFO block = new TAG_CHARACTER_BLOCK_INFO();
				blocks.offer(block);

				if (!data.getUnban().isOver())
				{
					block.setCharID(data.getID());
					block.setUnbanTime(data.getUnban().toStringFormat(DATE_FORMAT));
				}

				else
				{
					block.setCharID(0);
					block.setUnbanTime("");

					try {

						data.getUnban().set(0);
						characters.unban(data.getID());

					} catch (RagnarokException e) {
						logWarning("falha ao remover banimento de personagem (aid: %d, charid: %d)", sd.getID(), data.getID());
					}
				}
			}
		}

		for (int slot = 0; slot < MAX_CHARS; slot++)
			if (sd.getCharData(slot) != null && !sd.getCharData(slot).getUnban().isOver())
			{
				TimerMap timers = getTimerSystem().getTimers();

				Timer timer = timers.acquireTimer();
				timer.setTick(getTimerSystem().getCurrentTime() + seconds(10));
				timer.setListener(CHAR_BLOCK_TIMER);
				timer.setObjectID(sd.getID());
				sd.setCharBlockTime(timer);

				break;
			}

		HC_BlockCharacter packet = new HC_BlockCharacter();
		packet.setBlocks(blocks);
		packet.send(fd);
	}

	private final TimerListener CHAR_BLOCK_TIMER = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			CharSessionData sd = null;
			CFileDescriptor fd = null;

			for (FileDescriptor sfd : getFileDescriptorSystem())
			{
				if (sfd == null || !sfd.isConnected())
					continue;

				fd = (CFileDescriptor) sfd;

				if (fd.getID() == timer.getObjectID())
				{
					sd = fd.getSessionData();
					break;
				}
			}

			if (fd == null || sd == null || sd.getCharBlockTime() == null)
				return;

			if (!sd.getCharBlockTime().equals(timer))
				sd.setCharBlockTime(null);

			sendBlockCharacters(fd);
		}
		
		@Override
		public String getName()
		{
			return "CHAR_BLOCK_TIMER";
		}
	};

	/**
	 * Envia todos os dados dos personagens da conta respectiva a sess�o que foi estabelecida.
	 * Os dados s�o enviados ao cliente para que ele possa exibir a sele��o do personagem.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public boolean sendCharsPerPage(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados dos personagens por p�gina (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		for (int i = 0; i < MAX_CHARS; i++)
			sd.setCharData(null, i);

		try {

			Index<Character> characters = this.characters.list(sd.getID());

			HC_AckCharInfoPerPage packet = new HC_AckCharInfoPerPage();
			packet.setCharMoveCount(sd.getCharactersMove());
			packet.setCharMoveEnabled(getConfigs().getBool(CHAR_MOVE_ENABLED));
			packet.setCharMoveUnlimited(getConfigs().getBool(CHAR_MOVE_UNLIMITED));
			packet.setCharacters(characters);
			packet.send(fd);

			return true;

		} catch (RagnarokException e) {
			logError("falha ao carregar dados dos personagens (aid: %d):.\n", sd.getID());
			logException(e);
		}

		return false;
	}

	/**
	 * Envia ao cliente a quantidade de p�ginas dispon�veis para sele��o de personagens.
	 * Cada p�gina � composta por 3 slots para aloca��o de um �nico personagem no mesmo.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void sendCharPageCount(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		HC_CharListNotify packet = new HC_CharListNotify();
		packet.setPageCount(sd.getCharSlots() > 3 ? sd.getCharSlots() / 3 : 1);
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

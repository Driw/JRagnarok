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
	 * Instancia um novo serviço para recebimento de novos clientes no servidor.
	 * Para este serviço é necessário realizar chamados para iniciar e destruir.
	 * @param server servidor de personagem responsável por este serviço.
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

		SC_NotifyBan packet = new SC_NotifyBan();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Recusa a entrada de uma determinada sessão no servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param error resultado que será mostrado ao cliente.
	 */

	public void refuseEnter(CFileDescriptor fd, RefuseEnter error)
	{
		logDebug("entrada rejeitada em (fd: %d).\n", fd.getID());

		HC_RefuseEnter packet = new HC_RefuseEnter();
		packet.setError(error);
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

		HC_SecondPasswordLogin packet = new HC_SecondPasswordLogin();
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
			sendBlockCharacters(fd);
	}

	/**
	 * Envia ao cliente os dados relacionados a quantidade de slots disponíveis para personagens.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * Envia todos os dados dos personagens da conta respectiva a sessão que foi estabelecida.
	 * Os dados são enviados ao cliente para que ele possa exibir a seleção do personagem.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * Envia todos os dados dos personagens da conta respectiva a sessão que foi estabelecida.
	 * Os dados são enviados ao cliente para que ele possa exibir a seleção do personagem.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public boolean sendCharsPerPage(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados dos personagens por página (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

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
	 * Envia ao cliente a quantidade de páginas disponíveis para seleção de personagens.
	 * Cada página é composta por 3 slots para alocação de um único personagem no mesmo.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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

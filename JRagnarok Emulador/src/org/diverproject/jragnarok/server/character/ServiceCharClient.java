package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.DATE_FORMAT;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.mapid2mapname;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.i;
import static org.diverproject.util.Util.now;
import static org.diverproject.util.Util.b;
import static org.diverproject.util.Util.seconds;

import org.diverproject.jragnarok.packets.character.toclient.TAG_CHARACTER_BLOCK_INFO;
import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.jragnarok.packets.character.toclient.HC_Accept2;
import org.diverproject.jragnarok.packets.character.toclient.HC_AcceptDeleteChar;
import org.diverproject.jragnarok.packets.character.toclient.HC_AcceptEnterNeoUnion;
import org.diverproject.jragnarok.packets.character.toclient.HC_AcceptMakeCharNeoUnion;
import org.diverproject.jragnarok.packets.character.toclient.HC_AckCharInfoPerPage;
import org.diverproject.jragnarok.packets.character.toclient.HC_BlockCharacter;
import org.diverproject.jragnarok.packets.character.toclient.HC_CharListNotify;
import org.diverproject.jragnarok.packets.character.toclient.HC_DeleteChar3;
import org.diverproject.jragnarok.packets.character.toclient.HC_DeleteCharCancel;
import org.diverproject.jragnarok.packets.character.toclient.HC_DeleteCharReserved;
import org.diverproject.jragnarok.packets.character.toclient.HC_NotifyZoneServer;
import org.diverproject.jragnarok.packets.character.toclient.HC_RefuseDeleteChar;
import org.diverproject.jragnarok.packets.character.toclient.HC_RefuseEnter;
import org.diverproject.jragnarok.packets.character.toclient.HC_RefuseMakeChar;
import org.diverproject.jragnarok.packets.character.toclient.HC_SecondPasswordLogin;
import org.diverproject.jragnarok.packets.common.DeleteChar;
import org.diverproject.jragnarok.packets.common.DeleteCharCancel;
import org.diverproject.jragnarok.packets.common.DeleteCharReserved;
import org.diverproject.jragnarok.packets.common.NotifyAuth;
import org.diverproject.jragnarok.packets.common.PincodeState;
import org.diverproject.jragnarok.packets.common.RefuseDeleteChar;
import org.diverproject.jragnarok.packets.common.RefuseEnter;
import org.diverproject.jragnarok.packets.common.RefuseMakeChar;
import org.diverproject.jragnarok.packets.inter.SC_NotifyBan;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerAdapt;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.common.entities.Character;
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
 * @see OnlineControl
 * @see CharacterControl
 *
 * @author Andrew
 */

public class ServiceCharClient extends AbstractCharService
{
	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineControl onlines;

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
		onlines = getServer().getFacade().getOnlineControl();
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param result resultado da libera��o do acesso para o cliente.
	 */

	public void sendNotifyResult(CFileDescriptor fd, NotifyAuth result)
	{
		logDebug("notificar resultado (fd: %d).\n", fd.getID());

		SC_NotifyBan packet = new SC_NotifyBan();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Recusa a entrada de uma determinada sess�o no servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void sendAccountSlot(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados de slot de account#%d (fd: %d).\n", sd.getID(), fd.getID());

		HC_Accept2 packet = new HC_Accept2();
		packet.setNormalSlots(sd.getCharSlots());
		packet.setPremiumSlots(b(MAX_CHARS - sd.getCharSlots()));
		packet.setBillingSlots(b(MAX_CHARS - sd.getCharSlots()));
		packet.setProducibleSlots(sd.getCharSlots());
		packet.setValidSlots(b(sd.getCharSlots()));
		packet.send(fd);
	}

	/**
	 * Envia todos os dados dos personagens da conta respectiva a sess�o que foi estabelecida.
	 * Os dados s�o enviados ao cliente para que ele possa exibir a sele��o do personagem.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void sendAccountChars(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados dos personagens de account#%d (fd: %d).\n", sd.getID(), fd.getID());

		if (sd.getVersion() >= dateToVersion(20100413))
		{
			try {

				Index<Character> characters = this.characters.list(sd.getID());
				this.characters.setCharData(sd, characters);

				HC_AcceptEnterNeoUnion packet = new HC_AcceptEnterNeoUnion();
				packet.setTotalSlots(MAX_CHARS);
				packet.setPremiumStartSlot(sd.getCharSlots());
				packet.setPremiumEndSlot(MAX_CHARS);
				packet.setCharMoveCount(sd.getCharactersMove());
				packet.setCharMoveEnabled(config().moveEnabled);
				packet.setCharMoveUnlimited(config().moveUnlimited);
				packet.setCharacters(characters);
				packet.send(fd);

			} catch (RagnarokException e) {
				logError("falha ao carregar dados dos personagens (aid: %d):.\n", sd.getID());
				logException(e);
			}
		}
	}

	/**
	 * Envia ao cliente a quantidade de p�ginas dispon�veis para sele��o de personagens.
	 * Cada p�gina � composta por 3 slots para aloca��o de um �nico personagem no mesmo.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void sendCharPageCount(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando quantidade de p�ginas para personagens de account#%d (fd: %d).\n", sd.getID(), fd.getID());

		HC_CharListNotify packet = new HC_CharListNotify();
		packet.setPageCount(sd.getCharSlots() > 3 ? sd.getCharSlots() / 3 : 1);
		packet.send(fd);
	}

	/**
	 * Envia todos os dados dos personagens da conta respectiva a sess�o que foi estabelecida.
	 * Os dados s�o enviados ao cliente para que ele possa exibir a sele��o do personagem.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public boolean sendCharsPerPage(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados dos personagens por p�gina (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		try {

			Index<Character> characters = this.characters.list(sd.getID());

			HC_AckCharInfoPerPage packet = new HC_AckCharInfoPerPage();
			packet.setCharMoveCount(sd.getCharactersMove());
			packet.setCharMoveEnabled(config().moveEnabled);
			packet.setCharMoveUnlimited(config().moveUnlimited);
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
	 * Informa ao cliente uma lista contendo as informa��es dos personagens que est�o bloqueados.
	 * Al�m disso ir� criar um temporizador que dever� reenviar a lista ap�s um intervalo de tempo.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

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
				TimerSystem ts = getTimerSystem();
				TimerMap timers = ts.getTimers();
				Timer timer = sd.getCharBlockTime();

				if (timer == null)
				{
					timer = timers.acquireTimer();
					timer.setListener(CHAR_BLOCK_TIMER);
					timer.setObjectID(sd.getID());
					sd.setCharBlockTime(timer);
				}

				timer.setTick(ts.getCurrentTime() + seconds(10));
				timers.add(timer);

				break;
			}

		HC_BlockCharacter packet = new HC_BlockCharacter();
		packet.setBlocks(blocks);
		packet.send(fd);
	}

	private final TimerListener CHAR_BLOCK_TIMER = new TimerAdapt()
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
			{
				getTimerSystem().getTimers().delete(timer);
				return;
			}

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
	 * Informa o cliente durante a sele��o de personagem que a cria��o do personagem falhou.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param error erro que especifica qual o problema ocorrido durante a cria��o.
	 */

	public void refuseMakeChar(CFileDescriptor fd, RefuseMakeChar error)
	{
		HC_RefuseMakeChar packet = new HC_RefuseMakeChar();
		packet.setError(error);
		packet.send(fd);		
	}

	/**
	 * Informa o cliente durante a sele��o de personagem a confirma��o e dados do personagem criado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param character personagem contendo todas as informa��es que possam ser necess�rias.
	 * @param slot n�mero de slot para aloca��o do personagem respectivamente a sua conta.
	 */

	public void acceptMakeChar(CFileDescriptor fd, Character character, byte slot)
	{
		HC_AcceptMakeCharNeoUnion packet = new HC_AcceptMakeCharNeoUnion();
		packet.setCharacter(character);
		packet.setSlot(slot);
		packet.setMoveEnabled(config().moveEnabled);
		packet.setMoveUnlimited(config().moveUnlimited);
		packet.setMoveCount(character.getMoves());
		packet.send(fd);		
	}

	/**
	 * Informa o cliente durante a sele��o de personagem que a exclus�o do personagem falhou.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param error erro que especifica qual o problema ocorrido durante a exclus�o.
	 */

	public void refuseDeleteChar(CFileDescriptor fd, RefuseDeleteChar error)
	{
		HC_RefuseDeleteChar packet = new HC_RefuseDeleteChar();
		packet.setError(error);
		packet.send(fd);
	}

	/**
	 * Informa o cliente durante a sele��o de personagem que a exclus�o do personagem foi feita.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void acceptDeleteChar(CFileDescriptor fd)
	{
		HC_AcceptDeleteChar packet = new HC_AcceptDeleteChar();
		packet.send(fd);
	}

	/**
	 * Informa ao cliente o resultado da solicita��o para agendar a exclus�o de um personagem.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param charID c�digo de identifica��o do personagem do qual a a��o foi feita.
	 * @param deleteDate hor�rio em que o personagem dever� ser exclu�do ou zero se n�o for.
	 * @param result resultado obtido da tentativa de agendar a exclus�o do personagem.
	 */

	public void deleteCharReserved(CFileDescriptor fd, int charID, long deleteDate, DeleteCharReserved result)
	{
		HC_DeleteCharReserved packet = new HC_DeleteCharReserved();
		packet.setCharID(charID);
		packet.setDeleteDate(i(deleteDate - now())/1000);
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Informa ao cliente o resultado da a��o para excluir um personagem de sua conta.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param charID c�digo de identifica��o do personagem do qual a a��o foi feita.
	 * @param result resultado obtido da tentativa de excluir o personagem especificado.
	 */

	public void deleteAccept(CFileDescriptor fd, int charID, DeleteChar result)
	{
		HC_DeleteChar3 packet = new HC_DeleteChar3();
		packet.setCharID(charID);
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Informa ao cliente o resultado da a��o para cancelar o agendamento de exclus�o do personagem especificado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param charID c�digo de identifica��o do personagem do qual a a��o foi feita.
	 * @param result resultado obtido ap�s realizar o cancelamento do agendamento.
	 */

	public void deleteCancel(CFileDescriptor fd, int charID, DeleteCharCancel result)
	{
		HC_DeleteCharCancel packet = new HC_DeleteCharCancel();
		packet.setCharID(charID);
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Informa ao cliente que este dever� exibir na tela o sistema de c�digo PIN para o jogador.
	 * Ap�s a janela ser aberta o jogador dever� utilizar o sistema de c�digo PIN para continuar.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void showPIncodeWindow(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("solicitar tela para digitar c�digo PIN (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!config().pincodeEnabled)
			return;

		// TODO chclif_parse_reqpincode_window
	}

	/**
	 * Informa ao cliente um novo estado (a��o) para ser exibido no sistema de c�digo PIN.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param state estado em que o sistema de c�digo PIN estar� assumindo.
	 */

	public void sendPincodeState(CFileDescriptor fd, PincodeState state)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando c�digo PIN paraa atuar em %s (fd: %d, aid: %d).\n", state, fd.getID(), sd.getID());

		if (!config().pincodeEnabled)
			return;

		// TODO chclif_pincode_sendstate
	}

	/**
	 * 
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void parseMoveCharSlot(CFileDescriptor fd)
	{
		// TODO chclif_parse_moveCharSlot
	}

	/**
	 * 
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void sendMoveCharSlotResult(CFileDescriptor fd)
	{
		// TODO chclif_moveCharSlotReply
	}

	/**
	 * Notifica ao cliente que o personagem foi selecionado no servidor para redirecion�-lo ao servidor de mapa.
	 * Ser� necess�rio especificar o personagem que foi selecionado e a identifica��o do servidor de mapa.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param character refer�ncia do objecto contendo as informa��es do personagem selecionado.
	 * @param mapServerID c�digo de identifica��o do servidor de mapa em que o personagem ir� entrar.
	 */

	public void notifyZoneServer(CFileDescriptor fd, Character character, int mapServerID)
	{
		ClientMapServer server = getServer().getMapServers().get(mapServerID);

		if (server != null)
		{
			HC_NotifyZoneServer packet = new HC_NotifyZoneServer();
			packet.setCharID(character.getID());
			packet.setMapName(mapid2mapname(character.getLocations().getLastPoint().getMapID()));
			packet.setAddressIP(server.getIP().get());
			packet.setPort(server.getPort());
			packet.send(fd);
		}

		else
			logWarning("tentativa de entrar em um servidor de mapa n�o existente (map-server: %d).\n", mapServerID);
	}
}

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
	 * Essa funcionalidade é chamada durante a análise de novos pacotes recebidos pelo cliente.
	 * Além disso a condição mínima para este chamado é que esteja em EOF (end of file).
	 * Deve verificar se o jogador (sessão) já foi autenticado e está online no sistema.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
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
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param result resultado da liberação do acesso para o cliente.
	 */

	public void sendNotifyResult(CFileDescriptor fd, NotifyAuth result)
	{
		logDebug("notificar resultado (fd: %d).\n", fd.getID());

		SC_NotifyBan packet = new SC_NotifyBan();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Recusa a entrada de uma determinada sessão no servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
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
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
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
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
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
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
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
	 * Envia todos os dados dos personagens da conta respectiva a sessão que foi estabelecida.
	 * Os dados são enviados ao cliente para que ele possa exibir a seleção do personagem.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
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
	 * Envia ao cliente a quantidade de páginas disponíveis para seleção de personagens.
	 * Cada página é composta por 3 slots para alocação de um único personagem no mesmo.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void sendCharPageCount(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando quantidade de páginas para personagens de account#%d (fd: %d).\n", sd.getID(), fd.getID());

		HC_CharListNotify packet = new HC_CharListNotify();
		packet.setPageCount(sd.getCharSlots() > 3 ? sd.getCharSlots() / 3 : 1);
		packet.send(fd);
	}

	/**
	 * Envia todos os dados dos personagens da conta respectiva a sessão que foi estabelecida.
	 * Os dados são enviados ao cliente para que ele possa exibir a seleção do personagem.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public boolean sendCharsPerPage(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando dados dos personagens por página (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

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
	 * Informa ao cliente uma lista contendo as informações dos personagens que estão bloqueados.
	 * Além disso irá criar um temporizador que deverá reenviar a lista após um intervalo de tempo.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
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
	 * Informa o cliente durante a seleção de personagem que a criação do personagem falhou.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param error erro que especifica qual o problema ocorrido durante a criação.
	 */

	public void refuseMakeChar(CFileDescriptor fd, RefuseMakeChar error)
	{
		HC_RefuseMakeChar packet = new HC_RefuseMakeChar();
		packet.setError(error);
		packet.send(fd);		
	}

	/**
	 * Informa o cliente durante a seleção de personagem a confirmação e dados do personagem criado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param character personagem contendo todas as informações que possam ser necessárias.
	 * @param slot número de slot para alocação do personagem respectivamente a sua conta.
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
	 * Informa o cliente durante a seleção de personagem que a exclusão do personagem falhou.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param error erro que especifica qual o problema ocorrido durante a exclusão.
	 */

	public void refuseDeleteChar(CFileDescriptor fd, RefuseDeleteChar error)
	{
		HC_RefuseDeleteChar packet = new HC_RefuseDeleteChar();
		packet.setError(error);
		packet.send(fd);
	}

	/**
	 * Informa o cliente durante a seleção de personagem que a exclusão do personagem foi feita.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void acceptDeleteChar(CFileDescriptor fd)
	{
		HC_AcceptDeleteChar packet = new HC_AcceptDeleteChar();
		packet.send(fd);
	}

	/**
	 * Informa ao cliente o resultado da solicitação para agendar a exclusão de um personagem.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param charID código de identificação do personagem do qual a ação foi feita.
	 * @param deleteDate horário em que o personagem deverá ser excluído ou zero se não for.
	 * @param result resultado obtido da tentativa de agendar a exclusão do personagem.
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
	 * Informa ao cliente o resultado da ação para excluir um personagem de sua conta.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param charID código de identificação do personagem do qual a ação foi feita.
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
	 * Informa ao cliente o resultado da ação para cancelar o agendamento de exclusão do personagem especificado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param charID código de identificação do personagem do qual a ação foi feita.
	 * @param result resultado obtido após realizar o cancelamento do agendamento.
	 */

	public void deleteCancel(CFileDescriptor fd, int charID, DeleteCharCancel result)
	{
		HC_DeleteCharCancel packet = new HC_DeleteCharCancel();
		packet.setCharID(charID);
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Informa ao cliente que este deverá exibir na tela o sistema de código PIN para o jogador.
	 * Após a janela ser aberta o jogador deverá utilizar o sistema de código PIN para continuar.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void showPIncodeWindow(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("solicitar tela para digitar código PIN (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!config().pincodeEnabled)
			return;

		// TODO chclif_parse_reqpincode_window
	}

	/**
	 * Informa ao cliente um novo estado (ação) para ser exibido no sistema de código PIN.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param state estado em que o sistema de código PIN estará assumindo.
	 */

	public void sendPincodeState(CFileDescriptor fd, PincodeState state)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("enviando código PIN paraa atuar em %s (fd: %d, aid: %d).\n", state, fd.getID(), sd.getID());

		if (!config().pincodeEnabled)
			return;

		// TODO chclif_pincode_sendstate
	}

	/**
	 * 
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void parseMoveCharSlot(CFileDescriptor fd)
	{
		// TODO chclif_parse_moveCharSlot
	}

	/**
	 * 
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void sendMoveCharSlotResult(CFileDescriptor fd)
	{
		// TODO chclif_moveCharSlotReply
	}

	/**
	 * Notifica ao cliente que o personagem foi selecionado no servidor para redirecioná-lo ao servidor de mapa.
	 * Será necessário especificar o personagem que foi selecionado e a identificação do servidor de mapa.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param character referência do objecto contendo as informações do personagem selecionado.
	 * @param mapServerID código de identificação do servidor de mapa em que o personagem irá entrar.
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
			logWarning("tentativa de entrar em um servidor de mapa não existente (map-server: %d).\n", mapServerID);
	}
}

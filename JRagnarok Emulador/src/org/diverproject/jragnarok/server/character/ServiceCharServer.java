package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.PACKETVER;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_CREATE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_IGNORING_CASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_NAME_LETTERS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_NAME_OPTION;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_UNLIMITED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_WISP_SERVER_NAME;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_CREATE_NEW_CHAR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_MAKE_CHAR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_MAKE_CHAR_NOT_STATS;
import static org.diverproject.jragnarok.packets.common.RefuseMakeChar.CREATION_DENIED;
import static org.diverproject.jragnarok.packets.common.RefuseMakeChar.NAME_USED;
import static org.diverproject.jragnarok.packets.common.RefuseMakeChar.NO_AVAIABLE_SLOT;
import static org.diverproject.jragnarok.server.common.Job.JOB_NOVICE;
import static org.diverproject.jragnarok.server.common.Job.JOB_SUMMER;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.character.fromclient.CH_CreateNewChar;
import org.diverproject.jragnarok.packets.character.fromclient.CH_MakeChar;
import org.diverproject.jragnarok.packets.character.fromclient.CH_MakeCharNotStats;
import org.diverproject.jragnarok.packets.character.fromclient.CH_Ping;
import org.diverproject.jragnarok.packets.character.toclient.HC_AcceptMakeCharNeoUnion;
import org.diverproject.jragnarok.packets.character.toclient.HC_RefuseMakeChar;
import org.diverproject.jragnarok.packets.common.RefuseMakeChar;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.character.entities.Character;
import org.diverproject.jragnarok.server.common.Job;
import org.diverproject.util.lang.HexUtil;

public class ServiceCharServer extends AbstractCharService
{
	/**
	 * Serviço para comunicação com o servidor de acesso.
	 */
	private ServiceCharLogin login;

	/**
	 * Controle dos dados básicos dos personagens.
	 */
	private CharacterControl characters;

	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineMap onlines;

	/**
	 * Cria uma nova instância do principal serviço para um servidor de personagem.
	 * @param server referência do servidor de personagem que irá usar o serviço.
	 */

	public ServiceCharServer(CharServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		login = getServer().getFacade().getLoginService();
		characters = getServer().getFacade().getCharacterControl();
		onlines = getServer().getFacade().getOnlineMap();

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer odcTimer = timers.acquireTimer();
		odcTimer.setListener(ONLINE_DATA_CLEANUP);
		odcTimer.setTick(ts.getCurrentTime() + seconds(1));
		timers.addLoop(odcTimer, seconds(10));
	}

	@Override
	public void destroy()
	{
		login = null;
		characters = null;
		onlines = null;
	}

	private final TimerListener ONLINE_DATA_CLEANUP = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			onlines.cleanup();
		}
		
		@Override
		public String getName()
		{
			return "onlineDataCleanup";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	public void setCharOnline(int mapID, OnlineCharData online)
	{
		onlines.makeOnline(online);
	}

	public void setCharOffline(int charID, int accountID)
	{
		// TODO Auto-generated method stub
		
	}

	public int getCountUsers()
	{
		// TODO Auto-generated method stub

		return 0;
	}

	public void disconnectPlayer(int accountID)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Identifica uma determinada conexão no servidor de que esta se encontra na seleção de personagem.
	 * Essa identificação é feita através da criação de um registro da sessão/conta como online.
	 * @param fd código de identificação do descritor de arquivo do cliente com o servidor.
	 */

	public void setCharSelectSection(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		OnlineCharData online = onlines.get(sd.getID());

		if (online == null)
		{
			online = new OnlineCharData();
			online.setAccountID(sd.getID());
			onlines.add(online);
		}

		MapServerList servers = getServer().getMapServers();

		if (online.getServer() > 0)
		{
			ClientMapServer server = servers.get(online.getServer());

			if (server.getUsers() > 0)
				server.setUsers(s(server.getUsers() - 1));
		}

		online.setCharID(0);
		online.setServer(0);

		if (online.getWaitingDisconnect() != null)
		{
			getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		login.setAccountOnline(sd.getID());
	}

	/**
	 * Recebe um pacote do cliente para dizer ao servidor que ele ainda está ativo (conectado).
	 * Uma vez que o pacote tenha sido recebido o timeout da sessão (fd) será reiniciado.
	 * @param fd código de identificação do descritor de arquivo do cliente com o servidor.
	 */

	public void keepAlive(CFileDescriptor fd)
	{
		CH_Ping packet = new CH_Ping();
		packet.receive(fd);
	}

	/**
	 * Cria um personagem de diversas formas de acordo com o comando passado, cada comando é um pacote diferente.
	 * Os seguintes tipos de pacotes são aceitos e terão efeitos ao utilizar este médo de criação do personagem:
	 * <code>PACKET_CH_MAKE_CHAR</code>, <code>PACKET_CH_MAKE_CHAR_NOT_STATS</code>, <code>PACKET_CH_CREATE_NEW_CHAR</code>.
	 * @param fd código de identificação do descritor de arquivo do cliente com o servidor.
	 * @param command código de identificação do pacote (comando) recebido para a criação do personagem.
	 */

	public void makeChar(CFileDescriptor fd, short command)
	{
		Character character = new Character();
		byte slot = -1;

		switch (command)
		{
			case PACKET_CH_MAKE_CHAR:
				CH_MakeChar makeChar = new CH_MakeChar();
				makeChar.receive(fd);
				character.setName(makeChar.getCharName());
				character.setSex(fd.getSessionData().getSex());
				character.getStats().setStrength(makeChar.getStrength());
				character.getStats().setAgility(makeChar.getAgility());
				character.getStats().setVitality(makeChar.getVitality());
				character.getStats().setIntelligence(makeChar.getIntelligence());
				character.getStats().setDexterity(makeChar.getDexterity());
				character.getStats().setLuck(makeChar.getLuck());
				character.getLook().setHairColor(makeChar.getHairColor());
				character.getLook().setClothesColor(makeChar.getClothesColor());
				character.setJob(JOB_NOVICE);
				slot = makeChar.getSlot();
				break;

			case PACKET_CH_MAKE_CHAR_NOT_STATS:
				CH_MakeCharNotStats makeCharNotStats = new CH_MakeCharNotStats();
				makeCharNotStats.receive(fd);
				character.setSex(fd.getSessionData().getSex());
				character.setName(makeCharNotStats.getCharName());
				character.getLook().setHairColor(makeCharNotStats.getHairColor());
				character.getLook().setClothesColor(makeCharNotStats.getClothesColor());
				character.setJob(JOB_NOVICE);
				slot = makeCharNotStats.getSlot();
				break;

			case PACKET_CH_CREATE_NEW_CHAR:
				CH_CreateNewChar createNewChar = new CH_CreateNewChar();
				createNewChar.receive(fd);
				character.setSex(createNewChar.getSex());
				character.setName(createNewChar.getCharName());
				character.getLook().setHair(createNewChar.getHairStyle());
				character.getLook().setHairColor(createNewChar.getHairColor());
				character.setJob(Job.parse(createNewChar.getStartJob()));
				slot = createNewChar.getSlot();
				break;

			default:
				throw new RagnarokRuntimeException("0x%s não é usado aqui", HexUtil.parseInt(command, 4));
		}

		RefuseMakeChar error = CREATION_DENIED;

		if (!getConfigs().getBool(CHARACTER_CREATE))
			error = CREATION_DENIED;

		error = tryMakeChar(fd.getSessionData(), character, slot);

		if (error == null)
		{
			HC_AcceptMakeCharNeoUnion accept = new HC_AcceptMakeCharNeoUnion();
			accept.setCharacter(character);
			accept.setSlot(slot);
			accept.setMoveEnabled(getConfigs().getBool(CHAR_MOVE_ENABLED));
			accept.setMoveUnlimited(getConfigs().getBool(CHAR_MOVE_UNLIMITED));
			accept.setMoveCount(character.getMoves());
			accept.send(fd);

			CharData data = new CharData();
			data.setID(character.getID());
			data.setCharMove(character.getMoves());
			fd.getSessionData().setCharData(data, slot);
		}

		if (error != null)
		{
			HC_RefuseMakeChar refuse = new HC_RefuseMakeChar();
			refuse.setError(error);
			refuse.send(fd);
		}
	}

	/**
	 * Realiza o procedimento de tentativa de registrar o personagem no banco de dados (criar).
	 * @param sd sessão contendo informações básicas do jogador que serão necessárias (aid).
	 * @param character personagem contendo todas as informações necessárias para salvá-lo.
	 * @param slot número do slot em que será salvo respectivamente a conta que o solicitou.
	 * @return null se tiver sido criado com sucesso ou o erro que ocorreu na tentativa.
	 */

	private RefuseMakeChar tryMakeChar(CharSessionData sd, Character character, byte slot)
	{
		RefuseMakeChar refuse = null;

		if ((refuse = checkCharName(character.getName())) != null)
			return refuse;

		if (!interval(slot, 0, sd.getCharSlots() - 1))
			return NO_AVAIABLE_SLOT;

		if (PACKETVER >= 20151001)
		{
			if (character.getJob() != JOB_NOVICE && character.getJob() != JOB_SUMMER)
				return CREATION_DENIED;
		}

		// TODO itens iniciais

		try {

			if (!characters.avaiableSlot(sd.getID(), slot))
				return NO_AVAIABLE_SLOT;

			if (characters.add(character) && characters.setSlot(sd.getID(), character.getID(), slot))
				return null;

			// TODO registrar itens iniciais

		} catch (RagnarokException e) {

			logError("falha durante a criação do personagem (aid: %d, name: %s).\n", sd.getID(), character.getName());
			logException(e);
		}

		return CREATION_DENIED;
	}

	/**
	 * Procedimento interno usando durante a tentativa de criação do personagem que irá validar seu nome.
	 * O nome de um personagem não pode ser nulo, em branco, menos de 4 caracteres, estar utilizando o nome
	 * usado pelo servidor (wisp), conter caracteres não permitidos (configurável) ou já estar sendo usado.
	 * @param name string contendo o nome do jogador do qual deseja validar a sua utilização.
	 * @return null se for válido ou o erro ocorrido devido a uma condição inválida no nome do personagem.
	 */

	private RefuseMakeChar checkCharName(String name)
	{
		if (name == null || name.isEmpty() || name.length() < 4)
			return CREATION_DENIED;

		if (name.replaceAll("[\u0000-\u001f]", "").length() != name.length())
			return CREATION_DENIED;

		if (name.equals(getConfigs().getString(CHAR_WISP_SERVER_NAME)))
			return NAME_USED;

		String letters = getConfigs().getString(CHARACTER_NAME_LETTERS);

		if (getConfigs().getInt(CHARACTER_NAME_OPTION) == 1)
			for (int i = 0; i < name.length(); i++)
			{
				if (!letters.contains(java.lang.Character.toString(name.charAt(i))))
					return CREATION_DENIED;
			}

		else if (getConfigs().getInt(CHARACTER_NAME_OPTION) == 2)
			for (int i = 0; i < name.length(); i++)
			{
				if (letters.contains(java.lang.Character.toString(name.charAt(i))))
					return CREATION_DENIED;
			}

		try {
			if (characters.exist(name, getConfigs().getBool(CHARACTER_IGNORING_CASE)))
				return NAME_USED;
		} catch (RagnarokException e) {
			return NAME_USED;
		}

		return null;
	}
}

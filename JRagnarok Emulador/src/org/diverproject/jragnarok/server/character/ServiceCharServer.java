package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.DEFAULT_EMAIL;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_ALBERTA;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_GEFFEN;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_IZLUDE;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_MORROC;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_PAYON;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_PRONTERA;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.PACKETVER;
import static org.diverproject.jragnarok.JRagnarokUtil.mapname2mapid;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_CREATE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_DELETE_DELAY;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_DELETE_LEVEL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_IGNORING_CASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_NAME_LETTERS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_NAME_OPTION;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_WISP_SERVER_NAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_ENABLED;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_CREATE_NEW_CHAR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR2;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_MAKE_CHAR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_MAKE_CHAR_NOT_STATS;
import static org.diverproject.jragnarok.packets.common.DeleteChar.DC_BIRTH_DATE;
import static org.diverproject.jragnarok.packets.common.DeleteChar.DC_DATABASE_ERROR_DELETE;
import static org.diverproject.jragnarok.packets.common.DeleteChar.DC_DUE_SETTINGS;
import static org.diverproject.jragnarok.packets.common.DeleteChar.DC_NOT_YET_POSSIBLE_TIME;
import static org.diverproject.jragnarok.packets.common.DeleteChar.DC_SUCCCESS_DELETE;
import static org.diverproject.jragnarok.packets.common.DeleteCharCancel.DCC_DATABASE_ERROR_CANCEL;
import static org.diverproject.jragnarok.packets.common.DeleteCharCancel.DCC_SUCCCESS_CANCEL;
import static org.diverproject.jragnarok.packets.common.DeleteCharReserved.DCR_ADDED_TO_QUEUE;
import static org.diverproject.jragnarok.packets.common.DeleteCharReserved.DCR_ALREADY_ON_QUEUE;
import static org.diverproject.jragnarok.packets.common.DeleteCharReserved.DCR_CHAR_NOT_FOUND;
import static org.diverproject.jragnarok.packets.common.NotifyAuth.NA_SERVER_CLOSED;
import static org.diverproject.jragnarok.packets.common.RefuseDeleteChar.RDC_CANNOT_BE_DELETED;
import static org.diverproject.jragnarok.packets.common.RefuseDeleteChar.RDC_DENIED;
import static org.diverproject.jragnarok.packets.common.RefuseDeleteChar.RDC_INCORRET_EMAIL_ADDRESS;
import static org.diverproject.jragnarok.packets.common.RefuseEnter.RE_REJECTED_FROM_SERVER;
import static org.diverproject.jragnarok.packets.common.RefuseMakeChar.RMC_CREATION_DENIED;
import static org.diverproject.jragnarok.packets.common.RefuseMakeChar.RMC_NAME_IN_USE;
import static org.diverproject.jragnarok.packets.common.RefuseMakeChar.RMC_UNAVAIABLE_SLOT;
import static org.diverproject.jragnarok.server.common.Job.JOB_NOVICE;
import static org.diverproject.jragnarok.server.common.Job.JOB_SUMMONER;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.util.lang.IntUtil.interval;
import static org.diverproject.util.Util.format;
import static org.diverproject.util.Util.now;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.Util.seconds;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.character.fromclient.CH_CreateNewChar;
import org.diverproject.jragnarok.packets.character.fromclient.CH_DeleteChar;
import org.diverproject.jragnarok.packets.character.fromclient.CH_DeleteChar2;
import org.diverproject.jragnarok.packets.character.fromclient.CH_DeleteChar3;
import org.diverproject.jragnarok.packets.character.fromclient.CH_DeleteCharCancel;
import org.diverproject.jragnarok.packets.character.fromclient.CH_DeleteCharReserved;
import org.diverproject.jragnarok.packets.character.fromclient.CH_MakeChar;
import org.diverproject.jragnarok.packets.character.fromclient.CH_MakeCharNotStats;
import org.diverproject.jragnarok.packets.character.fromclient.CH_Ping;
import org.diverproject.jragnarok.packets.character.fromclient.CH_SelectChar;
import org.diverproject.jragnarok.packets.common.RefuseMakeChar;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.common.Job;
import org.diverproject.jragnarok.server.common.entities.Character;
import org.diverproject.jragnarok.util.MapPoint;
import org.diverproject.util.lang.HexUtil;

/**
 * <h1>Servi�o para Gerenciamento do Servidor de Personagem</h1>
 *
 * <p>Esse � o principal servi�o do servidor de personagem onde ser� poss�vel gerenciar os personagens.
 * Todas as a��es dos jogadores de cria��o de personagens, solicita��o de reservas ou dele��o de personagens,
 * tal como utiliza��o do sistema de c�digo PIN ser� repassado a este servi�o que ir� processar adequadamente.</p>
 *
 * <p>Nele ficar� respons�vel ainda definir quando uma conta/personagem ficar� online no servidor.
 * Incluindo neste mesmo aspecto definir quando um ficar� offline e obter a quantidade de jogadores online.</p>
 *
 * @author Andrew
 */

public class ServiceCharServer extends AbstractCharService
{
	/**
	 * Servi�o para comunica��o inicial com o cliente.
	 */
	private ServiceCharClient client;

	/**
	 * Servi�o para comunica��o com o servidor de acesso.
	 */
	private ServiceCharLogin login;

	/**
	 * Servi�o para comunica��o com o servidor de mapa.
	 */
	private ServiceCharMap map;

	/**
	 * Controle dos dados b�sicos dos personagens.
	 */
	private CharacterControl characters;

	/**
	 * Controle para autentica��o de jogadores online.
	 */
	private AuthMap auths;

	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineControl onlines;

	/**
	 * Cria uma nova inst�ncia do principal servi�o para um servidor de personagem.
	 * @param server refer�ncia do servidor de personagem que ir� usar o servi�o.
	 */

	public ServiceCharServer(CharServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getCharClient();
		login = getServer().getFacade().getLoginService();
		map = getServer().getFacade().getMapService();
		characters = getServer().getFacade().getCharacterControl();
		auths = getServer().getFacade().getAuthMap();
		onlines = getServer().getFacade().getOnlineControl();
	}

	@Override
	public void destroy()
	{
		client = null;
		login = null;
		map = null;
		characters = null;
		auths = null;
		onlines = null;
	}

	/**
	 * 
	 * @param mapID
	 * @param online
	 */

	public void setCharOnline(int mapID, OnlineCharData online)
	{
		// TODO Auto-generated method stub
		//onlines.makeOnline(online);
	}

	/**
	 * 
	 * @param charID
	 * @param accountID
	 */

	public void setCharOffline(int charID, int accountID)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 * @return
	 */

	public int getCountUsers()
	{
		// TODO Auto-generated method stub

		return 0;
	}

	/**
	 * 
	 * @param accountID
	 */

	public void disconnectPlayer(int accountID)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Ap�s a realiza��o de autentica��o do jogador no servidor de personagem ele est� autorizado.
	 * Quando autorizado ser� necess�rio configur�-lo para saber que est� na sele��o de personagens.
	 * Aqui ser� aplicado a��es que ir� tornar sua conta online no sistema.
	 * @param fd c�digo de identifica��o do descritor de arquivo do cliente com o servidor.
	 */

	public void setCharSelectSection(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		OnlineCharData online = onlines.get(sd.getID());

		if (online == null)
			online = onlines.newOnlineCharData(sd.getID());

		else if (online.getServer() > OnlineCharData.NO_SERVER)
		{
			ClientMapServer server = getServer().getMapServers().get(online.getServer());

			if (server != null && server.getUsers() > 0)
				server.setUsers(s(server.getUsers() - 1));

			if (online.getWaitingDisconnect() != null)
			{
				getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
				online.setWaitingDisconnect(null);
			}

			online.setCharID(0);
			online.setServer(OnlineCharData.NO_SERVER);
		}

		login.sendAccountOnline(sd.getID());
	}

	/**
	 * Recebe um pacote do cliente para dizer ao servidor que ele ainda est� ativo (conectado).
	 * Uma vez que o pacote tenha sido recebido o timeout da sess�o (fd) ser� reiniciado.
	 * @param fd c�digo de identifica��o do descritor de arquivo do cliente com o servidor.
	 */

	public void keepAlive(CFileDescriptor fd)
	{
		CH_Ping packet = new CH_Ping();
		packet.receive(fd);
	}

	/**
	 * Cria um personagem de diversas formas de acordo com o comando passado, cada comando � um pacote diferente.
	 * Os seguintes tipos de pacotes s�o aceitos e ter�o efeitos ao utilizar este m�todo de cria��o do personagem:
	 * <code>PACKET_CH_MAKE_CHAR</code>, <code>PACKET_CH_MAKE_CHAR_NOT_STATS</code>, <code>PACKET_CH_CREATE_NEW_CHAR</code>.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param command identifica��o do pacote (comando) recebido para criar um personagem.
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
				throw new RagnarokRuntimeException("0x%s n�o � usado aqui", HexUtil.parseInt(command, 4));
		}

		RefuseMakeChar error = RMC_CREATION_DENIED;

		if (!getConfigs().getBool(CHARACTER_CREATE))
			error = RMC_CREATION_DENIED;

		error = tryMakeChar(fd.getSessionData(), character, slot);

		if (error == null)
		{
			client.acceptMakeChar(fd, character, slot);

			CharData data = new CharData();
			data.setID(character.getID());
			data.setCharMove(character.getMoves());
			fd.getSessionData().setCharData(data, slot);
		}

		else
			client.refuseMakeChar(fd, error);
	}

	/**
	 * Realiza o procedimento de tentativa de registrar o personagem no banco de dados (criar).
	 * @param sd sess�o contendo informa��es b�sicas do jogador que ser�o necess�rias (aid).
	 * @param character personagem contendo todas as informa��es necess�rias para salv�-lo.
	 * @param slot n�mero do slot em que ser� salvo respectivamente a conta que o solicitou.
	 * @return null se tiver sido criado com sucesso ou o erro que ocorreu na tentativa.
	 */

	private RefuseMakeChar tryMakeChar(CharSessionData sd, Character character, byte slot)
	{
		RefuseMakeChar refuse = null;

		if ((refuse = checkCharName(character.getName())) != null)
			return refuse;

		if (!interval(slot, 0, sd.getCharSlots() - 1))
			return RMC_UNAVAIABLE_SLOT;

		if (PACKETVER >= 20151001)
		{
			if (character.getJob() != JOB_NOVICE && character.getJob() != JOB_SUMMONER)
				return RMC_CREATION_DENIED;
		}

		// TODO itens iniciais

		try {

			if (!characters.avaiableSlot(sd.getID(), slot))
				return RMC_UNAVAIABLE_SLOT;

			if (characters.add(character) && characters.setSlot(sd.getID(), character.getID(), slot))
				return null;

			// TODO registrar itens iniciais

		} catch (RagnarokException e) {

			logError("falha durante a cria��o do personagem (aid: %d, name: %s).\n", sd.getID(), character.getName());
			logException(e);
		}

		return RMC_CREATION_DENIED;
	}

	/**
	 * Procedimento interno usando durante a tentativa de cria��o do personagem que ir� validar seu nome.
	 * O nome de um personagem n�o pode ser nulo, em branco, menos de 4 caracteres, estar utilizando o nome
	 * usado pelo servidor (wisp), conter caracteres n�o permitidos (configur�vel) ou j� estar sendo usado.
	 * @param name string contendo o nome do jogador do qual deseja validar a sua utiliza��o.
	 * @return null se for v�lido ou o erro ocorrido devido a uma condi��o inv�lida no nome do personagem.
	 */

	private RefuseMakeChar checkCharName(String name)
	{
		if (name == null || name.isEmpty() || name.length() < 4)
			return RMC_CREATION_DENIED;

		if (name.replaceAll("[\u0000-\u001f]", "").length() != name.length())
			return RMC_CREATION_DENIED;

		if (name.equals(getConfigs().getString(CHAR_WISP_SERVER_NAME)))
			return RMC_NAME_IN_USE;

		String letters = getConfigs().getString(CHARACTER_NAME_LETTERS);

		if (getConfigs().getInt(CHARACTER_NAME_OPTION) == 1)
			for (int i = 0; i < name.length(); i++)
			{
				if (!letters.contains(java.lang.Character.toString(name.charAt(i))))
					return RMC_CREATION_DENIED;
			}

		else if (getConfigs().getInt(CHARACTER_NAME_OPTION) == 2)
			for (int i = 0; i < name.length(); i++)
			{
				if (letters.contains(java.lang.Character.toString(name.charAt(i))))
					return RMC_CREATION_DENIED;
			}

		try {
			if (characters.exist(name, getConfigs().getBool(CHARACTER_IGNORING_CASE)))
				return RMC_NAME_IN_USE;
		} catch (RagnarokException e) {
			return RMC_NAME_IN_USE;
		}

		return null;
	}

	/**
	 * Procedimento interno utilizado para localizar o n�mero de slot de um determinado personagem.
	 * A busca � feita levando em considera��o uma sess�o no servidor de personagem.
	 * @param sd sess�o no servidor do qual ser� realizada a busca pelo slot do personagem.
	 * @param charID c�digo de identifica��o do personagem do qual deseja o n�mero de slot.
	 * @return aquisi��o do n�mero de slot respectivo a identifica��o do personagem ou -1 se n�o encontrar.
	 */

	private int foundCharSlot(CharSessionData sd, int charID)
	{
		for (int slot = 0; slot < MAX_CHARS; slot++)
			if (sd.getCharData(slot) != null && sd.getCharData(slot).getID() == charID)
				return slot;

		return -1;
	}

	/**
	 * Procedimento para realizar a dele��o de todos os dados poss�veis de um determinado personagem.
	 * Esta dele��o � feita atrav�s da confirma��o do e-mail que ser� recebido atrav�s de pacote.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param command identifica��o do pacote (comando) recebido para excluir um personagem.
	 */

	public void deleteCharByEmail(CFileDescriptor fd, short command)
	{
		CharSessionData sd = fd.getSessionData();

		int charID;
		String email;

		switch (command)
		{
			case PACKET_CH_DELETE_CHAR:
				CH_DeleteChar deleteChar = new CH_DeleteChar();
				deleteChar.receive(fd);
				charID = deleteChar.getCharID();
				email = deleteChar.getEmail();
				break;

			case PACKET_CH_DELETE_CHAR2:
				CH_DeleteChar2 deleteChar2 = new CH_DeleteChar2();
				deleteChar2.receive(fd);
				charID = deleteChar2.getCharID();
				email = deleteChar2.getEmail();
				break;

			default:
				throw new RagnarokRuntimeException("0x%s n�o � usado aqui", HexUtil.parseInt(command, 4));
		}

		logInfo("solicita��o para dele��o de personagem por e-mail (aid: %d, cid: %d).\n", sd.getID(), charID);

		if (email.isEmpty() || !email.equals(sd.getEmail()) || email.equals(DEFAULT_EMAIL))
		{
			client.refuseDeleteChar(fd, RDC_INCORRET_EMAIL_ADDRESS);
			return;
		}

		int slot = foundCharSlot(sd, charID);

		if (slot < 0)
		{
			client.refuseDeleteChar(fd, RDC_DENIED);
			return;
		}

		try {

			if (characters.remove(charID))
			{
				sd.setCharData(null, slot);
				client.acceptDeleteChar(fd);
			}

			else
				client.refuseDeleteChar(fd, RDC_CANNOT_BE_DELETED);

		} catch (RagnarokException e) {

			logError("falha ao excluir personagem (aid: %d, cid: %d).\n", sd.getID(), charID);
			logException(e);
		}
	}

	/**
	 * Realiza a a��o de reservar de um hor�rio para que um personagem espec�fico possa ser exclu�do.
	 * A reserva considera o hor�rio atual do servidor com um adicional de intervalo configur�vel.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void deleteCharReserved(CFileDescriptor fd)
	{
		CH_DeleteCharReserved packet = new CH_DeleteCharReserved();
		packet.receive(fd);

		CharSessionData sd = fd.getSessionData();

		int charID = packet.getCharID();
		int slot = foundCharSlot(sd, charID);

		if (slot == -1)
		{
			client.deleteCharReserved(fd, charID, 0, DCR_CHAR_NOT_FOUND);
			return;
		}

		try {

			long deleteDate = characters.getDeleteDate(charID);

			if (deleteDate == 0)
			{
				// TODO verificar se est� em um cl�
				// TODO verificar se est� em um grupo

				deleteDate = now() + seconds(getConfigs().getInt(CHARACTER_DELETE_DELAY));

				if (characters.setDeleteDate(charID, deleteDate))
				{
					logInfo("personagem reservado para ser exclu�do (aid: %d, cid: %d).\n", sd.getID(), charID);
					client.deleteCharReserved(fd, charID, deleteDate, DCR_ADDED_TO_QUEUE);
					return;
				}
			}

		} catch (RagnarokException e) {

			logError("falha ao obter hor�rio de dele��o (cid: %d):\n", charID);
			logException(e);
		}

		client.deleteCharReserved(fd, charID, 0, DCR_ALREADY_ON_QUEUE);
	}

	/**
	 * Solicita��o para confirmar a dele��o do personagem da conta ap�s o hor�rio de reserva do mesmo.
	 * Ir� verificar se as condi��es de hor�rio, data de nascimento, n�vel de base est�o de acordo.
	 * Em seguida informa ao cliente o resultado da a��o, se foi bem sucedido ou o problema ocorrido.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void deleteCharByBirthDate(CFileDescriptor fd)
	{
		CH_DeleteChar3 packet = new CH_DeleteChar3();
		packet.receive(fd);

		CharSessionData sd = fd.getSessionData();

		int charID = packet.getCharID();
		String temp = packet.getBirthDate();
		String birthDate = format("%s-%s-%s", temp.substring(0, 2), temp.substring(2, 4), temp.substring(4, 6));

		logInfo("solicita��o para dele��o de personagem por data de nascimento (aid: %d, cid: %d).\n", sd.getID(), charID);

		int slot = foundCharSlot(sd, charID);

		if (slot == -1)
		{
			client.deleteCharReserved(fd, charID, 0, DCR_CHAR_NOT_FOUND);
			return;
		}

		try {

			long deleteDate = characters.getDeleteDate(charID);

			if (deleteDate == 0 || deleteDate > now())
			{
				client.deleteAccept(fd, charID, DC_NOT_YET_POSSIBLE_TIME);
				return;
			}

			if (!birthDate.equals(sd.getBirthdate()))
			{
				client.deleteAccept(fd, charID, DC_BIRTH_DATE);
				return;
			}

			int baseLevel = characters.getBaseLevel(charID);
			int deleteLevel = getConfigs().getInt(CHARACTER_DELETE_LEVEL);

			if (deleteLevel > 0 && baseLevel >= deleteLevel || deleteLevel < 0 && baseLevel <= deleteLevel)
			{
				client.deleteAccept(fd, charID, DC_DUE_SETTINGS);
				return;
			}

			if (!characters.remove(charID))
			{
				sd.setCharData(null, slot);
				client.deleteAccept(fd, charID, DC_SUCCCESS_DELETE);
				return;
			}

			client.deleteAccept(fd, charID, DC_DATABASE_ERROR_DELETE);

		} catch (RagnarokException e) {

			logError("falha ao confirmar dele��o de personagem (aid: %d, cid: %d):\n");
			logException(e);
		}
	}

	/**
	 * Cancela a reserva para a dele��o de um personagem do qual j� esteja em reserva marcada.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void deleteCharCancel(CFileDescriptor fd)
	{
		CH_DeleteCharCancel packet = new CH_DeleteCharCancel();
		packet.receive(fd);

		CharSessionData sd = fd.getSessionData();

		int charID = packet.getCharID();

		try {

			if (foundCharSlot(sd, charID) != -1 && characters.cancelDelete(charID))
			{
				client.deleteCancel(fd, charID, DCC_SUCCCESS_CANCEL);
				return;
			}

		} catch (RagnarokException e) {

			logError("falha ao cancelar reserva para excluir personagem (aid: %d, cid: %d):\n", sd.getID(), charID);
			logException(e);
		}

		client.deleteCancel(fd, charID, DCC_DATABASE_ERROR_CANCEL);
	}

	/**
	 * 
	 * @param pincode
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public boolean isPincodeAllowed(String pincode)
	{
		// TODO pincode_allowed
		return false;
	}

	/**
	 * 
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void parsePincodeSetNew(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("definindo primeiro c�digo PIN (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_setnew
	}

	/**
	 * 
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void parsePincodeChange(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("alterando c�digo PIN existente (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_change
	}

	/**
	 * 
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void parsePincodeCheck(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("recebendo c�digo PIN inserido (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		if (!getConfigs().getBool(PINCODE_ENABLED))
			return;

		// TODO chclif_parse_pincode_check
	}

	/**
	 * Recebe a solicita��o de um jogador para selecionar um personagem especificado pelo mesmo.
	 * Verifica a exist�ncia do personagem no slot especificado e a conex�o com o servidor de mapa.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 */

	public void selectChar(CFileDescriptor fd)
	{
		CH_SelectChar packet = new CH_SelectChar();
		packet.receive(fd);

		try {

			Character character = null;
			CharSessionData sd = fd.getSessionData();
			int charID = characters.getCharID(sd.getID(), packet.getSlot());

			logDebug("recebendo solicita��o para selecionar personagem (aid: %d, slot: %d).\n", sd.getID(), packet.getSlot());

			if (sd.getFlag().is(CharSessionData.RETRIEVING_GUILD_BOUND_ITEMS))
			{
				client.refuseEnter(fd, RE_REJECTED_FROM_SERVER);
				return;
			}

			login.setCharOnline(fd, charID, OnlineCharData.UNKNOW_SERVER);

			if ((character = characters.get(charID)) == null)
			{
				login.setCharOffline(fd.getID(), charID);
				client.refuseEnter(fd, RE_REJECTED_FROM_SERVER);
				return;
			}

			logNotice("personagem selecionado (fd: %d, aid: %d, cid: %d).\n", fd.getID(), sd.getID(), charID);

			int mapServerID = 0;

			if ((mapServerID = map.searchMapServerID(character.getLocations().getLastPoint().getMapID(), -1, s(-1))) < 0 ||
				character.getLocations().getLastPoint().getMapID() == 0)
			{
				if (!map.hasConnection())
				{
					client.sendNotifyResult(fd, NA_SERVER_CLOSED);
					return;
				}

				short mapID = 0;
				MapPoint lastPosition = character.getLocations().getLastPoint();

				if ((mapServerID = map.searchMapServerID((mapID = mapname2mapid(MAP_PRONTERA)), -1, s(-1))) >= 0)
				{
					lastPosition.setX(273);
					lastPosition.setY(354);
				}

				else if ((mapServerID = map.searchMapServerID((mapID = mapname2mapid(MAP_GEFFEN)), -1, s(-1))) >= 0)
				{
					lastPosition.setX(120);
					lastPosition.setY(100);
				}

				else if ((mapServerID = map.searchMapServerID((mapID = mapname2mapid(MAP_MORROC)), -1, s(-1))) >= 0)
				{
					lastPosition.setX(160);
					lastPosition.setY(94);
				}

				else if ((mapServerID = map.searchMapServerID((mapID = mapname2mapid(MAP_ALBERTA)), -1, s(-1))) >= 0)
				{
					lastPosition.setX(116);
					lastPosition.setY(57);
				}

				else if ((mapServerID = map.searchMapServerID((mapID = mapname2mapid(MAP_PAYON)), -1, s(-1))) >= 0)
				{
					lastPosition.setX(87);
					lastPosition.setY(117);
				}

				else if ((mapServerID = map.searchMapServerID((mapID = mapname2mapid(MAP_IZLUDE)), -1, s(-1))) >= 0)
				{
					lastPosition.setX(94);
					lastPosition.setY(103);
				}

				else
				{
					logInfo("conex�o fecahda, nenhum servidor de mapa dispon�vel com uma cidade principal.\n");
					client.sendNotifyResult(fd, NA_SERVER_CLOSED);
					return;
				}

				lastPosition.setMapID(mapID);
			}

			if (!map.hasConnection(mapServerID))
			{
				client.sendNotifyResult(fd, NA_SERVER_CLOSED);
				return;
			}

			client.notifyZoneServer(fd, character, mapServerID);

			AuthNode auth = new AuthNode();
			auth.setAccountID(sd.getID());
			auth.setCharID(charID);
			auth.setSeed(sd.getSeed());
			auth.getExpiration().set(sd.getExpiration().get());
			auth.setGroup(sd.getGroup());
			auths.add(auth);

		} catch (RagnarokException e) {

			logError("falha ao selecionar personagem (aid: %d, slot: %d):\n", fd.getID(), packet.getSlot());
			logException(e);
		}

		client.refuseEnter(fd, RE_REJECTED_FROM_SERVER);
	}
}

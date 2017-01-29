package org.diverproject.jragnarok.server.character.control;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.timestamp;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.util.lang.IntUtil.interval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.character.ChangeSex;
import org.diverproject.jragnarok.server.character.CharData;
import org.diverproject.jragnarok.server.character.CharSessionData;
import org.diverproject.jragnarok.server.common.Job;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.jragnarok.server.common.entities.Character;
import org.diverproject.jragnarok.server.common.entities.Experience;
import org.diverproject.jragnarok.server.common.entities.MercenaryRank;
import org.diverproject.jragnarok.util.MapPoint;
import org.diverproject.util.collection.Index;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.DynamicList;
import org.diverproject.util.collection.abstraction.StaticArray;

/**
 * <h1>Controle para Personagens</h1>
 *
 * <p>Neste controle não será trabalhado apenas a tabela contendo os dados básicos dos personagens.
 * Também será considerado algumas das dependências referente a ele que seja diretamente dele.
 * Essas dependências incluem: atributos, aparência, família, experiência e classificação de assistentes.</p>
 *
 * <p>Para este controle será permitido obter todas as informações de um personagem de forma direta e completa.
 * Também será possível adicionar, atualizar ou recarregar as informações ou parte das informações (dependências).
 * Apenas no caso da exclusão que será único, já que as dependências não podem continuar sem um personagem.</p>
 *
 * @see AbstractControl
 * @see Connection
 * @see Character
 * @see Stats
 * @see Look
 * @see Family
 * @see Experience
 * @see MercenaryRank
 *
 * @author Andrew
 */

public class CharacterControl extends AbstractControl
{
	/**
	 * Cria uma nova instância de um controlador para persistência de dados dos personagens.
	 * @param connection conexão com o banco de dados que será usada.
	 */

	public CharacterControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Procedimento interno para validação das informações com valores não aceitáveis de um personagem.
	 * Não será aceito objetos nulos, com dependências nulas ou com identificação inválida.
	 * @param character referência do objeto que representa os dados do personagem no sistema.
	 * @param checkID true para verificar o código de identificação ou false caso contrário.
	 * @throws RagnarokException informações inválidas ou nulas.
	 */

	public void validate(Character character, boolean checkID) throws RagnarokException
	{
		if (character == null)
			throw new RagnarokException("personagem nulo");

		if (character.getName().equals(Character.UNKNOWN))
			throw new RagnarokException("personagem com nome inválido");

		if (checkID && character.getID() <= 0)
			throw new RagnarokException("personagem inválido");
	}

	/**
	 * Seleciona todas as informações necessárias para a formação do objeto representativo de um personagem.
	 * @param charID código de identificação do personagem do qual deseja obter as informações.
	 * @return aquisição do objeto contendo as informações do personagem desejado.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public Character get(int charID) throws RagnarokException
	{
		Character character = null;

		String table = Tables.getInstance().getCharacters();
		String tableStats = Tables.getInstance().getCharStats();
		String tableLook = Tables.getInstance().getCharLook();
		String tableFamily = Tables.getInstance().getCharFamily();
		String tableExp = Tables.getInstance().getCharExperiences();
		String tableRank = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT id, name, sex, zeny, status_point, skill_point, jobid, hp, max_hp, sp, max_sp, "
						+	"manner, effect_state, virtue, base_level, job_level, rename_count, unban_time, delete_date, "
						+	"moves, font, unique_item_counter, "
						+	"strength, agility, vitality, intelligence, dexterity, luck, "
						+	"hair, hair_color, clothes_color, body, weapon, shield, head_top, head_mid, head_bottom, robe, "
						+	"partner, father, mother, child, "
						+	"base, job, fame, "
						+	"archer_faith, archer_calls, spear_faith, spear_calls, sword_faith, sword_calls "
						+	"FROM %s "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"WHERE id = ?", table, tableStats, tableStats, table, tableLook, tableLook, table,
						tableFamily, tableFamily, table, tableRank, tableRank, table, tableExp, tableExp, table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				character = new Character();
				character.setID(charID);

				setCharacterResult(character, rs);
				setCharacterStats(character, rs);
				setCharacterLook(character, rs);
				setCharacterFamily(character, rs);
				setCharacterExperience(character, rs);
				setCharacterMercenaryRank(character, rs);
				reloadLocations(character);
			}

			if (character != null)
				logDebug("Character#%d obtido.\n", character.getID());

			return character;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as informações de um personagem respectivo aos dados básicos conforme um resultado.
	 * @param character referência do objeto contendo as informações do personagem a serem definidas.
	 * @param rs resultado da consulta efetuada no banco de dados contendo os atributos.
	 * @throws SQLException apenas por falha de conexão com o banco de dados.
	 */

	public void setCharacterResult(Character character, ResultSet rs) throws SQLException
	{
		character.setName(rs.getString("name"));
		character.setSex(Sex.parse(rs.getString("sex").charAt(0)));
		character.setZeny(rs.getInt("zeny"));
		character.setStatusPoint(rs.getShort("status_point"));
		character.setSkillPoint(rs.getShort("skill_point"));
		character.setJob(Job.parse(rs.getShort("jobid")));
		character.setHP(rs.getInt("hp"));
		character.setMaxHP(rs.getInt("max_hp"));
		character.setSP(rs.getShort("sp"));
		character.setMaxSP(rs.getShort("max_sp"));
		character.setManner(rs.getShort("manner"));
		character.getEffectState().setValue(rs.getInt("effect_state"));
		character.setVirtue(rs.getShort("virtue"));
		character.setBaseLevel(rs.getInt("base_level"));
		character.setJobLevel(rs.getInt("job_level"));
		character.setRename(rs.getShort("rename_count"));
		character.getUnbanTime().set(timestamp(rs.getTimestamp("unban_time")));
		character.getDeleteDate().set(timestamp(rs.getTimestamp("delete_date")));
		character.setMoves(rs.getShort("moves"));
		character.setFont(rs.getByte("font"));
		character.setUniqueItemCounter(rs.getInt("unique_item_counter"));		
	}

	/**
	 * Atualiza as informações de um personagem respectivo aos atributos distribuídos conforme um resultado.
	 * @param character referência do objeto contendo as informações do personagem a serem definidas.
	 * @param rs resultado da consulta efetuada no banco de dados contendo os atributos.
	 * @throws SQLException apenas por falha de conexão com o banco de dados.
	 */

	public void setCharacterStats(Character character, ResultSet rs) throws SQLException
	{
		character.getStats().setStrength(rs.getShort("strength"));
		character.getStats().setAgility(rs.getShort("agility"));
		character.getStats().setVitality(rs.getShort("vitality"));
		character.getStats().setIntelligence(rs.getShort("intelligence"));
		character.getStats().setDexterity(rs.getShort("dexterity"));
		character.getStats().setLuck(rs.getShort("luck"));
	}

	/**
	 * Atualiza as informações de um personagem respectivo aos detalhes de aparência conforme um resultado.
	 * @param character referência do objeto contendo as informações do personagem a serem definidas.
	 * @param rs resultado da consulta efetuada no banco de dados contendo a aparência.
	 * @throws SQLException apenas por falha de conexão com o banco de dados.
	 */

	public void setCharacterLook(Character character, ResultSet rs) throws SQLException
	{
		character.getLook().setHair(rs.getShort("hair"));
		character.getLook().setHairColor(rs.getShort("hair_color"));
		character.getLook().setClothesColor(rs.getShort("clothes_color"));
		character.getLook().setBody(rs.getShort("body"));
		character.getLook().setWeapon(rs.getShort("weapon"));
		character.getLook().setShield(rs.getShort("shield"));
		character.getLook().setHeadTop(rs.getShort("head_top"));
		character.getLook().setHeadMid(rs.getShort("head_mid"));
		character.getLook().setHeadBottom(rs.getShort("head_bottom"));
		character.getLook().setRobe(rs.getShort("robe"));
	}

	/**
	 * Atualiza as informações de um personagem respectivo as relações de parentesco conforme um resultado.
	 * @param character referência do objeto contendo as informações do personagem a serem definidas.
	 * @param rs resultado da consulta efetuada no banco de dados contendo os parentescos.
	 * @throws SQLException apenas por falha de conexão com o banco de dados.
	 */

	public void setCharacterFamily(Character character, ResultSet rs) throws SQLException
	{
		character.getFamily().setPartner(rs.getInt("partner"));
		character.getFamily().setFather(rs.getInt("father"));
		character.getFamily().setMother(rs.getInt("mother"));
		character.getFamily().setChild(rs.getInt("child"));
	}

	/**
	 * Atualiza as informações de um personagem respectivo os níveis de experiências conforme um resultado.
	 * @param character referência do objeto contendo as informações do personagem a serem definidas.
	 * @param rs resultado da consulta efetuada no banco de dados contendo as experiências.
	 * @throws SQLException apenas por falha de conexão com o banco de dados.
	 */

	public void setCharacterExperience(Character character, ResultSet rs) throws SQLException
	{
		character.getExperience().setBase(rs.getInt("base"));
		character.getExperience().setFame(rs.getInt("job"));
		character.getExperience().setJob(rs.getInt("fame"));
	}

	/**
	 * Atualiza as informações de um personagem respectivo as classificações dos assistentes conforme um resultado.
	 * @param character referência do objeto contendo as informações do personagem a serem definidas.
	 * @param rs resultado da consulta efetuada no banco de dados contendo as classificações.
	 * @throws SQLException apenas por falha de conexão com o banco de dados.
	 */

	public void setCharacterMercenaryRank(Character character, ResultSet rs) throws SQLException
	{
		character.getMercenaryRank().setArcherFaith(rs.getInt("archer_faith"));
		character.getMercenaryRank().setArcherCalls(rs.getInt("archer_calls"));
		character.getMercenaryRank().setSpearFaith(rs.getInt("spear_faith"));
		character.getMercenaryRank().setSpearCalls(rs.getInt("spear_calls"));
		character.getMercenaryRank().setSwordFaith(rs.getInt("sword_faith"));
		character.getMercenaryRank().setSwordCalls(rs.getInt("sword_calls"));
	}

	/**
	 * Atualiza as informações de um personagem respectivo as localizações em mapas conforme um resultado.
	 * @param mapPoint objeto que representa uma localização em mapa que será atualizado.
	 * @param rs resultado da consulta efetuada no banco de dados contendo as classificações.
	 * @throws SQLException apenas por falha de conexão com o banco de dados.
	 */

	public void setLocation(MapPoint mapPoint, ResultSet rs) throws SQLException
	{
		if (mapPoint == null)
			return;

		mapPoint.setMapID(rs.getShort("mapid"));
		mapPoint.setX(rs.getInt("coord_x"));
		mapPoint.setY(rs.getInt("coord_Y"));
	}

	/**
	 * Adiciona um novo personagem ao banco de dados inserindo novas informações relacionadas a ele.
	 * Caso alguma das dependências já existam por algum motivo, elas serão atualizadas com base neste.
	 * @param character personagem contendo todas as informações básicas e suas dependências.
	 * @return true se conseguir adicionar ou false caso contrário (não esperado).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean add(Character character) throws RagnarokException
	{
		validate(character, false);

		String table = Tables.getInstance().getCharacters();
		String sql = format("REPLACE INTO %s (name, sex, zeny, status_point, skill_point, jobid, hp, max_hp, sp, max_sp, "
						+	"manner, effect_state, virtue, base_level, job_level, rename_count, unban_time, delete_date, "
						+	"moves, font, unique_item_counter) "
						+	"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, character.getName());
			ps.setString(2, java.lang.Character.toString(character.getSex().c));
			ps.setInt(3, character.getZeny());
			ps.setInt(4, character.getStatusPoint());
			ps.setInt(5, character.getSkillPoint());
			ps.setShort(6, character.getJob().CODE);
			ps.setInt(7, character.getHP());
			ps.setInt(8, character.getMaxHP());
			ps.setShort(9, character.getSP());
			ps.setShort(10, character.getMaxSP());
			ps.setShort(11, character.getManner());
			ps.setInt(12, character.getEffectState().getValue());
			ps.setShort(13, character.getVirtue());
			ps.setInt(14, character.getBaseLevel());
			ps.setInt(15, character.getJobLevel());
			ps.setShort(16, character.getRename());
			ps.setTimestamp(17, timestamp(character.getUnbanTime().get()));
			ps.setTimestamp(18, timestamp(character.getDeleteDate().get()));
			ps.setShort(19, character.getMoves());
			ps.setByte(20, character.getFont());
			ps.setInt(21, character.getUniqueItemCounter());

			if (interval(ps.executeUpdate(), 1, 2))
			{
				logDebug("Character#%d adicionado a '%s'.\n", character.getID(), table);

				sql = format("SELECT id FROM %s WHERE name = ?", table);

				ps = prepare(sql);
				ps.setString(1, character.getName());

				ResultSet rs = ps.executeQuery();

				if (rs.next())
				{
					character.setID(rs.getInt("id"));

					addStats(character);
					addLook(character);
					addFamily(character);
					addExperience(character);
					addMercenaryRank(character);
				}

				return true;
			}

			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona uma nova dependência de um personagem referente aos atributos básicos do mesmo.
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir adicionar ou false caso contrário (não esperado).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean addStats(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharStats();
		String sql = format("REPLACE INTO %s (charid, strength, agility, vitality, intelligence, dexterity, luck) "
						+	"VALUES (?, ?, ?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());
			ps.setShort(2, character.getStats().getStrength());
			ps.setShort(3, character.getStats().getAgility());
			ps.setShort(4, character.getStats().getVitality());
			ps.setShort(5, character.getStats().getIntelligence());
			ps.setShort(6, character.getStats().getDexterity());
			ps.setShort(7, character.getStats().getLuck());

			logDebug("Stats#%d adicionado à '%s'.\n", character.getID(), table);

			return ps.execute();

		} catch (SQLException e) {

			if (e.getErrorCode() == DUPLICATED_KEY)
				return setStats(character);
			else
				throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona uma nova dependência de um personagem referente a definição de aparência do mesmo.
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir adicionar ou false caso contrário (não esperado).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean addLook(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharLook();
		String sql = format("REPLACE INTO %s (charid, hair, hair_color, clothes_color, body, weapon, shield, "
						+	"head_top, head_mid, head_bottom, robe) "
						+	"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());
			ps.setShort(2, character.getLook().getHair());
			ps.setShort(3, character.getLook().getHairColor());
			ps.setShort(4, character.getLook().getClothesColor());
			ps.setShort(5, character.getLook().getBody());
			ps.setShort(6, character.getLook().getWeapon());
			ps.setShort(7, character.getLook().getShield());
			ps.setShort(8, character.getLook().getHeadTop());
			ps.setShort(9, character.getLook().getHeadMid());
			ps.setShort(10, character.getLook().getHeadBottom());
			ps.setShort(11, character.getLook().getRobe());

			logDebug("Look#%d adicionado à '%s'.\n", character.getID(), table);

			return ps.execute();

		} catch (SQLException e) {

			if (e.getErrorCode() == DUPLICATED_KEY)
				return setLook(character);
			else
				throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona uma nova dependência de um personagem referente a relações familiares do mesmo.
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir adicionar ou false caso contrário (não esperado).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean addFamily(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharFamily();
		String sql = format("REPLACE INTO %s (charid, partner, father, mother, child) VALUES (?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());
			ps.setInt(2, character.getFamily().getPartner());
			ps.setInt(3, character.getFamily().getFather());
			ps.setInt(4, character.getFamily().getMother());
			ps.setInt(5, character.getFamily().getChild());

			logDebug("Family#%d adicionado à '%s'.\n", character.getID(), table);

			return ps.execute();

		} catch (SQLException e) {

			if (e.getErrorCode() == DUPLICATED_KEY)
				return setFamily(character);
			else
				throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona uma nova dependência de um personagem referente aos níveis de experiência do mesmo.
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir adicionar ou false caso contrário (não esperado).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean addExperience(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharExperiences();
		String sql = format("REPLACE INTO %s (charid, base, job, fame) VALUES (?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());
			ps.setInt(2, character.getExperience().getBase());
			ps.setInt(3, character.getExperience().getJob());
			ps.setInt(4, character.getExperience().getFame());

			logDebug("Experience#%d adicionado à '%s'.\n", character.getID(), table);

			return ps.execute();

		} catch (SQLException e) {

			if (e.getErrorCode() == DUPLICATED_KEY)
				return setFamily(character);
			else
				throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona uma nova dependência de um personagem referente a classificação de assistentes do mesmo.
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir adicionar ou false caso contrário (não esperado).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean addMercenaryRank(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("REPLACE INTO %s (charid, archer_faith, archer_calls, spear_faith, spear_calls, sword_faith, sword_calls) "
						+	"VALUES (?, ?, ?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());
			ps.setInt(2, character.getMercenaryRank().getArcherFaith());
			ps.setInt(3, character.getMercenaryRank().getArcherCalls());
			ps.setInt(4, character.getMercenaryRank().getSpearFaith());
			ps.setInt(5, character.getMercenaryRank().getSpearCalls());
			ps.setInt(6, character.getMercenaryRank().getSwordFaith());
			ps.setInt(7, character.getMercenaryRank().getSwordCalls());

			logDebug("MercenaryRank#%d adicionado à '%s'.\n", character.getID(), table);

			return ps.execute();

		} catch (SQLException e) {

			if (e.getErrorCode() == DUPLICATED_KEY)
				return setMercenaryRank(character);
			else
				throw new RagnarokException(e.getMessage());
		}		
	}

	/**
	 * Adiciona uma nova dependência de um personagem referente as localizações salvas do mesmo.
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir adicionar ou false caso contrário (não esperado).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean addLocations(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("REPLACE INTO %s (chaird, num, mapid, coord_x, coord_y) VALUES (?, ?, ?, ?, ?)", table);

		try {

			addLocations_Sub(character.getID(), 0, character.getLocations().getLastPoint(), sql);
			addLocations_Sub(character.getID(), 1, character.getLocations().getSavePoint(), sql);

			for (int i = 0; i < character.getLocations().getMemoPoints().length; i++)
				addLocations_Sub(character.getID(), 2+i, character.getLocations().getMemoPoint(i), sql);

			logDebug("Location#%d adicionado à '%s'.\n", character.getID(), table);

			return true;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}		
	}

	/**
	 * Procedimento interno usado para executar uma query que deverá garantir as informações de um ponto em mapa.
	 * @param charID código de identificação do personagem do qual será associado ao ponto especificado.
	 * @param num número do índice da localização onde - 0: last_point, 1: save_point, 2+: memo_point.
	 * @param mapPoint informações referente ao nome do mapa e coordenadas do mesmo para serem salvos.
	 * @param sql string contendo a query que será executada no banco de dados para realizar a atualização.
	 * @return true se conseguir adicionar ou false caso contrário (charID/num não encontrados).
	 * @throws SQLException apenas se houver problema de conexão com o banco de dados.
	 * @throws RagnarokException apenas se houver problema de conexão com o banco de dados.
	 */

	private boolean addLocations_Sub(int charID, int num, MapPoint mapPoint, String sql) throws SQLException, RagnarokException
	{
		PreparedStatement ps = prepare(sql);
		ps.setInt(1, charID);
		ps.setInt(2, num);
		ps.setShort(3, mapPoint.getMapID());
		ps.setInt(4, mapPoint.getX());
		ps.setInt(5, mapPoint.getY());

		return interval(ps.executeUpdate(), 1, 2);
	}

	/**
	 * Atualiza todas as informações básicas e de dependências de um determinado personagem.
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean set(Character character) throws RagnarokException
	{
		validate(character, true);

		return	setBase(character) ||
				setStats(character) ||
				setLook(character) ||
				setFamily(character) ||
				setExperience(character) ||
				setMercenaryRank(character) ||
				setLocations(character);
	}

	/**
	 * Atualiza as informações existentes de um personagem no banco de dados conforme abaixo:
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	private boolean setBase(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET name = ?, sex = ?, zeny = ?, status_point = ?, skill_point = ?, jobid = ?, "
						+	"hp = ?, max_hp = ?, sp = ?, max_sp = ?, manner = ?, effect_state = ?, virtue = ?, base_level = ?, "
						+	"job_level = ?, rename = ?, unban_time = ?, delete_date = ?, moves = ?, font = ?, unique_item_counter = ? "
						+	"WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, character.getName());
			ps.setString(2, java.lang.Character.toString(character.getSex().c));
			ps.setInt(3, character.getZeny());
			ps.setInt(4, character.getStatusPoint());
			ps.setInt(5, character.getSkillPoint());
			ps.setShort(6, character.getJob().CODE);
			ps.setInt(7, character.getHP());
			ps.setInt(8, character.getMaxHP());
			ps.setShort(9, character.getSP());
			ps.setShort(10, character.getMaxSP());
			ps.setShort(11, character.getManner());
			ps.setInt(12, character.getEffectState().getValue());
			ps.setShort(13, character.getVirtue());
			ps.setInt(14, character.getBaseLevel());
			ps.setInt(15, character.getJobLevel());
			ps.setShort(16, character.getRename());
			ps.setTimestamp(17, timestamp(character.getUnbanTime().get()));
			ps.setTimestamp(18, timestamp(character.getDeleteDate().get()));
			ps.setShort(19, character.getMoves());
			ps.setByte(20, character.getFont());
			ps.setInt(21, character.getUniqueItemCounter());
			ps.setInt(22, character.getID());

			if (ps.executeUpdate() == 1)
			{

				logDebug("Character#%d atualizado em '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("Character#%d não atualizado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza os pontos de atributos existente de um personagem no banco de dados conforme abaixo:
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	private boolean setStats(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharStats();
		String sql = format("UPDATE %s SET strength = ?, agility = ?, vitality = ?, intelligence = ?, dexterity = ?, luck = ? "
						+	"WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setShort(1, character.getStats().getStrength());
			ps.setShort(2, character.getStats().getAgility());
			ps.setShort(3, character.getStats().getVitality());
			ps.setShort(4, character.getStats().getIntelligence());
			ps.setShort(5, character.getStats().getDexterity());
			ps.setShort(6, character.getStats().getLuck());
			ps.setInt(7, character.getID());

			if (ps.executeUpdate() == 1)
			{

				logDebug("Stats#%d atualizado em '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("Stats#%d não encontrado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as definições de aparência existente de um personagem no banco de dados conforme abaixo:
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	private boolean setLook(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharLook();
		String sql = format("UPDATE %s SET hair = ?, hair_color = ?, clothes_color = ?, body = ?, "
						+	"weapon = ?, shield = ?, head_top = ?, head_mid = ?, head_bottom = ?, robe = ? "
						+	"WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setShort(1, character.getLook().getHair());
			ps.setShort(2, character.getLook().getHairColor());
			ps.setShort(3, character.getLook().getClothesColor());
			ps.setShort(4, character.getLook().getBody());
			ps.setShort(5, character.getLook().getWeapon());
			ps.setShort(6, character.getLook().getShield());
			ps.setShort(7, character.getLook().getHeadTop());
			ps.setShort(8, character.getLook().getHeadMid());
			ps.setShort(9, character.getLook().getHeadBottom());
			ps.setShort(10, character.getLook().getRobe());
			ps.setInt(11, character.getID());

			if (ps.executeUpdate() == 1)
			{

				logDebug("Look#%d atualizado em '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("Look#%d não atualizado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as relações familiares existente de um personagem no banco de dados conforme abaixo:
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	private boolean setFamily(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharStats();
		String sql = format("UPDATE %s SET partner = ?, father = ?, mother = ?, child = ? WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getFamily().getPartner());
			ps.setInt(2, character.getFamily().getFather());
			ps.setInt(3, character.getFamily().getMother());
			ps.setInt(4, character.getFamily().getChild());
			ps.setInt(5, character.getID());

			if (ps.executeUpdate() == 1)
			{

				logDebug("Family#%d atualizado em '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("Family#%d não atualizado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza os níveis de experiência existente de um personagem no banco de dados conforme abaixo:
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	private boolean setExperience(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharStats();
		String sql = format("UPDATE %s SET base = ?, job = ?, fame = ? WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getExperience().getBase());
			ps.setInt(2, character.getExperience().getJob());
			ps.setInt(3, character.getExperience().getFame());
			ps.setInt(4, character.getID());

			if (ps.executeUpdate() == 1)
			{
				logDebug("Experience#%d atualizado em '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("Experience#%d não atualizado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza a classificação dos mercenários existentes de um personagem no banco de dados conforme abaixo:
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean setMercenaryRank(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("UPDATE %s SET archer_faith = ?, archer_calls = ?, spear_faith = ?, "
						+	"spear_calls = ?, sword_faith = ?, sword_calls = ? WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getMercenaryRank().getArcherFaith());
			ps.setInt(2, character.getMercenaryRank().getArcherCalls());
			ps.setInt(3, character.getMercenaryRank().getSpearFaith());
			ps.setInt(4, character.getMercenaryRank().getSpearCalls());
			ps.setInt(5, character.getMercenaryRank().getSwordFaith());
			ps.setInt(6, character.getMercenaryRank().getSwordCalls());
			ps.setInt(7, character.getID());

			logDebug("MercenaryRank#%d atualizado em '%s'.\n", character.getID(), table);

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as localizações em mapas salvos existentes de um personagem no banco de dados conforme abaixo:
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean setLocations(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("UPDATE %s SET mpaname = ?, coord_x = ?, coord_y = ? WHERE charid = ? AND num = ?", table);

		try {

			boolean updated = false;

			if (setLocations_Sub(character.getID(), 0, character.getLocations().getLastPoint(), sql) &&
				setLocations_Sub(character.getID(), 0, character.getLocations().getSavePoint(), sql))
				updated = true;

			for (int i = 0; i < character.getLocations().getMemoPoints().length; i++)
				setLocations_Sub(character.getID(), 2+i, character.getLocations().getMemoPoint(i), sql);

			if (updated)
				logDebug("MercenaryRank#%d atualizado em '%s'.\n", character.getID(), table);

			return updated;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Procedimento interno usado para executar uma query que deverá atualizar as informações de um ponto em mapa.
	 * @param charID código de identificação do personagem do qual será associado ao ponto especificado.
	 * @param num número do índice da localização onde - 0: last_point, 1: save_point, 2+: memo_point.
	 * @param mapPoint informações referente ao nome do mapa e coordenadas do mesmo para serem salvos.
	 * @param sql string contendo a query que será executada no banco de dados para realizar a atualização.
	 * @return true se conseguir atualizar ou false caso contrário (charID/num não encontrados).
	 * @throws SQLException apenas se houver problema de conexão com o banco de dados.
	 * @throws RagnarokException apenas se houver problema de conexão com o banco de dados.
	 */

	private boolean setLocations_Sub(int charID, int num, MapPoint mapPoint, String sql) throws SQLException, RagnarokException
	{
		PreparedStatement ps = prepare(sql);
		ps.setShort(1, mapPoint.getMapID());
		ps.setInt(2, mapPoint.getX());
		ps.setInt(3, mapPoint.getY());
		ps.setInt(4, charID);
		ps.setInt(5, num);

		return ps.executeUpdate() == 1;
	}

	/**
	 * Recarrega todas as informações básicas do banco de dados de um personagem e suas dependências.
	 * Recarregar as informações pode ser necessário para garantir que alterações realizadas de fora do
	 * sistema possam ser aplicadas sobre o personagem sem que seja necessário efetuar um logout.
	 * @param character personagem do qual deverá ter suas informações recarregadas.
	 * @return true se conseguir atualizar ou false caso os dados do personagem não sejam encontrados.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean reload(Character character) throws RagnarokException
	{
		validate(character, true);

		String table = Tables.getInstance().getCharacters();
		String tableStats = Tables.getInstance().getCharStats();
		String tableLook = Tables.getInstance().getCharLook();
		String tableFamily = Tables.getInstance().getCharFamily();
		String tableExp = Tables.getInstance().getCharExperiences();
		String tableRank = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT id, name, sex, zeny, status_point, skill_point, jobid, hp, max_hp, sp, max_sp, "
						+	"manner, effect_state, virtue, base_level, job_level, rename_count, unban_time, delete_date, "
						+	"moves, font, unique_item_counter, "
						+	"strength, agility, vitality, intelligence, dexterity, luck, "
						+	"hair, hair_color, clothes_color, body, weapon, shield, head_top, head_mid, head_bottom, robe, "
						+	"partner, father, mother, child, "
						+	"base, job, fame, "
						+	"archer_faith, archer_calls, spear_faith, spear_calls, sword_faith, sword_calls "
						+	"FROM %s "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"WHERE id = ?", table, tableStats, tableStats, table, tableLook, tableLook, table,
						tableFamily, tableFamily, table, tableRank, tableRank, table, tableExp, tableExp, table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				setCharacterResult(character, rs);
				setCharacterStats(character, rs);
				setCharacterLook(character, rs);
				setCharacterFamily(character, rs);
				setCharacterExperience(character, rs);
				setCharacterMercenaryRank(character, rs);
				reloadLocations(character);

				logDebug("Character#%d com dados recarregados de '%s'.\n", character.getID(), table);

				return true;
			}

			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Recarrega todas as localizações em mapas do banco de dados de um personagem e suas dependências.
	 * Recarregar as informações pode ser necessário para garantir que alterações realizadas de fora do
	 * sistema possam ser aplicadas sobre o personagem sem que seja necessário efetuar um logout.
	 * @param character personagem do qual deverá ter suas informações recarregadas.
	 * @return true se conseguir atualizar ou false caso os dados do personagem não sejam encontrados.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean reloadLocations(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharLocations();
		String sql = format("SELECT num, mapid, coord_x, coord_y FROM %s WHERE charid = ? ORDER BY num", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
			{
				logDebug("Locations#%d não encontrado em '%s'.\n", character.getID(), table);
				return false;
			}

			do
			{
				int num = rs.getInt("num");

				switch (num)
				{
					case 0: setLocation(character.getLocations().getLastPoint(), rs); break;
					case 1: setLocation(character.getLocations().getSavePoint(), rs); break;
					default: setLocation(character.getLocations().getMemoPoint(num), rs); break;
				}

			} while (rs.next());

			logDebug("Locations#%d recarregado de '%s'.\n", character.getID(), table);
			return true;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Recarrega todas as informações referente a dependência de atributos básicos de um personagem.
	 * Recarregar as informações pode ser necessário para garantir que alterações realizadas de fora do
	 * sistema possam ser aplicadas sobre o personagem sem que seja necessário efetuar um logout.
	 * @param character personagem do qual deverá ter suas informações recarregadas.
	 * @return true se conseguir atualizar ou false caso essa dependência não exista.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean reloadStats(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharStats();
		String sql = format("SELECT strength, agility, vitality, intelligence, dexterity, luck FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				setStats(character);

				logDebug("Stats#%d recarregado de '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("Stats#%d não encontrado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Recarrega todas as informações referente a definições de aparência de um personagem.
	 * Recarregar as informações pode ser necessário para garantir que alterações realizadas de fora do
	 * sistema possam ser aplicadas sobre o personagem sem que seja necessário efetuar um logout.
	 * @param character personagem do qual deverá ter suas informações recarregadas.
	 * @return true se conseguir atualizar ou false caso essa dependência não exista.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean reloadLook(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharLook();
		String sql = format("SELECT hair, hair_color, clothes_color, body, weapon, shield, head_top, head_mid, head_bottom, robe "
						+	"FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				setLook(character);

				logDebug("Look#%d recarregado de '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("Look#%d não encontrado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Recarrega todas as informações referente a personagens que compõe a família de um personagem.
	 * Recarregar as informações pode ser necessário para garantir que alterações realizadas de fora do
	 * sistema possam ser aplicadas sobre o personagem sem que seja necessário efetuar um logout.
	 * @param character personagem do qual deverá ter suas informações recarregadas.
	 * @return true se conseguir atualizar ou false caso essa dependência não exista.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean reloadFamily(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharFamily();
		String sql = format("SELECT parent, father, mother, child FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				setMercenaryRank(character);

				logDebug("MercenaryRank#%d recarregado de '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("MercenaryRank#%d não encontrado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Recarrega todas as informações referente aos níveis de experiência de um personagem.
	 * Recarregar as informações pode ser necessário para garantir que alterações realizadas de fora do
	 * sistema possam ser aplicadas sobre o personagem sem que seja necessário efetuar um logout.
	 * @param character personagem do qual deverá ter suas informações recarregadas.
	 * @return true se conseguir atualizar ou false caso essa dependência não exista.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean reloadExperience(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharExperiences();
		String sql = format("SELECT base, job, fame FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				setExperience(character);

				logDebug("Experience#%d recarregado de '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("Experience#%d não encontrado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Recarrega todas as informações referente a classificação dos assistentes de um personagem.
	 * Recarregar as informações pode ser necessário para garantir que alterações realizadas de fora do
	 * sistema possam ser aplicadas sobre o personagem sem que seja necessário efetuar um logout.
	 * @param character personagem do qual deverá ter suas informações recarregadas.
	 * @return true se conseguir atualizar ou false caso essa dependência não exista.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean reloadMercenaryRank(Character character) throws RagnarokException
	{
		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT archer_faith, archer_calls, spear_faith, "
						+	"spear_calls, sword_faith, sword_calls FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				setMercenaryRank(character);

				logDebug("MercenaryRank#%d recarregado de '%s'.\n", character.getID(), table);
				return true;
			}

			logDebug("MercenaryRank#%d não encontrado em '%s'.\n", character.getID(), table);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Exclui todas as informações básicas e das dependências de um personagem existentes no banco de dados.
	 * @param charID código de identificação do personagem do qual deseja excluir do sistema.
	 * @return true se tiver sido excluído com sucesso ou false caso contrário.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean remove(int charID) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("DELETE FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);

			logDebug("Character#%d excluído.\n", charID);

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Faz uma busca que irá listar todos os códigos de identificação dos personagens de uma conta.
	 * A listagem é feita definindo o código respectivamente ao seu slot e se não encontrar é 0 (zero).
	 * @param accountID código de identificação da conta do qual deseja listar os personagens.
	 * @return aquisição de um vetor contendo o código de identificação dos personagens.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public int[] listCharID(int accountID) throws RagnarokException
	{
		String table = Tables.getInstance().getAccountsCharacters();
		String sql = format("SELECT slot, charid FROM %s WHERE accountid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);

			ResultSet rs = ps.executeQuery();
			int ids[] = new int[MAX_CHARS];

			while (rs.next())
			{
				int slot = rs.getInt("slot");
				int charid = rs.getInt("charid");

				if (interval(slot, 0, ids.length - 1))
					ids[slot] = charid;
			}

			return ids;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Cria uma indexação através de um vetor alocando todos os dados dos personagens de uma conta.
	 * Primeiramente busca o código de identificação dos personagens existentes em uma conta.
	 * Os dados dos personagens são alocados conforme o número de slot em que estão alocados.
	 * @param accountID código de identificação da conta que será buscado os personagens.
	 * @return aquisição de uma indexação por vetor dos personagens que foram encontrados.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public Index<Character> list(int accountID) throws RagnarokException
	{
		Index<Character> characters = new StaticArray<>(MAX_CHARS);
		int ids[] = listCharID(accountID);

		for (int i = 0; i < ids.length; i++)
			if (ids[i] != 0)
				characters.add(i, get(ids[i]));

		return characters;
	}

	/**
	 * Atualiza o sexo de um determinado personagem conforme as especificações abaixo:
	 * @param charID código de identificação do personagem que terá o sexo alterado.
	 * @param sex caracter que representa o sexo do personagem (M: masculino, F: feminino).
	 * @return true se conseguir alterar ou false se não encontrar ou for igual ao atual.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean setSex(int charID, Sex sex) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET sex = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, java.lang.Character.toString(sex.c));
			ps.setInt(2, charID);

			if (ps.executeUpdate() == 1)
			{
				logDebug("character#%d agora é do sexo '%s'.\n", sex);
				return true;
			}

			logDebug("character#%d não se tornou '%s'.\n", sex);
			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Solicita ao banco de dados identificações específicas para a alteração do sexo de um personagem.
	 * As identificações são referentes a conta do personagem, clã em que se encontra e sua classe.
	 * @param charID código de identificação do personagem do qual deseja obter as informações.
	 * @return objeto contendo as informações para alteração do sexo ou null se não encontrar o personagem.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public ChangeSex getChangeSex(int charID) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String tableCharList = Tables.getInstance().getAccountsCharacters();
		String sql = format("SELECT %s.account, %s.jobid, %s.guildid FROM %s "
						+	"INNER JOIN %s ON %s.charid = %s.id "
						+	"WHERE %s.id = ?",
						tableCharList, table, table, table,
						tableCharList, tableCharList, table, table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);

			ResultSet rs = ps.executeQuery();
			ChangeSex change = null;

			if (rs.next())
			{
				change = new ChangeSex();
				change.setCharID(charID);
				change.setAccountID(rs.getInt("account"));
				change.setClassID(rs.getInt("jobid"));
				change.setGuildID(rs.getInt("guilid"));
			}

			return null;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Solicita ao banco de dados identificações específicas para a alteração do sexo de vários personagens.
	 * As identificações são referentes ao código do personagem, clã em que se encontra e sua classe.
	 * @param accountID código de identificação da conta que será listado os personagens a serem alterados.
	 * @return lista contendo as informações para alteração do sexo dos personagens da conta passada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public List<ChangeSex> listChangeSex(int accountID) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String tableCharList = Tables.getInstance().getAccountsCharacters();
		String sql = format("SELECT %s.charid, %s.jobid, %s.guildid FROM %s "
						+	"INNER JOIN %s ON %s.account = %s.id ",
						tableCharList, table, table, table,
						tableCharList, tableCharList, table);

		try {

			List<ChangeSex> changes = new DynamicList<>();

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				ChangeSex change = new ChangeSex();
				change.setAccountID(accountID);
				change.setCharID(rs.getInt("charid"));
				change.setClassID(rs.getInt("jobid"));
				change.setGuildID(rs.getInt("guilid"));
				changes.add(change);
			}

			return changes;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Anula completamente o tempo de banimento de um jogador definindo-o com TimeStmap 0.
	 * Todo banimento que venha definido neste valor significa que o mesmo não está banido.
	 * @param charID código de identificação do personagem que deve ser removido o banimento.
	 * @return true se conseguir remover ou false caso não encontrado ou não esteja banido.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean unban(int charID) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET unban_time = null WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Verifica se um determinado nome de personagem já está sendo utilizado por outro personagem.
	 * @param name nome completo do personagem do qual deverá ser verificado no banco de dados.
	 * @param ignoringCase true para ignorar 'case sensitive' ou false para considerar.
	 * @return true se o nome de personagem já estiver sendo usado ou false caso contrário.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean exist(String name, boolean ignoringCase) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("SELECT 1 FROM %s WHERE " +(ignoringCase ? "BINARY " : "")+ "name = ? LIMIT 1", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, name);

			ResultSet rs = ps.executeQuery();
			return rs.next();

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Verifica se um determinado slot de personagem em uma conta especificada está disponível.
	 * @param accountID código de identificação da conta do qual deseja verificar.
	 * @param slot número do slot que deseja verificar a disponibilidade.
	 * @return true se estiver disponível ou false caso já esteja sendo utilizado.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean avaiableSlot(int accountID, byte slot) throws RagnarokException
	{
		String table = Tables.getInstance().getAccountsCharacters();
		String sql = format("SELECT 1 FROM %s WHERE accountid = ? AND slot = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);
			ps.setInt(2, slot);

			ResultSet rs = ps.executeQuery();
			return !rs.next();

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Define a ocupação de um slot de personagem em uma conta especificadamente conforme abaixo:
	 * @param accountID código de identificação da conta no qual o personagem será vinculado.
	 * @param charID código de identificação do personagem do qual será vinculado ao slot.
	 * @param slot número do slot em que o personagem será alocado na conta.
	 * @return true se conseguir definir ou false caso não seja possível ou esteja ocupado.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean setSlot(int accountID, int charID, byte slot) throws RagnarokException
	{
		String table = Tables.getInstance().getAccountsCharacters();
		String sql = format("INSERT INTO %s (accountid, charid, slot) VALUES (?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);
			ps.setInt(2, charID);
			ps.setByte(3, slot);

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {

			if (e.getErrorCode() == DUPLICATED_KEY)
				return false;

			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Permite obter o horário de agendamento para exclusão de um personagem especificado abaixo:
	 * @param charID código de identificação do personagem do qual deseja o horário agendado.
	 * @return aquisição do horário (timestamp) para que o personagem possa ser excluído.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public long getDeleteDate(int charID) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("SELECT delete_date FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return timestamp(rs.getTimestamp("delete_date"));

			return 0;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Permite obter o nível de base de um personagem especificado conforme identificação abaixo:
	 * @param charID código de identificação do personagem do qual deseja o nível de base.
	 * @return aquisição do nível de base do personagem especificado por parâmetro.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public int getBaseLevel(int charID) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("SELECT base_level FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return rs.getInt("base_level");

			return 0;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza um personagem definindo um horário de agendamento para que este seja excluído.
	 * @param charID código de identificação do personagem do qual deseja fazer o agendamento.
	 * @param deleteDate horário agendado para ele possa ser excluído em segurança.
	 * @return true se conseguir realizar o agendamento ou false caso não encontre.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean setDeleteDate(int charID, long deleteDate) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET delete_date = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setTimestamp(1, timestamp(deleteDate));
			ps.setInt(2, charID);

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza um personagem removendo o seu horário para agendamento de sua própria exclusão.
	 * @param charID código de identificação do personagem do qual deseja cancelar o agendamento.
	 * @return true se conseguir cancelar o agendamento ou false caso contrário.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean cancelDelete(int charID) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET delete_date = NULL WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Busca no banco de dados o código de identificação de um personagem de acordo com a conta e slot.
	 * @param accountID código de identificação da conta do qual o personagem será buscado.
	 * @param slot número de slot na conta do personagem que deseja o código de identificação.
	 * @return aquisição do código de identificação do personagem ou zero se não encontrar.
	 * @throws RagnarokException
	 */

	public int getCharID(int accountID, byte slot) throws RagnarokException
	{
		String table = Tables.getInstance().getAccountsCharacters();
		String sql = format("SELECT charid FROM %s WHERE accountid = ? AND slot = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);
			ps.setByte(2, slot);

			ResultSet rs = ps.executeQuery();

			return rs.next() ? rs.getInt("charid") : 0;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Procedimento utilizado para atualizar as informações mínimas do personagem conforme abaixo:
	 * @param sd sessão do qual terá as informações atualizadas de todos os CharData.
	 * @param characters indexação dos personagens através do seu número de slot.
	 */

	public void setCharData(CharSessionData sd, Index<Character> characters)
	{
		for (int i = 0; i < MAX_CHARS; i++)
		{
			sd.setCharData(null, i);

			if (characters.get(i) != null)
			{
				Character character = characters.get(i);

				CharData data = new CharData();
				data.setID(character.getID());
				data.setCharMove(character.getMoves());
				data.getUnban().set(character.getUnbanTime().get());
				sd.setCharData(data, i);
			}
		}
	}
}

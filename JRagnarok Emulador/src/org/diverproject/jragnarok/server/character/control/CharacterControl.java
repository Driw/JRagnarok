package org.diverproject.jragnarok.server.character.control;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.util.lang.IntUtil.interval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.character.structures.CharData;
import org.diverproject.jragnarok.server.character.structures.Character;
import org.diverproject.jragnarok.server.character.structures.Experience;
import org.diverproject.jragnarok.server.character.structures.MercenaryRank;
import org.diverproject.util.collection.Index;
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
	 * @throws RagnarokException informações inválidas ou nulas.
	 */

	public void validate(Character character) throws RagnarokException
	{
		if (character == null)
			throw new RagnarokException("personagem nulo");

		if (character.getID() == 0 || character.getName().equals(Character.UNKNOWN))
			throw new RagnarokException("personagem inválido");
	}

	/**
	 * Cria uma indexação através do número de slot do personagem de todos os personagens de uma conta.
	 * @param accountID código de identificação da conta do qual deseja listar os personagens.
	 * @return aquisição de uma indexação dos personagens referentes a conta especificada.
	 * @throws RagnarokException apenas se houver falha na conexão com o banco de dados.
	 */

	public Index<CharData> getCharData(int accountID) throws RagnarokException
	{
		Index<CharData> characters = new StaticArray<>(MAX_CHARS);

		String table = Tables.getInstance().getAccountsCharacters();
		String tableChars = Tables.getInstance().getAccountsCharacters();
		String sql = format("SELECT slot, charid, moves, uban FROM %s "
						+	"INNER JOIN %s ON %s.id = charid "
						+	"WHERE account = ?", table, tableChars, tableChars);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);

			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				CharData data = new CharData();
				data.setID(rs.getInt("charid"));
				data.setCharMove(rs.getInt("moves"));
				data.getUnban().set(rs.getTimestamp("unban").getTime());
				characters.add(rs.getInt("slot"), data);
			}

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		return characters;
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
		String tableExp = Tables.getInstance().getCharExperience();
		String tableRank = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT id, name, sex, zeny, status_point, skill_point, jobid, hp, max_hp, sp, max_sp, "
						+	"manner, effect_state, karma, base_level, job_level, rename, delete_date, moves, font, "
						+	"unique_item_counter, "
						+	"strength, agility, vitality, intelligence, dexterity, luck, "
						+	"hair, hair_color, clothes_color, body, weapon, shield, head_top, head_mid, head_bottom, robe, "
						+	"partner, father, mother, child, "
						+	"base, job, fame, "
						+	"archer_faith, archer_faith, spear_faith, spear_calls, sword_faith, sword_faith "
						+	"FROM %s "
						+	"INNER JOIN %s ON %s.charid = id"
						+	"INNER JOIN %s ON %s.charid = id"
						+	"INNER JOIN %s ON %s.charid = id"
						+	"INNER JOIN %s ON %s.charid = id"
						+	"INNER JOIN %s ON %s.charid = id"
						+	"WHERE id = ?", table, tableStats, tableStats, tableLook, tableLook,
						tableFamily, tableFamily, tableRank, tableRank, tableExp, tableExp);

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
		character.setSex(rs.getString("sex").charAt(0));
		character.setZeny(rs.getInt("zeny"));
		character.setStatusPoint(rs.getShort("status_point"));
		character.setSkillPoint(rs.getShort("skill_point"));
		character.setJobID(rs.getShort("jobid"));
		character.setManner(rs.getShort("manner"));
		character.setBaseLevel(rs.getInt("base_level"));
		character.setJobLevel(rs.getInt("job_level"));
		character.setHP(rs.getInt("hp"));
		character.setMaxHP(rs.getInt("max_hp"));
		character.setSP(rs.getShort("sp"));
		character.setMaxSP(rs.getShort("max_sp"));
		character.setManner(rs.getShort("manner"));
		character.getEffectState().setValue(rs.getInt("effect_state"));
		character.setKarma(rs.getShort("karma"));
		character.setBaseLevel(rs.getInt("base_level"));
		character.setJobLevel(rs.getShort("job_id"));
		character.setRename(rs.getShort("rename"));
		character.getDeleteDate().set(rs.getInt("delete_date"));
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
	 * Adiciona um novo personagem ao banco de dados inserindo novas informações relacionadas a ele.
	 * Caso alguma das dependências já existam por algum motivo, elas serão atualizadas com base neste.
	 * @param character personagem contendo todas as informações básicas e suas dependências.
	 * @return true se conseguir adicionar ou false caso contrário (não esperado).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean add(Character character) throws RagnarokException
	{
		validate(character);

		String table = Tables.getInstance().getCharacters();
		String sql = format("INSERT INTO %s (name, sex, zeny, status_point, skill_point, jobid, hp, max_hp, sp, max_sp, "
						+	"manner, effect_state, karma, base_level, job_level, rename, delete_date, moves, font"
						+	"unique_item_counter)"
						+	"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, character.getName());
			ps.setString(2, java.lang.Character.toString(character.getSex()));
			ps.setInt(3, character.getZeny());
			ps.setInt(4, character.getStatusPoint());
			ps.setInt(5, character.getSkillPoint());
			ps.setShort(6, character.getJobID());
			ps.setInt(7, character.getHP());
			ps.setInt(8, character.getMaxHP());
			ps.setShort(9, character.getSP());
			ps.setShort(10, character.getMaxSP());
			ps.setShort(11, character.getManner());
			ps.setInt(12, character.getEffectState().getValue());
			ps.setShort(13, character.getKarma());
			ps.setInt(14, character.getBaseLevel());
			ps.setInt(15, character.getJobLevel());
			ps.setShort(16, character.getRename());
			ps.setTimestamp(17, character.getDeleteDate().toTimestamp());
			ps.setShort(18, character.getMoves());
			ps.setByte(19, character.getFont());
			ps.setInt(20, character.getUniqueItemCounter());

			if (ps.execute())
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
		String sql = format("INSERT INTO %s (charid, strength, agility, vitality, intelligence, dexterity, luck) "
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
		String sql = format("INSERT INTO %s (charid, hair, hair_color, clothes_color, body, weapon, shield, "
						+	"head_top, head_mid, head_bottom, robe) "
						+	"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", table);

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
		String sql = format("INSERT INTO %s (charid, partner, father, mother, child) VALUES (?, ?, ?, ?, ?)", table);

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
		String table = Tables.getInstance().getCharFamily();
		String sql = format("INSERT INTO %s (charid, base, job, fame) VALUES (?, ?, ?, ?)", table);

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
		String sql = format("INSERT INTO %s (charid, archer_faith, archer_calls, spear_faith, spear_calls, sword_faith, sword_calls) "
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
	 * Atualiza todas as informações básicas e de dependências de um determinado personagem.
	 * @param character personagem contendo todas as informações necessárias dessa dependência.
	 * @return true se conseguir atualizar ou false caso a dependência não seja encontrada.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean set(Character character) throws RagnarokException
	{
		validate(character);

		return	setBase(character) ||
				setStats(character) ||
				setLook(character) ||
				setFamily(character) ||
				setExperience(character) ||
				setMercenaryRank(character);
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
						+	"hp = ?, max_hp = ?, sp = ?, max_sp = ?, manner = ?, effect_state = ?, karma = ?, base_level = ?, "
						+	"job_level = ?, rename = ?, delete_date = ?, moves = ?, font = ?, unique_item_counter = ? "
						+	"WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, character.getName());
			ps.setString(2, java.lang.Character.toString(character.getSex()));
			ps.setInt(3, character.getZeny());
			ps.setInt(4, character.getStatusPoint());
			ps.setInt(5, character.getSkillPoint());
			ps.setShort(6, character.getJobID());
			ps.setInt(7, character.getHP());
			ps.setInt(8, character.getMaxHP());
			ps.setShort(9, character.getSP());
			ps.setShort(10, character.getMaxSP());
			ps.setShort(11, character.getManner());
			ps.setInt(12, character.getEffectState().getValue());
			ps.setShort(13, character.getKarma());
			ps.setInt(14, character.getBaseLevel());
			ps.setInt(15, character.getJobLevel());
			ps.setShort(16, character.getRename());
			ps.setTimestamp(17, character.getDeleteDate().toTimestamp());
			ps.setShort(18, character.getMoves());
			ps.setByte(19, character.getFont());
			ps.setInt(20, character.getUniqueItemCounter());
			ps.setInt(21, character.getID());

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
	 * Recarrega todas as informações básicas do banco de dados de um personagem e suas dependências.
	 * Recarregar as informações pode ser necessário para garantir que alterações realizadas de fora do
	 * sistema possam ser aplicadas sobre o personagem sem que seja necessário efetuar um logout.
	 * @param character personagem do qual deverá ter suas informações recarregadas.
	 * @return true se conseguir atualizar ou false caso os dados do personagem não sejam encontrados.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public boolean reload(Character character) throws RagnarokException
	{
		validate(character);

		String table = Tables.getInstance().getCharacters();
		String tableStats = Tables.getInstance().getCharStats();
		String tableLook = Tables.getInstance().getCharLook();
		String tableFamily = Tables.getInstance().getCharFamily();
		String tableExp = Tables.getInstance().getCharExperience();
		String tableRank = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT id, name, sex, zeny, status_point, skill_point, jobid, hp, max_hp, sp, max_sp, "
						+	"manner, effect_state, karma, base_level, job_level, rename, delete_date, moves, font, "
						+	"unique_item_counter, "
						+	"strength, agility, vitality, intelligence, dexterity, luck, "
						+	"hair, hair_color, clothes_color, body, weapon, shield, head_top, head_mid, head_bottom, robe, "
						+	"partner, father, mother, child, "
						+	"base, job, fame, "
						+	"archer_faith, archer_faith, spear_faith, spear_calls, sword_faith, sword_faith "
						+	"FROM %s "
						+	"INNER JOIN %s ON %s.charid = id"
						+	"INNER JOIN %s ON %s.charid = id"
						+	"INNER JOIN %s ON %s.charid = id"
						+	"INNER JOIN %s ON %s.charid = id"
						+	"INNER JOIN %s ON %s.charid = id"
						+	"WHERE id = ?", table, tableStats, tableStats, tableLook, tableLook,
						tableFamily, tableFamily, tableRank, tableRank, tableExp, tableExp);

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

				logDebug("Character#%d com dados recarregados de '%s'.\n", character.getID(), table);

				return true;
			}

			return false;

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
		String table = Tables.getInstance().getCharExperience();
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
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
	 */

	public void remove(int charID) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String tableLook = Tables.getInstance().getCharLook();
		String tableStats = Tables.getInstance().getCharStats();
		String tableExperiences = Tables.getInstance().getCharExperience();
		String tableFamily = Tables.getInstance().getCharFamily();
		String tableMercenaryRank = Tables.getInstance().getCharMercenaryRank();
		String sql = format("DELETE FROM %s WHERE id = ?; "
						+	"DELETE FROM %s WHERE id = ?; "
						+	"DELETE FROM %s WHERE id = ?; "
						+	"DELETE FROM %s WHERE id = ?; "
						+	"DELETE FROM %s WHERE id = ?; "
						+	"DELETE FROM %s WHERE id = ?",
						table, tableLook, tableStats, tableExperiences, tableFamily, tableMercenaryRank);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);
			ps.setInt(2, charID);
			ps.setInt(3, charID);
			ps.setInt(4, charID);
			ps.setInt(5, charID);
			ps.setInt(6, charID);

			logDebug("Character#%d excluído.\n", charID);

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
		String sql = format("slot, charid WHERE account = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);

			ResultSet rs = ps.executeQuery();
			int ids[] = new int[MAX_CHARS];

			while (rs.next())
			{
				int slot = rs.getInt("slot");

				if (interval(slot, 0, ids.length - 1))
					ids[slot] = rs.getInt(rs.getInt("charid"));
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

		for (int i = 0; i < characters.size(); i++)
			if (ids[i] != 0)
				characters.add(i, get(ids[i]));

		return characters;
	}
}

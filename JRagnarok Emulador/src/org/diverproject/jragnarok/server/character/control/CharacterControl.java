package org.diverproject.jragnarok.server.character.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.log.LogSystem.logDebug;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.character.structures.Character;

/**
 * <h1>Controle para Personagens</h1>
 *
 * <p>Para a tabela de personagens é armazenado os principais valores de um personagem como o nome.
 * Além do nome, estará especificado o código de identificação único, sexo, dinheiro (zeny),
 * nível de base, nível de classe, pontos de atributos/habilidades disponíveis e outros.</p>
 *
 * <p>Através deste controle as dependências como experiência, família, localizações, visual,
 * classificação no sistema de assistentes poderão ser gerenciadas conforme necessário.
 * Por exemplo: se adicionar um novo personagem as dependências são adicionadas.</p>
 *
 * @see AbstractControl
 * @see Connection
 * @see Character
 *
 * @author Andrew
 */

public class CharacterControl extends AbstractControl
{
	/**
	 * Controle para gerenciar os níveis de experiência dos personagens.
	 */
	private ExperienceControl experiences;

	/**
	 * Controle para gerenciar a relação familiar dos personagens.
	 */
	private FamilyControl families;

	/**
	 * Controle para gerenciar as localizações como pontos de retorno dos personagens.
	 */
	private LocationControl locations;

	/**
	 * Controle para gerenciar a aparência de estilos e cores dos personagens.
	 */
	private LookControl looks;

	/**
	 * Controle para gerenciar a classificação no sistema de assistentes dos personagens.
	 */
	private MercenaryRankControl ranks;

	/**
	 * 
	 * @param connection
	 */

	public CharacterControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * @param experiences the experiences to set
	 */
	public void setExperiences(ExperienceControl experiences)
	{
		this.experiences = experiences;
	}

	/**
	 * @param families the families to set
	 */
	public void setFamilies(FamilyControl families)
	{
		this.families = families;
	}

	/**
	 * @param locations the locations to set
	 */
	public void setLocations(LocationControl locations)
	{
		this.locations = locations;
	}

	/**
	 * @param looks the looks to set
	 */
	public void setLooks(LookControl looks)
	{
		this.looks = looks;
	}

	/**
	 * @param ranks the ranks to set
	 */
	public void setRanks(MercenaryRankControl ranks)
	{
		this.ranks = ranks;
	}

	/**
	 * 
	 * @param character
	 * @throws RagnarokException
	 */

	private void validate(Character character) throws RagnarokException
	{
		if (character == null)
			throw new RagnarokException("personagem nulo");

		if (character.getExperience() == null)
			throw new RagnarokException("experiências nula");

		if (character.getFamily() == null)
			throw new RagnarokException("família nula");

		if (character.getLocations() == null)
			throw new RagnarokException("localizações nula");

		if (character.getLook() == null)
			throw new RagnarokException("visuais nulo");

		if (character.getMercenaryRank() == null)
			throw new RagnarokException("classificações nula");

		if (character.getID() == 0 || character.getName().equals(Character.UNKNOWN))
			throw new RagnarokException("personagem inválido");
	}

	/**
	 * 
	 * @param charID
	 * @return
	 * @throws RagnarokException
	 */

	public Character get(int charID) throws RagnarokException
	{
		Character character = null;

		String table = Tables.getInstance().getCharacters();
		String sql = format("SELECT id, name, sex, zeny, status_point, skill_point, jobid, manner, "
						+	"base_level, job_level, str, agi, vit, int, dex, luk FROM %s "
						+	"WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, charID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				character = new Character();
				character.setID(charID);
				character.setName(rs.getString("name"));
				character.setSex(rs.getString("sex").charAt(0));
				character.setZeny(rs.getInt("zeny"));
				character.setStatusPoint(rs.getInt("status_point"));
				character.setSkillPoint(rs.getInt("skill_point"));
				character.setJobID(rs.getShort("jobid"));
				character.setManner(rs.getShort("manner"));
				character.setBaseLevel(rs.getInt("base_level"));
				character.setJobLevel(rs.getInt("job_level"));
				character.setStrength(rs.getInt("str"));
				character.setAgility(rs.getInt("agi"));
				character.setVitality(rs.getInt("vit"));
				character.setIntelligence(rs.getInt("int"));
				character.setDexterity(rs.getInt("dex"));
				character.setLuck(rs.getInt("luk"));

				character.setLook(looks.get(charID));
				character.setFamily(families.get(charID));
				character.setExperience(experiences.get(charID));
				character.setMercenaryRank(ranks.get(charID));
				character.setLocations(locations.get(charID));
			}

			if (character != null)
				logDebug("Character#%d obtido.\n", character.getID());

			return character;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * 
	 * @param character
	 * @return
	 * @throws RagnarokException
	 */

	public boolean add(Character character) throws RagnarokException
	{
		validate(character);

		String table = Tables.getInstance().getCharacters();
		String sql = format("INSERT INTO %s (name, sex, zeny, status_point, skill_point, jobid, manner, "
				+	"base_level, job_level, str, agi, vit, int, dex, luk) "
				+	"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, character.getName());
			ps.setString(2, java.lang.Character.toString(character.getSex()));
			ps.setInt(3, character.getZeny());
			ps.setInt(4, character.getStatusPoint());
			ps.setInt(5, character.getSkillPoint());
			ps.setShort(6, character.getJobID());
			ps.setShort(7, character.getManner());
			ps.setInt(8, character.getBaseLevel());
			ps.setInt(9, character.getJobLevel());
			ps.setInt(10, character.getStrength());
			ps.setInt(11, character.getAgility());
			ps.setInt(12, character.getVitality());
			ps.setInt(13, character.getIntelligence());
			ps.setInt(14, character.getDexterity());
			ps.setInt(15, character.getLuck());

			if (ps.executeUpdate() == 1)
			{
				logDebug("Character#%d adicionado.\n", character.getID());

				sql = format("SELECT id FROM %s WHERE name = ?", table);

				ps = prepare(sql);
				ps.setString(1, character.getName());

				ResultSet rs = ps.executeQuery();

				if (rs.next())
					character.setID(rs.getInt("id"));
			}

			if (looks.add(character.getLook()) &&
				families.add(character.getFamily()) &&
				experiences.add(character.getExperience()) &&
				ranks.add(character.getMercenaryRank()) &&
				locations.add(character.getLocations()))
				return true;

			looks.remove(character.getLook());
			families.add(character.getFamily());
			experiences.add(character.getExperience());
			ranks.add(character.getMercenaryRank());
			locations.add(character.getLocations());

			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * 
	 * @param character
	 * @return
	 * @throws RagnarokException
	 */

	public boolean set(Character character) throws RagnarokException
	{
		validate(character);

		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET name = ?, sex = ?, zeny = ?, status_point = ?, skill_point = ?, "
				+	"jobid = ?, manner = ?, base_level = ?, job_level = ?, "
				+	"str = ?, agi = ?, vit = ?, int = ?, dex = ?, luk = ? "
				+	"WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, character.getName());
			ps.setString(2, java.lang.Character.toString(character.getSex()));
			ps.setInt(3, character.getZeny());
			ps.setInt(4, character.getStatusPoint());
			ps.setInt(5, character.getSkillPoint());
			ps.setShort(6, character.getJobID());
			ps.setShort(7, character.getManner());
			ps.setInt(8, character.getBaseLevel());
			ps.setInt(9, character.getJobLevel());
			ps.setInt(10, character.getStrength());
			ps.setInt(11, character.getAgility());
			ps.setInt(12, character.getVitality());
			ps.setInt(13, character.getIntelligence());
			ps.setInt(14, character.getDexterity());
			ps.setInt(15, character.getLuck());
			ps.setInt(16, character.getID());

			if (!looks.set(character.getLook()))
				if (!looks.add(character.getLook()))
					throw new RagnarokException("falha ao atualizar visual [1/5]");

			if (!families.set(character.getFamily()))
				if (!families.add(character.getFamily()))
					throw new RagnarokException("falha ao atualizar família [2/5]");

			if (!experiences.set(character.getExperience()))
				if (!experiences.add(character.getExperience()))
					throw new RagnarokException("falha ao atualizar experiências [3/5]");

			if (!ranks.set(character.getMercenaryRank()))
				if (!ranks.add(character.getMercenaryRank()))
					throw new RagnarokException("falha ao atualizar classificação de assitentes [4/5]");

			if (!locations.set(character.getLocations()))
				if (!locations.add(character.getLocations()))
					throw new RagnarokException("falha ao atualizar localizações [5/5]");

			boolean result = ps.executeUpdate() != 1;

			if (result)
				logDebug("Character#%d atualizado.\n", character.getID());

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * 
	 * @param character
	 * @return
	 * @throws RagnarokException
	 */

	public boolean reload(Character character) throws RagnarokException
	{
		validate(character);

		String table = Tables.getInstance().getCharacters();
		String sql = format("SELECT name, sex, zeny, status_point, skill_point, jobid, manner, "
				+	"base_level, job_level, str, agi, vit, int, dex, luk "
				+	"FROM %s WHERE id + ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				character.setLook(looks.get(character.getID()));
				character.setFamily(families.get(character.getID()));
				character.setExperience(experiences.get(character.getID()));
				character.setMercenaryRank(ranks.get(character.getID()));
				character.setLocations(locations.get(character.getID()));

				character.setName(rs.getString("name"));
				character.setSex(rs.getString("sex").charAt(0));
				character.setZeny(rs.getInt("zeny"));
				character.setStatusPoint(rs.getInt("status_point"));
				character.setSkillPoint(rs.getInt("skill_point"));
				character.setJobID(rs.getShort("jobid"));
				character.setManner(rs.getShort("manner"));
				character.setBaseLevel(rs.getInt("base_level"));
				character.setJobLevel(rs.getInt("job_level"));
				character.setStrength(rs.getInt("str"));
				character.setAgility(rs.getInt("agi"));
				character.setVitality(rs.getInt("vit"));
				character.setIntelligence(rs.getInt("int"));
				character.setDexterity(rs.getInt("dex"));
				character.setLuck(rs.getInt("luk"));

				logDebug("Character#%d recarregado.\n", character.getID());

				return true;
			}

			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * 
	 * @param character
	 * @return
	 * @throws RagnarokException
	 */

	public boolean delete(Character character) throws RagnarokException
	{
		validate(character);

		String table = Tables.getInstance().getCharacters();
		String sql = format("DELETE FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, character.getID());

			boolean result =
					looks.remove(character.getLook()) &&
					families.remove(character.getFamily()) &&
					experiences.remove(character.getExperience()) &&
					ranks.remove(character.getMercenaryRank()) &&
					locations.remove(character.getLocations()) &&
					ps.executeUpdate() != 1;

			if (result)
				logDebug("Character#%d excluído.\n", character.getID());

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

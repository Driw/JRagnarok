package org.diverproject.jragnarok.server.character.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.character.structures.Family;

/**
 * <h1>Controle para Família dos Personagens</h1>
 *
 * <p>Para a tabela de famílias é armazenado valores referentes ao parentesco, conjugue e filho.
 * Uma família pode ser registrada apenas se o personagem for filho de alguém ou possuir conjugue.
 * Para quem for filho deverá ser definido o código do personagem dos pais caso contrário do conjugue.</p>
 *
 * @see AbstractControl
 * @see Connection
 * @see Family
 *
 * @author Andrew
 */

public class FamilyControl extends AbstractControl
{
	/**
	 * Cria uma nova instância de um controle para gerenciamento das famílias.
	 * @param connection referência do objeto contendo a conexão com o banco de dados.
	 */

	public FamilyControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Procedimento interno que garante a validade do objeto controlado por este controle.
	 * @param family referência do objeto que será validado pelo controle.
	 * @throws RagnarokException pode ocorrer apenas se o objeto passado for nulo
	 * ou então se o código de identificação for inválido (menor que um | <1).
	 */

	private void validate(Family family) throws RagnarokException
	{
		if (family == null)
			throw new RagnarokException("família nula");

		if (family.getID() < 1)
			throw new RagnarokException("família inválida");
	}

	/**
	 * Permite obter as informações de famílias obtidas de um determinado personagem.
	 * @param id código de identificação do personagem que deseja a família.
	 * @return aquisição do objeto contendo as informações de família do personagem
	 * ou no caso do código de identificação do personagem ser inválido: null.
	 * @throws RagnarokException falha de conexão com o banco de dados.
	 */

	public Family get(int id) throws RagnarokException
	{
		String table = Tables.getInstance().getCharFamily();
		String sql = format("SELECT partner, father, mother, child FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				return null;

			Family family = new Family();
			family.setID(id);
			family.setPartner(rs.getInt("partner"));
			family.setFather(rs.getInt("father"));
			family.setMother(rs.getInt("mother"));
			family.setChild(rs.getInt("child"));

			return family;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona informações referentes aos parentescos familiares de um personagem.
	 * @param family objeto contendo informações do personagem à adicionar.
	 * @return true se conseguir adicionar ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou já existe.
	 */

	public boolean add(Family family) throws RagnarokException
	{
		validate(family);

		String table = Tables.getInstance().getCharFamily();
		String sql = format("INSERT INTO %s (charid, partner, father, mother, child) VALUES (?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, family.getID());
			set(ps, 2, family.getPartner(), 0);
			set(ps, 3, family.getFather(), 0);
			set(ps, 4, family.getMother(), 0);
			set(ps, 5, family.getChild(), 0);

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as informações referentes aos parentescos familiares de um personagem.
	 * @param family objeto contendo informações do personagem à atualizar.
	 * @return true se conseguir atualizar ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou não existe.
	 */

	public boolean set(Family family) throws RagnarokException
	{
		validate(family);

		String table = Tables.getInstance().getCharFamily();
		String sql = format("UPDATE %s SET partner = ?, father = ?, mother = ?, child = ? WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			set(ps, 1, family.getPartner(), 0);
			set(ps, 2, family.getFather(), 0);
			set(ps, 3, family.getMother(), 0);
			set(ps, 4, family.getChild(), 0);
			ps.setInt(5, family.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Remove as informações referentes aos parentescos familiares de um personagem.
	 * @param family objeto contendo informações do personagem à remover.
	 * @return true se conseguir remover ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou não existe.
	 */

	public boolean remove(Family family) throws RagnarokException
	{
		validate(family);

		String table = Tables.getInstance().getCharFamily();
		String sql = format("DELETE FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, family.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

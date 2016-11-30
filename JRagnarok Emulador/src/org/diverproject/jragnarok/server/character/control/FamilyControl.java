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
 * <h1>Controle para Fam�lia dos Personagens</h1>
 *
 * <p>Para a tabela de fam�lias � armazenado valores referentes ao parentesco, conjugue e filho.
 * Uma fam�lia pode ser registrada apenas se o personagem for filho de algu�m ou possuir conjugue.
 * Para quem for filho dever� ser definido o c�digo do personagem dos pais caso contr�rio do conjugue.</p>
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
	 * Cria uma nova inst�ncia de um controle para gerenciamento das fam�lias.
	 * @param connection refer�ncia do objeto contendo a conex�o com o banco de dados.
	 */

	public FamilyControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Procedimento interno que garante a validade do objeto controlado por este controle.
	 * @param family refer�ncia do objeto que ser� validado pelo controle.
	 * @throws RagnarokException pode ocorrer apenas se o objeto passado for nulo
	 * ou ent�o se o c�digo de identifica��o for inv�lido (menor que um | <1).
	 */

	private void validate(Family family) throws RagnarokException
	{
		if (family == null)
			throw new RagnarokException("fam�lia nula");

		if (family.getID() < 1)
			throw new RagnarokException("fam�lia inv�lida");
	}

	/**
	 * Permite obter as informa��es de fam�lias obtidas de um determinado personagem.
	 * @param id c�digo de identifica��o do personagem que deseja a fam�lia.
	 * @return aquisi��o do objeto contendo as informa��es de fam�lia do personagem
	 * ou no caso do c�digo de identifica��o do personagem ser inv�lido: null.
	 * @throws RagnarokException falha de conex�o com o banco de dados.
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
	 * Adiciona informa��es referentes aos parentescos familiares de um personagem.
	 * @param family objeto contendo informa��es do personagem � adicionar.
	 * @return true se conseguir adicionar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou j� existe.
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
	 * Atualiza as informa��es referentes aos parentescos familiares de um personagem.
	 * @param family objeto contendo informa��es do personagem � atualizar.
	 * @return true se conseguir atualizar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
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
	 * Remove as informa��es referentes aos parentescos familiares de um personagem.
	 * @param family objeto contendo informa��es do personagem � remover.
	 * @return true se conseguir remover ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
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

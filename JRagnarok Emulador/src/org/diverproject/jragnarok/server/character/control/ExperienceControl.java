package org.diverproject.jragnarok.server.character.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.character.structures.Experience;

/**
 * <h1>Controle para Experi�ncia dos Personagens</h1>
 *
 * <p>Para a tabela de experi�ncias � armazenado valores quantitativos de n�veis da jogabilidade do jogador.
 * Os dois principais valores � o de experi�ncia do n�vel de base (base) e n�vel de classe (job).
 * Desta forma � poss�vel adicionar novas informa��es referentes ao n�vel do jogador ao sistema.</p>
 *
 * @see AbstractControl
 * @see Connection
 * @see Experience
 *
 * @author Andrew
 */

public class ExperienceControl extends AbstractControl
{
	/**
	 * Cria uma nova inst�ncia de um controle para gerenciamento das experi�ncias.
	 * @param connection refer�ncia do objeto contendo a conex�o com o banco de dados.
	 */

	public ExperienceControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Permite obter as informa��es de experi�ncias obtidas de um determinado personagem.
	 * @param id c�digo de identifica��o do personagem que deseja as experi�ncias.
	 * @return aquisi��o do objeto contendo as informa��es de experi�ncia do personagem
	 * ou no caso do c�digo de identifica��o do personagem ser inv�lido: null.
	 * @throws RagnarokException falha de conex�o com o banco de dados.
	 */

	public Experience get(int id) throws RagnarokException
	{
		String table = Tables.getInstance().getCharExperiences();
		String sql = format("SELECT base, job, fame FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				return null;

			Experience experience = new Experience();
			experience.setID(id);
			experience.setFame(rs.getInt("fame"));
			experience.setBase(rs.getInt("base"));
			experience.setJob(rs.getInt("job"));

			return experience;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona informa��es referentes ao n�vel de experi�ncia de um personagem.
	 * @param experience objeto contendo informa��es do personagem � adicionar.
	 * @return true se conseguir adicionar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou j� existe.
	 */

	public boolean add(Experience experience) throws RagnarokException
	{
		validate(experience);

		String table = Tables.getInstance().getCharExperiences();
		String sql = format("INSERT INTO %s (charid, base, job, fame) VALUES (?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, experience.getID());
			ps.setInt(2, experience.getBase());
			ps.setInt(3, experience.getJob());
			ps.setInt(4, experience.getFame());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as informa��es referentes ao n�vel de experi�ncia de um personagem.
	 * @param experience objeto contendo informa��es do personagem � atualizar.
	 * @return true se conseguir atualizar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
	 */

	public boolean set(Experience experience) throws RagnarokException
	{
		validate(experience);

		String table = Tables.getInstance().getCharExperiences();
		String sql = format("UPDATE %s SET base = ?, job = ?, fame = ? WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, experience.getBase());
			ps.setInt(2, experience.getJob());
			ps.setInt(3, experience.getFame());
			ps.setInt(4, experience.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Remove as informa��es referentes ao n�vel de experi�ncia de um personagem.
	 * @param experience objeto contendo informa��es do personagem � remover.
	 * @return true se conseguir remover ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
	 */

	public boolean remove(Experience experience) throws RagnarokException
	{
		validate(experience);

		String table = Tables.getInstance().getCharExperiences();
		String sql = format("DELETE FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, experience.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Procedimento interno que garante a validade do objeto controlado por este controle.
	 * @param experience refer�ncia do objeto que ser� validado pelo controle.
	 * @throws RagnarokException pode ocorrer apenas se o objeto passado for nulo
	 * ou ent�o se o c�digo de identifica��o for inv�lido (menor que um | <1).
	 */

	private void validate(Experience experience) throws RagnarokException
	{
		if (experience == null)
			throw new RagnarokException("experi�ncia nula");

		if (experience.getID() < 1)
			throw new RagnarokException("experi�ncia inv�lida");
	}
}

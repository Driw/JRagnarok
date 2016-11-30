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
 * <h1>Controle para Experiência dos Personagens</h1>
 *
 * <p>Para a tabela de experiências é armazenado valores quantitativos de níveis da jogabilidade do jogador.
 * Os dois principais valores é o de experiência do nível de base (base) e nível de classe (job).
 * Desta forma é possível adicionar novas informações referentes ao nível do jogador ao sistema.</p>
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
	 * Cria uma nova instância de um controle para gerenciamento das experiências.
	 * @param connection referência do objeto contendo a conexão com o banco de dados.
	 */

	public ExperienceControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Permite obter as informações de experiências obtidas de um determinado personagem.
	 * @param id código de identificação do personagem que deseja as experiências.
	 * @return aquisição do objeto contendo as informações de experiência do personagem
	 * ou no caso do código de identificação do personagem ser inválido: null.
	 * @throws RagnarokException falha de conexão com o banco de dados.
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
	 * Adiciona informações referentes ao nível de experiência de um personagem.
	 * @param experience objeto contendo informações do personagem à adicionar.
	 * @return true se conseguir adicionar ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou já existe.
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
	 * Atualiza as informações referentes ao nível de experiência de um personagem.
	 * @param experience objeto contendo informações do personagem à atualizar.
	 * @return true se conseguir atualizar ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou não existe.
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
	 * Remove as informações referentes ao nível de experiência de um personagem.
	 * @param experience objeto contendo informações do personagem à remover.
	 * @return true se conseguir remover ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou não existe.
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
	 * @param experience referência do objeto que será validado pelo controle.
	 * @throws RagnarokException pode ocorrer apenas se o objeto passado for nulo
	 * ou então se o código de identificação for inválido (menor que um | <1).
	 */

	private void validate(Experience experience) throws RagnarokException
	{
		if (experience == null)
			throw new RagnarokException("experiência nula");

		if (experience.getID() < 1)
			throw new RagnarokException("experiência inválida");
	}
}

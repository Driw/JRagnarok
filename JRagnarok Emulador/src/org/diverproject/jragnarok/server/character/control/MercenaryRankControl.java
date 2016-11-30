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
import org.diverproject.jragnarok.server.character.structures.MercenaryRank;

/**
 * <h1>Controle para Classificação de Assistentes</h1>
 *
 * <p>Para a tabela de classificação de assistentes é armazenados dados do jogador com assistentes.
 * As informações consistem em quantidade de contratos feitos e nível de confiança dos assistentes.
 * Cada tipo de assistente irá possuir uma informação referente a quantidade de contratos e confiança.</p>
 *
 * @see AbstractControl
 * @see Connection
 * @see MercenaryRank
 *
 * @author Andrew
 */

public class MercenaryRankControl extends AbstractControl
{
	/**
	 * Cria uma nova instância de um controle para gerenciamento da classificação dos assistentes.
	 * @param connection referência do objeto contendo a conexão com o banco de dados.
	 */

	public MercenaryRankControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Procedimento interno que garante a validade do objeto controlado por este controle.
	 * @param rank referência do objeto que será validado pelo controle.
	 * @throws RagnarokException pode ocorrer apenas se o objeto passado for nulo
	 * ou então se o código de identificação for inválido (menor que um | <1).
	 */

	private void validate(MercenaryRank rank) throws RagnarokException
	{
		if (rank == null)
			throw new RagnarokException("classificação nula");

		if (rank.getID() < 1)
			throw new RagnarokException("classificaiação inválida");
	}

	/**
	 * Permite obter as informações da classificação dos assistentes de um determinado personagem.
	 * @param id código de identificação do personagem que deseja a classificação dos assistentes.
	 * @return aquisição do objeto contendo as informações de classificação dos assistentes
	 * ou no caso do código de identificação do personagem ser inválido: null.
	 * @throws RagnarokException falha de conexão com o banco de dados.
	 */

	public MercenaryRank get(int id) throws RagnarokException
	{
		MercenaryRank rank = null;

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT archer_faith, archer_calls, spear_faith, "
						+	"spear_calls, sword_faith, sword_calls FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				rank = new MercenaryRank();
				rank.setID(id);
				rank.setArcherFaith(rs.getInt("archer_faith"));
				rank.setArcherCalls(rs.getInt("archer_calls"));
				rank.setSpearFaith(rs.getInt("spear_faith"));
				rank.setSpearCalls(rs.getInt("spear_calls"));
				rank.setSwordFaith(rs.getInt("sword_faith"));
				rank.setSwordCalls(rs.getInt("sword_calls"));

				logDebug("MercenaryRank#%d carregado.\n", rank.getID());
			}

			return rank;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona informações referentes a classificação dos assistentes de um personagem.
	 * @param rank objeto contendo informações do personagem à adicionar.
	 * @return true se conseguir adicionar ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou já existe.
	 */

	public boolean add(MercenaryRank rank) throws RagnarokException
	{
		validate(rank);

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("INSERT INTO %s (archer_faith, archer_calls, spear_faith, "
						+	"spear_calls, sword_faith, sword_calls) "
						+	"VALUES (?, ?, ?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, rank.getID());
			ps.setInt(2, rank.getArcherFaith());
			ps.setInt(3, rank.getArcherCalls());
			ps.setInt(4, rank.getSpearFaith());
			ps.setInt(5, rank.getSpearCalls());
			ps.setInt(6, rank.getSwordFaith());
			ps.setInt(7, rank.getSwordCalls());

			boolean result = ps.executeUpdate() == 1;

			if (result)
				logDebug("MercenaryRank#%d adicionado.\n", rank.getID());

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as informações referentes a classificação dos assistentes de um personagem.
	 * @param rank objeto contendo informações do personagem à atualizar.
	 * @return true se conseguir atualizar ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou não existe.
	 */

	public boolean set(MercenaryRank rank) throws RagnarokException
	{
		validate(rank);

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("UPDATE %s SET archer_faith = ?, archer_calls = ?, spear_faith = ?, "
						+	"spear_calls = ?, sword_faith = ?, sword_calls = ? WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, rank.getArcherFaith());
			ps.setInt(2, rank.getArcherCalls());
			ps.setInt(3, rank.getSpearFaith());
			ps.setInt(4, rank.getSpearCalls());
			ps.setInt(5, rank.getSwordFaith());
			ps.setInt(6, rank.getSwordCalls());
			ps.setInt(7, rank.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Remove as informações referentes a classificação dos assistentes de um personagem.
	 * @param rank objeto contendo informações do personagem à remover.
	 * @return true se conseguir remover ou false caso contrário.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou não existe.
	 */

	public boolean remove(MercenaryRank rank) throws RagnarokException
	{
		validate(rank);

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("DELETE FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, rank.getID());

			boolean result = ps.executeUpdate() == 1;

			if (result)
				logDebug("MercenaryRank#%d removido.\n", rank.getID());

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Recarrega as informações de uma determinada classificação conforme o banco de dados.
	 * Esse procedimento pode ser utilizado quando o banco de dados for alterador por fora,
	 * ou seja, quando houve mudança nos dados porém não vieram do servidor desse controle.
	 * @param rank classificação dos assistentes do qual terá os dados recarregados.
	 * @return true se tiver recarregado ou false caso não encontre a classificação.
	 * @throws RagnarokException falha de conexão com o banco de dados,
	 * objeto passado sem código de identificação válido ou não existe.
	 */

	public boolean reload(MercenaryRank rank) throws RagnarokException
	{
		validate(rank);

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT archer_faith, archer_calls, spear_faith, "
				+	"spear_calls, sword_faith, sword_calls FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, rank.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				rank.setArcherFaith(rs.getInt("archer_faith"));
				rank.setArcherCalls(rs.getInt("archer_calls"));
				rank.setSpearFaith(rs.getInt("spear_faith"));
				rank.setSpearCalls(rs.getInt("spear_calls"));
				rank.setSwordFaith(rs.getInt("sword_faith"));
				rank.setSwordCalls(rs.getInt("sword_calls"));

				logDebug("MercenaryRank#%d recarregado.\n", rank.getID());

				return true;
			}

			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

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
 * <h1>Controle para Classifica��o de Assistentes</h1>
 *
 * <p>Para a tabela de classifica��o de assistentes � armazenados dados do jogador com assistentes.
 * As informa��es consistem em quantidade de contratos feitos e n�vel de confian�a dos assistentes.
 * Cada tipo de assistente ir� possuir uma informa��o referente a quantidade de contratos e confian�a.</p>
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
	 * Cria uma nova inst�ncia de um controle para gerenciamento da classifica��o dos assistentes.
	 * @param connection refer�ncia do objeto contendo a conex�o com o banco de dados.
	 */

	public MercenaryRankControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Procedimento interno que garante a validade do objeto controlado por este controle.
	 * @param rank refer�ncia do objeto que ser� validado pelo controle.
	 * @throws RagnarokException pode ocorrer apenas se o objeto passado for nulo
	 * ou ent�o se o c�digo de identifica��o for inv�lido (menor que um | <1).
	 */

	private void validate(MercenaryRank rank) throws RagnarokException
	{
		if (rank == null)
			throw new RagnarokException("classifica��o nula");

		if (rank.getID() < 1)
			throw new RagnarokException("classificaia��o inv�lida");
	}

	/**
	 * Permite obter as informa��es da classifica��o dos assistentes de um determinado personagem.
	 * @param id c�digo de identifica��o do personagem que deseja a classifica��o dos assistentes.
	 * @return aquisi��o do objeto contendo as informa��es de classifica��o dos assistentes
	 * ou no caso do c�digo de identifica��o do personagem ser inv�lido: null.
	 * @throws RagnarokException falha de conex�o com o banco de dados.
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
	 * Adiciona informa��es referentes a classifica��o dos assistentes de um personagem.
	 * @param rank objeto contendo informa��es do personagem � adicionar.
	 * @return true se conseguir adicionar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou j� existe.
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
	 * Atualiza as informa��es referentes a classifica��o dos assistentes de um personagem.
	 * @param rank objeto contendo informa��es do personagem � atualizar.
	 * @return true se conseguir atualizar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
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
	 * Remove as informa��es referentes a classifica��o dos assistentes de um personagem.
	 * @param rank objeto contendo informa��es do personagem � remover.
	 * @return true se conseguir remover ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
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
	 * Recarrega as informa��es de uma determinada classifica��o conforme o banco de dados.
	 * Esse procedimento pode ser utilizado quando o banco de dados for alterador por fora,
	 * ou seja, quando houve mudan�a nos dados por�m n�o vieram do servidor desse controle.
	 * @param rank classifica��o dos assistentes do qual ter� os dados recarregados.
	 * @return true se tiver recarregado ou false caso n�o encontre a classifica��o.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
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

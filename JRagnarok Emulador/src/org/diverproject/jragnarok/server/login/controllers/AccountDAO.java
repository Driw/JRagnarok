package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractDAO;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountState;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>DAO para Contas</h1>
 *
 * <p>Comunica��o do sistema com o banco de dados para trabalhar com as contas dos jogadores.
 * Permite realizar a sele��o e atualiza��o das informa��es de determinadas contas.
 * Comunica-se com a DAO de grupos e c�digos pin para carregar todas as informa��es.</p>
 *
 * @see PincodeDAO
 * @see GroupDAO
 *
 * @author Andrew
 */

public class AccountDAO extends AbstractDAO
{
	/**
	 * DAO para obter informa��es do c�digo pin.
	 */
	private PincodeDAO pincodeDAO;

	/**
	 * DAO para obter informa��es dos grupos de jogadores.
	 */
	private GroupDAO groupDAO;

	/**
	 * Cria um nova DAO que ir� permitir comunicar-se com o banco de dados das contas.
	 * @param connection refer�ncia da conex�o que deve ser considerada.
	 */

	public AccountDAO(Connection connection)
	{
		super(connection);

		pincodeDAO = new PincodeDAO(connection);
		groupDAO = new GroupDAO(connection);
	}

	/**
	 * Seleciona as informa��es de uma determinada conta no banco de dados pelo nome de usu�rio.
	 * @param username nome de usu�rio da conta desejada, cada conta possui um usu�rio �nico.
	 * @return aquisi��o da conta contendo as informa��es carregadas ou null se n�o encontrar.
	 * @throws RagnarokException apenas se houver falha na conex�o.
	 */

	public Account select(String username) throws RagnarokException
	{
		String accountTable = Tables.getInstance().getAccounts();

		String sql = format("SELECT id, password, last_login, registered, email, birth_date,"
						+ " login_count, unban, expiration, pincode, groupid, state, last_ip"
						+ " FROM %s WHERE username = ?",
						accountTable);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				Account account = new Account();
				account.setID(rs.getInt("id"));
				account.setUsername(username);
				account.setPassword(rs.getString("password"));
				account.getRegistered().set(rs.getDate("registered").getTime());
				account.getLastLogin().set(rs.getDate("last_login").getTime());
				account.setEmail(rs.getString("email"));
				account.setBirthDate(rs.getString("birth_date"));
				account.setLoginCount(rs.getInt("login_count"));
				account.getUnban().set(rs.getTimestamp("unban").getTime());
				account.getExpiration().set(rs.getTimestamp("expiration").getTime());
				account.setState(AccountState.parse(rs.getInt("state")));
				account.getLastIP().set(rs.getInt("last_ip"));
				account.getPincode().setID(rs.getInt("pincode"));
				account.getGroup().setID(rs.getInt("groupid"));

				pincodeDAO.load(account.getPincode());
				groupDAO.load(account.getGroup());

				return account;
			}

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}

		return null;
	}

	/**
	 * Realiza a atualiza��o de algumas das informa��es de uma determinada conta.
	 * Para que isso seja feito � necess�rio um c�digo de identifica��o v�lido.
	 * Os dados atualizados s�o: hor�rio do �ltimo acesso, �ltimo endere�o de ip,
	 * contagem de acessos, tempo de ban, tempo para expirar e estado da conta.
	 * @param account refer�ncia da conta do qual deseja atualizar os dados.
	 * @return true se conseguir atualizar as informa��es ou false caso contr�rio.
	 * @throws RagnarokException apenas se houver falha na conex�o.
	 */

	public boolean update(Account account) throws RagnarokException
	{
		return updateLastLogin(account) && updateAccount(account);
	}

	/**
	 * Realiza a atualiza��o do hor�rio em que uma determinada conta fez seu �ltimo acesso.
	 * @param account refer�ncia da conta que ter� seu hor�rio de acesso atualizado.
	 * @return true se conseguir atualizar ou false caso contr�rio.
	 * @throws RagnarokException apenas se houver falha na conex�o.
	 */

	public boolean updateLastLogin(Account account) throws RagnarokException
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("UPDATE %s SET last_login = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setTimestamp(1, account.getLastLogin().toTimestamp());
			ps.setInt(2, account.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Realiza a atualiza��o da contagem de acessos, tempo de banimento, tempo para expirar,
	 * estado da conta e �ltimo endere�o de IP usado de uma determinada conta.
	 * @param account refer�ncia da conta que ter� os dados acima atualizados.
	 * @return true se conseguir atualizar ou false caso contr�rio.
	 * @throws RagnarokException apenas se houver falha na conex�o.
	 */

	public boolean updateAccount(Account account) throws RagnarokException
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("UPDATE %s SET login_count = ?, unban = ?, expiration = ?, state = ?, last_ip = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, account.getLoginCount());
			ps.setTimestamp(2, account.getUnban().toTimestamp());
			ps.setTimestamp(3, account.getExpiration().toTimestamp());
			ps.setInt(4, account.getState().CODE);
			ps.setInt(5, account.getLastIP().get());
			ps.setInt(6, account.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		return description.toString();
	}
}

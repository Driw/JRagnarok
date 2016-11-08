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
 * <p>Comunicação do sistema com o banco de dados para trabalhar com as contas dos jogadores.
 * Permite realizar a seleção e atualização das informações de determinadas contas.
 * Comunica-se com a DAO de grupos e códigos pin para carregar todas as informações.</p>
 *
 * @see PincodeDAO
 * @see GroupDAO
 *
 * @author Andrew
 */

public class AccountDAO extends AbstractDAO
{
	/**
	 * DAO para obter informações do código pin.
	 */
	private PincodeDAO pincodeDAO;

	/**
	 * DAO para obter informações dos grupos de jogadores.
	 */
	private GroupDAO groupDAO;

	/**
	 * Cria um nova DAO que irá permitir comunicar-se com o banco de dados das contas.
	 * @param connection referência da conexão que deve ser considerada.
	 */

	public AccountDAO(Connection connection)
	{
		super(connection);

		pincodeDAO = new PincodeDAO(connection);
		groupDAO = new GroupDAO(connection);
	}

	/**
	 * Procedimento interno usado para criar uma nova conta a partir de um resultado.
	 * @param rs resultado obtido de uma consulta contendo dados de uma conta.
	 * @return aquisição de uma nova instância de conta com os dados da consulta.
	 * @throws SQLException apenas se houver falha na leitura dos dados.
	 */

	private Account newAccount(ResultSet rs) throws SQLException
	{
		Account account = new Account();
		account.setID(rs.getInt("id"));
		account.setUsername(rs.getString("username"));
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

		return account;
	}

	/**
	 * Seleciona as informações de uma determinada conta no banco de dados pelo código de identificação.
	 * @param id código de identificação da conta desejada, cada conta possui um código único.
	 * @return aquisição da conta contendo as informações carregadas ou null se não encontrar.
	 * @throws RagnarokException apenas se houver falha na conexão.
	 */

	public Account select(int id) throws RagnarokException
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("SELECT id, username, password, last_login, registered, email, birth_date,"
						+ " login_count, unban, expiration, pincode, groupid, state, last_ip"
						+ " FROM %s WHERE id = ?",
						table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				Account account = newAccount(rs);

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
	 * Seleciona as informações de uma determinada conta no banco de dados pelo nome de usuário.
	 * @param username nome de usuário da conta desejada, cada conta possui um usuário único.
	 * @return aquisição da conta contendo as informações carregadas ou null se não encontrar.
	 * @throws RagnarokException apenas se houver falha na conexão.
	 */

	public Account select(String username) throws RagnarokException
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("SELECT id, username, password, last_login, registered, email, birth_date,"
						+ " login_count, unban, expiration, pincode, groupid, state, last_ip"
						+ " FROM %s WHERE username = ?",
						table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				Account account = newAccount(rs);

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
	 * Realiza a atualização de algumas das informações de uma determinada conta.
	 * Para que isso seja feito é necessário um código de identificação válido.
	 * Os dados atualizados são: horário do último acesso, último endereço de ip,
	 * contagem de acessos, tempo de ban, tempo para expirar e estado da conta.
	 * @param account referência da conta do qual deseja atualizar os dados.
	 * @return true se conseguir atualizar as informações ou false caso contrário.
	 * @throws RagnarokException apenas se houver falha na conexão.
	 */

	public boolean update(Account account) throws RagnarokException
	{
		return updateLastLogin(account) && updateAccount(account);
	}

	/**
	 * Realiza a atualização do horário em que uma determinada conta fez seu último acesso.
	 * @param account referência da conta que terá seu horário de acesso atualizado.
	 * @return true se conseguir atualizar ou false caso contrário.
	 * @throws RagnarokException apenas se houver falha na conexão.
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
	 * Realiza a atualização da contagem de acessos, tempo de banimento, tempo para expirar,
	 * estado da conta e último endereço de IP usado de uma determinada conta.
	 * @param account referência da conta que terá os dados acima atualizados.
	 * @return true se conseguir atualizar ou false caso contrário.
	 * @throws RagnarokException apenas se houver falha na conexão.
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

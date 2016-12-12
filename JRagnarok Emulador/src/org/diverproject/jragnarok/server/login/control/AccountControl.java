package org.diverproject.jragnarok.server.login.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.log.LogSystem.logExeceptionSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountState;

/**
 * <h1>Controle de Contas</h1>
 *
 * <p>O controle de contas permite buscar dados de constas dos jogadores no banco de dados.
 * Possuindo métodos básicos para garantir o gerenciamento destas contas no banco de dados.
 * O gerenciamento deverá incluir métodos para se: obter dados e atualizar os dados.</p>
 *
 * <p>Este controle ainda irá comunicar-se com outros dois controles caso necessário.
 * O primeiro controle é para gerenciamento de código PIN e o segundo para grupo de contas.</p>
 *
 * @see Connection
 * @see AbstractControl
 * @see PincodeControl
 * @see GroupControl
 *
 * @author Andrew
 */

public class AccountControl extends AbstractControl
{
	/**
	 * Controle para códigos PIN.
	 */
	private PincodeControl pincodes;

	/**
	 * Controle para grupo de contas.
	 */
	private GroupControl groups;

	/**
	 * Cria um novo controle para gerenciar a persistência de contas dos jogadores.
	 * @param connection conexão com o banco de dados referente a persistência.
	 */

	public AccountControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * @param pincodeControl controle para códigos PIN que será usado.
	 */

	public void setPincodeControl(PincodeControl pincodeControl)
	{
		this.pincodes = pincodeControl;
	}

	/**
	 * @param groupControl controle para grupos que será usado.
	 */

	public void setGroupControl(GroupControl groupControl)
	{
		this.groups = groupControl;
	}

	/**
	 * Procedimento interno usado para criar uma nova conta a partir de um resultado.
	 * @param rs resultado obtido de uma consulta contendo dados de uma conta.
	 * @return aquisição de uma nova instância de conta com os dados da consulta.
	 * @throws SQLException apenas se houver falha na leitura dos dados.
	 * @throws RagnarokException inconsistência nas informações carregadas.
	 */

	private Account newAccount(ResultSet rs) throws SQLException, RagnarokException
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

		pincodes.load(account.getPincode());
		groups.load(account.getGroup());

		return account;
	}

	/**
	 * Permite obter os dados de uma determinada conta de jogador pelo nome de usuário.
	 * @param username nome de usuário da conta do qual deseja as informações.
	 * @return aquisição da conta respectiva ao nome de usuário acima.
	 */

	public Account get(String username)
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("SELECT id, username, password, last_login, registered, email, birth_date,"
						+ " login_count, unban, expiration, pincode, groupid, state, last_ip"
						+ " FROM %s WHERE username = ?",
						table);

		Account account = null;

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				account = newAccount(rs);

		} catch (Exception e) {
			logExeceptionSource(e);
		}

		return account;
	}

	/**
	 * Permite obter os dados de uma determinada conta de jogador pelo seu identificador.
	 * @param accountID código de identificação da conta do qual deseja as informações.
	 * @return aquisição da conta respectiva ao código de identificação acima ou
	 * null caso o identificador passado seja inválido (não existe).
	 */

	public Account get(int accountID)
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("SELECT id, username, password, last_login, registered, email, birth_date,"
						+ " login_count, unban, expiration, pincode, groupid, state, last_ip"
						+ " FROM %s WHERE id = ?",
						table);

		Account account = null;

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				account = newAccount(rs);

		} catch (Exception e) {
			logExeceptionSource(e);
		}

		return account;
	}

	/**
	 * Atualiza algumas das informações de uma determinada conta no banco de dados.
	 * As informações atualizadas são como: último acesso, endereço de ip, estado e outros.
	 * @param account referência da conta do qual deseja atualizar os dados.
	 * @return true se conseguir atualizar ou false caso contrário.
	 * @throws RagnarokException falha de conexão ou inconsistência nos dados.
	 */

	public boolean set(Account account)
	{
		return setLastLogin(account) && setAccount(account);
	}

	/**
	 * Realiza a atualização do horário em que uma determinada conta fez seu último acesso.
	 * @param account referência da conta que terá seu horário de acesso atualizado.
	 * @return true se conseguir atualizar ou false caso contrário.
	 */

	public boolean setLastLogin(Account account)
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("UPDATE %s SET last_login = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setTimestamp(1, account.getLastLogin().toTimestamp());
			ps.setInt(2, account.getID());

			return ps.executeUpdate() == 1;

		} catch (Exception e) {
			logExeceptionSource(e);
		}

		return false;
	}

	/**
	 * Realiza a atualização da contagem de acessos, tempo de banimento, tempo para expirar,
	 * estado da conta e último endereço de IP usado de uma determinada conta.
	 * @param account referência da conta que terá os dados acima atualizados.
	 * @return true se conseguir atualizar ou false caso contrário.
	 */

	public boolean setAccount(Account account)
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

		} catch (Exception e) {
			logExeceptionSource(e);
		}

		return false;
	}
}

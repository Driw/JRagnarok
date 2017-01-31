package org.diverproject.jragnarok.server.login.control;

import static org.diverproject.util.Util.format;
import static org.diverproject.util.Util.timestamp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.Pincode;


/**
 * <h1>Controle para C�digos PIN</h1>
 *
 * <p>C�digos PIN s�o usados como uma forma de seguran�a extra para as contas dos jogadores.
 * Atrav�s desse controle � poss�vel realizar intera��es com o banco de dados sobre estes.
 * Os m�todos dispon�veis permite obter, carregar ou atualizar um c�digo PIN.</p>
 *
 * @see Connection
 * @see AbstractControl
 * @see Pincode
 *
 * @author Andrew
 */

public class PincodeControl extends AbstractControl
{
	/**
	 * Cria um novo controle para gerenciar a persist�ncia de C�digo PIN dos jogadores.
	 * @param connection conex�o com o banco de dados referente a persist�ncia.
	 */

	public PincodeControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Permite obter todas as informa��es referentes a um determinado c�digo PIN.
	 * @param accountID c�digo de identifica��o dos dados referente ao c�digo PIN desejado.
	 * @return aquisi��o do c�digo PIN desejado ou null se n�o encontrar.
	 * @throws RagnarokException falha de conex�o durante o procedimento.
	 */

	public Pincode get(int accountID) throws RagnarokException
	{
		String table = Tables.getInstance().getPincodes();
		String sql = format("SELECT enabled, code_number, change_time FROM %s WHERE accountid = ?", table);

		try {

			Pincode pincode = null;

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
			{
				pincode = new Pincode();
				pincode.setEnabled(rs.getBoolean("enabled"));
				pincode.setCode(rs.getString("code_number"));
				pincode.getChanged().set(timestamp(rs.getTimestamp("change_time")));
			}

			return pincode;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Atualiza as informa��es de um determinado objeto de c�digo PIN do sistema.
	 * Usado para garantir que os dados do mesmo considerem os valores corretos.
	 * Ser� necess�rio no m�nimo a defini��o do seu c�digo de identifica��o.
	 * @param account conta do qual deseja carregar os dados de c�digo PIN.
	 * @throws RagnarokException falha de conex�o ou id inv�lido.
	 */

	public void load(Account account) throws RagnarokException
	{
		if (account == null)
			throw new RagnarokException("conta nulo inesperado");

		String table = Tables.getInstance().getPincodes();
		String sql = format("SELECT enabled, code_number, change_time FROM %s WHERE accountid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, account.getID());

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				throw new RagnarokException("pincode '%d' n�o encontrado", account.getID());

			Pincode pincode = account.getPincode();
			pincode.setEnabled(rs.getBoolean("enabled"));
			pincode.setCode(rs.getString("code_number"));
			pincode.getChanged().set(timestamp(rs.getTimestamp("change_time")));

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite atualizar as informa��es contidas no banco de dados de um determinado c�digo PIN.
	 * Para que as atualiza��es possam ocorrer de fato um id v�lido ser� necess�rio.
	 * @param account conta do qual deseja atualizar as informa��es relacionadas ao c�digo PIN.
	 * @return true se conseguir atualizar ou fase caso o id seja inv�lido.
	 * @throws RagnarokException falha de conex�o ou id inv�lido.
	 */

	public boolean update(Account account) throws RagnarokException
	{
		if (account == null)
			throw new RagnarokException("conta nulo inesperado");

		String table = Tables.getInstance().getPincodes();
		String sql = format("UPDATE pincode SET enabled = ?, code_number = ?, change_time = ? WHERE accountid = ?", table);

		try {

			Pincode pincode = account.getPincode();

			PreparedStatement ps = prepare(sql);
			ps.setBoolean(1, pincode.isEnabled());
			ps.setString(2, pincode.getCode());
			ps.setTimestamp(3, timestamp(pincode.getChanged().get()));
			ps.setInt(4, account.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}
}

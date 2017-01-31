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
 * <h1>Controle para Códigos PIN</h1>
 *
 * <p>Códigos PIN são usados como uma forma de segurança extra para as contas dos jogadores.
 * Através desse controle é possível realizar interações com o banco de dados sobre estes.
 * Os métodos disponíveis permite obter, carregar ou atualizar um código PIN.</p>
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
	 * Cria um novo controle para gerenciar a persistência de Código PIN dos jogadores.
	 * @param connection conexão com o banco de dados referente a persistência.
	 */

	public PincodeControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Permite obter todas as informações referentes a um determinado código PIN.
	 * @param accountID código de identificação dos dados referente ao código PIN desejado.
	 * @return aquisição do código PIN desejado ou null se não encontrar.
	 * @throws RagnarokException falha de conexão durante o procedimento.
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
	 * Atualiza as informações de um determinado objeto de código PIN do sistema.
	 * Usado para garantir que os dados do mesmo considerem os valores corretos.
	 * Será necessário no mínimo a definição do seu código de identificação.
	 * @param account conta do qual deseja carregar os dados de código PIN.
	 * @throws RagnarokException falha de conexão ou id inválido.
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
				throw new RagnarokException("pincode '%d' não encontrado", account.getID());

			Pincode pincode = account.getPincode();
			pincode.setEnabled(rs.getBoolean("enabled"));
			pincode.setCode(rs.getString("code_number"));
			pincode.getChanged().set(timestamp(rs.getTimestamp("change_time")));

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite atualizar as informações contidas no banco de dados de um determinado código PIN.
	 * Para que as atualizações possam ocorrer de fato um id válido será necessário.
	 * @param account conta do qual deseja atualizar as informações relacionadas ao código PIN.
	 * @return true se conseguir atualizar ou fase caso o id seja inválido.
	 * @throws RagnarokException falha de conexão ou id inválido.
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

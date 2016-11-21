package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
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
	 * @param id c�digo de identifica��o dos dados referente ao c�digo PIN desejado.
	 * @return aquisi��o do c�digo PIN desejado ou null se n�o encontrar.
	 * @throws RagnarokException falha de conex�o durante o procedimento.
	 */

	public Pincode get(int id) throws RagnarokException
	{
		String table = Tables.getInstance().getPincodes();
		String sql = format("SELECT enabled, code, changed FROM %s WHERE id = ?", table);

		try {

			Pincode pincode = null;

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
			{
				pincode = new Pincode();
				pincode.setID(id);
				pincode.setEnabled(rs.getBoolean("enabled"));
				pincode.setCode(rs.getString("code"));
				pincode.getChanged().set(rs.getDate("changed").getTime());
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
	 * @param pincode c�digo PIN do qual deseja obter carregar os dados.
	 * @throws RagnarokException falha de conex�o ou id inv�lido.
	 */

	public void load(Pincode pincode) throws RagnarokException
	{
		if (pincode == null)
			throw new RagnarokException("c�digo PIN nulo inesperado");

		String table = Tables.getInstance().getPincodes();
		String sql = format("SELECT enabled, code, changed FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, pincode.getID());

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				throw new RagnarokException("pincode '%d' n�o encontrado", pincode.getID());

			pincode.setEnabled(rs.getBoolean("enabled"));
			pincode.setCode(rs.getString("code"));
			pincode.getChanged().set(rs.getDate("changed").getTime());

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite atualizar as informa��es contidas no banco de dados de um determinado c�digo PIN.
	 * Para que as atualiza��es possam ocorrer de fato um id v�lido ser� necess�rio.
	 * @param pincode c�digo pin do qual ter� as informa��es atualizadas no banco de dados.
	 * @return true se conseguir atualizar ou fase caso o id seja inv�lido.
	 * @throws RagnarokException falha de conex�o ou id inv�lido.
	 */

	public boolean update(Pincode pincode) throws RagnarokException
	{
		if (pincode == null)
			throw new RagnarokException("c�digo PIN nulo inesperado");

		String table = Tables.getInstance().getPincodes();
		String sql = format("UPDATE pincode SET enabled = ?, code = ?, changed = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setBoolean(1, pincode.isEnabled());
			ps.setString(2, pincode.getCode());
			ps.setTimestamp(3, pincode.getChanged().toTimestamp());
			ps.setInt(4, pincode.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}
}

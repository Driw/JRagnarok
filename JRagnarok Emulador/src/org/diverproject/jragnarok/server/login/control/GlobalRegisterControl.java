package org.diverproject.jragnarok.server.login.control;

import static org.diverproject.util.Util.format;
import static org.diverproject.util.lang.IntUtil.interval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.common.GlobalRegister;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;

/**
 * <h1>Controle para Registro de Vari�veis Global</h1>
 *
 * <p>Este controle permite realizar a persist�ncia (adicionar/atualizar/remover) valores de vari�veis.
 * Para este caso as vari�veis ser�o vinculadas em uma conta atrav�s do seu c�digo de identifica��o.
 * Al�m disso as vari�veis devem possuir obviamente um nome e obviamente o valor que este representa.</p>
 *
 * @see AbstractControl
 * @see GlobalRegisterControl
 * @see GlobalRegister
 *
 * @author Andrew
 */

public class GlobalRegisterControl extends AbstractControl
{
	/**
	 * Cria uma nova inst�ncia de um controle para persistir registro de vari�veis global.
	 * @param connection conex�o com o banco de dados que ser� usado na persist�ncia.
	 */

	public GlobalRegisterControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Insere ou atualiza uma determinada vari�vel conforme as informa��es nela contida.
	 * Caso n�o exista ir� inserir a vari�vel e se exibir ir� atualizar o seu valor.
	 * @param register registro do qual dever� ser substitu�do no banco de dados.
	 * @return true se adicionar ou atualizar o valor da vari�vel ou false caso contr�rio.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	@SuppressWarnings("unchecked")
	public boolean replace(GlobalRegister<?> register) throws RagnarokException
	{
		if (register.getValue() != null)
		{
			if (register.getValue() instanceof Integer)
				return replaceInt((GlobalRegister<Integer>) register);

			else if (register.getValue() instanceof String)
				return replaceStr((GlobalRegister<String>) register);
		}

		return false;
	}

	/**
	 * Excluir um determinado registro do sistema conforme as informa��es nele contido.
	 * @param register registro do qual dever� ser exclu�do do banco de dados.
	 * @return true se conseguir excluir ou false caso n�o encontre a vari�vel.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	@SuppressWarnings("unchecked")
	public boolean delete(GlobalRegister<?> register) throws RagnarokException
	{
		if (register.getValue() != null)
		{
			if (register.getValue() instanceof Integer)
				return deleteInt((GlobalRegister<Integer>) register);

			else if (register.getValue() instanceof String)
				return deleteStr((GlobalRegister<String>) register);
		}

		return false;
	}

	/**
	 * Insere ou atualiza uma determinada vari�vel conforme as informa��es nela contida.
	 * Caso n�o exista ir� inserir a vari�vel e se exibir ir� atualizar o seu valor.
	 * Este procedimento s� permitir� trabalhar com vari�veis de valor do tipo Integer.
	 * @param register registro do qual dever� ser substitu�do no banco de dados.
	 * @return true se adicionar ou atualizar o valor da vari�vel ou false caso contr�rio.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	private boolean replaceInt(GlobalRegister<Integer> register) throws RagnarokException
	{
		String table = Tables.getInstance().getGlobalIntRegister();
		String sql = format("REPLACE INTO %s (accountid, name, value) VALUES (?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, register.getAccountID());
			ps.setString(2, register.getKey());
			ps.setInt(3, register.getValue());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Excluir um determinado registro do sistema conforme as informa��es nele contido.
	 * Este procedimento s� permitir� excluir vari�veis com valores do tipo Integer.
	 * @param register registro do qual dever� ser exclu�do do banco de dados.
	 * @return true se conseguir excluir ou false caso n�o encontre a vari�vel.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	private boolean deleteInt(GlobalRegister<Integer> register) throws RagnarokException
	{
		String table = Tables.getInstance().getGlobalIntRegister();
		String sql = format("DELETE FROM %s WHERE accountid = ? AND name = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, register.getAccountID());
			ps.setString(2, register.getKey());

			return interval(ps.executeUpdate(), 1, 2);

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Insere ou atualiza uma determinada vari�vel conforme as informa��es nela contida.
	 * Caso n�o exista ir� inserir a vari�vel e se exibir ir� atualizar o seu valor.
	 * Este procedimento s� permitir� trabalhar com vari�veis de valor do tipo String.
	 * @param register registro do qual dever� ser substitu�do no banco de dados.
	 * @return true se adicionar ou atualizar o valor da vari�vel ou false caso contr�rio.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	private boolean replaceStr(GlobalRegister<String> register) throws RagnarokException
	{
		String table = Tables.getInstance().getGlobalStrRegister();
		String sql = format("REPLACE INTO %s (accountid, name, value) VALUES (?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, register.getAccountID());
			ps.setString(2, register.getKey());
			ps.setString(3, register.getValue());

			return interval(ps.executeUpdate(), 1, 2);

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Excluir um determinado registro do sistema conforme as informa��es nele contido.
	 * Este procedimento s� permitir� excluir vari�veis com valores do tipo String.
	 * @param register registro do qual dever� ser exclu�do do banco de dados.
	 * @return true se conseguir excluir ou false caso n�o encontre a vari�vel.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	private boolean deleteStr(GlobalRegister<String> register) throws RagnarokException
	{
		String table = Tables.getInstance().getGlobalStrRegister();
		String sql = format("DELETE FROM %s WHERE accountid = ? AND name = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, register.getAccountID());
			ps.setString(2, register.getKey());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Seleciona todas as informa��es sobre registro de vari�veis globais de uma conta especificada.
	 * @param accountID c�digo de identifica��o da conta do qual deseja as vari�veis.
	 * @return aquisi��o de uma fila com todos os registros das vari�veis encontrados.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	public Queue<GlobalRegister<?>> getAll(int accountID) throws RagnarokException
	{
		Queue<GlobalRegister<?>> registers = new DynamicQueue<>();

		selectInt(registers, accountID);
		selectStr(registers, accountID);

		return registers;
	}

	/**
	 * Seleciona todas as informa��es sobre registro de vari�veis globais de uma conta especificada.
	 * Para este caso as vari�veis que ser�o obtidas s�o apenas aquelas de valores em Integer.
	 * @param registers fila onde os registros encontrados dever�o ser alocados.
	 * @param accountID c�digo de identifica��o da conta do qual deseja as vari�veis.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	private void selectInt(Queue<GlobalRegister<?>> registers, int accountID) throws RagnarokException
	{
		String table = Tables.getInstance().getGlobalIntRegister();
		String sql = format("SELECT name, value FROM %s WHERE accountid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);

			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				GlobalRegister<Integer> register = new GlobalRegister<>(accountID, rs.getString("name"));
				register.setValue(rs.getInt("value"));
				register.setUpdatable(false);
				registers.offer(register);
			}

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Seleciona todas as informa��es sobre registro de vari�veis globais de uma conta especificada.
	 * Para este caso as vari�veis que ser�o obtidas s�o apenas aquelas de valores em String.
	 * @param registers fila onde os registros encontrados dever�o ser alocados.
	 * @param accountID c�digo de identifica��o da conta do qual deseja as vari�veis.
	 * @throws RagnarokException apenas se houver falha na conex�o com o banco de dados.
	 */

	private void selectStr(Queue<GlobalRegister<?>> registers, int accountID) throws RagnarokException
	{
		String table = Tables.getInstance().getGlobalStrRegister();
		String sql = format("SELECT name, value FROM %s WHERE accountid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountID);

			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				GlobalRegister<String> register = new GlobalRegister<>(accountID, rs.getString("name"));
				register.setValue(rs.getString("value"));
				register.setUpdatable(false);
				registers.offer(register);
			}

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

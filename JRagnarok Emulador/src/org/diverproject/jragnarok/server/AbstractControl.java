package org.diverproject.jragnarok.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Controle Abstrato</h1>
 *
 * <p>Todos os controles existentes no emulador deverão ser herdados desta classe.
 * Irá definir como requisito básico a existência de uma conexão com o banco de dados.
 * Deverá implementar métodos internamente para executar comandos no banco de dados.</p>
 *
 * @see Connection
 *
 * @author Andrew
 */

public abstract class AbstractControl
{
	/**
	 * Código do erro para: chave duplicada.
	 */
	public static final int DUPLICATED_KEY = 1062;

	/**
	 * Referência da conexão com o banco de dados.
	 */
	private Connection connection;

	/**
	 * Cria um novo controle sendo necessário especificar a conexão para persistência.
	 * @param connection conexão com o banco de dados referente a persistência.
	 */

	public AbstractControl(Connection connection)
	{
		this.connection = connection;
	}

	/**
	 * Permite obter a conexão e irá validar se a mesma está definida e válida.
	 * @return aquisição do objeto contendo a conexão com o banco de dados.
	 * @throws RagnarokException apenas se não for definida ou inválida.
	 */

	protected Connection getConnection() throws RagnarokException
	{
		if (connection == null)
			throw new RagnarokException("conexão não definida");

		try {

			if (connection.isClosed())
				throw new RagnarokException("conexão fechada");

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		return connection;
	}

	/**
	 * Prepara uma cona query para ser executada no banco de dados conforme conexão especificada.
	 * Esse preparamento permite que a query não sofra qualquer tipo de SQL Injection.
	 * Como também facilita a definição dos parâmetros e utilização das consultas feitas.
	 * @param sql query contendo os comandos a serem executados no banco de dados.
	 * @return aquisição de um novo objeto que irá trabalhar em cima da query especificada.
	 * @throws RagnarokException apenas se houver problemas com a conexão.
	 * @throws SQLException falha ao criar o preparador de queries.
	 */

	protected PreparedStatement prepare(String sql) throws RagnarokException, SQLException
	{
		return getConnection().prepareStatement(sql);
	}

	/**
	 * Permite definir um valor numérico inteiro em uma declaração preparada em SQL.
	 * Define o valor numérico passado ou null caso seja necessário conforme:
	 * @param ps declaração preparada que terá o valor numérico definido.
	 * @param index número do índice da variável que será definida.
	 * @param value valor numérico inteiro para ser definido na declaração.
	 * @param nullValue valor numérico inteiro que será dado como nulo.
	 * @throws SQLException falha de conexão.
	 */

	protected void set(PreparedStatement ps, int index, int value, int nullValue) throws SQLException
	{
		if (value == nullValue)
			ps.setNull(index, Types.INTEGER);
		else
			ps.setInt(index, value);
	}

	/**
	 * Procedimento interno usado para descrever as informações do objeto.
	 * @param description descrição do objeto que será usado.
	 */

	protected void toString(ObjectDescription description)
	{
		
	}

	@Override
	public final String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		try {

			if (connection.isClosed())
				description.append("closed");
			else
				description.append("connected");

		} catch (SQLException e) {
			description.append("closed", e.getMessage());
		}

		toString(description);

		return description.toString();
	}
}

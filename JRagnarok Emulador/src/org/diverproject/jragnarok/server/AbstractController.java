package org.diverproject.jragnarok.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Controlador Abstrato</h1>
 *
 * <p>Todos os controladores existentes no emulador deverão ser herdados desta classe.
 * Irá definir como requisito básico a existência de uma conexão com o banco de dados.
 * Deverá implementar métodos internamente para executar comandos no banco de dados.</p>
 *
 * @see Connection
 *
 * @author Andrew
 */

public class AbstractController
{
	/**
	 * Referência da conexão com o banco de dados.
	 */
	private Connection connection;

	/**
	 * Cria um novo controlador sendo necessário especificar a conexão.
	 * @param connection referência da conexão com o banco de dados.
	 */

	public AbstractController(Connection connection)
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

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		try {
			description.append("closed", connection.isClosed());
		} catch (SQLException e) {
			description.append("closed", e.getMessage());
		}

		return description.toString();
	}
}

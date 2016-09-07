package org.diverproject.jragnarok.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Controlador Abstrato</h1>
 *
 * <p>Todos os controladores existentes no emulador dever�o ser herdados desta classe.
 * Ir� definir como requisito b�sico a exist�ncia de uma conex�o com o banco de dados.
 * Dever� implementar m�todos internamente para executar comandos no banco de dados.</p>
 *
 * @see Connection
 *
 * @author Andrew
 */

public class AbstractController
{
	/**
	 * Refer�ncia da conex�o com o banco de dados.
	 */
	private Connection connection;

	/**
	 * Cria um novo controlador sendo necess�rio especificar a conex�o.
	 * @param connection refer�ncia da conex�o com o banco de dados.
	 */

	public AbstractController(Connection connection)
	{
		this.connection = connection;
	}

	/**
	 * Permite obter a conex�o e ir� validar se a mesma est� definida e v�lida.
	 * @return aquisi��o do objeto contendo a conex�o com o banco de dados.
	 * @throws RagnarokException apenas se n�o for definida ou inv�lida.
	 */

	protected Connection getConnection() throws RagnarokException
	{
		if (connection == null)
			throw new RagnarokException("conex�o n�o definida");

		try {

			if (connection.isClosed())
				throw new RagnarokException("conex�o fechada");

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		return connection;
	}

	/**
	 * Prepara uma cona query para ser executada no banco de dados conforme conex�o especificada.
	 * Esse preparamento permite que a query n�o sofra qualquer tipo de SQL Injection.
	 * Como tamb�m facilita a defini��o dos par�metros e utiliza��o das consultas feitas.
	 * @param sql query contendo os comandos a serem executados no banco de dados.
	 * @return aquisi��o de um novo objeto que ir� trabalhar em cima da query especificada.
	 * @throws RagnarokException apenas se houver problemas com a conex�o.
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

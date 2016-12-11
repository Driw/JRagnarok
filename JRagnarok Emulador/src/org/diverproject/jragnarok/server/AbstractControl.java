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
 * <p>Todos os controles existentes no emulador dever�o ser herdados desta classe.
 * Ir� definir como requisito b�sico a exist�ncia de uma conex�o com o banco de dados.
 * Dever� implementar m�todos internamente para executar comandos no banco de dados.</p>
 *
 * @see Connection
 *
 * @author Andrew
 */

public abstract class AbstractControl
{
	/**
	 * C�digo do erro para: chave duplicada.
	 */
	public static final int DUPLICATED_KEY = 1062;

	/**
	 * Refer�ncia da conex�o com o banco de dados.
	 */
	private Connection connection;

	/**
	 * Cria um novo controle sendo necess�rio especificar a conex�o para persist�ncia.
	 * @param connection conex�o com o banco de dados referente a persist�ncia.
	 */

	public AbstractControl(Connection connection)
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

	/**
	 * Permite definir um valor num�rico inteiro em uma declara��o preparada em SQL.
	 * Define o valor num�rico passado ou null caso seja necess�rio conforme:
	 * @param ps declara��o preparada que ter� o valor num�rico definido.
	 * @param index n�mero do �ndice da vari�vel que ser� definida.
	 * @param value valor num�rico inteiro para ser definido na declara��o.
	 * @param nullValue valor num�rico inteiro que ser� dado como nulo.
	 * @throws SQLException falha de conex�o.
	 */

	protected void set(PreparedStatement ps, int index, int value, int nullValue) throws SQLException
	{
		if (value == nullValue)
			ps.setNull(index, Types.INTEGER);
		else
			ps.setInt(index, value);
	}

	/**
	 * Procedimento interno usado para descrever as informa��es do objeto.
	 * @param description descri��o do objeto que ser� usado.
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

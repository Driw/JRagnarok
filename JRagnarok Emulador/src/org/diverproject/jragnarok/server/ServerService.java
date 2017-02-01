package org.diverproject.jragnarok.server;

import java.sql.Connection;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.configs.CommonConfigs;
import org.diverproject.jragnarok.console.Show;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.sql.MySQL;

/**
 * <h1>Servi�o para Servidor</h1>
 *
 * <p>Classe que permite a cria��o de servi�os, servi�os s�o fronteiras internas do sistema.
 * O servidor ir� funcionar como controlador de fronteiras repassando aos servi�os chamados.
 * Os chamados ser�o reconhecidos conforme a identifica��o dos pacotes recebidos.</p>
 *
 * <p>A fun��o de um servi�o � validar a conex�o do servidor com o banco de dados.
 * Al�m disso dever� permitir obter a refer�ncia do servidor e configura��es do mesmo.
 * Alguns servi�os ir�o precisar dessas informa��es para realizar suas opera��es.</p>
 *
 * @author Andrew
 */

public abstract class ServerService
{
	/**
	 * Refer�ncia do servidor que est� criando o servi�o.
	 */
	private Server server;

	/**
	 * Cria um novo servi�o a partir de um servidor especificado.
	 * @param server refer�ncia do servidor que est� criando o servi�o.
	 */

	public ServerService(Server server)
	{
		this.server = server;
	}

	/**
	 * @return aquisi��o do servidor que det�m esse servi�o.
	 */

	protected Server getServer()
	{
		return server;
	}

	/**
	 * @return aquisi��o do sistema de temporiza��o do servidor.
	 */

	protected final TimerSystem getTimerSystem()
	{
		return server.getTimerSystem();
	}

	/**
	 * @return aquisi��o do sistema controlador de sess�es do servidor.
	 */

	protected final FileDescriptorSystem getFileDescriptorSystem()
	{
		return server.getFileDescriptorSystem();
	}

	/**
	 * @return aquisi��o do sistema que exibe as mensagens no console.
	 */

	protected final Show getShow()
	{
		return server.getShow();
	}

	/**
	 * @return aquisi��o do acesso r�pido as configura��es do servidor.
	 */

	protected abstract CommonConfigs config();

	/**
	 * Permite obter um objeto que realiza opera��es no banco de dados.
	 * @return aquisi��o da conex�o do servidor com o banco de dados.
	 * @throws RagnarokException servidor n�o definido ou conex�o inv�lida.
	 */

	protected final Connection getConnection() throws RagnarokException
	{
		if (server == null)
			throw new RagnarokException("servi�o sem servidor");

		MySQL mysql = server.getMySQL();

		if (mysql == null)
			throw new RagnarokException("sem conex�o com o banco de dados");

		Connection connection = mysql.getConnection();

		try {
			if (connection == null || connection.isClosed())
				throw new RagnarokException("conex�o fecahda");
		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		return connection;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("server", server.getThreadName());

		return description.toString();
	}
}

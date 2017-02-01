package org.diverproject.jragnarok.server;

import java.sql.Connection;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.configs.CommonConfigs;
import org.diverproject.jragnarok.console.Show;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.sql.MySQL;

/**
 * <h1>Serviço para Servidor</h1>
 *
 * <p>Classe que permite a criação de serviços, serviços são fronteiras internas do sistema.
 * O servidor irá funcionar como controlador de fronteiras repassando aos serviços chamados.
 * Os chamados serão reconhecidos conforme a identificação dos pacotes recebidos.</p>
 *
 * <p>A função de um serviço é validar a conexão do servidor com o banco de dados.
 * Além disso deverá permitir obter a referência do servidor e configurações do mesmo.
 * Alguns serviços irão precisar dessas informações para realizar suas operações.</p>
 *
 * @author Andrew
 */

public abstract class ServerService
{
	/**
	 * Referência do servidor que está criando o serviço.
	 */
	private Server server;

	/**
	 * Cria um novo serviço a partir de um servidor especificado.
	 * @param server referência do servidor que está criando o serviço.
	 */

	public ServerService(Server server)
	{
		this.server = server;
	}

	/**
	 * @return aquisição do servidor que detêm esse serviço.
	 */

	protected Server getServer()
	{
		return server;
	}

	/**
	 * @return aquisição do sistema de temporização do servidor.
	 */

	protected final TimerSystem getTimerSystem()
	{
		return server.getTimerSystem();
	}

	/**
	 * @return aquisição do sistema controlador de sessões do servidor.
	 */

	protected final FileDescriptorSystem getFileDescriptorSystem()
	{
		return server.getFileDescriptorSystem();
	}

	/**
	 * @return aquisição do sistema que exibe as mensagens no console.
	 */

	protected final Show getShow()
	{
		return server.getShow();
	}

	/**
	 * @return aquisição do acesso rápido as configurações do servidor.
	 */

	protected abstract CommonConfigs config();

	/**
	 * Permite obter um objeto que realiza operações no banco de dados.
	 * @return aquisição da conexão do servidor com o banco de dados.
	 * @throws RagnarokException servidor não definido ou conexão inválida.
	 */

	protected final Connection getConnection() throws RagnarokException
	{
		if (server == null)
			throw new RagnarokException("serviço sem servidor");

		MySQL mysql = server.getMySQL();

		if (mysql == null)
			throw new RagnarokException("sem conexão com o banco de dados");

		Connection connection = mysql.getConnection();

		try {
			if (connection == null || connection.isClosed())
				throw new RagnarokException("conexão fecahda");
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

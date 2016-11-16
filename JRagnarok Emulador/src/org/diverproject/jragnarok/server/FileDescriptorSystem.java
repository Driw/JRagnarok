package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokConstants.FD_SIZE;
import static org.diverproject.jragnarok.JRagnarokUtil.indexOn;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_EOF;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_PING;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_SERVER;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeceptionSource;
import static org.diverproject.log.LogSystem.logInfo;

import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.collection.abstraction.LoopList;

/**
 * <h1>Sistema Criador para Descritor de Arquivos</h1>
 *
 * <p>Esse sistema permite criar uma quantidade limitada de Descritores de Arquivos (<code>FD_SIZE</code>).
 * A criação desses objetos são feitos a partir de uma conexão socket e este será salvo aqui.</p>
 *
 * <p>Assim é possível manter um controle das conexões estabelecidas e atualizá-las conforme necessário.
 * Para que a atualização possa ser feita é necessário a definição de um sistema de temporização.
 * Ainda referente as atualizações é possível definir ações para serem executadas uma única vez (actions).</p>
 *
 * @see FileDescriptor
 * @see TimerSystem
 * @see LoopList
 * @see DynamicQueue
 * @see FileDescriptorAction
 *
 * @author Andrew Mello
 */

public class FileDescriptorSystem
{
	/**
	 * Lista contendo todas as conexões sockets.
	 */
	private final List<FileDescriptor> sessions = new LoopList<>(FD_SIZE);

	/**
	 * Lista contendo todas as ações únicas a se executar.
	 */
	private final Queue<FileDescriptorAction> actions = new DynamicQueue<>();

	/**
	 * Sitema de temporização do servidor.
	 */
	private TimerSystem timerSystem;

	/**
	 * Cria um novo sistema para criação de Descritores de Arquivos a partir de sockets.
	 * Deve ser definido um sistema de temporização para que ele possa ser atualizado.
	 * @param timerSystem referência do sistema de temporização que será usado.
	 */

	public FileDescriptorSystem(TimerSystem timerSystem)
	{
		if (timerSystem == null)
			throw new RagnarokRuntimeException("sistema de temporização nulo");

		this.timerSystem = timerSystem;
	}

	/**
	 * Cria um novo Descritor de Arquivo que irá permitir trabalhar com uma conexão socket.
	 * @param socket referência da conexão socket que foi estabelecida com o cliente.
	 * @return aquisição de uma novo Descritor de Arquivo a partir do socket definido.
	 */

	public FileDescriptor newFileDecriptor(Socket socket)
	{
		synchronized (sessions)
		{
			FileDescriptor fd = new FileDescriptor(socket);

			if (!sessions.add(fd))
			{
				fd.close();

				return null;
			}

			fd.id = indexOn(sessions, fd);
			fd.system = this;

			return fd;
		}
	}

	/**
	 * @return aquisição do sistema para temporização dos descritores de arquivo.
	 */

	TimerSystem getTimerSystem()
	{
		return timerSystem;
	}

	/**
	 * Procedimento estático usado para atualizar todos os Arquivos Descritores.
	 * Deverá garantir que todas conexões sejam processadas igualmente.
	 * @param now tempo atual que ocorre a atualização em milissegundos.
	 * @param tick milissegundos passados desde a última atualização.
	 */

	public void update(int now, int tick)
	{
		executeActions();

		for (int i = 0; i < sessions.size(); i++)
		{
			FileDescriptor fd = sessions.get(i);

			if (fd.getFlag().is(FLAG_EOF) || !fd.isConnected())
			{
				if (fd.isConnected())
					fd.close();

				logInfo("sessão #%d fechada e removida (ip: %s).\n", fd.getID(), fd.getAddressString());

				sessions.remove(i);
			}

			else if (fd.getTimeout() > 0 && (fd.getTimeout() - now) <= 0)
			{
				if (fd.getFlag().is(FLAG_SERVER) && fd.getFlag().is(FLAG_PING))
					fd.getFlag().unset(FLAG_PING);

				else
				{
					logInfo("sessão #%d encerrada por ociosidade (ip: %s).\n", fd.getID(), fd.getAddressString());
					setEndOfFile(fd);
				}
			}

			else
				executeListener(fd);
		}
	}

	/**
	 * Procedimento chamado quando solicitado para atualizar os temporizadores.
	 * Deverá executar todas as ações em file que foram adicionadas no último loop.
	 * Uma vez que a ação tenha sido executada ela terá sido removido da fila.
	 */

	private void executeActions()
	{
		while (!actions.isEmpty())
		{
			FileDescriptorAction action = actions.poll();

			for (FileDescriptor fd : sessions)
				action.execute(fd);
		}
	}

	/**
	 * Procedimento chamado quando solicitado para atualizar os temporizadores.
	 * Deverá garantir que o descritor de arquivo chame o seu listener de análise.
	 * @param fd referência do arquivo descritor que será analisado os dados recebidos.
	 */

	private void executeListener(FileDescriptor fd)
	{
		try {

			if (fd.getParseListener() != null)
				if (fd.hasData() && !fd.getParseListener().onCall(fd))
					setEndOfFile(fd);

		} catch (RagnarokException e) {

			logError("processamento inválido encontrado:\n");
			logExeceptionSource(e);

			setEndOfFile(fd);

		} catch (RagnarokRuntimeException e) {

			logError("informação inválida encontrada:\n");
			logExeceptionSource(e);

			setEndOfFile(fd);

		} catch (Exception e) {

			logError("erro inesperado ocorrido:\n");
			logExeceptionSource(e);

			setEndOfFile(fd);

		}
	}

	/**
	 * Define o fim dos dados para um descritor de arquivo, assim ele será fechado forçadamente.
	 * @param fd referência do arquivo descritor do qual será terminado.
	 */

	private void setEndOfFile(FileDescriptor fd)
	{
		fd.getFlag().set(FLAG_EOF);
	}

	/**
	 * Executa uma ação para ser processada por todos os descritores de arquivos.
	 * A ação será executada no primeiro update que for executado pós adição.
	 * @param action referência do objeto que contém a ação a ser executada.
	 */

	public void execute(FileDescriptorAction action)
	{
		actions.offer(action);
	}

	/**
	 * Permite obter uma determinada sessão estabelecida conforme sua identificação.
	 * @param id código de identificação da sessão do qual deseja.
	 * @return aquisição do descritor de arquivo da sessão especificada.
	 */

	public FileDescriptor get(int id)
	{
		return sessions.get(id);
	}

	/**
	 * Verifica se uma determinada conexão foi estabelecida e se está conectada.
	 * @param id código de identificação único da conexão desejada.
	 * @return true se estiver conectado ou false caso contrário.
	 */

	public boolean isAlive(int id)
	{
		return sessions.get(id) != null && sessions.get(id).isConnected();
	}

	/**
	 * @return aquisição da quantidade de sessões existentes no momento.
	 */

	public int size()
	{
		return sessions.size();
	}

	/**
	 * Fecha forçadamente todas as conexões existentes com o servidor sem log.
	 * Em seguida irá limpar a lista que que contém a conexão dessas sessões.
	 * Também limpa a fila que contém as ações a serem executadas.
	 */

	public void destroy()
	{
		sessions.clear();
		actions.clear();
	}
}

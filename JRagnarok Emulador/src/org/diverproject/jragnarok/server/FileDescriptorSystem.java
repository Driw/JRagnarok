
package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.indexOn;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_EOF;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_PING;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_SERVER;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeceptionSource;
import static org.diverproject.log.LogSystem.logInfo;

import java.util.Iterator;

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

public class FileDescriptorSystem implements Iterable<FileDescriptor>
{
	/**
	 * Quantidade limite de conexões por servidor.
	 */
	public static final int FD_SIZE = 4096;


	/**
	 * Lista contendo todas as conexões sockets.
	 */
	private final List<FileDescriptor> sessions = new LoopList<>(FD_SIZE);

	/**
	 * Lista contendo todas as ações únicas a se executar.
	 */
	private final Queue<FileDescriptorAction> actions = new DynamicQueue<>();

	/**
	 * Cria um novo sistema para criação de Descritores de Arquivos a partir de sockets. Deve ser definido um sistema de temporização para que ele possa ser atualizado.
	 * @param timerSystem  referência do sistema de temporização que será usado.
	 */

	public FileDescriptorSystem(TimerSystem timerSystem)
	{
		if (timerSystem == null)
			throw new RagnarokRuntimeException("sistema de temporização nulo");
	}

	/**
	 * Adiciona um Descritor de Arquivo ao sistema para que possa ser atualizado.
	 * @param fd descritor de arquivo que será adicionado ao sistema.
	 * @return true se conseguir adicionar ou false se já estiver cheio.
	 */

	public boolean addFileDecriptor(FileDescriptor fd)
	{
		synchronized (sessions)
		{
			if (sessions.isFull() || !sessions.add(fd))
			{
				fd.close();
				return false;
			}

			fd.id = indexOn(sessions, fd) + 1;

			return true;
		}
	}

	/**
	 * Procedimento estático usado para atualizar todos os Arquivos Descritores. Deverá garantir que todas conexões sejam processadas igualmente.
	 * @param now tempo atual que ocorre a atualização em milissegundos.
	 * @param tick milissegundos passados desde a última atualização.
	 */

	public void update(int now, int tick)
	{
		executeActions();

		for (int i = 0; i < sessions.size(); i++)
		{
			FileDescriptor fd = sessions.get(i);
			fd.lastTickUpdate = now;

			if (fd.getFlag().is(FLAG_EOF))
			{
				if (!fd.isConnected())
					fd.close();

				try {
					fd.getCloseListener().onCall(fd);
				} catch (RagnarokException e) {
					logError("falha ao encerrar conexão (fd: %d).\n", fd.getID());
					logExeceptionSource(e);
				}

				if (fd.getFlag().is(FLAG_SERVER))
					logInfo("sessão server#%d fechada e removida (ip: %s).\n", fd.getID(), fd.getAddressString());
				else
					logInfo("sessão client#%d fechada e removida (ip: %s).\n", fd.getID(), fd.getAddressString());

				sessions.remove(i);
			}

			else if (fd.getTimeout() > 0 && (fd.getTimeout() - now) <= 0)
			{
				if (fd.getFlag().is(FLAG_SERVER) && fd.getFlag().is(FLAG_PING))
					fd.getFlag().unset(FLAG_PING);

				else if (!fd.getFlag().is(FLAG_SERVER))
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
	 * Procedimento chamado quando solicitado para atualizar os temporizadores. Deverá executar todas as ações em file que foram adicionadas no último loop. Uma vez que a ação tenha sido executada ela terá sido removido da fila.
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
	 * Procedimento chamado quando solicitado para atualizar os temporizadores. Deverá garantir que o descritor de arquivo chame o seu listener de análise.
	 * @param fd referência do arquivo descritor que será analisado os dados recebidos.
	 */

	private void executeListener(FileDescriptor fd)
	{
		try
		{

			if (fd.getParseListener() != null)
				if (fd.hasData() && !fd.getParseListener().onCall(fd))
					setEndOfFile(fd);

		} catch (RagnarokException e) {
			setExceptionsEOF(fd, e, "processamento inválido encontrado");
		} catch (RagnarokRuntimeException e) {
			setExceptionsEOF(fd, e, "informação inválida encontrada");
		} catch (Exception e) {
			setExceptionsEOF(fd, e, "erro inesperado ocorrido");
		}
	}

	public static void setExceptionsEOF(FileDescriptor fd, Exception e, String message)
	{
		logError(message+ ":\n");
		logExeceptionSource(e);

		e.printStackTrace();

		if (!fd.getFlag().is(FLAG_SERVER))
			setEndOfFile(fd);
	}

	/**
	 * Executa uma ação para ser processada por todos os descritores de arquivos. A ação será executada no primeiro update que for executado pós adição.
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
		return sessions.get(id - 1);
	}

	/**
	 * Verifica se uma determinada conexão foi estabelecida e se está conectada.
	 * @param id código de identificação único da conexão desejada.
	 * @return true se estiver conectado ou false caso contrário.
	 */

	public boolean isAlive(int id)
	{
		return sessions.get(id - 1) != null && sessions.get(id - 1).isConnected();
	}

	/**
	 * @return aquisição da quantidade de sessões existentes no momento.
	 */

	public int size()
	{
		return sessions.size();
	}

	/**
	 * Fecha forçadamente todas as conexões existentes com o servidor sem log. Em seguida irá limpar a lista que que contém a conexão dessas sessões. Também limpa a fila que contém as ações a serem executadas.
	 */

	public void destroy()
	{
		sessions.clear();
		actions.clear();
	}

	/**
	 * Define o fim dos dados para um descritor de arquivo, assim ele será fechado forçadamente.
	 * @param fd referência do arquivo descritor do qual será terminado.
	 */

	public static void setEndOfFile(FileDescriptor fd)
	{
		fd.getFlag().set(FLAG_EOF);
	}

	@Override
	public Iterator<FileDescriptor> iterator()
	{
		return sessions.iterator();
	}
}

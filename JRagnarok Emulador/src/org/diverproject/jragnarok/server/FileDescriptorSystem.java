
package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.thread;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_EOF;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_PING;
import static org.diverproject.jragnarok.server.FileDescriptor.FLAG_SERVER;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeceptionSource;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.util.Util.indexOn;

import java.util.Iterator;

import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.jragnarok.RagnarokRuntimeException;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.collection.abstraction.LoopList;

/**
 * <h1>Sistema Criador para Descritor de Arquivos</h1>
 *
 * <p>Esse sistema permite criar uma quantidade limitada de Descritores de Arquivos (<code>FD_SIZE</code>).
 * A cria��o desses objetos s�o feitos a partir de uma conex�o socket e este ser� salvo aqui.</p>
 *
 * <p>Assim � poss�vel manter um controle das conex�es estabelecidas e atualiz�-las conforme necess�rio.
 * Para que a atualiza��o possa ser feita � necess�rio a defini��o de um sistema de temporiza��o.
 * Ainda referente as atualiza��es � poss�vel definir a��es para serem executadas uma �nica vez (actions).</p>
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
	 * Quantidade limite de conex�es por servidor.
	 */
	public static final int FD_SIZE = 4096;


	/**
	 * Lista contendo todas as conex�es sockets.
	 */
	private final List<FileDescriptor> sessions = new LoopList<>(FD_SIZE);

	/**
	 * Lista contendo todas as a��es �nicas a se executar.
	 */
	private final Queue<FileDescriptorAction> actions = new DynamicQueue<>();

	/**
	 * Cria um novo sistema para cria��o de Descritores de Arquivos a partir de sockets. Deve ser definido um sistema de temporiza��o para que ele possa ser atualizado.
	 * @param timerSystem  refer�ncia do sistema de temporiza��o que ser� usado.
	 */

	public FileDescriptorSystem(TimerSystem timerSystem)
	{
		if (timerSystem == null)
			throw new RagnarokRuntimeException("sistema de temporiza��o nulo");
	}

	/**
	 * Adiciona um Descritor de Arquivo ao sistema para que possa ser atualizado.
	 * @param fd descritor de arquivo que ser� adicionado ao sistema.
	 * @return true se conseguir adicionar ou false se j� estiver cheio.
	 */

	public boolean addFileDecriptor(FileDescriptor fd)
	{
		synchronized (sessions)
		{
			if (sessions.isFull() || !sessions.add(fd))
			{
				fd.closeSocket();
				return false;
			}

			fd.id = indexOn(sessions, fd) + 1;

			return true;
		}
	}

	/**
	 * Procedimento est�tico usado para atualizar todos os Arquivos Descritores. Dever� garantir que todas conex�es sejam processadas igualmente.
	 * @param now tempo atual que ocorre a atualiza��o em milissegundos.
	 * @param tick milissegundos passados desde a �ltima atualiza��o.
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
				try {

					if (fd.getCloseListener() != null)
					{
						fd.getCloseListener().onCall(fd);
						fd.setCloseListener(null);
					}

					if (fd.isConnected())
						fd.closeSocket();

				} catch (RagnarokException e) {
					logError("falha ao encerrar conex�o (fd: %d, thread: %s).\n", fd.getID(), thread());
					logExeceptionSource(e);
				}

				if (fd.getFlag().is(FLAG_SERVER))
					logInfo("sess�o server#%d fechada e removida (ip: %s, thread: %s).\n", fd.getID(), fd.getAddressString(), thread());
				else
					logInfo("sess�o client#%d fechada e removida (ip: %s, thread: %s).\n", fd.getID(), fd.getAddressString(), thread());

				sessions.remove(i);
			}

			else if (fd.getTimeout() > 0 && (fd.getTimeout() - now) <= 0)
			{
				if (fd.getFlag().is(FLAG_SERVER) && fd.getFlag().is(FLAG_PING))
					fd.getFlag().unset(FLAG_PING);

				else if (!fd.getFlag().is(FLAG_SERVER))
				{
					logInfo("sess�o client#%d econtrou-se ociosa no sistema (ip: %s, thread: %s).\n", fd.getID(), fd.getAddressString(), thread());
					setEndOfFile(fd);
				}
			}

			else
				executeListener(fd);
		}
	}

	/**
	 * Procedimento chamado quando solicitado para atualizar os temporizadores. Dever� executar todas as a��es em file que foram adicionadas no �ltimo loop. Uma vez que a a��o tenha sido executada ela ter� sido removido da fila.
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
	 * Procedimento chamado quando solicitado para atualizar os temporizadores. Dever� garantir que o descritor de arquivo chame o seu listener de an�lise.
	 * @param fd refer�ncia do arquivo descritor que ser� analisado os dados recebidos.
	 */

	private void executeListener(FileDescriptor fd)
	{
		try
		{

			if (fd.getParseListener() != null)
				if (fd.hasData() && !fd.getParseListener().onCall(fd))
					setEndOfFile(fd);

		} catch (RagnarokException e) {
			setExceptionsEOF(fd, e, "processamento inv�lido encontrado");
		} catch (RagnarokRuntimeException e) {
			setExceptionsEOF(fd, e, "informa��o inv�lida encontrada");
		} catch (Exception e) {
			setExceptionsEOF(fd, e, "erro inesperado ocorrido");
		}
	}

	public static void setExceptionsEOF(FileDescriptor fd, Exception e, String message)
	{
		logError(message+ " {%s}:\n", thread());
		logExeceptionSource(e);

		e.printStackTrace();

		if (!fd.getFlag().is(FLAG_SERVER))
			setEndOfFile(fd);
	}

	/**
	 * Executa uma a��o para ser processada por todos os descritores de arquivos. A a��o ser� executada no primeiro update que for executado p�s adi��o.
	 * @param action refer�ncia do objeto que cont�m a a��o a ser executada.
	 */

	public void execute(FileDescriptorAction action)
	{
		actions.offer(action);
	}

	/**
	 * Permite obter uma determinada sess�o estabelecida conforme sua identifica��o.
	 * @param id c�digo de identifica��o da sess�o do qual deseja.
	 * @return aquisi��o do descritor de arquivo da sess�o especificada.
	 */

	public FileDescriptor get(int id)
	{
		return sessions.get(id - 1);
	}

	/**
	 * Verifica se uma determinada conex�o foi estabelecida e se est� conectada.
	 * @param id c�digo de identifica��o �nico da conex�o desejada.
	 * @return true se estiver conectado ou false caso contr�rio.
	 */

	public boolean isAlive(int id)
	{
		return sessions.get(id - 1) != null && sessions.get(id - 1).isConnected();
	}

	/**
	 * @return aquisi��o da quantidade de sess�es existentes no momento.
	 */

	public int size()
	{
		return sessions.size();
	}

	/**
	 * Fecha for�adamente todas as conex�es existentes com o servidor sem log. Em seguida ir� limpar a lista que que cont�m a conex�o dessas sess�es. Tamb�m limpa a fila que cont�m as a��es a serem executadas.
	 */

	public void destroy()
	{
		sessions.clear();
		actions.clear();
	}

	/**
	 * Define o fim dos dados para um descritor de arquivo, assim ele ser� fechado for�adamente.
	 * @param fd refer�ncia do arquivo descritor do qual ser� terminado.
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

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

public class FileDescriptorSystem
{
	/**
	 * Lista contendo todas as conex�es sockets.
	 */
	private final List<FileDescriptor> sessions = new LoopList<>(FD_SIZE);

	/**
	 * Lista contendo todas as a��es �nicas a se executar.
	 */
	private final Queue<FileDescriptorAction> actions = new DynamicQueue<>();

	/**
	 * Sitema de temporiza��o do servidor.
	 */
	private TimerSystem timerSystem;

	/**
	 * Cria um novo sistema para cria��o de Descritores de Arquivos a partir de sockets.
	 * Deve ser definido um sistema de temporiza��o para que ele possa ser atualizado.
	 * @param timerSystem refer�ncia do sistema de temporiza��o que ser� usado.
	 */

	public FileDescriptorSystem(TimerSystem timerSystem)
	{
		if (timerSystem == null)
			throw new RagnarokRuntimeException("sistema de temporiza��o nulo");

		this.timerSystem = timerSystem;
	}

	/**
	 * Cria um novo Descritor de Arquivo que ir� permitir trabalhar com uma conex�o socket.
	 * @param socket refer�ncia da conex�o socket que foi estabelecida com o cliente.
	 * @return aquisi��o de uma novo Descritor de Arquivo a partir do socket definido.
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
	 * @return aquisi��o do sistema para temporiza��o dos descritores de arquivo.
	 */

	TimerSystem getTimerSystem()
	{
		return timerSystem;
	}

	/**
	 * Procedimento est�tico usado para atualizar todos os Arquivos Descritores.
	 * Dever� garantir que todas conex�es sejam processadas igualmente.
	 * @param next milissegundos para expirar o pr�ximo temporizador.
	 */

	public void update(int next)
	{
		int lastTick = timerSystem.getLastTickCount();

		executeActions();

		for (int i = 0; i < sessions.size(); i++)
		{
			FileDescriptor fd = sessions.get(i);

			if (fd.getTimeout() > 0 && (fd.getTimeout() - lastTick) < 0)
			{
				if (fd.getFlag().is(FLAG_SERVER) && fd.getFlag().is(FLAG_PING))
					fd.getFlag().unset(FLAG_PING);

				else
				{
					logInfo("sess�o #%d terminada por ociosidade (ip: %s).\n", fd.getID(), fd.getAddressString());
					setEndOfFile(fd);
				}
			}

			executeListener(fd, lastTick);

			if (!fd.isConnected())
			{
				logInfo("sess�o encerrada (ip: %s).\n", fd.getAddressString());

				fd.close();
				sessions.remove(i);
			}
		}
	}

	/**
	 * Procedimento chamado quando solicitado para atualizar os temporizadores.
	 * Dever� executar todas as a��es em file que foram adicionadas no �ltimo loop.
	 * Uma vez que a a��o tenha sido executada ela ter� sido removido da fila.
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
	 * Dever� garantir que o descritor de arquivo chame o seu listener de an�lise.
	 * @param fd refer�ncia do arquivo descritor que ser� analisado os dados recebidos.
	 * @param lastTick tempo atual referente a �ltima atualiza��o (loop) do servidor.
	 */

	private void executeListener(FileDescriptor fd, int lastTick)
	{
		try {

			if (fd.getParseListener() != null)
				if (!fd.getParseListener().onCall(fd))
					setEndOfFile(fd);

		} catch (RagnarokException e) {

			logError("processamento inv�lido encontrado:\n");
			logExeceptionSource(e);

			setEndOfFile(fd);

		} catch (RagnarokRuntimeException e) {

			logError("informa��o inv�lida encontrada:\n");
			logExeceptionSource(e);

			setEndOfFile(fd);

		} catch (Exception e) {

			logError("erro inesperado ocorrido:\n");
			logExeceptionSource(e);

			setEndOfFile(fd);

		}
	}

	/**
	 * Define o fim dos dados para um descritor de arquivo, assim ele ser� fechado for�adamente.
	 * @param fd refer�ncia do arquivo descritor do qual ser� terminado.
	 */

	private void setEndOfFile(FileDescriptor fd)
	{
		fd.getFlag().set(FLAG_EOF);
	}

	/**
	 * Executa uma a��o para ser processada por todos os descritores de arquivos.
	 * A a��o ser� executada no primeiro update que for executado p�s adi��o.
	 * @param action refer�ncia do objeto que cont�m a a��o a ser executada.
	 */

	public void execute(FileDescriptorAction action)
	{
		actions.offer(action);
	}

	/**
	 * @return aquisi��o da quantidade de sess�es existentes no momento.
	 */

	public int size()
	{
		return sessions.size();
	}
}
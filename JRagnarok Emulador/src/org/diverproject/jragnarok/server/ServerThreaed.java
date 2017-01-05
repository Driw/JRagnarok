package org.diverproject.jragnarok.server;

import org.diverproject.jragnarok.console.ShowThread;

/**
 * <h1>Thread para Servidor</h1>
 *
 * <p>Essa thread está sendo criada para que seja possível ao sistema de console saber qual Show utilizar.
 * A thread irá carregar com sigo um referência de um servidor que é o que o está utilizando,
 * assim quando uma mensagem de log for recebida no sistema poderá repassar ao Show contido no servidor.</p>
 *
 * @see Thread
 * @see ShowThread
 * @see Server
 *
 * @author Andrew
 */

public class ServerThreaed extends Thread
{
	/**
	 * Sistema para exibição de mensagens no console por thread.
	 */
	private ShowThread show;

	/**
	 * Servidor que está utilizando esta thread.
	 */
	private Server server;

	/**
	 * Cria uma nova instância para realizar processamento em paralelo através de Thread.
	 * Esta thread deve ser utilizada apenas por um servidor afim de anexar informações a thread.
	 * @param server referência do servidor que está solicitando a instância desta thread.
	 * @param runnable interface que será executada no momento em que a thread for executada.
	 */

	public ServerThreaed(Server server, Runnable runnable)
	{
		super(runnable);

		this.server = server;
	}

	/**
	 * @return aquisição do sistema para exibição de mensagens em console.
	 */

	public ShowThread getShowThread()
	{
		return show;
	}

	/**
	 * @param show sistema para exibição de mensagens em console.
	 */

	public void setShowThread(ShowThread show)
	{
		this.show = show;
	}

	/**
	 * @return aquisição do servidor que está sendo utilizado por esta thread.
	 */

	public Server getServer()
	{
		return server;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void destroy()
	{
		super.destroy();

		this.show = null;
		this.server = null;
	}

	@Override
	public String toString()
	{
		return server.getThreadName();
	}
}

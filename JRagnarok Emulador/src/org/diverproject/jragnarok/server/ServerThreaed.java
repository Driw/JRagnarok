package org.diverproject.jragnarok.server;

import org.diverproject.jragnarok.console.ShowThread;

/**
 * <h1>Thread para Servidor</h1>
 *
 * <p>Essa thread est� sendo criada para que seja poss�vel ao sistema de console saber qual Show utilizar.
 * A thread ir� carregar com sigo um refer�ncia de um servidor que � o que o est� utilizando,
 * assim quando uma mensagem de log for recebida no sistema poder� repassar ao Show contido no servidor.</p>
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
	 * Sistema para exibi��o de mensagens no console por thread.
	 */
	private ShowThread show;

	/**
	 * Servidor que est� utilizando esta thread.
	 */
	private Server server;

	/**
	 * Cria uma nova inst�ncia para realizar processamento em paralelo atrav�s de Thread.
	 * Esta thread deve ser utilizada apenas por um servidor afim de anexar informa��es a thread.
	 * @param server refer�ncia do servidor que est� solicitando a inst�ncia desta thread.
	 * @param runnable interface que ser� executada no momento em que a thread for executada.
	 */

	public ServerThreaed(Server server, Runnable runnable)
	{
		super(runnable);

		this.server = server;
	}

	/**
	 * @return aquisi��o do sistema para exibi��o de mensagens em console.
	 */

	public ShowThread getShowThread()
	{
		return show;
	}

	/**
	 * @param show sistema para exibi��o de mensagens em console.
	 */

	public void setShowThread(ShowThread show)
	{
		this.show = show;
	}

	/**
	 * @return aquisi��o do servidor que est� sendo utilizado por esta thread.
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

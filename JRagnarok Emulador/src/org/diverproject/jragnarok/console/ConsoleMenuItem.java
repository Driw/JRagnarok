package org.diverproject.jragnarok.console;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import org.diverproject.jragnarok.server.Server;

/**
 * <h1>Item de Menu para Console</h1>
 *
 * <p>Essa classe permite especificar um JMenuItem para que seja vinculado um servidor ao mesmo.
 * Al�m de vincular o servidor ao item de menu permite realizar a��es especificas quando clicado.
 * A a��o corresponde em trocar o painel de console que est� sendo usado no console para o do servidor.</p>
 *
 * @see JMenuItem
 * @see Server
 * @see JRagnarokConsole
 *
 * @author Andrew
 *
 * @param <E> tipo de servidor do qual ser� vinculado ao item de menu.
 */

@SuppressWarnings("serial")
public class ConsoleMenuItem<E extends Server> extends JMenuItem
{
	/**
	 * Servidor do qual o item de menu corresponde.
	 */
	private Server server;

	/**
	 * A��o do qual ser� executada quando o item de menu for clicado.
	 */
	private Action action;

	/**
	 * Define um servidor para ser acionado sempre que este item de menu for selecionado.
	 * Ir� criar um ActionListener que ir� realizar a troca do painel para console usado.
	 * @param console refer�ncia do console do qual ter� sofrer� a a��o realizada.
	 * @param server refer�ncia do servidor que corresponde ao item de menu.
	 */

	public void setServer(JRagnarokConsole console, E server)
	{
		this.server = server;
		this.setText(format("[%d] %s", server.getID(), server.getThreadName()));

		if (action == null)
		{
			addActionListener((action = new AbstractAction()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (e.getID() == ActionEvent.ACTION_PERFORMED)
						console.setConsolePanel(server.getShow().getConsolePanel());
				}
			}));
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Server)
			return server.equals(obj);

		if (obj instanceof ConsoleMenuItem)
			return super.equals(obj);

		return false;
	}
}

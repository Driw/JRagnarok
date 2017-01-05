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
 * Além de vincular o servidor ao item de menu permite realizar ações especificas quando clicado.
 * A ação corresponde em trocar o painel de console que está sendo usado no console para o do servidor.</p>
 *
 * @see JMenuItem
 * @see Server
 * @see JRagnarokConsole
 *
 * @author Andrew
 *
 * @param <E> tipo de servidor do qual será vinculado ao item de menu.
 */

@SuppressWarnings("serial")
public class ConsoleMenuItem<E extends Server> extends JMenuItem
{
	/**
	 * Servidor do qual o item de menu corresponde.
	 */
	private Server server;

	/**
	 * Ação do qual será executada quando o item de menu for clicado.
	 */
	private Action action;

	/**
	 * Define um servidor para ser acionado sempre que este item de menu for selecionado.
	 * Irá criar um ActionListener que irá realizar a troca do painel para console usado.
	 * @param console referência do console do qual terá sofrerá a ação realizada.
	 * @param server referência do servidor que corresponde ao item de menu.
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

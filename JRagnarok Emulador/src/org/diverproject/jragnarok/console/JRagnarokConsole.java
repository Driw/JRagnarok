package org.diverproject.jragnarok.console;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.diverproject.console.Console;
import org.diverproject.jragnarok.server.ServerControl;
import org.diverproject.jragnarok.server.character.CharServer;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.map.MapServer;

/**
 * <h1>Console</h1>
 *
 * <p>A utilização do console permite exibir uma janela que possui um campo de texto e área de texto.
 * Utiliza-se ainda do padrão de projetos Singleton que determina a possibilidade uma única instância.</p>
 *
 * <p>O campo de texto irá permitir executar comandos que devem ser especificado pelo desenvolvedor.
 * Os comandos podem ser interpretados de diversas formas, onde todo o conteúdo digitado no campo
 * pode ser obtido através da utilização da escuta do console, que irá repassar o texto a um método.</p>
 *
 * <p>A área de texto pode ser usada através de interface de ações do console na escuta para console.
 * Essas ações irá permitir que nessa área de texto seja exibido mensagens relativas aos comandos.
 * podendo ainda limpar o mesmo ou definir cores para tornar as mensagens mais dinâmicas e nítidas.</p>
 *
 * @see ConsoleActions
 * @see ConsoleListener
 *
 * @author Andrew
 */

@SuppressWarnings("serial")
public class JRagnarokConsole extends Console
{
	/**
	 * Barra de menu que irá conter as operações permitidas a serem realizadas.
	 */
	private JMenuBar menuBar;

	/**
	 * Menu principal que irá permitir realizar operações gerais como gerenciamento de servidores.
	 */
	private JMenu mMain;

	/**
	 * Item do menu principal que irá chamar o painel para console genérico.
	 */
	private JMenuItem iDefaultConsole;

	/**
	 * Menu que irá listar os servidores de acesso disponíveis.
	 */
	private JMenu mLoginServers;

	/**
	 * Menu que irá listar os servidores de personagem disponíveis.
	 */
	private JMenu mCharServers;

	/**
	 * Menu que irá listar os servidores de mapa disponíveis.
	 */
	private JMenu mMapServers;

	/**
	 * Procedimento interno chamado pelo construtor que garante a existência do console.
	 * Inicializa a janela definindo algumas preferências de utilização para o console.
	 * Como também cria o painel de texto, campo de texto painel de rolagem e afins.
	 */

	protected void initConsole()
	{
		super.initConsole();

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				ServerControl.getInstance().destroyAll(false);
				System.exit(0);
			}
		});
	}

	@Override
	protected JMenuBar createMenuBar()
	{
		menuBar = new JMenuBar();

		mMain = new JMenu("Inicio");
		mMain.setMnemonic('I');
		menuBar.add(mMain);

		mLoginServers = new JMenu("Servidores de Acesso");
		mLoginServers.setMnemonic('A');
		menuBar.add(mLoginServers);

		mCharServers = new JMenu("Servidores de Personagem");
		mCharServers.setMnemonic('P');
		menuBar.add(mCharServers);

		mMapServers = new JMenu("Servidores de Map");
		mMapServers.setMnemonic('M');
		menuBar.add(mMapServers);

		setDefaultConsole();

		return menuBar;
	}

	/**
	 * Define a utilização do painel para console padrão (genérico) para ser utilizado.
	 * Chamado no momento em que a barra de menu for solicitada para ser criada.
	 */

	private void setDefaultConsole()
	{
		if (iDefaultConsole == null)
		{
			iDefaultConsole = new JMenuItem("Console Geral");
			iDefaultConsole.setMnemonic('C');
			iDefaultConsole.addActionListener(new AbstractAction()
			{
				private ShowThread show = ShowThread.getInstance();

				@Override
				public void actionPerformed(ActionEvent e)
				{
					JRagnarokConsole.this.setConsolePanel(show.getConsolePanel());
					JRagnarokConsole.this.repaint();
				}
			});
			mMain.add(iDefaultConsole);
		}

		setConsolePanel(ShowThread.getInstance().getConsolePanel());
	}

	/**
	 * Adiciona um determinado servidor de acesso para ser registrado ao menu que lista o mesmo.
	 * @param server referência do servidor do qual deseja registrar ao console.
	 * @return true se conseguir adicionar ou false se o servidor for inválido ou estiver adicionado.
	 */

	public boolean addLoginServer(LoginServer server)
	{
		if (server.getID() != 0)
		{
			for (Component component : mLoginServers.getComponents())
				if (component instanceof ConsoleMenuItem)
					if (component.equals(server))
						return false;

			ConsoleMenuItem<LoginServer> item = new ConsoleMenuItem<>();
			item.setServer(this, server);
			mLoginServers.add(item);
			repaint();

			return true;
		}

		return false;
	}

	/**
	 * Adiciona um determinado servidor de personagem para ser registrado ao menu que lista o mesmo.
	 * @param server referência do servidor do qual deseja registrar ao console.
	 * @return true se conseguir adicionar ou false se o servidor for inválido ou estiver adicionado.
	 */

	public boolean addCharServer(CharServer server)
	{
		if (server.getID() != 0)
		{
			for (Component component : mLoginServers.getComponents())
				if (component instanceof ConsoleMenuItem)
					if (component.equals(server))
						return false;

			ConsoleMenuItem<CharServer> item = new ConsoleMenuItem<>();
			item.setServer(this, server);
			mCharServers.add(item);
			repaint();

			return true;
		}

		return false;
	}

	/**
	 * Adiciona um determinado servidor de mapa para ser registrado ao menu que lista o mesmo.
	 * @param server referência do servidor do qual deseja registrar ao console.
	 * @return true se conseguir adicionar ou false se o servidor for inválido ou estiver adicionado.
	 */

	public boolean addMapServer(MapServer server)
	{
		if (server.getID() != 0)
		{
			for (Component component : mLoginServers.getComponents())
				if (component instanceof ConsoleMenuItem)
					if (component.equals(server))
						return false;

			ConsoleMenuItem<MapServer> item = new ConsoleMenuItem<>();
			item.setServer(this, server);
			mMapServers.add(item);
			repaint();

			return true;
		}

		return false;
	}
}

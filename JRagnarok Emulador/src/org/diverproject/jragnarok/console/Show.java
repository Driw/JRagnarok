package org.diverproject.jragnarok.console;

import org.diverproject.console.ConsolePanel;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Exibição</h1>
 *
 * <p>Classe designada para implementar as formas de exibição das mensagens registradas nos servidores (log).
 * Através dela será possível repassar as mensagens em log à um painel para console que irá exibir a mensagem.
 * Assim será possível que cada servidor possua uma única exibição que será respectivo ao painel para console.</p>
 *
 * @see ConsolePanel
 * @see MessageType
 *
 * @author Andrew
 */

public abstract class Show
{
	/**
	 * Painel para console que irá ser utilizado.
	 */
	private ConsolePanel consolePanel;

	/**
	 * Cria uma nova instância para exibição de mensagens log do sistema e irá instanciar o painel para console.
	 */

	public Show()
	{
		consolePanel = new ConsolePanel();
	}

	/**
	 * A inicialização da exibição consistem em limpar todas as informações contidas no painel de console.
	 * É chamado para limpar sempre que o servidor é iniciado, evitando que informações indesejadas apareçam.
	 */

	public void init()
	{
		consolePanel.clear();
	}

	/**
	 * @return aquisição da instância do painel para console utilizado por essa exibição.
	 */

	public ConsolePanel getConsolePanel()
	{
		return consolePanel;
	}

	/**
	 * Procedimento que deve ser implementado pela classe mais especializada na exibição das mensagens log.
	 * Deverá especificar exatamente como e onde as mensagens deverão ser exibidas no console.
	 * @param throwable objeto que contém as informações referente ao momento do log.
	 * @param type tipo de mensagem que está sendo exibida ou null se não houver nenhum.
	 * @param format formado da mensagem do qual a exibição deverá mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public abstract void log(Throwable throwable, MessageType type, String format, Object... args);

	/**
	 * Registra uma nova mensagem para entrar em exibição no console do tipo <b>Debug</b> conforme:
	 * @param format formado da mensagem do qual a exibição deverá mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logDebug(String format, Object... args)
	{
		log(new Throwable(), MessageType.DEBUG, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibição no console do tipo <b>Info</b> conforme:
	 * @param format formado da mensagem do qual a exibição deverá mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logInfo(String format, Object... args)
	{
		log(new Throwable(), MessageType.INFO, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibição no console do tipo <b>Notice</b> conforme:
	 * @param format formado da mensagem do qual a exibição deverá mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logNotice(String format, Object... args)
	{
		log(new Throwable(), MessageType.NOTICE, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibição no console do tipo <b>Warning</b> conforme:
	 * @param format formado da mensagem do qual a exibição deverá mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logWarning(String format, Object... args)
	{
		log(new Throwable(), MessageType.WARNING, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibição no console do tipo <b>Error</b> conforme:
	 * @param format formado da mensagem do qual a exibição deverá mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logError(String format, Object... args)
	{
		log(new Throwable(), MessageType.ERROR, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibição no console do tipo <b>Exception</b> conforme:
	 * @param e exceção que foi gerada no sistema e deve ser exibida a sua mensagem em console.
	 */

	public final void logException(Exception e)
	{
		log(new Throwable(), MessageType.EXCEPTION, e.getMessage());
	}

	/**
	 * Registra uma nova mensagem para entrar em exibição no console do tipo <b>Error</b> conforme:
	 * @param format formado da mensagem do qual a exibição deverá mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logFatalError(String format, Object... args)
	{
		log(new Throwable(), MessageType.FATAL, format, args);
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		return description.toString();
	}
}

package org.diverproject.jragnarok.console;

import org.diverproject.console.ConsolePanel;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Exibi��o</h1>
 *
 * <p>Classe designada para implementar as formas de exibi��o das mensagens registradas nos servidores (log).
 * Atrav�s dela ser� poss�vel repassar as mensagens em log � um painel para console que ir� exibir a mensagem.
 * Assim ser� poss�vel que cada servidor possua uma �nica exibi��o que ser� respectivo ao painel para console.</p>
 *
 * @see ConsolePanel
 * @see MessageType
 *
 * @author Andrew
 */

public abstract class Show
{
	/**
	 * Painel para console que ir� ser utilizado.
	 */
	private ConsolePanel consolePanel;

	/**
	 * Cria uma nova inst�ncia para exibi��o de mensagens log do sistema e ir� instanciar o painel para console.
	 */

	public Show()
	{
		consolePanel = new ConsolePanel();
	}

	/**
	 * A inicializa��o da exibi��o consistem em limpar todas as informa��es contidas no painel de console.
	 * � chamado para limpar sempre que o servidor � iniciado, evitando que informa��es indesejadas apare�am.
	 */

	public void init()
	{
		consolePanel.clear();
	}

	/**
	 * @return aquisi��o da inst�ncia do painel para console utilizado por essa exibi��o.
	 */

	public ConsolePanel getConsolePanel()
	{
		return consolePanel;
	}

	/**
	 * Procedimento que deve ser implementado pela classe mais especializada na exibi��o das mensagens log.
	 * Dever� especificar exatamente como e onde as mensagens dever�o ser exibidas no console.
	 * @param throwable objeto que cont�m as informa��es referente ao momento do log.
	 * @param type tipo de mensagem que est� sendo exibida ou null se n�o houver nenhum.
	 * @param format formado da mensagem do qual a exibi��o dever� mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public abstract void log(Throwable throwable, MessageType type, String format, Object... args);

	/**
	 * Registra uma nova mensagem para entrar em exibi��o no console do tipo <b>Debug</b> conforme:
	 * @param format formado da mensagem do qual a exibi��o dever� mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logDebug(String format, Object... args)
	{
		log(new Throwable(), MessageType.DEBUG, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibi��o no console do tipo <b>Info</b> conforme:
	 * @param format formado da mensagem do qual a exibi��o dever� mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logInfo(String format, Object... args)
	{
		log(new Throwable(), MessageType.INFO, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibi��o no console do tipo <b>Notice</b> conforme:
	 * @param format formado da mensagem do qual a exibi��o dever� mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logNotice(String format, Object... args)
	{
		log(new Throwable(), MessageType.NOTICE, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibi��o no console do tipo <b>Warning</b> conforme:
	 * @param format formado da mensagem do qual a exibi��o dever� mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logWarning(String format, Object... args)
	{
		log(new Throwable(), MessageType.WARNING, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibi��o no console do tipo <b>Error</b> conforme:
	 * @param format formado da mensagem do qual a exibi��o dever� mostrar conforme argumentos.
	 * @param args argumentos respectivos ao formado da mensagem passada acima.
	 */

	public final void logError(String format, Object... args)
	{
		log(new Throwable(), MessageType.ERROR, format, args);
	}

	/**
	 * Registra uma nova mensagem para entrar em exibi��o no console do tipo <b>Exception</b> conforme:
	 * @param e exce��o que foi gerada no sistema e deve ser exibida a sua mensagem em console.
	 */

	public final void logException(Exception e)
	{
		log(new Throwable(), MessageType.EXCEPTION, e.getMessage());
	}

	/**
	 * Registra uma nova mensagem para entrar em exibi��o no console do tipo <b>Error</b> conforme:
	 * @param format formado da mensagem do qual a exibi��o dever� mostrar conforme argumentos.
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

package org.diverproject.jragnarok.console;

import static org.diverproject.log.LogSystem.getUpSource;
import static org.diverproject.util.Util.format;

import org.diverproject.jragnarok.server.ServerThreaed;

/**
 * <h1>Exibição por Thread</h1>
 *
 * <p>Através dessa classe será possível que uma determinada thread possua uma exibição.
 * Assim que uma nova mensagem log for identificada no sistema será repassado a essa exibição.</p>
 *
 * <p>Há duas formas de se utilizar esta classe, a primeira é através do Singleton ou registerThread().
 * A exibição do Singleton deve ser usada pela thread principal como forma de exibição genérica.
 * Já a criação por registerThread() deve ser feito pelos servidores quando quiserem uma exibição.</p>
 *
 * @see Show
 * @see MessageType
 *
 * @author Andrew
 */

public class ShowThread extends Show
{
	/**
	 * Instância para exibição genérica de mensagens log.
	 */
	private static final ShowThread INSTANCE = new ShowThread();

	/**
	 * Construtor privado para evitar instâncias desnecessárias desta classe e respeitar a forma como é usada.
	 */

	private ShowThread()
	{
		
	}

	@Override
	public void log(Throwable throwable, MessageType type, String message, Object... args)
	{
		StackTraceElement trace = throwable.getStackTrace()[2 + getUpSource()];

		String methodName = trace.getMethodName();
		String className = trace.getClassName().substring(trace.getClassName().lastIndexOf('.') + 1);
		String format = "";

		if (type == null)
			format = format("%s.%s: %s", className, methodName, message);
		else
			format = format("[%s] %s.%s: %s", type.prefix, className, methodName, message);

		getConsolePanel().setMessage(format, args);
		getConsolePanel().callPrintMessage();
	}

	/**
	 * Procedimento que irá registrar uma exibição na thread em que está solicitando uma exibição.
	 * Essa exibição só poderá ser vinculada a thread caso a thread seja do tipo ServerThread.
	 * Caso a thread não seja deste tipo nenhuma exibição é vinculada logo nada é retornada.
	 * @return aquisição de uma nova instância de exibição que foi vinculada a thread.
	 */

	public static Show registerThread()
	{
		Thread thread = Thread.currentThread();
		ShowThread show = null;

		if (thread instanceof ServerThreaed)
		{
			ServerThreaed serverThreaed = (ServerThreaed) thread;

			if (serverThreaed.getShowThread() == null)
				serverThreaed.setShowThread(show = new ShowThread());
		}

		return show;
	}

	/**
	 * A exibição genérica deve ser utilizada por qualquer menasgem log que não seja específica de um servidor.
	 * @return aquisição da instância para exibição de mensagens log de forma genérica.
	 */

	public static ShowThread getInstance()
	{
		return INSTANCE;
	}
}

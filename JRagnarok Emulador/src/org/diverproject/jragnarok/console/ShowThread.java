package org.diverproject.jragnarok.console;

import static org.diverproject.log.LogSystem.getUpSource;
import static org.diverproject.util.Util.format;

import org.diverproject.jragnarok.server.ServerThreaed;

/**
 * <h1>Exibi��o por Thread</h1>
 *
 * <p>Atrav�s dessa classe ser� poss�vel que uma determinada thread possua uma exibi��o.
 * Assim que uma nova mensagem log for identificada no sistema ser� repassado a essa exibi��o.</p>
 *
 * <p>H� duas formas de se utilizar esta classe, a primeira � atrav�s do Singleton ou registerThread().
 * A exibi��o do Singleton deve ser usada pela thread principal como forma de exibi��o gen�rica.
 * J� a cria��o por registerThread() deve ser feito pelos servidores quando quiserem uma exibi��o.</p>
 *
 * @see Show
 * @see MessageType
 *
 * @author Andrew
 */

public class ShowThread extends Show
{
	/**
	 * Inst�ncia para exibi��o gen�rica de mensagens log.
	 */
	private static final ShowThread INSTANCE = new ShowThread();

	/**
	 * Construtor privado para evitar inst�ncias desnecess�rias desta classe e respeitar a forma como � usada.
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
	 * Procedimento que ir� registrar uma exibi��o na thread em que est� solicitando uma exibi��o.
	 * Essa exibi��o s� poder� ser vinculada a thread caso a thread seja do tipo ServerThread.
	 * Caso a thread n�o seja deste tipo nenhuma exibi��o � vinculada logo nada � retornada.
	 * @return aquisi��o de uma nova inst�ncia de exibi��o que foi vinculada a thread.
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
	 * A exibi��o gen�rica deve ser utilizada por qualquer menasgem log que n�o seja espec�fica de um servidor.
	 * @return aquisi��o da inst�ncia para exibi��o de mensagens log de forma gen�rica.
	 */

	public static ShowThread getInstance()
	{
		return INSTANCE;
	}
}

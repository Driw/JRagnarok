package org.diverproject.jragnarok;

import org.diverproject.jragnarok.console.MessageType;
import org.diverproject.jragnarok.console.Show;
import org.diverproject.jragnarok.console.ShowThread;
import org.diverproject.jragnarok.server.ServerThreaed;
import org.diverproject.log.Log;
import org.diverproject.log.LogListener;

/**
 * <h1>Listener para Registros do JRagnarok</h1>
 *
 * <p>Classe usada para permitir a utiliza��o da biblioteca JUtil Log.
 * Ela ir� receber todos os registros (logs) que forem realizados.
 * Dever� exibir no console do JAVA e em um console em Janela.</p>
 *
 * @see LogListener
 * @see Log
 *
 * @author Andrew
 */

public class JRagnarokLogListener implements LogListener
{
	/**
	 * Inst�ncia �nica dessa classe (Singleton).
	 */
	private static final LogListener INSTANCE = new JRagnarokLogListener();

	/**
	 * Construtor privado para aderir ao Padr�o de Projetos: Singleton.
	 */

	private JRagnarokLogListener()
	{
		
	}

	@Override
	public void onMessage(Log log)
	{
		if (log.getType() == null || !log.getType().equals("Debug"))
			System.out.print(log.toString());

		if (log.getType().equals("Debug"))
			printMessage(ShowThread.getInstance(), log);

		else if (Thread.currentThread() instanceof ServerThreaed)
			printMessage(((ServerThreaed) Thread.currentThread()).getShowThread(), log);

		else
			printMessage(ShowThread.getInstance(), log);
	}

	/**
	 * Procedimento interno que ir� notificar uma exibi��o para mostrar uma mensagem de log no mesmo.
	 * @param show refer�ncia da exibi��o para console que ir� mostrar a mensagem.
	 * @param log refer�ncia do registro da mensagem no sistema para console.
	 */

	private void printMessage(Show show, Log log)
	{
		if (show != null)
			switch (log.getType())
			{
				case "Log":			show.log(log.getThrowable(), null, log.getMessage()); break;
				case "Debug":		show.log(log.getThrowable(), MessageType.DEBUG, log.getMessage()); break;
				case "Packet":		show.log(log.getThrowable(), MessageType.PACKET, log.getMessage()); break;
				case "Info":		show.log(log.getThrowable(), MessageType.INFO, log.getMessage()); break;
				case "Notice":		show.log(log.getThrowable(), MessageType.NOTICE, log.getMessage()); break;
				case "Warning":		show.log(log.getThrowable(), MessageType.WARNING, log.getMessage()); break;
				case "Error":		show.log(log.getThrowable(), MessageType.ERROR, log.getMessage()); break;
				case "Fatal":		show.log(log.getThrowable(), MessageType.FATAL, log.getMessage()); break;
				case "Exception":	show.log(log.getThrowable(), MessageType.EXCEPTION, log.getMessage()); break;
			}
	}

	/**
	 * N�o h� necessidade da exist�ncia de outras inst�ncias por isso usar Singleton.
	 * Atrav�s desse m�todo ser� poss�vel obter essa inst�ncia conforme o Singleton.
	 * @return aquisi��o da �nica inst�ncia dispon�vel dessa classe.
	 */

	public static LogListener getInstance()
	{
		return INSTANCE;
	}
}

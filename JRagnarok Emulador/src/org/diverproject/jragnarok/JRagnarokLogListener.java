package org.diverproject.jragnarok;

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
		System.out.print(log.toString());
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

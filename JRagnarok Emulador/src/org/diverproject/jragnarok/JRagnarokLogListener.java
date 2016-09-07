package org.diverproject.jragnarok;

import org.diverproject.log.Log;
import org.diverproject.log.LogListener;

/**
 * <h1>Listener para Registros do JRagnarok</h1>
 *
 * <p>Classe usada para permitir a utilização da biblioteca JUtil Log.
 * Ela irá receber todos os registros (logs) que forem realizados.
 * Deverá exibir no console do JAVA e em um console em Janela.</p>
 *
 * @see LogListener
 * @see Log
 *
 * @author Andrew
 */

public class JRagnarokLogListener implements LogListener
{
	/**
	 * Instância única dessa classe (Singleton).
	 */
	private static final LogListener INSTANCE = new JRagnarokLogListener();

	/**
	 * Construtor privado para aderir ao Padrão de Projetos: Singleton.
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
	 * Não há necessidade da existência de outras instâncias por isso usar Singleton.
	 * Através desse método será possível obter essa instância conforme o Singleton.
	 * @return aquisição da única instância disponível dessa classe.
	 */

	public static LogListener getInstance()
	{
		return INSTANCE;
	}
}

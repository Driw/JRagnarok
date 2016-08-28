package org.diverproject.jragnaork;

import org.diverproject.util.UtilException;

/**
 * <h1>Exce��o de Ragnarok</h1>
 *
 * <p>Essa exce��o n�o possui nenhuma funcionalidade extra al�m a de permitir mensagem formatada.
 * Utilizado pelas classes utilit�rias ou outras classes do conjunto da biblioteca API do Ragnarok.
 * Todas exce��es que forem geradas nesse projeto ter�o essa classe como m�e ou root na hierarquia.</p>
 *
 * @see UtilException
 *
 * @author Andrew
 */

@SuppressWarnings("serial")
public class RagnarokRuntimeException extends UtilException
{
	/**
	 * Constr�i uma nova exce��o de ragnarok sendo necess�rio definir uma mensagem.
	 * @param message mensagem que ser� exibida quando a exce��o for gerada.
	 */

	public RagnarokRuntimeException(String message)
	{
		super(message);
	}

	/**
	 * Constr�i uma nova exce��o de ragnarok sendo necess�rio definir uma mensagem.
	 * @param format string contendo o formato da mensagem que ser� exibida.
	 * @param args argumentos respectivos a formata��o da mensagem.
	 */

	public RagnarokRuntimeException(String format, Object... args)
	{
		super(format, args);
	}

	/**
	 * Constr�i uma nova exce��o de ragnarok sendo necess�rio definir a exce��o.
	 * Nesse caso ir� construir uma nova exce��o a partir de uma exce��o existente.
	 * Utilizando a mensagem dessa exce��o como mensagem desta.
	 * @param e exce��o do qual ser� considerada para criar uma nova.
	 */

	public RagnarokRuntimeException(Exception e)
	{
		super(e);
	}

	/**
	 * Constr�i uma nova exce��o de ragnarok sendo necess�rio definir uma mensagem.
	 * Nesse caso a mensagem ser� usada de uma exce��o j� criada, por�m permite adicionar
	 * um determinado conte�do extra como dados que ser� posicionado entre aspas.
	 * @param e exce��o para usar a mensagem armazenada no mesmo como exce��o.
	 * @param format string contendo o formato do conte�do extra.
	 * @param args argumentos respectivos a formata��o da mensagem.
	 */

	public RagnarokRuntimeException(Exception e, String format, Object... args)
	{
		super(e, format, args);
	}
}

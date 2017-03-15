package org.diverproject.jragnarok.database.io;

import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.util.UtilException;

/**
 * <h1>Exceção de Map Cache</h1>
 *
 * <p>Essa exceção não possui nenhuma funcionalidade extra além a de permitir mensagem formatada.
 * Utilizado pelas classes utilitárias ou outras classes do conjunto da biblioteca API do Map Cache.
 * Todas exceções que forem geradas nesse projeto terão essa classe como mãe ou root na hierarquia.</p>
 *
 * @see UtilException
 *
 * @author Andrew
 */

@SuppressWarnings("serial")
public class MapCacheException extends RagnarokException
{
	/**
	 * Constrói uma nova exceção de Map Cache sendo necessário definir uma mensagem.
	 * @param message mensagem que será exibida quando a exceção for gerada.
	 */

	public MapCacheException(String message)
	{
		super(message);
	}

	/**
	 * Constrói uma nova exceção de Map Cache sendo necessário definir uma mensagem.
	 * @param format string contendo o formato da mensagem que será exibida.
	 * @param args argumentos respectivos a formatação da mensagem.
	 */

	public MapCacheException(String format, Object... args)
	{
		super(format, args);
	}

	/**
	 * Constrói uma nova exceção de Map Cache sendo necessário definir a exceção.
	 * Nesse caso irá construir uma nova exceção a partir de uma exceção existente.
	 * Utilizando a mensagem dessa exceção como mensagem desta.
	 * @param e exceção do qual será considerada para criar uma nova.
	 */

	public MapCacheException(Exception e)
	{
		super(e);
	}

	/**
	 * Constrói uma nova exceção de Map Cache sendo necessário definir uma mensagem.
	 * Nesse caso a mensagem será usada de uma exceção já criada, porém permite adicionar
	 * um determinado conteúdo extra como dados que será posicionado entre aspas.
	 * @param e exceção para usar a mensagem armazenada no mesmo como exceção.
	 * @param format string contendo o formato do conteúdo extra.
	 * @param args argumentos respectivos a formatação da mensagem.
	 */

	public MapCacheException(Exception e, String format, Object... args)
	{
		super(e, format, args);
	}
}

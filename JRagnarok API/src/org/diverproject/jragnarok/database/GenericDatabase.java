package org.diverproject.jragnarok.database;

/**
 * <h1>Base de Dados Gen�rica</h1>
 *
 * <p>Uma base de dados gen�rica especifica todos os m�todos m�nimos necess�rio para a cria��o de uma.
 * Para n�o � necess�rio possuir as seguintes funcionalidades: para limpar todos os itens armazenados,
 * contar o espa�o ocupado/limite/dispon�vel e para verificar se cont�m um item especificado.</p>
 *
 * <p>Uma base de dados tamb�m pode iterar seus itens por foreach ou por um m�todo implementado.</p>
 *
 * @see Iterable
 *
 * @author Andrew
 *
 * @param <I>
 */

public interface GenericDatabase<I> extends Iterable<I>
{
	/**
	 * Limpar a base de dados significa remover todos os itens que nela foram armazenados.
	 * Os objetos em quest�o ser�o exclu�dos do Java apenas se n�o especificado em outro lugar.
	 */

	void clear();

	/**
	 * @return aquisi��o da quantidade de itens j� armazenados.
	 */

	int size();

	/**
	 * @return aquisi��o da quantidade de limite de itens que podem ser armazenados.
	 */

	int length();

	/**
	 * @return aquisi��o da quantidade te itens que ainda podem ser armazenados.
	 */

	int space();

	/**
	 * @return true se estiver vazia.
	 */

	boolean isEmpty();

	/**
	 * @return true se estiver cheia.
	 */

	boolean isFull();

	/**
	 * Verifica se um item especificado por par�metro est� inserido nessa base de dados.
	 * @param item refer�ncia do item (objeto) do qual deseja verificar na base de dados.
	 * @return true se o item estiver alocado nessa base de dados ou false caso contr�rio.
	 */

	boolean contains(I item);
}

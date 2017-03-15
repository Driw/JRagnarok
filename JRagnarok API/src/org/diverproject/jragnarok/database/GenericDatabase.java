package org.diverproject.jragnarok.database;

/**
 * <h1>Base de Dados Genérica</h1>
 *
 * <p>Uma base de dados genérica especifica todos os métodos mínimos necessário para a criação de uma.
 * Para não é necessário possuir as seguintes funcionalidades: para limpar todos os itens armazenados,
 * contar o espaço ocupado/limite/disponível e para verificar se contém um item especificado.</p>
 *
 * <p>Uma base de dados também pode iterar seus itens por foreach ou por um método implementado.</p>
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
	 * Os objetos em questão serão excluídos do Java apenas se não especificado em outro lugar.
	 */

	void clear();

	/**
	 * @return aquisição da quantidade de itens já armazenados.
	 */

	int size();

	/**
	 * @return aquisição da quantidade de limite de itens que podem ser armazenados.
	 */

	int length();

	/**
	 * @return aquisição da quantidade te itens que ainda podem ser armazenados.
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
	 * Verifica se um item especificado por parâmetro está inserido nessa base de dados.
	 * @param item referÊncia do item (objeto) do qual deseja verificar na base de dados.
	 * @return true se o item estiver alocado nessa base de dados ou false caso contrário.
	 */

	boolean contains(I item);
}

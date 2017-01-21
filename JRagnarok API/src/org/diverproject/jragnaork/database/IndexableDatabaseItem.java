package org.diverproject.jragnaork.database;

/**
 * <h1>Item de Database com Índice</h1>
 *
 * <p>Interface utilizada por database que identificam seus itens através de um valor numérico inteiro.
 * Deverá implementar apenas um procedimento para se obter o número de identificação do mesmo.
 * As databases aceitarão apenas parametrização de classes que tenham implementado essa interface.</p>
 *
 * @author Andrew
 */

public interface IndexableDatabaseItem
{
	/**
	 * Através do get é possível alocar o item de database adequadamente.
	 * @return aquisição do código de identificação do item de database.
	 */

	int getID();

	/**
	 * Através do set é possível definir a posição em que o item foi alocado.
	 * @param id código de identificação do item de database.
	 */

	void setID(int id);
}

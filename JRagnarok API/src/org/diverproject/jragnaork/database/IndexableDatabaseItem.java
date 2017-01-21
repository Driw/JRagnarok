package org.diverproject.jragnaork.database;

/**
 * <h1>Item de Database com �ndice</h1>
 *
 * <p>Interface utilizada por database que identificam seus itens atrav�s de um valor num�rico inteiro.
 * Dever� implementar apenas um procedimento para se obter o n�mero de identifica��o do mesmo.
 * As databases aceitar�o apenas parametriza��o de classes que tenham implementado essa interface.</p>
 *
 * @author Andrew
 */

public interface IndexableDatabaseItem
{
	/**
	 * Atrav�s do get � poss�vel alocar o item de database adequadamente.
	 * @return aquisi��o do c�digo de identifica��o do item de database.
	 */

	int getID();

	/**
	 * Atrav�s do set � poss�vel definir a posi��o em que o item foi alocado.
	 * @param id c�digo de identifica��o do item de database.
	 */

	void setID(int id);
}

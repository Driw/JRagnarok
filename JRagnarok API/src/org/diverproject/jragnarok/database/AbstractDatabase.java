package org.diverproject.jragnarok.database;

import static org.diverproject.util.Util.format;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Base de dados Abstrata</h1>
 *
 * <p>Classe que implementa algumas funcionalidades básicas que todas as base de dados possuem.
 * Neste caso toda base de dados possuem um nome, tipo de iteração espaço disponível.</p>
 *
 * @see GenericDatabase
 *
 * @author Andrew
 *
 * @param <I>
 */

public abstract class AbstractDatabase<I> implements GenericDatabase<I>
{
	/**
	 * Nome para identificação visual da base de dados.
	 */
	private String name;

	/**
	 * Cria uma nova instância de uma base de dados abstrata sendo necessário definir um nome.
	 * @param name nome para identificação visual da base de dados.
	 */

	public AbstractDatabase(String name)
	{
		this.name = name;
	}

	/**
	 * @return nome para identificação visual da base de dados.
	 */

	public String getName()
	{
		return name;
	}

	@Override
	public int space()
	{
		return length() - size();
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public boolean isFull()
	{
		return size() == length();
	}

	/**
	 * Método utilizado para descrever as informações contidas no objeto em forma de string.
	 * @param description objeto criado em toString que será usado para descrever o objeto.
	 */

	protected void toString(ObjectDescription description)
	{
		description.append("name", name);
		description.append("space", format("%d/%d", size(), length()));
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		toString(description);

		return description.toString();
	}
}

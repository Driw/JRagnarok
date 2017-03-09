package org.diverproject.jragnarok.database;

import static org.diverproject.util.Util.format;
import static org.diverproject.util.lang.IntUtil.interval;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.diverproject.util.ObjectDescription;

/**
 * <h1></h1>
 *
 * <p>Implementa��o parcial (abstrata) de uma base de dados que armazena itens do tipo index�vel.
 * Itens deste tipo implementam uma interface e por meio desta saber o seu c�digo de identifica��o.</p>
 *
 * @see IndexableDatabaseItem
 * @see AbstractDatabase
 *
 * @author Andrew
 *
 * @param <I> Tipo do item que ser� armazenado na base de dados.
 */

public abstract class IndexableDatabase<I extends IndexableDatabaseItem> extends AbstractDatabase<I>
{
	/**
	 * Quantidade de items que est�o atualmente guardados.
	 */
	private int size;

	/**
	 * Vetor que ir� guardar a refer�ncia dos objetos na base de dados.
	 */
	protected I items[];

	/**
	 * Cria uma nova inst�ncia de uma base de dados para armazenamento de itens index�veis.
	 * @param cls tipo de classe que ser� usada durante a itera��o (mesmo da parametrizada).
	 * @param name nome que ser� dado a base de dados para identifica��o visual.
	 * @param max quantidade limite de itens que poder� ser armazenado.
	 */

	@SuppressWarnings("unchecked")
	public IndexableDatabase(Class<I> cls, String name, int max)
	{
		super(name);

		items = (I[]) Array.newInstance(cls, max);
	}

	@Override
	public void clear()
	{
		for (int i = 0; i < items.length; i++)
			items[i] = null;

		size = 0;
	}

	/**
	 * Insere um novo item na base de dados sendo necess�rio especificar a refer�ncia do item.
	 * @param item refer�ncia do item do qual deseja inserir conforme a parametrizada.
	 * @return true se conseguir inserir ou false caso o �ndice esteja ocupado ou inv�lido.
	 */

	public boolean insert(I item)
	{
		if (interval(item.getID(), 1, items.length))
			if (items[item.getID() - 1] == null)
			{
				size++;
				items[item.getID() - 1] = item;
				return true;
			}

		return false;
	}

	/**
	 * Exclui um determinado item da base de dados especificando o �ndice do item no mesmo.
	 * @param index n�mero do �ndice do item do qual deseja excluir da base de dados.
	 * @return true se conseguir excluir ou false se o �ndice for inv�lido ou for null.
	 */

	public boolean delete(int index)
	{
		if (interval(index, 1, items.length) && items[index - 1] != null)
		{
			items[index - 1] = null;
			return true;
		}

		return false;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public int length()
	{
		return items.length;
	}

	@Override
	public boolean contains(I item)
	{
		return item != null && contains(item.getID());
	}

	/**
	 * Permite verificar se a base de dados possui um item ocupando um �ndice especificado.
	 * @param index n�mero do �ndice do qual deseja verificar se j� est� sendo usado.
	 * @return true se j� estiver sendo utilizado ou false caso contr�rio.
	 */

	public boolean contains(int index)
	{
		return interval(index, 1, items.length) ? items[index - 1] != null : false;
	}

	@Override
	public Iterator<I> iterator()
	{
		return new Iterator<I>()
		{
			private int offset;
			private int iteration;
			private int size = IndexableDatabase.this.size;
			private I items[] = IndexableDatabase.this.items.clone();

			@Override
			public boolean hasNext()
			{
				return iteration < size;
			}

			@Override
			public I next()
			{
				while (offset < items.length)
					if (items[offset++] != null)
						break;

				iteration++;

				return items[offset - 1];
			}

			@Override
			public String toString()
			{
				ObjectDescription description = new ObjectDescription(getClass());

				description.append("name", getName());
				description.append("size", format("%d/%d", size(), length()));

				return description.toString();
			}
		};
	}
}

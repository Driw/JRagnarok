package org.diverproject.jragnarok.server.common;

import org.diverproject.jragnarok.RagnarokRuntimeException;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Registro</h1>
 *
 * <p>Objeto que corresponde a um valor de variável que é vinculada a um jogador ou personagem.
 * Neste caso o registro é do tipo abstrato para que seja especificado o tipo de dado armazenado.
 * Para o mesmo será dado uma chave que é usado para identificá-lo: nome da variável.</p>
 *
 * @author Andrew
 *
 * @param <E> tipo da variável no registro
 */

public abstract class Register<E>
{
	/**
	 * Nome dado para identificar o registro.
	 */
	private String key;

	/**
	 * Determina se a variável foi atualizada ou não.
	 */
	private boolean updatable;

	/**
	 * Objeto que irá conter o valor respectivo à variável.
	 */
	private E value;

	/**
	 * Cria uma nova instância de um registro sendo obrigatório definir uma chave ao mesmo.
	 * @param key nome do registro que corresponde a identificação da variável.
	 */

	public Register(String key)
	{
		if (key == null || key.isEmpty())
			throw new RagnarokRuntimeException("chave não definida");

		this.key = key;
	}

	/**
	 * @return aquisição do nome da chave para identificar o registro.
	 */

	public String getKey()
	{
		return key;
	}

	/**
	 * @return aquisição do valor armazenado por este registro.
	 */

	public E getValue()
	{
		return value;
	}

	/**
	 * Permite atualizar o valor do qual esse registro está armazenando no momento.
	 * Também define o registro como <b>updatable</b> se o valor mudar.
	 * @param element referência do novo valor que o registro deve assumir.
	 */

	public void setValue(E element)
	{
		if (this.value == null || !this.value.equals(element))
		{
			this.value = element;
			this.updatable = true;
		}
	}

	/**
	 * @return true se o valor foi alterado ou false caso contrário.
	 */

	public boolean isUpdatable()
	{
		return updatable;
	}

	/**
	 * Ao forçar a atualização será dito que o registro deve ser sobrescrito no banco de dados.
	 * Pode ocorrer casos em que seja necessário utilizar este método ao invés de atualizá-lo.
	 * @param updatable true para forçar a atualização do valor do registro.
	 */

	public void setUpdatable(boolean updatable)
	{
		this.updatable = updatable;
	}

	/**
	 * Procedimento interno para preencher a descrição dos valores referentes a este objeto.
	 * @param description objeto que descreve as informações contidas neste objeto.
	 */

	protected void toString(ObjectDescription description)
	{
		description.append(key, value);

		if (updatable)
			description.append("updatable");
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());
		toString(description);

		return description.toString();
	}
}

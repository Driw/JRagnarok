package org.diverproject.jragnarok.server.common;

import org.diverproject.jragnarok.RagnarokRuntimeException;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Registro</h1>
 *
 * <p>Objeto que corresponde a um valor de vari�vel que � vinculada a um jogador ou personagem.
 * Neste caso o registro � do tipo abstrato para que seja especificado o tipo de dado armazenado.
 * Para o mesmo ser� dado uma chave que � usado para identific�-lo: nome da vari�vel.</p>
 *
 * @author Andrew
 *
 * @param <E> tipo da vari�vel no registro
 */

public abstract class Register<E>
{
	/**
	 * Nome dado para identificar o registro.
	 */
	private String key;

	/**
	 * Determina se a vari�vel foi atualizada ou n�o.
	 */
	private boolean updatable;

	/**
	 * Objeto que ir� conter o valor respectivo � vari�vel.
	 */
	private E value;

	/**
	 * Cria uma nova inst�ncia de um registro sendo obrigat�rio definir uma chave ao mesmo.
	 * @param key nome do registro que corresponde a identifica��o da vari�vel.
	 */

	public Register(String key)
	{
		if (key == null || key.isEmpty())
			throw new RagnarokRuntimeException("chave n�o definida");

		this.key = key;
	}

	/**
	 * @return aquisi��o do nome da chave para identificar o registro.
	 */

	public String getKey()
	{
		return key;
	}

	/**
	 * @return aquisi��o do valor armazenado por este registro.
	 */

	public E getValue()
	{
		return value;
	}

	/**
	 * Permite atualizar o valor do qual esse registro est� armazenando no momento.
	 * Tamb�m define o registro como <b>updatable</b> se o valor mudar.
	 * @param element refer�ncia do novo valor que o registro deve assumir.
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
	 * @return true se o valor foi alterado ou false caso contr�rio.
	 */

	public boolean isUpdatable()
	{
		return updatable;
	}

	/**
	 * Ao for�ar a atualiza��o ser� dito que o registro deve ser sobrescrito no banco de dados.
	 * Pode ocorrer casos em que seja necess�rio utilizar este m�todo ao inv�s de atualiz�-lo.
	 * @param updatable true para for�ar a atualiza��o do valor do registro.
	 */

	public void setUpdatable(boolean updatable)
	{
		this.updatable = updatable;
	}

	/**
	 * Procedimento interno para preencher a descri��o dos valores referentes a este objeto.
	 * @param description objeto que descreve as informa��es contidas neste objeto.
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

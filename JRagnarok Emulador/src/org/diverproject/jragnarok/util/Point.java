package org.diverproject.jragnarok.util;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Ponto</h1>
 *
 * <p>Identifica um ponto espec�fico no espa�o de 2D.</p>
 *
 * @author Andrew
 */

public class Point
{
	/**
	 * Coordenada do ponto no eixo X.
	 */
	private int x;

	/**
	 * Coordenada do ponto no eixo Y.
	 */
	private int y;

	/**
	 * @return aquisi��o da coordenada do ponto no eixo X.
	 */

	public int getX()
	{
		return x;
	}

	/**
	 * @param x coordenada do ponto no eixo X.
	 */

	public void setX(int x)
	{
		if (x >= 0)
			this.x = x;
	}

	/**
	 * @return aquisi��o da coordenada do ponto no eixo Y.
	 */

	public int getY()
	{
		return y;
	}

	/**
	 * @param y coordenada do ponto no eixo Y.
	 */

	public void setY(int y)
	{
		if (y >= 0)
			this.y = y;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("x", x);
		description.append("y", y);

		return description.toString();
	}
}

package org.diverproject.jragnarok.util;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_SIZE;
import static org.diverproject.util.lang.IntUtil.limit;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Ponto</h1>
 *
 * <p>Identifica um ponto específico no espaço de 2D.</p>
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
	 * @return aquisição da coordenada do ponto no eixo X.
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
		this.x = limit(x, 0, MAX_MAP_SIZE);
	}

	/**
	 * @return aquisição da coordenada do ponto no eixo Y.
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
		this.y = limit(y, 0, MAX_MAP_SIZE);
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

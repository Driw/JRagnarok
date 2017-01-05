package org.diverproject.jragnarok.server.character.entities;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MEMOPOINTS;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.util.MapPoint;
import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Localiza��es</h1>
 *
 * <p>Determina coordenadas X e Y do posicionamento de um determinado jogador.
 * As localiza��es s�o utilizadas como pontos padr�es em um mapa como retorno.</h1>
 *
 * <p>As formas de retornos s�o: local onde o personagem ficou quando saiu do jogo,
 * ponto de retorno em caso de morte e pontos memorizados de acesso por habilidades.
 * Nos dois primeiros casos h� um ponto para cada, no memorizado h� <code>MAX_MEMO_POINTS</code>.</p>
 *
 * @see MapPoint
 *
 * @author Andrew
 */

public class Locations implements CanCopy<Locations>
{
	/**
	 * Localiza��o da onde o personagem se encontra.
	 */
	private MapPoint lastPoint;

	/**
	 * Localiza��o do ponto de retorno em caso de morte.
	 */
	private MapPoint savePoint;

	/**
	 * Localiza��es memorizadas atrav�s de comando/habilidade.
	 */
	private MapPoint memoPoints[];

	/**
	 * Cria uma nova inst�ncia de um objeto que armazena informa��es sobre localiza��es.
	 * Inicializa o vetor de pontos memorizados com um tamanho de <code>MAX_MEMOPOINTS</code>.
	 */

	public Locations()
	{
		lastPoint = new MapPoint();
		savePoint = new MapPoint();
		memoPoints = new MapPoint[MAX_MEMOPOINTS];
	}

	/**
	 * @return aquisi��o da localiza��o do personagem.
	 */

	public MapPoint getLastPoint()
	{
		return lastPoint;
	}

	/**
	 * @return aquisi��o da localiza��o do ponto de retorno.
	 */

	public MapPoint getSavePoint()
	{
		return savePoint;
	}

	/**
	 * @return aquisi��o das localiza��es memorizadas.
	 */

	public MapPoint[] getMemoPoints()
	{
		return memoPoints;
	}

	/**
	 * @param index �ndice da localiza��o memorizada.
	 * @return aquisi��o da localiza��o desejada se houver.
	 */

	public MapPoint getMemoPoint(int index)
	{
		if (!interval(index, 0, memoPoints.length - 1))
			return null;

		if (memoPoints[index] == null)
			memoPoints[index] = new MapPoint();

		return memoPoints[index];
	}

	@Override
	public void copyFrom(Locations e)
	{
		if (e != null)
		{
			lastPoint = e.lastPoint.clone();
			savePoint = e.savePoint.clone();
			memoPoints = e.memoPoints.clone();

			for (int i = 0; i < memoPoints.length; i++)
				if (memoPoints[i] != null)
					memoPoints[i] = memoPoints[i].clone();
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("last", lastPoint);
		description.append("save", savePoint);

		for (int i = 0; i < memoPoints.length; i++)
			description.append("memo" +i, memoPoints[i]);

		return description.toString();
	}
}

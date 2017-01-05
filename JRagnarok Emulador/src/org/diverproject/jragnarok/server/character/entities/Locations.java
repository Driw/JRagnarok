package org.diverproject.jragnarok.server.character.entities;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MEMOPOINTS;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.util.MapPoint;
import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Localizações</h1>
 *
 * <p>Determina coordenadas X e Y do posicionamento de um determinado jogador.
 * As localizações são utilizadas como pontos padrões em um mapa como retorno.</h1>
 *
 * <p>As formas de retornos são: local onde o personagem ficou quando saiu do jogo,
 * ponto de retorno em caso de morte e pontos memorizados de acesso por habilidades.
 * Nos dois primeiros casos há um ponto para cada, no memorizado há <code>MAX_MEMO_POINTS</code>.</p>
 *
 * @see MapPoint
 *
 * @author Andrew
 */

public class Locations implements CanCopy<Locations>
{
	/**
	 * Localização da onde o personagem se encontra.
	 */
	private MapPoint lastPoint;

	/**
	 * Localização do ponto de retorno em caso de morte.
	 */
	private MapPoint savePoint;

	/**
	 * Localizações memorizadas através de comando/habilidade.
	 */
	private MapPoint memoPoints[];

	/**
	 * Cria uma nova instância de um objeto que armazena informações sobre localizações.
	 * Inicializa o vetor de pontos memorizados com um tamanho de <code>MAX_MEMOPOINTS</code>.
	 */

	public Locations()
	{
		lastPoint = new MapPoint();
		savePoint = new MapPoint();
		memoPoints = new MapPoint[MAX_MEMOPOINTS];
	}

	/**
	 * @return aquisição da localização do personagem.
	 */

	public MapPoint getLastPoint()
	{
		return lastPoint;
	}

	/**
	 * @return aquisição da localização do ponto de retorno.
	 */

	public MapPoint getSavePoint()
	{
		return savePoint;
	}

	/**
	 * @return aquisição das localizações memorizadas.
	 */

	public MapPoint[] getMemoPoints()
	{
		return memoPoints;
	}

	/**
	 * @param index índice da localização memorizada.
	 * @return aquisição da localização desejada se houver.
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

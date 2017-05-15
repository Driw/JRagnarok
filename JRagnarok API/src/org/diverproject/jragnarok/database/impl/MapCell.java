package org.diverproject.jragnarok.database.impl;

import static org.diverproject.util.Util.format;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.RagnarokRuntimeException;
import org.diverproject.jragnarok.util.MapDimension;
import org.diverproject.util.BitWise8;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SizeUtil;

/**
 * <h1>Mapeamento de Células</h1>
 *
 * <p>Classe que permite mapear detalhadamente as propriedades que cada célula do mapa possui.
 * Inicialmente será definido apenas uma propriedade que é referente ao arquivo GAT encontrado nas GRF.
 * Entretanto há outras duas propriedades que ainda não serão implementadas (somente quando forem usadas).</p>
 *
 * @see MapDimension
 * @see BitWise8
 *
 * @author Andrew
 */

public class MapCell
{
	/**
	 * Propriedade GAT que determina o terreno como acessível.
	 */
	public static final byte GAT_WALKABLE_GROUND = 0;

	/**
	 * Propriedade GAT que determina o terreno como inacessível.
	 */
	public static final byte GAT_NON_WALKABLE_GROUND = 1;

	/**
	 * Propriedade GAT não definida (desconhecida) número 1.
	 */
	public static final byte GAT_UNDEFINED1 = 2;

	/**
	 * Propriedade GAT que determina o terreno com água e acessível.
	 */
	public static final byte GAT_WALKWABLE_WATER = 3;

	/**
	 * Propriedade GAT não definida (desconhecida) número 2.
	 */
	public static final byte GAT_UNDEFINED2 = 4;

	/**
	 * Propriedade GAT que determina o terreno que apenas projéteis passam.
	 */
	public static final byte GAT_GAP_SNIPABLE = 5;

	/**
	 * Propriedade GAT não definida (desconhecida) número 3.
	 */
	public static final byte GAT_UNDEFINED3 = 6;


	/**
	 * Flag para especificar um terreno padrão (nenhuma propriedade).
	 */
	public static final byte TERRAIN_DEFAULT_VALUE = 0x00;

	/**
	 * Flag para especificar um terreno como acessível: pode-se andar sobre.
	 */
	public static final byte TERRAIN_WALKABLE = 0x01;

	/**
	 * Flag para especificar um terreno como atirável: projéteis passam por ele.
	 */
	public static final byte TERRAIN_SHOOTABLE = 0x02;

	/**
	 * Flag para especificar um terreno como água: habilidades influenciadas por água.
	 */
	public static final byte TERRAIN_WATER = 0x04;

	/**
	 * Flag não especificada (desconhecida) número 1.
	 */
	public static final byte TERRAIN_UNDEFINED1 = 0x08;

	/**
	 * Flag não especificada (desconhecida) número 2.
	 */
	public static final byte TERRAIN_UNDEFINED2 = 0x10;

	/**
	 * Flag não especificada (desconhecida) número 3.
	 */
	public static final byte TERRAIN_UNDEFINED3 = 0x20;


	/**
	 * Dimensionamento do mapa.
	 */
	private MapDimension dimension;

	/**
	 * Vetor contendo todas as flags de cada célula do mapa.
	 */
	private byte[] terrain;

	/**
	 * @return aquisição do dimensionamento do mapa.
	 */

	public MapDimension getDimension()
	{
		return dimension;
	}

	/**
	 * @param dimension novo dimensionamento do mapa.
	 */

	public void setDimension(MapDimension dimension)
	{
		this.dimension = dimension;
	}

	/**
	 * Método que irá recriar o vetor que armazena as propriedades de cada célula do mapa.
	 * Caso o tamanho do mapa (largura por comprimento) seja do mesmo tamanho atual,
	 * apenas será redefinido as flags do mapa para <code>TERRAIN_DEFAULT_VALUES</code>.
	 */

	public void initData()
	{
		if (dimension == null)
			throw new RagnarokRuntimeException("dimensão não definida");

		int size = dimension.getWidth() * dimension.getLength();

		if (terrain == null)
			terrain = new byte[size];

		else if (terrain.length != size)
			terrain = null;

		for (int i = 0; i < terrain.length; i++)
			terrain[i] = TERRAIN_DEFAULT_VALUE;
	}

	/**
	 * Método que permite calcular o valor do offset (índice) no vetor interno usado.
	 * O cálculo é feito conforme o dimensionamento do mapa: <b>((y * width) + x)</b>.
	 * @param x coordenada no mapa referente ao eixo da largura (longitude).
	 * @param y coordenada no mapa referente ao eixo da comprimento (latitude).
	 * @return aquisição do número (índice) do offset referente as coordenadas acima.
	 */

	public int calcOffset(int x, int y)
	{
		if (interval(x, 0, dimension.getWidth() - 1) && interval(y, 0, dimension.getLength() - 1))
			return (y * dimension.getWidth()) + x;

		throw new RagnarokRuntimeException("coordenada %d,%d inválidas", x, y);
	}

	/**
	 * Permite definir as propriedades de uma célula específica do mapa com um valor GAT.
	 * @param x coordenada no mapa referente ao eixo da largura (longitude).
	 * @param y coordenada no mapa referente ao eixo da comprimento (latitude).
	 * @param gatValue valor obtido de um arquivo GAT.
	 */

	public void setTerrainGAT(int x, int y, byte gatValue)
	{
		int offset = calcOffset(x, y);

		switch (gatValue)
		{
			case GAT_WALKABLE_GROUND:
				terrain[offset] = BitWise8.set(TERRAIN_DEFAULT_VALUE, TERRAIN_WALKABLE | TERRAIN_SHOOTABLE);
				return;

			case GAT_NON_WALKABLE_GROUND:
				terrain[offset] = BitWise8.set(TERRAIN_DEFAULT_VALUE, TERRAIN_DEFAULT_VALUE);
				return;

			case GAT_UNDEFINED1:
				terrain[offset] = BitWise8.set(TERRAIN_DEFAULT_VALUE, TERRAIN_WALKABLE | TERRAIN_SHOOTABLE | TERRAIN_UNDEFINED1);
				return;

			case GAT_WALKWABLE_WATER:
				terrain[offset] = BitWise8.set(TERRAIN_DEFAULT_VALUE, TERRAIN_WALKABLE | TERRAIN_SHOOTABLE | TERRAIN_WATER);
				return;

			case GAT_UNDEFINED2:
				terrain[offset] = BitWise8.set(TERRAIN_DEFAULT_VALUE, TERRAIN_WALKABLE | TERRAIN_SHOOTABLE | TERRAIN_UNDEFINED2);
				return;

			case GAT_GAP_SNIPABLE:
				terrain[offset] = BitWise8.set(TERRAIN_DEFAULT_VALUE, TERRAIN_SHOOTABLE);
				return;

			case GAT_UNDEFINED3:
				terrain[offset] = BitWise8.set(TERRAIN_DEFAULT_VALUE, TERRAIN_WALKABLE | TERRAIN_SHOOTABLE | TERRAIN_UNDEFINED3);
				return;
		}

		throw new RagnarokRuntimeException("%d não é um GatValue", gatValue);
	}

	/**
	 * Converte as propriedades de uma célula para um valor armazenável em arquivo GAT.
	 * @param x coordenada no mapa referente ao eixo da largura (longitude).
	 * @param y coordenada no mapa referente ao eixo da comprimento (latitude).
	 * @return aquisição do valor GAT correspondente as propriedades do mapa.
	 */

	public byte getTerrainGAT(int x, int y)
	{
		int offset = calcOffset(x, y);
		byte gat = terrain[offset];

			 if (BitWise8.is(gat, TERRAIN_DEFAULT_VALUE))									return GAT_NON_WALKABLE_GROUND;
		else if (BitWise8.is(gat, TERRAIN_UNDEFINED1))										return GAT_UNDEFINED1;
		else if (BitWise8.is(gat, TERRAIN_UNDEFINED2))										return GAT_UNDEFINED2;
		else if (BitWise8.is(gat, TERRAIN_UNDEFINED3))										return GAT_UNDEFINED3;
		else if (BitWise8.is(gat, TERRAIN_WALKABLE | TERRAIN_SHOOTABLE | TERRAIN_WALKABLE))	return GAT_WALKWABLE_WATER;
		else if (BitWise8.is(gat, TERRAIN_WALKABLE | TERRAIN_SHOOTABLE))					return GAT_WALKABLE_GROUND;
		else if (BitWise8.is(gat, TERRAIN_SHOOTABLE))										return GAT_GAP_SNIPABLE;

		throw new RagnarokRuntimeException("terreno na posição %d,%d com gat inválido (value: %d)", gat);
	}

	/**
	 * Verifica se uma determinada célula possui uma propriedade de terreno especificada:
	 * @param x coordenada no mapa referente ao eixo da largura (longitude).
	 * @param y coordenada no mapa referente ao eixo da comprimento (latitude).
	 * @param terrainProperties propriedade(s) que será verificada.
	 * @return true se todas as propriedades forem atendidas ou false caso contrário.
	 */

	public boolean isTerrain(int x, int y, byte terrainProperties)
	{
		int offset = calcOffset(x, y);
		byte value = terrain[offset];

		return BitWise8.is(value, terrainProperties);
	}

	/**
	 * Verifica se uma determinada célula pode ser acessada (andar por cima).
	 * @param x coordenada no mapa referente ao eixo da largura (longitude).
	 * @param y coordenada no mapa referente ao eixo da comprimento (latitude).
	 * @return true se for possível andar sobre ela ou false caso contrário.
	 */

	public boolean isTerrainWalkable(int x, int y)
	{
		return isTerrain(x, y, TERRAIN_WALKABLE);
	}

	/**
	 * Verifica se uma determinada célula permite que projéteis à atravessem.
	 * @param x coordenada no mapa referente ao eixo da largura (longitude).
	 * @param y coordenada no mapa referente ao eixo da comprimento (latitude).
	 * @return true se for permitido projéteis atravessar sobre ela ou false caso contrário.
	 */

	public boolean isTerrainShootable(int x, int y)
	{
		return isTerrain(x, y, TERRAIN_SHOOTABLE);
	}

	/**
	 * Verifica se uma determinada célula for definida como área com água.
	 * @param x coordenada no mapa referente ao eixo da largura (longitude).
	 * @param y coordenada no mapa referente ao eixo da comprimento (latitude).
	 * @return true se for uma área com água ou false caso contrário.
	 */

	public boolean isTerrainWater(int x, int y)
	{
		return isTerrain(x, y, TERRAIN_WATER);
	}

	/**
	 * Verifica se uma determinada célula possui uma propriedade de terreno especificada:
	 * @param x coordenada no mapa referente ao eixo da largura (longitude).
	 * @param y coordenada no mapa referente ao eixo da comprimento (latitude).
	 * @param walkable deve ser acessível (jogadores podem passar por cima).
	 * @param shootable deve ser projetável (projéteis podem passar por cima).
	 * @param water deve conter água (permite uso de habilidades de água especificas).
	 * @return true se todas as propriedades forem atendidas ou false caso contrário.
	 */

	public boolean isTerrain(int x, int y, boolean walkable, boolean shootable, boolean water)
	{
		byte properties = TERRAIN_DEFAULT_VALUE;

		if (walkable)	properties |= TERRAIN_WALKABLE;
		if (shootable)	properties |= TERRAIN_SHOOTABLE;
		if (water)		properties |= TERRAIN_WATER;

		return isTerrain(x, y, properties);
	}

	/**
	 * @return aquisição da quantidade de bytes gastos para especificar as propriedades do terreno,
	 * caso retorne zero significa que o mesmo ainda não foi criado.
	 */

	public int getTerrainSize()
	{
		return terrain == null ? 0 : terrain.length;
	}

	/**
	 * @return aquisição da quantidade de bytes gastos para especificar todas as propriedades do mapa.
	 */

	public int getBufferSize()
	{
		return getTerrainSize();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		if (dimension != null)
			description.append("dimension", format("%dx%d", dimension.getWidth(), dimension.getLength()));
		else
			description.append("dimension", null);

		description.append("terrainSize", SizeUtil.toString(getBufferSize()));

		return description.toString();
	}
}

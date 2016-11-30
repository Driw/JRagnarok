package org.diverproject.jragnarok.server.character.structures;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Apar�ncia</h1>
 *
 * <p>Guarda informa��es referentes a c�digos de identifica��o de sprites de um personagem.
 * Atrav�s destes c�digos a imagem visual do personagem poder� ser montada de acordo.</p>
 *
 * <p>Cont�m o id visual do: estilo de cabelo, cor do cabelo, cor das roupas, corpo,
 * e equipamentos se houver (armamento, escudo, equipamentos para cabe�a e capa).</p>
 *
 * @author Andrew
 */

public class Look
{
	/**
	 * C�digo de identifica��o do personagem.
	 */
	private int id;

	/**
	 * c�digo visual do estilo de cabelo.
	 */
	private short hair;

	/**
	 * c�digo visual da cor do cabelo.
	 */
	private short hairColor;

	/**
	 * c�digo visual da cor das roupas.
	 */
	private short clothesColor;

	/**
	 * c�digo visual do corpo (classe).
	 */
	private short body;

	/**
	 * c�digo visual do armamento usado.
	 */
	private short weapon;

	/**
	 * c�digo visual do escudo usado.
	 */
	private short shield;

	/**
	 * c�digo visual do equipamento para a cabe�a (topo).
	 */
	private short headTop;

	/**
	 * c�digo visual do equipamento para a cabe�a (meio).
	 */
	private short headMid;

	/**
	 * c�digo visual do equipamento para a cabe�a (baixo).
	 */
	private short headBottom;

	/**
	 * c�digo visual da capa.
	 */
	private short robe;

	/**
	 * @return aquisi��o do c�digo de identifica��o do personagem.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id c�digo de identifica��o do personagem.
	 */

	public void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisi��o do c�digo visual do estilo do cabelo.
	 */

	public short getHair()
	{
		return hair;
	}

	/**
	 * @param hair c�digo visual do estilo do cabelo.
	 */

	public void setHair(short hair)
	{
		this.hair = hair;
	}

	/**
	 * @return aquisi��o do c�digo visual da cor do cabelo.
	 */

	public short getHairColor()
	{
		return hairColor;
	}

	/**
	 * @param hairColor c�digo visual da cor do cabelo.
	 */

	public void setHairColor(short hairColor)
	{
		this.hairColor = hairColor;
	}

	/**
	 * @return aquisi��o do c�digo visual da cor das roupas.
	 */

	public short getClothesColor()
	{
		return clothesColor;
	}

	/**
	 * @param clothesColor c�digo visual da cor das roupas.
	 */

	public void setClothesColor(short clothesColor)
	{
		this.clothesColor = clothesColor;
	}

	/**
	 * @return aquisi��o do c�digo visual do corpo (classe).
	 */

	public short getBody()
	{
		return body;
	}

	/**
	 * @param body c�digo visual do corpo (classe).
	 */

	public void setBody(short body)
	{
		this.body = body;
	}

	/**
	 * @return aquisi��o do c�digo visual do armamento.
	 */

	public short getWeapon()
	{
		return weapon;
	}

	/**
	 * @param weapon c�digo visual do armamento.
	 */

	public void setWeapon(short weapon)
	{
		this.weapon = weapon;
	}

	/**
	 * @return aquisi��o do c�digo visual do escudo.
	 */

	public short getShield()
	{
		return shield;
	}

	/**
	 * @param shield c�digo visual do escudo.
	 */

	public void setShield(short shield)
	{
		this.shield = shield;
	}

	/**
	 * @return aquisi��o do c�digo visual do equipamento para cabe�a (topo).
	 */

	public short getHeadTop()
	{
		return headTop;
	}

	/**
	 * @param headTop c�digo visual do equipamento para cabe�a (topo).
	 */

	public void setHeadTop(short headTop)
	{
		this.headTop = headTop;
	}

	/**
	 * @return aquisi��o do c�digo visual do equipamento para cabe�a (meio).
	 */

	public short getHeadMid()
	{
		return headMid;
	}

	/**
	 * @param headMid c�digo visual do equipamento para cabe�a (meio).
	 */

	public void setHeadMid(short headMid)
	{
		this.headMid = headMid;
	}

	/**
	 * @return aquisi��o do c�digo visual do equipamento para cabe�a (baixo).
	 */

	public short getHeadBottom()
	{
		return headBottom;
	}

	/**
	 * @param headBottom c�digo visual do equipamento para cabe�a (baixo).
	 */

	public void setHeadBottom(short headBottom)
	{
		this.headBottom = headBottom;
	}

	/**
	 * @return aquisi��o do c�digo visual da capa.
	 */

	public short getRobe()
	{
		return robe;
	}

	/**
	 * @param robe c�digo visual da capa.
	 */

	public void setRobe(short robe)
	{
		this.robe = robe;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("hair", hair);
		description.append("hairColor", hairColor);
		description.append("clothesColor", clothesColor);
		description.append("body", body);
		description.append("weapon", weapon);
		description.append("shield", shield);
		description.append("headTop", headTop);
		description.append("headMid", headMid);
		description.append("headBottom", headBottom);
		description.append("robe", robe);

		return description.toString();
	}
}

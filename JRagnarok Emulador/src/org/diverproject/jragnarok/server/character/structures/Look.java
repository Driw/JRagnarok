package org.diverproject.jragnarok.server.character.structures;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Aparência</h1>
 *
 * <p>Guarda informações referentes a códigos de identificação de sprites de um personagem.
 * Através destes códigos a imagem visual do personagem poderá ser montada de acordo.</p>
 *
 * <p>Contém o id visual do: estilo de cabelo, cor do cabelo, cor das roupas, corpo,
 * e equipamentos se houver (armamento, escudo, equipamentos para cabeça e capa).</p>
 *
 * @author Andrew
 */

public class Look
{
	/**
	 * Código de identificação do personagem.
	 */
	private int id;

	/**
	 * código visual do estilo de cabelo.
	 */
	private short hair;

	/**
	 * código visual da cor do cabelo.
	 */
	private short hairColor;

	/**
	 * código visual da cor das roupas.
	 */
	private short clothesColor;

	/**
	 * código visual do corpo (classe).
	 */
	private short body;

	/**
	 * código visual do armamento usado.
	 */
	private short weapon;

	/**
	 * código visual do escudo usado.
	 */
	private short shield;

	/**
	 * código visual do equipamento para a cabeça (topo).
	 */
	private short headTop;

	/**
	 * código visual do equipamento para a cabeça (meio).
	 */
	private short headMid;

	/**
	 * código visual do equipamento para a cabeça (baixo).
	 */
	private short headBottom;

	/**
	 * código visual da capa.
	 */
	private short robe;

	/**
	 * @return aquisição do código de identificação do personagem.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id código de identificação do personagem.
	 */

	public void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisição do código visual do estilo do cabelo.
	 */

	public short getHair()
	{
		return hair;
	}

	/**
	 * @param hair código visual do estilo do cabelo.
	 */

	public void setHair(short hair)
	{
		this.hair = hair;
	}

	/**
	 * @return aquisição do código visual da cor do cabelo.
	 */

	public short getHairColor()
	{
		return hairColor;
	}

	/**
	 * @param hairColor código visual da cor do cabelo.
	 */

	public void setHairColor(short hairColor)
	{
		this.hairColor = hairColor;
	}

	/**
	 * @return aquisição do código visual da cor das roupas.
	 */

	public short getClothesColor()
	{
		return clothesColor;
	}

	/**
	 * @param clothesColor código visual da cor das roupas.
	 */

	public void setClothesColor(short clothesColor)
	{
		this.clothesColor = clothesColor;
	}

	/**
	 * @return aquisição do código visual do corpo (classe).
	 */

	public short getBody()
	{
		return body;
	}

	/**
	 * @param body código visual do corpo (classe).
	 */

	public void setBody(short body)
	{
		this.body = body;
	}

	/**
	 * @return aquisição do código visual do armamento.
	 */

	public short getWeapon()
	{
		return weapon;
	}

	/**
	 * @param weapon código visual do armamento.
	 */

	public void setWeapon(short weapon)
	{
		this.weapon = weapon;
	}

	/**
	 * @return aquisição do código visual do escudo.
	 */

	public short getShield()
	{
		return shield;
	}

	/**
	 * @param shield código visual do escudo.
	 */

	public void setShield(short shield)
	{
		this.shield = shield;
	}

	/**
	 * @return aquisição do código visual do equipamento para cabeça (topo).
	 */

	public short getHeadTop()
	{
		return headTop;
	}

	/**
	 * @param headTop código visual do equipamento para cabeça (topo).
	 */

	public void setHeadTop(short headTop)
	{
		this.headTop = headTop;
	}

	/**
	 * @return aquisição do código visual do equipamento para cabeça (meio).
	 */

	public short getHeadMid()
	{
		return headMid;
	}

	/**
	 * @param headMid código visual do equipamento para cabeça (meio).
	 */

	public void setHeadMid(short headMid)
	{
		this.headMid = headMid;
	}

	/**
	 * @return aquisição do código visual do equipamento para cabeça (baixo).
	 */

	public short getHeadBottom()
	{
		return headBottom;
	}

	/**
	 * @param headBottom código visual do equipamento para cabeça (baixo).
	 */

	public void setHeadBottom(short headBottom)
	{
		this.headBottom = headBottom;
	}

	/**
	 * @return aquisição do código visual da capa.
	 */

	public short getRobe()
	{
		return robe;
	}

	/**
	 * @param robe código visual da capa.
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

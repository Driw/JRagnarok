package org.diverproject.jragnarok.server.character.structures;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.MIN_NAME_LENGTH;
import static org.diverproject.util.lang.IntUtil.interval;
import static org.diverproject.util.lang.IntUtil.min;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.lang.ShortUtil;

/**
 * <h1>Personagem</h1>
 *
 * <p>Classe que ir� representar um personagem de uma conta no sistema com as devidas informa��es:
 * c�digo de identifica��o, nome, sexo, dinheiro, pontos de atributos/habilidades, atributos,
 * n�vel de classe/base, experi�ncias, visuais, fam�lia, localiza��es e tipos de grupos.</p>
 *
 * <p>Quando um c�digo de identifica��o � definido, todas as depend�ncias ser�o atualizadas tamb�m.
 * N�o ser� permitido definir uma depend�ncia nula ou que tenha um c�digo de identifica��o diferente do
 * que foi definido no personagem, isto � feito para evitar depend�ncias inv�lidas no personagem.</p>
 *
 * @see Look
 * @see Family
 * @see Experience
 * @see MercenaryRank
 * @see Locations
 *
 * @author Andrew
 */

public class Character
{
	/**
	 * Nome que ser� dado aos personagens sem nome escolhido.
	 */
	public static final String UNKNOWN = "Desconhecido";

	/**
	 * C�digo de identifica��o do personagem.
	 */
	private int id;

	/**
	 * Nome de exibi��o para os jogadores/sistema.
	 */
	private String name;

	/**
	 * Sexo do personagem (apenas M ou F).
	 */
	private char sex;

	/**
	 * Quantidade em dinheiro do jogo (zeny).
	 */
	private int zeny;

	/**
	 * Pontos de atributos dispon�veis para distribuir.
	 */
	private int statusPoint;

	/**
	 * Pontos de habilidades dispon�veis para distribuir.
	 */
	private int skillPoint;

	/**
	 * C�digo de identifica��o da classe.
	 */
	private short jobID;

	/**
	 * Tempo em minutos de chat banido por abuso do chat.
	 */
	private short manner;

	/**
	 * N�vel de base.
	 */
	private int baseLevel;

	/**
	 * N�vel de classe.
	 */
	private int jobLevel;

	/**
	 * Pontos distribu�dos em for�a.
	 */
	private int strength;

	/**
	 * Pontos distribu�dos em agilidade.
	 */
	private int agility;

	/**
	 * Pontos distribu�dos em vitalidade.
	 */
	private int vitality;

	/**
	 * Pontos distribu�dos em intelig�ncia.
	 */
	private int intelligence;

	/**
	 * Pontos distribu�dos em destreza.
	 */
	private int dexterity;

	/**
	 * Pontos distribu�dos em sorte.
	 */
	private int luck;

	/* TODO
	private int partyID;
	private int guildID;
	private int petID;
	private int homunculuID;
	private int mercenaryID;
	private int elementalID;
	private int clanID;
	*/

	/**
	 * Informa��es sobre o visual do personagem.
	 */
	private Look look;

	/**
	 * Personagens que formam a fam�lia deste.
	 */
	private Family family;

	/**
	 * N�veis de experi�ncias j� obtidos.
	 */
	private Experience experience;

	/**
	 * Classifica��o da utiliza��o do sistema de assistentes.
	 */
	private MercenaryRank mercenaryRank;

	/**
	 * Localiza��es de pontos salvos em mapas para retornos.
	 */
	private Locations locations;

	/**
	 * Cria uma nova inst�ncia para guardar informa��es referentes a um personagem.
	 * Inicializa as depend�ncias em branco para que possam ser utilizadas.
	 * Por padr�o o sexo do personagem ser� definido como M (masculino).
	 */

	public Character()
	{
		look = new Look();
		family = new Family();
		experience = new Experience();
		mercenaryRank = new MercenaryRank();
		locations = new Locations();

		sex = 'M';
		name = UNKNOWN;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do personagem.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * Quando definido um valor v�lido todas as depend�ncias ser�o atualizadas.
	 * A altera��o do c�digo s� � feita caso nenhum tenha sido definido ainda.
	 * @param id c�digo de identifica��o do personagem.
	 */

	public void setID(int id)
	{
		if (this.id == 0)
		{
			this.id = id;
			this.look.setID(id);
			this.family.setID(id);
			this.experience.setID(id);
			this.mercenaryRank.setID(id);
			this.locations.setID(id);
		}
	}

	/**
	 * @return aquisi��o do nome de exibi��o.
	 */

	public String getName()
	{
		return name;
	}

	/**
	 * O tamanho do nome deve respeitar os limites definidos por:
	 * <code>MIN_NAME_LENGTH</code>, <code>MAX_NAME_LENGTH</code>.
	 * @param name nome de exibi��o.
	 */

	public void setName(String name)
	{
		if (name != null && !name.equals(UNKNOWN) && interval(name.length(), MIN_NAME_LENGTH, MAX_NAME_LENGTH))
			this.name = name;
	}

	/**
	 * @return aquisi��o do sexo do personagem.
	 */

	public char getSex()
	{
		return sex;
	}

	/**
	 * @param sex sexo do personagem (M: masculino ou F: feminino).
	 */

	public void setSex(char sex)
	{
		switch (sex)
		{
			case 'm': case 'M': this.sex = 'M'; break;
			case 'f': case 'F': this.sex = 'F'; break;
		}
	}

	/**
	 * @return aquisi��o da quantidade de dinheiro em zeny.
	 */

	public int getZeny()
	{
		return zeny;
	}

	/**
	 * @param zeny quantidade de dinheiro em zeny.
	 */

	public void setZeny(int zeny)
	{
		this.zeny = min(zeny, 0);
	}

	/**
	 * @return aquisi��o da quantidade de pontos de atributos dispon�veis.
	 */

	public int getStatusPoint()
	{
		return statusPoint;
	}

	/**
	 * @param statusPoint quantidade de pontos de atributos dispon�veis.
	 */

	public void setStatusPoint(int statusPoint)
	{
		this.statusPoint = min(statusPoint, 0);
	}

	/**
	 * @return quantidade de pontos de habilidades dispon�veis.
	 */

	public int getSkillPoint()
	{
		return skillPoint;
	}

	/**
	 * @param skillPoint quantidade de pontos de habilidades dispon�veis.
	 */

	public void setSkillPoint(int skillPoint)
	{
		this.skillPoint = min(skillPoint, 0);
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o da classe.
	 */

	public short getJobID()
	{
		return jobID;
	}

	/**
	 * @param jobID c�digo de identifica��o da classe.
	 */

	public void setJobID(short jobID)
	{
		this.jobID = ShortUtil.min(jobID, (short) 0);
	}

	/**
	 * @return aquisi��o do tempo em minutos banido do chat.
	 */

	public short getManner()
	{
		return manner;
	}

	/**
	 * @param manner tempo em minutos banido do chat.
	 */

	public void setManner(short manner)
	{
		this.manner = ShortUtil.min(manner, (short) 0);
	}

	/**
	 * @return aquisi��o do n�vel de base.
	 */

	public int getBaseLevel()
	{
		return baseLevel;
	}

	/**
	 * @param baseLevel n�vel de base.
	 */

	public void setBaseLevel(int baseLevel)
	{
		this.baseLevel = min(baseLevel, 1);
	}

	/**
	 * @return aquisi��o do n�vel de classe.
	 */

	public int getJobLevel()
	{
		return jobLevel;
	}

	/**
	 * @param jobLevel n�vel de classe.
	 */

	public void setJobLevel(int jobLevel)
	{
		this.jobLevel = min(jobLevel, 1);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em for�a.
	 */

	public int getStrength()
	{
		return strength;
	}

	/**
	 * @param strength pontos distribu�dos em for�a.
	 */

	public void setStrength(int strength)
	{
		this.strength = min(strength, 1);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em agilidade.
	 */

	public int getAgility()
	{
		return agility;
	}

	/**
	 * @param agility pontos distribu�dos em agilidade.
	 */

	public void setAgility(int agility)
	{
		this.agility = min(agility, 1);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em vitalidade.
	 */

	public int getVitality()
	{
		return vitality;
	}

	/**
	 * @param vitality pontos distribu�dos em vitalidade.
	 */

	public void setVitality(int vitality)
	{
		this.vitality = min(vitality, 1);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em intelig�ncia.
	 */

	public int getIntelligence()
	{
		return intelligence;
	}

	/**
	 * @param intelligence pontos distribu�dos em intelig�ncia.
	 */

	public void setIntelligence(int intelligence)
	{
		this.intelligence = min(intelligence, 1);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em destreza.
	 */

	public int getDexterity()
	{
		return dexterity;
	}

	/**
	 * @param dexterity pontos distribu�dos em destreza.
	 */

	public void setDexterity(int dexterity)
	{
		this.dexterity = min(dexterity, 1);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em sorte.
	 */

	public int getLuck()
	{
		return luck;
	}

	/**
	 * @param luck pontos distribu�dos em sorte.
	 */

	public void setLuck(int luck)
	{
		this.luck = min(luck, 1);
	}

	/**
	 * @return aquisi��o dos tipos de apar�ncias visuais do personagem.
	 */

	public Look getLook()
	{
		return look;
	}

	/**
	 * Ser� definido apenas se n�o for nulo e tiver o mesmo c�digo de identifica��o.
	 * @param look tipos de apar�ncias visuais do personagem.
	 */

	public void setLook(Look look)
	{
		if (look != null && look.getID() == id)
			this.look = look;
	}

	/**
	 * @return aquisi��o da identifica��o dos personagens que comp�e a fam�lia.
	 */

	public Family getFamily()
	{
		return family;
	}

	/**
	 * Ser� definido apenas se n�o for nulo e tiver o mesmo c�digo de identifica��o.
	 * @param family identifica��o dos personagens que comp�e a fam�lia.
	 */

	public void setFamily(Family family)
	{
		if (family != null && family.getID() == id)
			this.family = family;
	}

	/**
	 * @return aquisi��o dos n�veis de experi�ncias j� obtidos.
	 */

	public Experience getExperience()
	{
		return experience;
	}

	/**
	 * Ser� definido apenas se n�o for nulo e tiver o mesmo c�digo de identifica��o.
	 * @param experience n�veis de experi�ncias j� obtidos.
	 */

	public void setExperience(Experience experience)
	{
		if (experience != null && experience.getID() == id)
			this.experience = experience;
	}

	/**
	 * @return aquisi��o da classifica��o de uso do sistema de assistentes.
	 */

	public MercenaryRank getMercenaryRank()
	{
		return mercenaryRank;
	}

	/**
	 * Ser� definido apenas se n�o for nulo e tiver o mesmo c�digo de identifica��o.
	 * @param mercenaryRank classifica��o de uso do sistema de assistentes.
	 */

	public void setMercenaryRank(MercenaryRank mercenaryRank)
	{
		if (mercenaryRank != null && mercenaryRank.getID() == id)
			this.mercenaryRank = mercenaryRank;
	}

	/**
	 * @return aquisi��o das localiza��es para pontos de retorno.
	 */

	public Locations getLocations()
	{
		return locations;
	}

	/**
	 * Ser� definido apenas se n�o for nulo e tiver o mesmo c�digo de identifica��o.
	 * @param locations localiza��es para pontos de retorno.
	 */

	public void setLocations(Locations locations)
	{
		if (locations != null && locations.getID() == id)
			this.locations = locations;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("name", name);
		description.append("sex", sex);
		description.append("zeny", zeny);
		description.append("statusPoint", statusPoint);
		description.append("skillPoint", skillPoint);
		description.append("jobID", jobID);
		description.append("manner", manner);
		description.append("baseLevel", baseLevel);
		description.append("jobLevel", jobLevel);
		description.append("strength", strength);
		description.append("agility", agility);
		description.append("vitality", vitality);
		description.append("intelligence", intelligence);
		description.append("dexterity", dexterity);
		description.append("luck", luck);

		return description.toString();
	}
}

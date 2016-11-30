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
 * <p>Classe que irá representar um personagem de uma conta no sistema com as devidas informações:
 * código de identificação, nome, sexo, dinheiro, pontos de atributos/habilidades, atributos,
 * nível de classe/base, experiências, visuais, família, localizações e tipos de grupos.</p>
 *
 * <p>Quando um código de identificação é definido, todas as dependências serão atualizadas também.
 * Não será permitido definir uma dependência nula ou que tenha um código de identificação diferente do
 * que foi definido no personagem, isto é feito para evitar dependências inválidas no personagem.</p>
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
	 * Nome que será dado aos personagens sem nome escolhido.
	 */
	public static final String UNKNOWN = "Desconhecido";

	/**
	 * Código de identificação do personagem.
	 */
	private int id;

	/**
	 * Nome de exibição para os jogadores/sistema.
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
	 * Pontos de atributos disponíveis para distribuir.
	 */
	private int statusPoint;

	/**
	 * Pontos de habilidades disponíveis para distribuir.
	 */
	private int skillPoint;

	/**
	 * Código de identificação da classe.
	 */
	private short jobID;

	/**
	 * Tempo em minutos de chat banido por abuso do chat.
	 */
	private short manner;

	/**
	 * Nível de base.
	 */
	private int baseLevel;

	/**
	 * Nível de classe.
	 */
	private int jobLevel;

	/**
	 * Pontos distribuídos em força.
	 */
	private int strength;

	/**
	 * Pontos distribuídos em agilidade.
	 */
	private int agility;

	/**
	 * Pontos distribuídos em vitalidade.
	 */
	private int vitality;

	/**
	 * Pontos distribuídos em inteligência.
	 */
	private int intelligence;

	/**
	 * Pontos distribuídos em destreza.
	 */
	private int dexterity;

	/**
	 * Pontos distribuídos em sorte.
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
	 * Informações sobre o visual do personagem.
	 */
	private Look look;

	/**
	 * Personagens que formam a família deste.
	 */
	private Family family;

	/**
	 * Níveis de experiências já obtidos.
	 */
	private Experience experience;

	/**
	 * Classificação da utilização do sistema de assistentes.
	 */
	private MercenaryRank mercenaryRank;

	/**
	 * Localizações de pontos salvos em mapas para retornos.
	 */
	private Locations locations;

	/**
	 * Cria uma nova instância para guardar informações referentes a um personagem.
	 * Inicializa as dependências em branco para que possam ser utilizadas.
	 * Por padrão o sexo do personagem será definido como M (masculino).
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
	 * @return aquisição do código de identificação do personagem.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * Quando definido um valor válido todas as dependências serão atualizadas.
	 * A alteração do código só é feita caso nenhum tenha sido definido ainda.
	 * @param id código de identificação do personagem.
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
	 * @return aquisição do nome de exibição.
	 */

	public String getName()
	{
		return name;
	}

	/**
	 * O tamanho do nome deve respeitar os limites definidos por:
	 * <code>MIN_NAME_LENGTH</code>, <code>MAX_NAME_LENGTH</code>.
	 * @param name nome de exibição.
	 */

	public void setName(String name)
	{
		if (name != null && !name.equals(UNKNOWN) && interval(name.length(), MIN_NAME_LENGTH, MAX_NAME_LENGTH))
			this.name = name;
	}

	/**
	 * @return aquisição do sexo do personagem.
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
	 * @return aquisição da quantidade de dinheiro em zeny.
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
	 * @return aquisição da quantidade de pontos de atributos disponíveis.
	 */

	public int getStatusPoint()
	{
		return statusPoint;
	}

	/**
	 * @param statusPoint quantidade de pontos de atributos disponíveis.
	 */

	public void setStatusPoint(int statusPoint)
	{
		this.statusPoint = min(statusPoint, 0);
	}

	/**
	 * @return quantidade de pontos de habilidades disponíveis.
	 */

	public int getSkillPoint()
	{
		return skillPoint;
	}

	/**
	 * @param skillPoint quantidade de pontos de habilidades disponíveis.
	 */

	public void setSkillPoint(int skillPoint)
	{
		this.skillPoint = min(skillPoint, 0);
	}

	/**
	 * @return aquisição do código de identificação da classe.
	 */

	public short getJobID()
	{
		return jobID;
	}

	/**
	 * @param jobID código de identificação da classe.
	 */

	public void setJobID(short jobID)
	{
		this.jobID = ShortUtil.min(jobID, (short) 0);
	}

	/**
	 * @return aquisição do tempo em minutos banido do chat.
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
	 * @return aquisição do nível de base.
	 */

	public int getBaseLevel()
	{
		return baseLevel;
	}

	/**
	 * @param baseLevel nível de base.
	 */

	public void setBaseLevel(int baseLevel)
	{
		this.baseLevel = min(baseLevel, 1);
	}

	/**
	 * @return aquisição do nível de classe.
	 */

	public int getJobLevel()
	{
		return jobLevel;
	}

	/**
	 * @param jobLevel nível de classe.
	 */

	public void setJobLevel(int jobLevel)
	{
		this.jobLevel = min(jobLevel, 1);
	}

	/**
	 * @return aquisição de pontos distribuídos em força.
	 */

	public int getStrength()
	{
		return strength;
	}

	/**
	 * @param strength pontos distribuídos em força.
	 */

	public void setStrength(int strength)
	{
		this.strength = min(strength, 1);
	}

	/**
	 * @return aquisição de pontos distribuídos em agilidade.
	 */

	public int getAgility()
	{
		return agility;
	}

	/**
	 * @param agility pontos distribuídos em agilidade.
	 */

	public void setAgility(int agility)
	{
		this.agility = min(agility, 1);
	}

	/**
	 * @return aquisição de pontos distribuídos em vitalidade.
	 */

	public int getVitality()
	{
		return vitality;
	}

	/**
	 * @param vitality pontos distribuídos em vitalidade.
	 */

	public void setVitality(int vitality)
	{
		this.vitality = min(vitality, 1);
	}

	/**
	 * @return aquisição de pontos distribuídos em inteligência.
	 */

	public int getIntelligence()
	{
		return intelligence;
	}

	/**
	 * @param intelligence pontos distribuídos em inteligência.
	 */

	public void setIntelligence(int intelligence)
	{
		this.intelligence = min(intelligence, 1);
	}

	/**
	 * @return aquisição de pontos distribuídos em destreza.
	 */

	public int getDexterity()
	{
		return dexterity;
	}

	/**
	 * @param dexterity pontos distribuídos em destreza.
	 */

	public void setDexterity(int dexterity)
	{
		this.dexterity = min(dexterity, 1);
	}

	/**
	 * @return aquisição de pontos distribuídos em sorte.
	 */

	public int getLuck()
	{
		return luck;
	}

	/**
	 * @param luck pontos distribuídos em sorte.
	 */

	public void setLuck(int luck)
	{
		this.luck = min(luck, 1);
	}

	/**
	 * @return aquisição dos tipos de aparências visuais do personagem.
	 */

	public Look getLook()
	{
		return look;
	}

	/**
	 * Será definido apenas se não for nulo e tiver o mesmo código de identificação.
	 * @param look tipos de aparências visuais do personagem.
	 */

	public void setLook(Look look)
	{
		if (look != null && look.getID() == id)
			this.look = look;
	}

	/**
	 * @return aquisição da identificação dos personagens que compõe a família.
	 */

	public Family getFamily()
	{
		return family;
	}

	/**
	 * Será definido apenas se não for nulo e tiver o mesmo código de identificação.
	 * @param family identificação dos personagens que compõe a família.
	 */

	public void setFamily(Family family)
	{
		if (family != null && family.getID() == id)
			this.family = family;
	}

	/**
	 * @return aquisição dos níveis de experiências já obtidos.
	 */

	public Experience getExperience()
	{
		return experience;
	}

	/**
	 * Será definido apenas se não for nulo e tiver o mesmo código de identificação.
	 * @param experience níveis de experiências já obtidos.
	 */

	public void setExperience(Experience experience)
	{
		if (experience != null && experience.getID() == id)
			this.experience = experience;
	}

	/**
	 * @return aquisição da classificação de uso do sistema de assistentes.
	 */

	public MercenaryRank getMercenaryRank()
	{
		return mercenaryRank;
	}

	/**
	 * Será definido apenas se não for nulo e tiver o mesmo código de identificação.
	 * @param mercenaryRank classificação de uso do sistema de assistentes.
	 */

	public void setMercenaryRank(MercenaryRank mercenaryRank)
	{
		if (mercenaryRank != null && mercenaryRank.getID() == id)
			this.mercenaryRank = mercenaryRank;
	}

	/**
	 * @return aquisição das localizações para pontos de retorno.
	 */

	public Locations getLocations()
	{
		return locations;
	}

	/**
	 * Será definido apenas se não for nulo e tiver o mesmo código de identificação.
	 * @param locations localizações para pontos de retorno.
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

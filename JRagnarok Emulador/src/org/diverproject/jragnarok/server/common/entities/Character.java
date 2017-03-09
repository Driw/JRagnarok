package org.diverproject.jragnarok.server.common.entities;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_HP;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_SP;
import static org.diverproject.jragnarok.JRagnarokConstants.MIN_NAME_LENGTH;
import static org.diverproject.jragnarok.server.common.Sex.FEMALE;
import static org.diverproject.jragnarok.server.common.Sex.SERVER;
import static org.diverproject.util.lang.IntUtil.interval;
import static org.diverproject.util.lang.IntUtil.min;
import static org.diverproject.util.Util.format;

import org.diverproject.jragnarok.server.common.Job;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;
import org.diverproject.util.lang.ShortUtil;
import org.diverproject.util.lang.StringUtil;

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
	 * Tamanho em bytes dos dados enviados de um personagem por pacote.
	 */
	public static final int BYTES = 147;


	/**
	 * C�digo de identifica��o do personagem.
	 */
	private int id;

	/**
	 * Nome de exibi��o para os jogadores/sistema.
	 */
	private String name;

	/**
	 * Enumera��o representativa do sexo do personagem no sistema.
	 */
	private Sex sex;

	/**
	 * Quantidade em dinheiro do jogo (zeny).
	 */
	private int zeny;

	/**
	 * Pontos de atributos dispon�veis para distribuir.
	 */
	private short statusPoint;

	/**
	 * Pontos de habilidades dispon�veis para distribuir.
	 */
	private short skillPoint;

	/**
	 * C�digo de identifica��o da classe.
	 */
	private Job jobID;

	/**
	 * Quantidade atual de HP.
	 */
	private int hp;

	/**
	 * Quantidade m�xima de SP.
	 */
	private int maxHP;

	/**
	 * Quantidade atual de SP.
	 */
	private short sp;

	/**
	 * Quantidade m�xima de SP.
	 */
	private short maxSP;

	/**
	 * Tempo em minutos de chat banido por abuso do chat.
	 */
	private short manner;

	/**
	 * N�vel de virtude para influ�ncia em �reas PK.
	 */
	private short virtue;

	/**
	 * N�vel de base.
	 */
	private int baseLevel;

	/**
	 * N�vel de classe.
	 */
	private int jobLevel;

	/**
	 * C�digo de identifica��o do grupo.
	 */
	private int partyID;

	/**
	 * C�digo de identifica��o do cl�.
	 */
	private int guildID;

	/**
	 * C�digo de identifica��o do mascote.
	 */
	private int petID;

	/**
	 * C�digo de identifica��o do hom�nculo.
	 */
	private int homunculuID;

	/**
	 * C�digo de identifica��o do assistente.
	 */
	private int mercenaryID;

	/**
	 * C�digo de identifica��o do elemental.
	 */
	private int elementalID;

	/**
	 * C�digo de identifica��o da tribo.
	 */
	private int clanID;

	/**
	 * Efeitos de estado do personagem.
	 */
	private BitWise effectState;

	/**
	 * Quantidade de vezes que o personagem foi renomado.
	 */
	private short rename;

	/**
	 * Hor�rio em que o personagem ser� exclu�do.
	 */
	private Time deleteDate;

	/**
	 * Hor�rio em que o personagem ser� desbloqueado.
	 */
	private Time unbanTime;

	/**
	 * Quantidade de vezes que o personagem foi movido.
	 */
	private short moves;

	/**
	 * Font que deve ser escolhida quando o jogador entrar no jogo.
	 */
	private byte font;

	/**
	 * TODO what is that?
	 */
	private int uniqueItemCounter;

	/***
	 * Atributos b�sicos do personagem.
	 */
	private Stats stats;

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
		stats = new Stats();
		look = new Look();
		family = new Family();
		experience = new Experience();
		mercenaryRank = new MercenaryRank();
		locations = new Locations();
		effectState = new BitWise(/* TODO: properties name */);
		deleteDate = new Time();
		unbanTime = new Time();

		sex = FEMALE;
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
			this.id = id;
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
	 * @return aquisi��o da enumera��o representativa do sexo do personagem no sistema.
	 */

	public Sex getSex()
	{
		return sex;
	}

	/**
	 * @param sex enumera��o representativa do sexo do personagem no sistema.
	 */

	public void setSex(Sex sex)
	{
		if (sex != null && sex != SERVER)
			this.sex = sex;
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

	public short getStatusPoint()
	{
		return statusPoint;
	}

	/**
	 * @param statusPoint quantidade de pontos de atributos dispon�veis.
	 */

	public void setStatusPoint(short statusPoint)
	{
		this.statusPoint = ShortUtil.min(statusPoint, (short) 0);
	}

	/**
	 * @return quantidade de pontos de habilidades dispon�veis.
	 */

	public short getSkillPoint()
	{
		return skillPoint;
	}

	/**
	 * @param skillPoint quantidade de pontos de habilidades dispon�veis.
	 */

	public void setSkillPoint(short skillPoint)
	{
		this.skillPoint = ShortUtil.min(skillPoint, (short) 0);
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o da classe.
	 */

	public Job getJob()
	{
		return jobID;
	}

	/**
	 * @param job c�digo de identifica��o da classe.
	 */

	public void setJob(Job job)
	{
		if (job != null)
			this.jobID = job;
	}

	/**
	 * @return aquisi��o da quantidade atual de HP.
	 */

	public int getHP()
	{
		return hp;
	}

	/**
	 * @param hp quantidade atual de HP.
	 */

	public void setHP(int hp)
	{
		this.hp = min(hp, 0, maxHP);
	}

	/**
	 * @return aquisi��o da quantidade m�xima de HP.
	 */

	public int getMaxHP()
	{
		return maxHP;
	}

	/**
	 * @param maxHP quantidade m�xima de HP.
	 */

	public void setMaxHP(int maxHP)
	{
		this.maxHP = min(maxHP, 1, MAX_HP);
	}

	/**
	 * @return aquisi��o da quantidade atual de SP.
	 */

	public short getSP()
	{
		return sp;
	}

	/**
	 * @param sp quantidade atual de SP.
	 */

	public void setSP(short sp)
	{
		this.sp = ShortUtil.limit(sp, (short) 0, maxSP);
	}

	/**
	 * @return aquisi��o da quantidade m�xima de SP.
	 */

	public short getMaxSP()
	{
		return maxSP;
	}

	/**
	 * @param maxSP quantidade m�xima de SP.
	 */

	public void setMaxSP(short maxSP)
	{
		this.maxSP = ShortUtil.limit(maxSP, (short) 0, (short) MAX_SP);
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
	 * @return aquisi��o do n�vel de virtude para influ�ncia em �reas PK.
	 */

	public short getVirtue()
	{
		return virtue;
	}

	/**
	 * @param virtue n�vel de virtude para influ�ncia em �reas PK.
	 */

	public void setVirtue(short virtue)
	{
		this.virtue = virtue;
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
	 * @return aquisi��o do c�digo de identifica��o do grupo.
	 */

	public int getPartyID()
	{
		return partyID;
	}

	/**
	 * @param partyID c�digo de identifica��o do grupo.
	 */

	public void setPartyID(int partyID)
	{
		this.partyID = partyID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do cl�.
	 */

	public int getGuildID()
	{
		return guildID;
	}

	/**
	 * @param guildID c�digo de identifica��o do cl�.
	 */

	public void setGuildID(int guildID)
	{
		this.guildID = guildID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do mascote.
	 */

	public int getPetID()
	{
		return petID;
	}

	/**
	 * @param petID c�digo de identifica��o do mascote.
	 */

	public void setPetID(int petID)
	{
		this.petID = petID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do hom�nculo.
	 */

	public int getHomunculuID()
	{
		return homunculuID;
	}

	/**
	 * @param homunculuID c�digo de identifica��o do hom�nculo.
	 */

	public void setHomunculuID(int homunculuID)
	{
		this.homunculuID = homunculuID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do assistente.
	 */

	public int getMercenaryID()
	{
		return mercenaryID;
	}

	/**
	 * @param mercenaryID c�digo de identifica��o do assistente.
	 */

	public void setMercenaryID(int mercenaryID)
	{
		this.mercenaryID = mercenaryID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do elemental.
	 */

	public int getElementalID()
	{
		return elementalID;
	}

	/**
	 * @param elementalID c�digo de identifica��o do elemental.
	 */

	public void setElementalID(int elementalID)
	{
		this.elementalID = elementalID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o da tribo.
	 */

	public int getClanID()
	{
		return clanID;
	}

	/**
	 * @param clanID c�digo de identifica��o da tribo.
	 */

	public void setClanID(int clanID)
	{
		this.clanID = clanID;
	}

	/**
	 * @return aquisi��o dos efeitos do personagem.
	 */

	public BitWise getEffectState()
	{
		return effectState;
	}

	/**
	 * @return aquisi��o da quantidade de vezes que o personagem foi renomado.
	 */

	public short getRename()
	{
		return rename;
	}

	/**
	 * @param rename quantidade de vezes que o personagem foi renomado.
	 */

	public void setRename(short rename)
	{
		this.rename = rename;
	}

	/**
	 * @return aquisi��o do hor�rio em que o personagem ser� exclu�do.
	 */

	public Time getDeleteDate()
	{
		return deleteDate;
	}

	/**
	 * @return aquisi��o do hor�rio em que o personagem ser� desbloqueado.
	 */

	public Time getUnbanTime()
	{
		return unbanTime;
	}

	/**
	 * @return aquisi��o da quantidade de vezes que o personagem foi movido.
	 */

	public short getMoves()
	{
		return moves;
	}

	/**
	 * @param moves quantidade de vezes que o personagem foi movido.
	 */

	public void setMoves(short moves)
	{
		this.moves = moves;
	}

	/**
	 * @return aquisi��o da font que deve ser escolhida quando o jogador entrar no jogo.
	 */

	public byte getFont()
	{
		return font;
	}

	/**
	 * @param font font que deve ser escolhida quando o jogador entrar no jogo.
	 */

	public void setFont(byte font)
	{
		this.font = font;
	}

	/**
	 * @return TODO
	 */

	public int getUniqueItemCounter()
	{
		return uniqueItemCounter;
	}

	/**
	 * @param uniqueItemCounter TODO
	 */

	public void setUniqueItemCounter(int uniqueItemCounter)
	{
		this.uniqueItemCounter = uniqueItemCounter;
	}

	/**
	 * @return aquisi��o dos atributos b�sicos do personagem.
	 */

	public Stats getStats()
	{
		return stats;
	}

	/**
	 * Ser� definido apenas se n�o for nulo.
	 * @param stats tipos de apar�ncias visuais do personagem.
	 */

	public void setStats(Stats stats)
	{
		this.stats.copyFrom(stats);
	}

	/**
	 * @return aquisi��o dos tipos de apar�ncias visuais do personagem.
	 */

	public Look getLook()
	{
		return look;
	}

	/**
	 * @param look tipos de apar�ncias visuais do personagem.
	 */

	public void setLook(Look look)
	{
		this.look.copyFrom(look);
	}

	/**
	 * @return aquisi��o da identifica��o dos personagens que comp�e a fam�lia.
	 */

	public Family getFamily()
	{
		return family;
	}

	/**
	 * @param family identifica��o dos personagens que comp�e a fam�lia.
	 */

	public void setFamily(Family family)
	{
		this.family.copyFrom(family);
	}

	/**
	 * @return aquisi��o dos n�veis de experi�ncias j� obtidos.
	 */

	public Experience getExperience()
	{
		return experience;
	}

	/**
	 * @param experience n�veis de experi�ncias j� obtidos.
	 */

	public void setExperience(Experience experience)
	{
		this.experience.copyFrom(experience);
	}

	/**
	 * @return aquisi��o da classifica��o de uso do sistema de assistentes.
	 */

	public MercenaryRank getMercenaryRank()
	{
		return mercenaryRank;
	}

	/**
	 * @param mercenaryRank classifica��o de uso do sistema de assistentes.
	 */

	public void setMercenaryRank(MercenaryRank mercenaryRank)
	{
		this.mercenaryRank.copyFrom(mercenaryRank);
	}

	/**
	 * @return aquisi��o das localiza��es para pontos de retorno.
	 */

	public Locations getLocations()
	{
		return locations;
	}

	/**
	 * @param locations localiza��es para pontos de retorno.
	 */

	public void setLocations(Locations locations)
	{
		this.locations.copyFrom(locations);
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("name", name);
		description.append("sex", sex);
		description.append("zeny", StringUtil.money(zeny));
		description.append("level", format("%d/%d", baseLevel, jobLevel));
		description.append("statusPoint", statusPoint);
		description.append("skillPoint", skillPoint);
		description.append("hp", format("%d/%d", hp, maxHP));
		description.append("sp", format("%d/%d", sp, maxSP));
		description.append("jobID", jobID);
		description.append("manner", manner);
		description.append("karma", virtue);
		description.append("effectState", effectState);
		description.append("rename", rename);
		description.append("deleteDate", deleteDate);
		description.append("moves", moves);
		description.append("font", font);
		description.append("uniqueItemCounter", uniqueItemCounter);

		return description.toString();
	}
}

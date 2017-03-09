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
	 * Tamanho em bytes dos dados enviados de um personagem por pacote.
	 */
	public static final int BYTES = 147;


	/**
	 * Código de identificação do personagem.
	 */
	private int id;

	/**
	 * Nome de exibição para os jogadores/sistema.
	 */
	private String name;

	/**
	 * Enumeração representativa do sexo do personagem no sistema.
	 */
	private Sex sex;

	/**
	 * Quantidade em dinheiro do jogo (zeny).
	 */
	private int zeny;

	/**
	 * Pontos de atributos disponíveis para distribuir.
	 */
	private short statusPoint;

	/**
	 * Pontos de habilidades disponíveis para distribuir.
	 */
	private short skillPoint;

	/**
	 * Código de identificação da classe.
	 */
	private Job jobID;

	/**
	 * Quantidade atual de HP.
	 */
	private int hp;

	/**
	 * Quantidade máxima de SP.
	 */
	private int maxHP;

	/**
	 * Quantidade atual de SP.
	 */
	private short sp;

	/**
	 * Quantidade máxima de SP.
	 */
	private short maxSP;

	/**
	 * Tempo em minutos de chat banido por abuso do chat.
	 */
	private short manner;

	/**
	 * Nível de virtude para influência em áreas PK.
	 */
	private short virtue;

	/**
	 * Nível de base.
	 */
	private int baseLevel;

	/**
	 * Nível de classe.
	 */
	private int jobLevel;

	/**
	 * Código de identificação do grupo.
	 */
	private int partyID;

	/**
	 * Código de identificação do clã.
	 */
	private int guildID;

	/**
	 * Código de identificação do mascote.
	 */
	private int petID;

	/**
	 * Código de identificação do homúnculo.
	 */
	private int homunculuID;

	/**
	 * Código de identificação do assistente.
	 */
	private int mercenaryID;

	/**
	 * Código de identificação do elemental.
	 */
	private int elementalID;

	/**
	 * Código de identificação da tribo.
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
	 * Horário em que o personagem será excluído.
	 */
	private Time deleteDate;

	/**
	 * Horário em que o personagem será desbloqueado.
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
	 * Atributos básicos do personagem.
	 */
	private Stats stats;

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
			this.id = id;
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
	 * @return aquisição da enumeração representativa do sexo do personagem no sistema.
	 */

	public Sex getSex()
	{
		return sex;
	}

	/**
	 * @param sex enumeração representativa do sexo do personagem no sistema.
	 */

	public void setSex(Sex sex)
	{
		if (sex != null && sex != SERVER)
			this.sex = sex;
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

	public short getStatusPoint()
	{
		return statusPoint;
	}

	/**
	 * @param statusPoint quantidade de pontos de atributos disponíveis.
	 */

	public void setStatusPoint(short statusPoint)
	{
		this.statusPoint = ShortUtil.min(statusPoint, (short) 0);
	}

	/**
	 * @return quantidade de pontos de habilidades disponíveis.
	 */

	public short getSkillPoint()
	{
		return skillPoint;
	}

	/**
	 * @param skillPoint quantidade de pontos de habilidades disponíveis.
	 */

	public void setSkillPoint(short skillPoint)
	{
		this.skillPoint = ShortUtil.min(skillPoint, (short) 0);
	}

	/**
	 * @return aquisição do código de identificação da classe.
	 */

	public Job getJob()
	{
		return jobID;
	}

	/**
	 * @param job código de identificação da classe.
	 */

	public void setJob(Job job)
	{
		if (job != null)
			this.jobID = job;
	}

	/**
	 * @return aquisição da quantidade atual de HP.
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
	 * @return aquisição da quantidade máxima de HP.
	 */

	public int getMaxHP()
	{
		return maxHP;
	}

	/**
	 * @param maxHP quantidade máxima de HP.
	 */

	public void setMaxHP(int maxHP)
	{
		this.maxHP = min(maxHP, 1, MAX_HP);
	}

	/**
	 * @return aquisição da quantidade atual de SP.
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
	 * @return aquisição da quantidade máxima de SP.
	 */

	public short getMaxSP()
	{
		return maxSP;
	}

	/**
	 * @param maxSP quantidade máxima de SP.
	 */

	public void setMaxSP(short maxSP)
	{
		this.maxSP = ShortUtil.limit(maxSP, (short) 0, (short) MAX_SP);
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
	 * @return aquisição do nível de virtude para influência em áreas PK.
	 */

	public short getVirtue()
	{
		return virtue;
	}

	/**
	 * @param virtue nível de virtude para influência em áreas PK.
	 */

	public void setVirtue(short virtue)
	{
		this.virtue = virtue;
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
	 * @return aquisição do código de identificação do grupo.
	 */

	public int getPartyID()
	{
		return partyID;
	}

	/**
	 * @param partyID código de identificação do grupo.
	 */

	public void setPartyID(int partyID)
	{
		this.partyID = partyID;
	}

	/**
	 * @return aquisição do código de identificação do clã.
	 */

	public int getGuildID()
	{
		return guildID;
	}

	/**
	 * @param guildID código de identificação do clã.
	 */

	public void setGuildID(int guildID)
	{
		this.guildID = guildID;
	}

	/**
	 * @return aquisição do código de identificação do mascote.
	 */

	public int getPetID()
	{
		return petID;
	}

	/**
	 * @param petID código de identificação do mascote.
	 */

	public void setPetID(int petID)
	{
		this.petID = petID;
	}

	/**
	 * @return aquisição do código de identificação do homúnculo.
	 */

	public int getHomunculuID()
	{
		return homunculuID;
	}

	/**
	 * @param homunculuID código de identificação do homúnculo.
	 */

	public void setHomunculuID(int homunculuID)
	{
		this.homunculuID = homunculuID;
	}

	/**
	 * @return aquisição do código de identificação do assistente.
	 */

	public int getMercenaryID()
	{
		return mercenaryID;
	}

	/**
	 * @param mercenaryID código de identificação do assistente.
	 */

	public void setMercenaryID(int mercenaryID)
	{
		this.mercenaryID = mercenaryID;
	}

	/**
	 * @return aquisição do código de identificação do elemental.
	 */

	public int getElementalID()
	{
		return elementalID;
	}

	/**
	 * @param elementalID código de identificação do elemental.
	 */

	public void setElementalID(int elementalID)
	{
		this.elementalID = elementalID;
	}

	/**
	 * @return aquisição do código de identificação da tribo.
	 */

	public int getClanID()
	{
		return clanID;
	}

	/**
	 * @param clanID código de identificação da tribo.
	 */

	public void setClanID(int clanID)
	{
		this.clanID = clanID;
	}

	/**
	 * @return aquisição dos efeitos do personagem.
	 */

	public BitWise getEffectState()
	{
		return effectState;
	}

	/**
	 * @return aquisição da quantidade de vezes que o personagem foi renomado.
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
	 * @return aquisição do horário em que o personagem será excluído.
	 */

	public Time getDeleteDate()
	{
		return deleteDate;
	}

	/**
	 * @return aquisição do horário em que o personagem será desbloqueado.
	 */

	public Time getUnbanTime()
	{
		return unbanTime;
	}

	/**
	 * @return aquisição da quantidade de vezes que o personagem foi movido.
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
	 * @return aquisição da font que deve ser escolhida quando o jogador entrar no jogo.
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
	 * @return aquisição dos atributos básicos do personagem.
	 */

	public Stats getStats()
	{
		return stats;
	}

	/**
	 * Será definido apenas se não for nulo.
	 * @param stats tipos de aparências visuais do personagem.
	 */

	public void setStats(Stats stats)
	{
		this.stats.copyFrom(stats);
	}

	/**
	 * @return aquisição dos tipos de aparências visuais do personagem.
	 */

	public Look getLook()
	{
		return look;
	}

	/**
	 * @param look tipos de aparências visuais do personagem.
	 */

	public void setLook(Look look)
	{
		this.look.copyFrom(look);
	}

	/**
	 * @return aquisição da identificação dos personagens que compõe a família.
	 */

	public Family getFamily()
	{
		return family;
	}

	/**
	 * @param family identificação dos personagens que compõe a família.
	 */

	public void setFamily(Family family)
	{
		this.family.copyFrom(family);
	}

	/**
	 * @return aquisição dos níveis de experiências já obtidos.
	 */

	public Experience getExperience()
	{
		return experience;
	}

	/**
	 * @param experience níveis de experiências já obtidos.
	 */

	public void setExperience(Experience experience)
	{
		this.experience.copyFrom(experience);
	}

	/**
	 * @return aquisição da classificação de uso do sistema de assistentes.
	 */

	public MercenaryRank getMercenaryRank()
	{
		return mercenaryRank;
	}

	/**
	 * @param mercenaryRank classificação de uso do sistema de assistentes.
	 */

	public void setMercenaryRank(MercenaryRank mercenaryRank)
	{
		this.mercenaryRank.copyFrom(mercenaryRank);
	}

	/**
	 * @return aquisição das localizações para pontos de retorno.
	 */

	public Locations getLocations()
	{
		return locations;
	}

	/**
	 * @param locations localizações para pontos de retorno.
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

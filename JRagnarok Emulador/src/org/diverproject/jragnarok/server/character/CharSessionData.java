package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.util.lang.IntUtil.interval;
import static org.diverproject.util.Util.format;
import static org.diverproject.util.Util.strcap;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.common.SessionData;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.jragnarok.server.common.entities.Vip;
import org.diverproject.jragnarok.server.login.entities.Pincode;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

/**
 * Dados da Sessão no Servidor de Personagem
 *
 * Objeto que guarda informações que irão ser carregadas juntos a uma conexão no servidor de personagem.
 * Informações estas que serão utilizadas em praticamente todas as funcionalidades disponíveis no servidor.
 * Essas informações são referentes a conta do jogador que está acessando e valores de autenticação.
 *
 * As informações disponíveis da conta são de: endereço de e-mail, data de aniversário, sexo do jogador,
 * informações de acesso vip, grupo de contas contido, código PIN, dados básicos de personagens,
 * temporizador para horário de bloqueio, quantidade de personagens movidos e seed de autenticação.
 *
 * @author Andrew
 */

public class CharSessionData extends SessionData
{
	/**
	 * Propriedade que identifica a sessão como recuperando itens vinculados do clã.
	 */
	public static final int RETRIEVING_GUILD_BOUND_ITEMS = 0x01;

	/**
	 * String contendo o nome de todas as propriedades da flag de uma sessão.
	 */
	public static final String FLAG_STRINGS[] = new String[] { "RETRIEVING_GUILD_BOUND_ITEMS" };


	/**
	 * Sessão autenticada pelo servidor de acesso.
	 */
	private boolean auth;

	/**
	 * Seed para autenticação do servidor com o cliente.
	 */
	private LoginSeed seed;

	/**
	 * Endereço de e-mail de registro da conta.
	 */
	private String email;

	/**
	 * Data de nascimento do jogador.
	 */
	private String birthdate;

	/**
	 * Horário de expiração da conta (0: desativado).
	 */
	private Time expiration;

	/**
	 * Sexo do jogador.
	 */
	private Sex sex;

	/**
	 * Informações de acesso VIP.
	 */
	private Vip vip;

	/**
	 * Informações do grupo de contas incluso.
	 */
	private Group group;

	/**
	 * Informações do código PIN.
	 */
	private Pincode pincode;

	/**
	 * Quantidade de slots adicionais para personagens.
	 */
	private byte charSlots;

	/**
	 * Vetor contendo as informações dos personagens carregados.
	 */
	private CharData chars[];

	/**
	 * Flag para determinar o tipo de acesso VIP do jogador.
	 */
	private BitWise flag;

	/**
	 * Temporizador para informar os personagens bloqueados.
	 */
	private Timer charBlockTime;

	/**
	 * Quantidade de vezes que personagens foram movidos na conta.
	 */
	private int charactersMove;

	/**
	 * Cria uma nova instância de um objeto para armazenar dados de uma sessão no servidor de personagem.
	 * Inicializa algumas dependências como horário de expedição, flag, vetor para dados dos personagens,
	 * seed de autenticação, código PIN, grupo de contas e informações VIP (todas em branco).
	 */

	public CharSessionData()
	{
		this.expiration = new Time();
		this.flag = new BitWise(FLAG_STRINGS);
		this.chars = new CharData[MAX_CHARS];
		this.seed = new LoginSeed();
		this.pincode = new Pincode();
		this.group = new Group();
		this.vip = new Vip();
	}

	/**
	 * @return sessão autenticada no servidor de acesso.
	 */

	public boolean isAuth()
	{
		return auth;
	}

	/**
	 * @param auth true para autenticar a sessão ou false caso contrário.
	 */

	public void setAuth(boolean auth)
	{
		this.auth = auth;
	}

	/**
	 * @return aquisição da seed para autenticação do servidor com o cliente.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @return aquisição do endereço de e-mail registrado pela conta.
	 */

	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email endereço de e-mail registrado pela conta.
	 */

	public void setEmail(String email)
	{
		this.email = strcap(email, EMAIL_LENGTH);
	}

	/**
	 * @return aquisição da data de aniversário do jogador.
	 */

	public String getBirthdate()
	{
		return birthdate;
	}

	/**
	 * @param birthdate data de aniversário do jogador.
	 */

	public void setBirthdate(String birthdate)
	{
		this.birthdate = strcap(birthdate, 10);
	}

	/**
	 * @return aquisição do horário em que a conta foi expirada.
	 */

	public Time getExpiration()
	{
		return expiration;
	}

	/**
	 * @return aquisição do sexo do jogador.
	 */

	public Sex getSex()
	{
		return sex;
	}

	/**
	 * @param sex sexo do jogador.
	 */

	public void setSex(Sex sex)
	{
		this.sex = sex;
	}

	/**
	 * @return aquisição das informações de acesso VIP da conta.
	 */

	public Vip getVip()
	{
		return vip;
	}

	/**
	 * @param vip informações de acesso VIP da conta.
	 */

	public void setVip(Vip vip)
	{
		this.vip = vip;
	}

	/**
	 * @return aquisição do grupo de contas em qua está contido.
	 */

	public Group getGroup()
	{
		return group;
	}

	/**
	 * @param group grupo de contas em qua está contido.
	 */

	public void setGroup(Group group)
	{
		this.group = group;
	}

	/**
	 * @return aquisição das informações de código PIN da conta.
	 */

	public Pincode getPincode()
	{
		return pincode;
	}

	/**
	 * @param pincode informações de código PIN da conta.
	 */

	public void setPincode(Pincode pincode)
	{
		this.pincode = pincode;
	}

	/**
	 * @return aquisição do número adicional de slots para personagens.
	 */

	public byte getCharSlots()
	{
		return charSlots;
	}

	/**
	 * @param charSlots número adicional de slots para personagens.
	 */

	public void setCharSlots(byte charSlots)
	{
		this.charSlots = charSlots;
	}

	/**
	 * @param index número de slot do personagem desejado.
	 * @return aquisição de informações base de um personagem.
	 */

	public CharData getCharData(int index)
	{
		return interval(index, 0, chars.length - 1) ? chars[index] : null;
	}

	/**
	 * @param data objeto com as informações base do personagem.
	 * @param index número de slot do personagem à definir.
	 */

	public void setCharData(CharData data, int index)
	{
		if (interval(index, 0, chars.length - 1))
			chars[index] = data;
	}

	/**
	 * @return aquisição da flag para configurar o tipo de acesso VIP.
	 */

	public BitWise getFlag()
	{
		return flag;
	}

	/**
	 * @return aquisição do temporizador que informa o cliente dos personagens banidos.
	 */

	public Timer getCharBlockTime()
	{
		return charBlockTime;
	}

	/**
	 * @param charBlockTime temporizador que informa o cliente dos personagens banidos.
	 */

	public void setCharBlockTime(Timer charBlockTime)
	{
		this.charBlockTime = charBlockTime;
	}

	/**
	 * @return aquisição da quantidade de vezes que personagens foram movidos na conta.
	 */

	public int getCharactersMove()
	{
		return charactersMove;
	}

	/**
	 * @param charactersMove quantidade de vezes que personagens foram movidos na conta.
	 */

	public void setCharactersMove(int charactersMove)
	{
		this.charactersMove = charactersMove;
	}

	@Override
	public void toString(ObjectDescription description)
	{
		if (auth)
			description.append("auth");

		if (seed != null)
			description.append("seed", format("%d %d", seed.getFirst(), seed.getSecond()));

		description.append("email", email);
		description.append("birthdate", birthdate);
		description.append("expiration", expiration);

		if (vip != null)	description.append("vip", vip.getName());
		if (group != null)	description.append("group", group.getName());
		if (pincode != null)description.append("pincode", pincode.getCode());

		for (int i = 0, j = 0; i < chars.length; i++)
		{
			if (chars[i] != null)
				j++;

			if (i == chars.length - 1)
				description.append("chars", j);
		}

		description.append("flag", flag.toStringProperties());
		description.append("charBlockTime", charBlockTime);
	}
}

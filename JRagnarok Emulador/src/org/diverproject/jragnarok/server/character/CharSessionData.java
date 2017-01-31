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
 * Dados da Sess�o no Servidor de Personagem
 *
 * Objeto que guarda informa��es que ir�o ser carregadas juntos a uma conex�o no servidor de personagem.
 * Informa��es estas que ser�o utilizadas em praticamente todas as funcionalidades dispon�veis no servidor.
 * Essas informa��es s�o referentes a conta do jogador que est� acessando e valores de autentica��o.
 *
 * As informa��es dispon�veis da conta s�o de: endere�o de e-mail, data de anivers�rio, sexo do jogador,
 * informa��es de acesso vip, grupo de contas contido, c�digo PIN, dados b�sicos de personagens,
 * temporizador para hor�rio de bloqueio, quantidade de personagens movidos e seed de autentica��o.
 *
 * @author Andrew
 */

public class CharSessionData extends SessionData
{
	/**
	 * Propriedade que identifica a sess�o como recuperando itens vinculados do cl�.
	 */
	public static final int RETRIEVING_GUILD_BOUND_ITEMS = 0x01;

	/**
	 * String contendo o nome de todas as propriedades da flag de uma sess�o.
	 */
	public static final String FLAG_STRINGS[] = new String[] { "RETRIEVING_GUILD_BOUND_ITEMS" };


	/**
	 * Sess�o autenticada pelo servidor de acesso.
	 */
	private boolean auth;

	/**
	 * Seed para autentica��o do servidor com o cliente.
	 */
	private LoginSeed seed;

	/**
	 * Endere�o de e-mail de registro da conta.
	 */
	private String email;

	/**
	 * Data de nascimento do jogador.
	 */
	private String birthdate;

	/**
	 * Hor�rio de expira��o da conta (0: desativado).
	 */
	private Time expiration;

	/**
	 * Sexo do jogador.
	 */
	private Sex sex;

	/**
	 * Informa��es de acesso VIP.
	 */
	private Vip vip;

	/**
	 * Informa��es do grupo de contas incluso.
	 */
	private Group group;

	/**
	 * Informa��es do c�digo PIN.
	 */
	private Pincode pincode;

	/**
	 * Quantidade de slots adicionais para personagens.
	 */
	private byte charSlots;

	/**
	 * Vetor contendo as informa��es dos personagens carregados.
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
	 * Cria uma nova inst�ncia de um objeto para armazenar dados de uma sess�o no servidor de personagem.
	 * Inicializa algumas depend�ncias como hor�rio de expedi��o, flag, vetor para dados dos personagens,
	 * seed de autentica��o, c�digo PIN, grupo de contas e informa��es VIP (todas em branco).
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
	 * @return sess�o autenticada no servidor de acesso.
	 */

	public boolean isAuth()
	{
		return auth;
	}

	/**
	 * @param auth true para autenticar a sess�o ou false caso contr�rio.
	 */

	public void setAuth(boolean auth)
	{
		this.auth = auth;
	}

	/**
	 * @return aquisi��o da seed para autentica��o do servidor com o cliente.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @return aquisi��o do endere�o de e-mail registrado pela conta.
	 */

	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email endere�o de e-mail registrado pela conta.
	 */

	public void setEmail(String email)
	{
		this.email = strcap(email, EMAIL_LENGTH);
	}

	/**
	 * @return aquisi��o da data de anivers�rio do jogador.
	 */

	public String getBirthdate()
	{
		return birthdate;
	}

	/**
	 * @param birthdate data de anivers�rio do jogador.
	 */

	public void setBirthdate(String birthdate)
	{
		this.birthdate = strcap(birthdate, 10);
	}

	/**
	 * @return aquisi��o do hor�rio em que a conta foi expirada.
	 */

	public Time getExpiration()
	{
		return expiration;
	}

	/**
	 * @return aquisi��o do sexo do jogador.
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
	 * @return aquisi��o das informa��es de acesso VIP da conta.
	 */

	public Vip getVip()
	{
		return vip;
	}

	/**
	 * @param vip informa��es de acesso VIP da conta.
	 */

	public void setVip(Vip vip)
	{
		this.vip = vip;
	}

	/**
	 * @return aquisi��o do grupo de contas em qua est� contido.
	 */

	public Group getGroup()
	{
		return group;
	}

	/**
	 * @param group grupo de contas em qua est� contido.
	 */

	public void setGroup(Group group)
	{
		this.group = group;
	}

	/**
	 * @return aquisi��o das informa��es de c�digo PIN da conta.
	 */

	public Pincode getPincode()
	{
		return pincode;
	}

	/**
	 * @param pincode informa��es de c�digo PIN da conta.
	 */

	public void setPincode(Pincode pincode)
	{
		this.pincode = pincode;
	}

	/**
	 * @return aquisi��o do n�mero adicional de slots para personagens.
	 */

	public byte getCharSlots()
	{
		return charSlots;
	}

	/**
	 * @param charSlots n�mero adicional de slots para personagens.
	 */

	public void setCharSlots(byte charSlots)
	{
		this.charSlots = charSlots;
	}

	/**
	 * @param index n�mero de slot do personagem desejado.
	 * @return aquisi��o de informa��es base de um personagem.
	 */

	public CharData getCharData(int index)
	{
		return interval(index, 0, chars.length - 1) ? chars[index] : null;
	}

	/**
	 * @param data objeto com as informa��es base do personagem.
	 * @param index n�mero de slot do personagem � definir.
	 */

	public void setCharData(CharData data, int index)
	{
		if (interval(index, 0, chars.length - 1))
			chars[index] = data;
	}

	/**
	 * @return aquisi��o da flag para configurar o tipo de acesso VIP.
	 */

	public BitWise getFlag()
	{
		return flag;
	}

	/**
	 * @return aquisi��o do temporizador que informa o cliente dos personagens banidos.
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
	 * @return aquisi��o da quantidade de vezes que personagens foram movidos na conta.
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

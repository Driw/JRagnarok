package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokConstants.PASSWORD_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.USERNAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.server.common.Sex.FEMALE;
import static org.diverproject.jragnarok.server.common.Sex.SERVER;

import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.common.SessionData;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.util.BitWise8;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

/**
 * <h1>Dados da Sess�o de Acesso</h1>
 *
 * <p>Os dados desse tipo de sess�o s�o definidos para serem utilizados no servidor de acesso.
 * Dever�o conter informa��es informadas pelo jogador e cliente execut�vel do qual usou.
 * Algumas outras informa��es ser�o definidas ap�s o servidor de acesso autorizar.</p>
 *
 * <p>As informa��es que s�o definidas no servidor s�o: �ltimo acesso, data de registro,
 * grupo de contas do jogador, criptografia de senha, seed do acesso e dados de senha MD5.
 * As informa��es obtidas do jogador s�o: usu�rio, senha, tipo de cliente e vers�o do cliente.</p>
 *
 * @see SessionData
 * @see Login
 * @see Time
 * @see Group
 * @see BitWise8
 * @see LoginSeed
 * @see ClientHash
 *
 * @author Andrew
 */

public class LoginSessionData extends SessionData implements Login
{
	/**
	 * C�digo para criptografia de tipo 1.
	 */
	public static final int PASSWORD_DENCRYPT = 1;

	/**
	 * C�digo para criptografia de tipo 2.
	 */
	public static final int PASSWORD_DENCRYPT2 = 2;

	/**
	 * Nome das criptografias para Bitwise.
	 */
	private static final String DENCRYPT_STRING[] = new String[]
	{
		"DENCRYPT", "DENCRYPT2"
	};


	/**
	 * Nome de usu�rio da conta do jogador.
	 */
	private String username;

	/**
	 * Senha respectiva ao nome de usu�rio.
	 */
	private String password;

	/**
	 * Enumera��o representativa do sexo da conta no sistema.
	 */
	private Sex sex;

	/**
	 * Hor�rio do �ltimo acesso desta conta no sistema.
	 */
	private Time lastLogin;

	/**
	 * Hor�rio em que a conta foi criada no sistema.
	 */
	private Time registered;

	/**
	 * Informa��es do grupo de contas do jogador.
	 */
	private Group group;

	/**
	 * Tipo de criptografia que ser� usada na senha.
	 */
	private BitWise8 passDencrypt;

	/**
	 * Seed para valida��o das informa��es dessa sess�o.
	 */
	private LoginSeed seed;

	/**
	 * Hash para valida��o do cliente execut�vel.
	 */
	private ClientHash clientHash;

	/**
	 * Comprimento da chave MD5 solicitada pelo cliente.
	 */
	private short md5KeyLenght;

	/**
	 * Chave MD5 gerada por solicita��o do cliente.
	 */
	private String md5Key;

	/**
	 * Cria uma nova inst�ncia para dados de uma sess�o dentro do servidor de acesso.
	 * Inicializa os hor�rios de registro e �ltimo acesso e configura��es de criptografia.
	 */

	public LoginSessionData()
	{
		lastLogin = new Time(System.currentTimeMillis());
		registered = new Time(System.currentTimeMillis());
		passDencrypt = new BitWise8(DENCRYPT_STRING);
		seed = new LoginSeed();

		sex = FEMALE;
	}

	/**
	 * @return aquisi��o do nome de usu�rio da conta do jogador.
	 */

	public String getUsername()
	{
		return username;
	}

	/**
	 * @param username nome de usu�rio da conta do jogador.
	 */

	public void setUsername(String username)
	{
		this.username = strcap(username, USERNAME_LENGTH);
	}

	/**
	 * @return aquisi��o da senha referente ao nome de usu�rio.
	 */

	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password senha respectiva a conta do jogador.
	 */

	public void setPassword(String password)
	{
		this.password = strcap(password, PASSWORD_LENGTH);
	}

	/**
	 * @return aquisi��o da enumera��o representativa do sexo da conta no sistema.
	 */

	public Sex getSex()
	{
		return sex;
	}

	/**
	 * @param sex enumera��o representativa do sexo da conta no sistema.
	 */

	public void setSex(Sex sex)
	{
		if (sex != null && sex != SERVER)
			this.sex = sex;
	}

	/**
	 * @return aquisi��o do hor�rio em que foi feito o �ltimo acesso.
	 */

	public Time getLastLogin()
	{
		return lastLogin;
	}

	/**
	 * @return aquisi��o do hor�rio em que a conta foi criada.
	 */

	public Time getRegistered()
	{
		return registered;
	}

	/**
	 * @return aquisi��o da grupo de contas do jogador.
	 */

	public Group getGroup()
	{
		return group;
	}

	/**
	 * @param group grupo de contas do jogador.
	 */

	public void setGroup(Group group)
	{
		this.group = group;
	}

	/**
	 * @return aquisi��o das configura��es de criptografia de senha.
	 */

	public BitWise8 getPassDencrypt()
	{
		return passDencrypt;
	}

	/**
	 * @return aquisi��o da seed para valida��o da sess�o.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @return aquisi��o do hash para valida��o do cliente.
	 */

	public ClientHash getClientHash()
	{
		return clientHash;
	}

	/**
	 * @param clientHash hash para valida��o do cliente.
	 */

	public void setClientHash(ClientHash clientHash)
	{
		this.clientHash = clientHash;
	}

	/**
	 * @return aquisi��o comprimento da chave MD5 solicitada pelo cliente.
	 */

	public short getMd5KeyLenght()
	{
		return md5KeyLenght;
	}

	/**
	 * @param md5KeyLenght comprimento da chave MD5 solicitada pelo cliente.
	 */

	public void setMd5KeyLenght(short md5KeyLenght)
	{
		this.md5KeyLenght = md5KeyLenght;
	}

	/**
	 * @return aquisi��o da chave MD5 gerada por solicita��o do cliente.
	 */

	public String getMd5Key()
	{
		return md5Key;
	}

	/**
	 * @param md5Key chave MD5 gerada por solicita��o do cliente.
	 */

	public void setMd5Key(String md5Key)
	{
		this.md5Key = strcap(md5Key, 20);
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		description.append("username", username);
		description.append("password", password);
		description.append("sex", sex);
		description.append("lastLogin", lastLogin);
		description.append("group", group);
		description.append("passDencrypt", passDencrypt);

		if (clientHash != null)
			description.append("clientHash", new String(clientHash.getHashString()));

		if (seed != null)
			description.append("seed", format("%d %d", seed.getFirst(), seed.getSecond()));

		if (md5KeyLenght > 0)
		{
			description.append("md5KeyLenght", md5KeyLenght);
			description.append("md5Key", md5Key);
		}
	}
}

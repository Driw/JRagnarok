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
 * <h1>Dados da Sessão de Acesso</h1>
 *
 * <p>Os dados desse tipo de sessão são definidos para serem utilizados no servidor de acesso.
 * Deverão conter informações informadas pelo jogador e cliente executável do qual usou.
 * Algumas outras informações serão definidas após o servidor de acesso autorizar.</p>
 *
 * <p>As informações que são definidas no servidor são: último acesso, data de registro,
 * grupo de contas do jogador, criptografia de senha, seed do acesso e dados de senha MD5.
 * As informações obtidas do jogador são: usuário, senha, tipo de cliente e versão do cliente.</p>
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
	 * Código para criptografia de tipo 1.
	 */
	public static final int PASSWORD_DENCRYPT = 1;

	/**
	 * Código para criptografia de tipo 2.
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
	 * Nome de usuário da conta do jogador.
	 */
	private String username;

	/**
	 * Senha respectiva ao nome de usuário.
	 */
	private String password;

	/**
	 * Enumeração representativa do sexo da conta no sistema.
	 */
	private Sex sex;

	/**
	 * Horário do último acesso desta conta no sistema.
	 */
	private Time lastLogin;

	/**
	 * Horário em que a conta foi criada no sistema.
	 */
	private Time registered;

	/**
	 * Informações do grupo de contas do jogador.
	 */
	private Group group;

	/**
	 * Tipo de criptografia que será usada na senha.
	 */
	private BitWise8 passDencrypt;

	/**
	 * Seed para validação das informações dessa sessão.
	 */
	private LoginSeed seed;

	/**
	 * Hash para validação do cliente executável.
	 */
	private ClientHash clientHash;

	/**
	 * Comprimento da chave MD5 solicitada pelo cliente.
	 */
	private short md5KeyLenght;

	/**
	 * Chave MD5 gerada por solicitação do cliente.
	 */
	private String md5Key;

	/**
	 * Cria uma nova instância para dados de uma sessão dentro do servidor de acesso.
	 * Inicializa os horários de registro e último acesso e configurações de criptografia.
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
	 * @return aquisição do nome de usuário da conta do jogador.
	 */

	public String getUsername()
	{
		return username;
	}

	/**
	 * @param username nome de usuário da conta do jogador.
	 */

	public void setUsername(String username)
	{
		this.username = strcap(username, USERNAME_LENGTH);
	}

	/**
	 * @return aquisição da senha referente ao nome de usuário.
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
	 * @return aquisição da enumeração representativa do sexo da conta no sistema.
	 */

	public Sex getSex()
	{
		return sex;
	}

	/**
	 * @param sex enumeração representativa do sexo da conta no sistema.
	 */

	public void setSex(Sex sex)
	{
		if (sex != null && sex != SERVER)
			this.sex = sex;
	}

	/**
	 * @return aquisição do horário em que foi feito o último acesso.
	 */

	public Time getLastLogin()
	{
		return lastLogin;
	}

	/**
	 * @return aquisição do horário em que a conta foi criada.
	 */

	public Time getRegistered()
	{
		return registered;
	}

	/**
	 * @return aquisição da grupo de contas do jogador.
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
	 * @return aquisição das configurações de criptografia de senha.
	 */

	public BitWise8 getPassDencrypt()
	{
		return passDencrypt;
	}

	/**
	 * @return aquisição da seed para validação da sessão.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @return aquisição do hash para validação do cliente.
	 */

	public ClientHash getClientHash()
	{
		return clientHash;
	}

	/**
	 * @param clientHash hash para validação do cliente.
	 */

	public void setClientHash(ClientHash clientHash)
	{
		this.clientHash = clientHash;
	}

	/**
	 * @return aquisição comprimento da chave MD5 solicitada pelo cliente.
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
	 * @return aquisição da chave MD5 gerada por solicitação do cliente.
	 */

	public String getMd5Key()
	{
		return md5Key;
	}

	/**
	 * @param md5Key chave MD5 gerada por solicitação do cliente.
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

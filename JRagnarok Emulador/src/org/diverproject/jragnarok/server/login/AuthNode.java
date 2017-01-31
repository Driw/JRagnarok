package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.server.common.Sex.FEMALE;
import static org.diverproject.jragnarok.server.common.Sex.SERVER;
import static org.diverproject.util.Util.format;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.util.ObjectDescription;

/**
 * Nó de Autenticação
 *
 * <p>Um nó de autenticação é usado no servidor de acesso para garantir a autenticação de uma conta.
 * Quando a autenticação do usuário e senha passado pelo jogador são validados o nó é registrado.</p>
 *
 * <p>Assim que o jogador selecionar o servidor de personagem ele irá solicitar esse nó de autenticação.
 * Após a solicitação feita, o nó é removido do servidor de acesso repassando as informações ao
 * servidor de personagem, concluindo portanto a autenticação completa do acesso da conta no sistema.</p>
 *
 * <p>Para este nó será necessário identificar a conta e informações básicas do cliente com o servidor.
 * As informações são de: seed gerado pelo servidor de acesso (primeira e segunda seed); endereço de IP;
 * versão do cliente e tipo de cliente que são fornecidos somente ao servidor de acesso.</p>
 *
 * @see LoginSeed
 * @see InternetProtocol
 * @see ClientType
 *
 * @author Andrew
 */

public class AuthNode
{
	/**
	 * Código de identificação da conta do jogador.
	 */
	private int accountID;

	/**
	 * Enumeração representativa do sexo da conta no sistema.
	 */
	private Sex sex;

	/**
	 * Seed gerada pelo servidor de acesso.
	 */
	private LoginSeed seed;

	/**
	 * Endereço de IP usado pelo cliente para se conectar ao servidor.
	 */
	private InternetProtocol ip;

	/**
	 * Versão do cliente que influencia em quais dados serão transmitidos.
	 */
	private int version;

	/**
	 * Tipo do cliente.
	 */
	private ClientType clientType;

	/**
	 * Cria uma nova instância de um nó de autenticação inicializando o endereço de IP.
	 */

	public AuthNode()
	{
		ip = new InternetProtocol();
		seed = new LoginSeed();
		sex = FEMALE;
	}

	/**
	 * @return aquisição do código de identificação da conta do jogador.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @param accountID código de identificação da conta do jogador.
	 */

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
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
	 * @return aquisição da seed que foi gerada pelo servidor de acesso.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @return aquisição do endereço de IP usado pelo cliente.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * @return aquisição da versão usada pelo cliente para se conectar.
	 */

	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version versão usada pelo cliente para se conectar.
	 */

	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * @return aquisição do tipo de cliente usado para se conectar.
	 */

	public ClientType getClientType()
	{
		return clientType;
	}

	/**
	 * @param clientType tipo de cliente usado para se conectar.
	 */

	public void setClientType(ClientType clientType)
	{
		this.clientType = clientType;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);
		description.append("seed", format("%d|%d", seed.getFirst(), seed.getSecond()));
		description.append("ip", ip != null ? ip.getString() : null);
		description.append("version", version);
		description.append("clientType", clientType);

		return description.toString();
	}
}

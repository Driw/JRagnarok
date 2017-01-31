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
 * N� de Autentica��o
 *
 * <p>Um n� de autentica��o � usado no servidor de acesso para garantir a autentica��o de uma conta.
 * Quando a autentica��o do usu�rio e senha passado pelo jogador s�o validados o n� � registrado.</p>
 *
 * <p>Assim que o jogador selecionar o servidor de personagem ele ir� solicitar esse n� de autentica��o.
 * Ap�s a solicita��o feita, o n� � removido do servidor de acesso repassando as informa��es ao
 * servidor de personagem, concluindo portanto a autentica��o completa do acesso da conta no sistema.</p>
 *
 * <p>Para este n� ser� necess�rio identificar a conta e informa��es b�sicas do cliente com o servidor.
 * As informa��es s�o de: seed gerado pelo servidor de acesso (primeira e segunda seed); endere�o de IP;
 * vers�o do cliente e tipo de cliente que s�o fornecidos somente ao servidor de acesso.</p>
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
	 * C�digo de identifica��o da conta do jogador.
	 */
	private int accountID;

	/**
	 * Enumera��o representativa do sexo da conta no sistema.
	 */
	private Sex sex;

	/**
	 * Seed gerada pelo servidor de acesso.
	 */
	private LoginSeed seed;

	/**
	 * Endere�o de IP usado pelo cliente para se conectar ao servidor.
	 */
	private InternetProtocol ip;

	/**
	 * Vers�o do cliente que influencia em quais dados ser�o transmitidos.
	 */
	private int version;

	/**
	 * Tipo do cliente.
	 */
	private ClientType clientType;

	/**
	 * Cria uma nova inst�ncia de um n� de autentica��o inicializando o endere�o de IP.
	 */

	public AuthNode()
	{
		ip = new InternetProtocol();
		seed = new LoginSeed();
		sex = FEMALE;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o da conta do jogador.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @param accountID c�digo de identifica��o da conta do jogador.
	 */

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
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
	 * @return aquisi��o da seed que foi gerada pelo servidor de acesso.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @return aquisi��o do endere�o de IP usado pelo cliente.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * @return aquisi��o da vers�o usada pelo cliente para se conectar.
	 */

	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version vers�o usada pelo cliente para se conectar.
	 */

	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * @return aquisi��o do tipo de cliente usado para se conectar.
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

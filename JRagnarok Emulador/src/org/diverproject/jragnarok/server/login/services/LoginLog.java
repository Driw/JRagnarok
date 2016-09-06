package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

/**
 * <h1>Registro de Acesso</h1>
 *
 * <p>Classe de entidade usada para armazenar informa��es do registro de um acesso no servidor.
 * Dever� especificar o momento, da onde foi acesso (IP) e quem o acessou com o nome de usu�rio.
 * Ser� vinculado ainda um c�digo representando o ocorrido e uma mensagem se necess�rio.</p>
 *
 * @see Time
 *
 * @author Andrew
 */

public class LoginLog
{
	/**
	 * Hor�rio em que o registro foi feito.
	 */
	private Time time;

	/**
	 * Endere�o de IP de quem realizou o acesso.
	 */
	private InternetProtocol ip;

	/**
	 * Nome de usu�rio da conta que acessou.
	 */
	private String user;

	/**
	 * C�digo referente ao resultado a a��o do acesso.
	 */
	private int rCode;

	/**
	 * Mensagem a ser vinculada junto ao registro.
	 */
	private String message;

	/**
	 * Cria um novo registro de acesso inicializando o endere�o de IP e hor�rio.
	 * O hor�rio por padr�o ser� definido no momento da inst�ncia e o IP em localhost.
	 */

	public LoginLog()
	{
		message = "";
		time = new Time(System.currentTimeMillis());
		ip = new InternetProtocol();
	}

	/**
	 * Hor�rio determina o momento em que o registro do acesso foi criado.
	 * @return aquisi��o do objeto que permite trabalhar com tempo.
	 */

	public Time getTime()
	{
		return time;
	}

	/**
	 * Endere�o de IP permite saber da onde o acesso foi realizado.
	 * @return aquisi��o do objeto que permite trabalhar com IP.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * Nome de usu�rio permite saber qual a conta que solicitou o acesso.
	 * @return aquisi��o do nome de usu�rio (�nico) da conta acessada.
	 */

	public String getUser()
	{
		return user;
	}

	/**
	 * Define um novo nome de usu�rio para esse registro de acesso.
	 * @param user nome de usu�rio (�nico) da conta acessada.
	 */

	public void setUser(String user)
	{
		this.user = strcap(user, 24);
	}

	/**
	 * RCode � um c�digo que permite saber qual foi o resultado do acesso no servidor.
	 * @return aquisi��o do c�digo do resultado da solicita��o do acesso.
	 */

	public int getRCode()
	{
		return rCode;
	}

	/**
	 * RCode � um c�digo que permite saber qual foi o resultado do acesso no servidor.
	 * @param rCode c�digo do resultado da solicita��o do acesso.
	 */

	public void setRCode(int rCode)
	{
		if (rCode >= 0)
			this.rCode = rCode;
	}

	/**
	 * Mensagem � usado para vincular uma informa��o visual e leg�vel ao registro.
	 * @return aquisi��o da mensagem que foi definida para detalhar o registro.
	 */

	public String getMessage()
	{
		return message;
	}

	/**
	 * Mensagem � usado para vincular uma informa��o visual e leg�vel ao registro.
	 * @param log mensagem a ser definida para detalhar o registro.
	 */

	public void setMessage(String log)
	{
		if (log != null)
			this.message = strcap(log, 255);
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("time", time);
		description.append("ip", ip.getString());
		description.append("user", user);
		description.append("rCode", rCode);
		description.append("message", message);

		return description.toString();
	}
}

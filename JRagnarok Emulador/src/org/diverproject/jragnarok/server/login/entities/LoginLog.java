package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

/**
 * <h1>Registro de Acesso</h1>
 *
 * <p>Classe de entidade usada para armazenar informações do registro de um acesso no servidor.
 * Deverá especificar o momento, da onde foi acesso (IP) e quem o acessou com o nome de usuário.
 * Será vinculado ainda um código representando o ocorrido e uma mensagem se necessário.</p>
 *
 * @see Time
 *
 * @author Andrew
 */

public class LoginLog
{
	/**
	 * Horário em que o registro foi feito.
	 */
	private Time time;

	/**
	 * Endereço de IP de quem realizou o acesso.
	 */
	private InternetProtocol ip;

	/**
	 * Informações do usuário a registrar o acesso.
	 */
	private Login login;

	/**
	 * Código referente ao resultado a ação do acesso.
	 */
	private int rCode;

	/**
	 * Mensagem a ser vinculada junto ao registro.
	 */
	private String message;

	/**
	 * Cria um novo registro de acesso inicializando o endereço de IP e horário.
	 * O horário por padrão será definido no momento da instância e o IP em localhost.
	 */

	public LoginLog()
	{
		message = "";
		time = new Time(System.currentTimeMillis());
		ip = new InternetProtocol();
	}

	/**
	 * Horário determina o momento em que o registro do acesso foi criado.
	 * @return aquisição do objeto que permite trabalhar com tempo.
	 */

	public Time getTime()
	{
		return time;
	}

	/**
	 * Endereço de IP permite saber da onde o acesso foi realizado.
	 * @return aquisição do objeto que permite trabalhar com IP.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * Todo registro de acesso é vinculado a algum usuário que o fez.
	 * @return aquisição do objeto contendo os dados do acesso.
	 */

	public Login getLogin()
	{
		return login;
	}

	/**
	 * Todo registro de acesso é vinculado a algum usuário que o fez.
	 * @param login referência do objeto contendo os dados do acesso.
	 */

	public void setLogin(Login login)
	{
		this.login = login;
	}

	/**
	 * RCode é um código que permite saber qual foi o resultado do acesso no servidor.
	 * @return aquisição do código do resultado da solicitação do acesso.
	 */

	public int getRCode()
	{
		return rCode;
	}

	/**
	 * RCode é um código que permite saber qual foi o resultado do acesso no servidor.
	 * @param rCode código do resultado da solicitação do acesso.
	 */

	public void setRCode(int rCode)
	{
		if (rCode >= 0)
			this.rCode = rCode;
	}

	/**
	 * Mensagem é usado para vincular uma informação visual e legível ao registro.
	 * @return aquisição da mensagem que foi definida para detalhar o registro.
	 */

	public String getMessage()
	{
		return message;
	}

	/**
	 * Mensagem é usado para vincular uma informação visual e legível ao registro.
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

		description.append("login", login != null ? login.getID() : null);
		description.append("time", time);
		description.append("ip", ip.getString());
		description.append("rCode", rCode);
		description.append("message", message);

		return description.toString();
	}
}

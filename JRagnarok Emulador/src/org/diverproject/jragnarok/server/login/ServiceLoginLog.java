package org.diverproject.jragnarok.server.login;

import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.login.control.LoginLogControl;
import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.jragnarok.server.login.entities.LoginLog;

/**
 * <h1>Servi�o para Registro de Acessos</h1>
 *
 * <p>Servi�o designado para realizar a adi��o de novos registros de acessos e contagens do mesmo.
 * A realiza��o desses registros devem ser feitas apenas durante ou p�s autentica��o de uma conta.
 * Quanto a contagem ser� necess�rio especificar um intervalo de tempo para que possa ser realizada.</p>
 *
 * @see AbstractServiceLogin
 * @see LoginServer
 * @see LoginLogControl
 * @see Login
 * @see InternetProtocol
 *
 * @author Andrew
 */

public class ServiceLoginLog extends AbstractServiceLogin
{
	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	private LoginLogControl logs;

	/**
	 * Cria um novo servi�o para registro de acessos no banco de dados.
	 * @param server servidor de acesso que ir� utilizar o servi�o.
	 */

	public ServiceLoginLog(LoginServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		logs = getServer().getFacade().getLoginLogControl();
	}

	@Override
	public void destroy()
	{
		logs = null;
	}

	/**
	 * Adiciona um novo registro de acesso identificando o resultado do mesmo.
	 * @param ip endere�o de IP do cliente que realizou o acesso com o servidor.
	 * @param login objeto contendo as informa��es de acesso do cliente.
	 * @param code c�digo resultante da solicita��o do acesso com o servidor.
	 * @param message mensagem que ser� vinculada ao registro referente ao resultado.
	 */

	public void add(int ip, Login login, int code, String message)
	{
		add(new InternetProtocol(ip), login, code, message);
	}

	/**
	 * Adiciona um novo registro de acesso identificando o resultado do mesmo.
	 * @param ip endere�o de IP do cliente que realizou o acesso com o servidor.
	 * @param login objeto contendo as informa��es de acesso do cliente.
	 * @param code c�digo resultante da solicita��o do acesso com o servidor.
	 * @param message mensagem que ser� vinculada ao registro referente ao resultado.
	 */

	public void add(InternetProtocol ip, Login login, int code, String message)
	{
		try {

			LoginLog log = new LoginLog();
			log.getTime().set(System.currentTimeMillis());
			log.getIP().copy(ip);
			log.setLogin(login);
			log.setRCode(code);
			log.setMessage(message);

			if (!logs.add(log))
				logWarning("falha ao registrar log (ip: %s, username: %s)", ip, login.getUsername());

		} catch (RagnarokException e) {
			logExeception(e);
		}
	}

	/**
	 * Atrav�s de acessos j� realizados faz a contagem de quantos acessos ocorreram.
	 * A contagem � feita considerando os registros dos acesso que falharam.
	 * @param ip endere�o de IP do qual deseja contar as tentativas falhas.
	 * @param minutes no intervalo de quantos minutos ser� considerado.
	 * @return aquisi��o da quantidade de tentativas falhas nos �ltimos minutos.
	 */

	public int getFailedAttempts(String ip, int minutes)
	{
		int failures = 0;

		try {
			failures = logs.getFailedAttempts(ip, minutes);
		} catch (RagnarokException e) {
			logExeception(e);
		}

		return failures;
	}
}

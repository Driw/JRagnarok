package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.JRagnarokUtil.skip;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_CLEANUP_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_LIMIT;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.login.toclient.AC_RefuseLogin;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.common.AuthResult;
import org.diverproject.jragnarok.server.login.control.IpBanControl;
import org.diverproject.util.SocketUtil;

/**
 * <h1>Servi�o para Banimento de Endere�os IP</h1>
 *
 * <p>Esse servi�o permite que determinados endere�os de IP possam ser banidos do servi�o de acesso.
 * Uma vez que um endere�o de IP tenha sido banido, seu acesso n�o ser� autorizado de maneira alguma.
 * Assim sendo, seja um cliente ou servidor, n�o poder� se conectar no mesmo tendo nenhuma utilidade.</p>
 *
 * <p>O principal banimento � em casos em que um cliente (jogador) tenta se conectar com senha inv�lida.
 * � feita uma contagem interna atrav�s de logs que mostram a quantidade de tentativas recentes.
 * Caso tenha passado do limite (configurado) o endere�o de IP desse jogador � banido temporariamente.</p>
 *
 * <p>Esse servi�o pode ser usado ainda para banir o acesso de endere�os de IP de locais espec�ficos do mundo.
 * Por exemplo, o servidor n�o quer que jogadores de um determinado pa�s acesse o servidor de acesso.
 * Assim, basta banir endere�os de IP que sejam igual a 180.*.*.* do sistema (supondo que seja esse o do pa�s).</p>
 *
 * @see AbstractServiceLogin
 * @see LoginServer
 * @see ServiceLoginLog
 * @see IpBanControl
 *
 * @author Andrew
 */

public class ServiceLoginIpBan extends AbstractServiceLogin
{
	/**
	 * Servi�o para registro de acessos.
	 */
	private ServiceLoginLog log;

	/**
	 * Controle para banimento de endere�os de IP.
	 */
	private IpBanControl ipbans;

	/**
	 * Cria uma nova inst�ncia do servi�o para banimento de endere�os IP.
	 * @param server servidor de acesso que ir� utilizar esse servi�o.
	 */

	public ServiceLoginIpBan(LoginServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		log = getServer().getFacade().getLogService();
		ipbans = getServer().getFacade().getIpBanControl();

		int interval = getConfigs().getInt(IPBAN_CLEANUP_INTERVAL);

		if (interval > 0)
		{
			TimerMap timers = getTimerSystem().getTimers();

			Timer timer = timers.acquireTimer();
			timer.setTick(getTimerSystem().getCurrentTime());
			timer.setListener(cleanup);
			timers.addLoop(timer, seconds(interval));
		}
	}

	@Override
	public void destroy()
	{
		log = null;
		ipbans = null;
	}

	/**
	 * Listener usado pelo temporizador que ir� garantir que endere�os de IP n�o fiquem banidos para sempre.
	 * Ir� chamar o controle dos banimentos para endere�os de IP e realizar uma limpeza no mesmo.
	 * A limpeza consiste em remover do cache e banco de dados todos os banimentos que estejam expirados.
	 */

	private TimerListener cleanup = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			try {

				logNotice("%d listas de endere�os de IP banidos expirados.\n", ipbans.cleanup());

			} catch (RagnarokException e) {
				logError("falha durante a limpeza de endere�os de IP banidos expirados:\n");
				logException(e);
			}
		}
		
		@Override
		public String getName()
		{
			return "cleanup";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	/**
	 * @return true se o servi�o estiver habilitado para ser utilizado ou false caso contr�rio.
	 */

	private boolean isEnabled()
	{
		return getConfigs().getBool(IPBAN_ENABLED);
	}

	/**
	 * Verifica se um endere�o de IP especificado est� banido do sistema em alguma das listas.
	 * Caso o servi�o esteja configurado para n�o ser utilizado, este m�todo n�o ter� efeito.
	 * @param ip n�mero inteiro que representa o endere�o de IP (4 bytes | 4 d�gitos).
	 * @return true se estiver contido em alguma lista de ban ou false caso contr�rio.
	 */

	public boolean isBanned(int ip)
	{
		if (!isEnabled())
			return false;

		return ipbans.addressBanned(ip);
	}

	/**
	 * Verifica quantas tentativas de acesso falha um determinado endere�o de IP sofreu.
	 * Caso essa quantidade de tentativas tenha passado o limite permitido (configurado),
	 * ser� registrado o endere�o passado por par�metro como banido temporariamente.
	 * @param ipAddress endere�o de IP formatado da seguinte forma: %d.%d.%d.%d.
	 */

	public boolean addBanLog(String ipAddress)
	{
		if (SocketUtil.isIP(ipAddress))
		{
			int minutes = getConfigs().getInt(IPBAN_PASS_FAILURE_INTERVAL);
			int limit = getConfigs().getInt(IPBAN_PASS_FAILURE_LIMIT);
			int failures = log.getFailedAttempts(ipAddress, minutes);

			if (failures >= limit)
				return ipbans.addressBanned(SocketUtil.socketIPInt(ipAddress));
		}

		return false;
	}

	/**
	 * Verifica se o endere�o de IP de uma conex�o foi banida afim de recusar seu acesso.
	 * Essa opera��o s� ter� efeito se tiver sido habilitado o banimento por IP.
	 * @param fd refer�ncia da conex�o do qual deseja verificar o banimento.
	 * @return true se estiver liberado o acesso ou false se estiver banido.
	 */

	public boolean parseBanTime(LFileDescriptor fd)
	{
		if (getConfigs().getBool("ipban.enabled") && isBanned(fd.getAddress()))
		{
			log("conex�o recusada, ip n�o autorizado (ip: %s).\n", fd.getAddressString());

			log.add(fd.getAddress(), null, -3, "ip banned");
			skip(fd, false, 23);

			AC_RefuseLogin refuseLoginPacket = new AC_RefuseLogin();
			refuseLoginPacket.setResult(AuthResult.REJECTED_FROM_SERVER);
			refuseLoginPacket.setBlockDate("");
			refuseLoginPacket.send(fd);

			fd.close();

			return false;
		}

		return true;
	}
}

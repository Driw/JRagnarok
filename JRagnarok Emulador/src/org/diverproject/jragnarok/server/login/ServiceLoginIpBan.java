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
 * <h1>Serviço para Banimento de Endereços IP</h1>
 *
 * <p>Esse serviço permite que determinados endereços de IP possam ser banidos do serviço de acesso.
 * Uma vez que um endereço de IP tenha sido banido, seu acesso não será autorizado de maneira alguma.
 * Assim sendo, seja um cliente ou servidor, não poderá se conectar no mesmo tendo nenhuma utilidade.</p>
 *
 * <p>O principal banimento é em casos em que um cliente (jogador) tenta se conectar com senha inválida.
 * É feita uma contagem interna através de logs que mostram a quantidade de tentativas recentes.
 * Caso tenha passado do limite (configurado) o endereço de IP desse jogador é banido temporariamente.</p>
 *
 * <p>Esse serviço pode ser usado ainda para banir o acesso de endereços de IP de locais específicos do mundo.
 * Por exemplo, o servidor não quer que jogadores de um determinado país acesse o servidor de acesso.
 * Assim, basta banir endereços de IP que sejam igual a 180.*.*.* do sistema (supondo que seja esse o do país).</p>
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
	 * Serviço para registro de acessos.
	 */
	private ServiceLoginLog log;

	/**
	 * Controle para banimento de endereços de IP.
	 */
	private IpBanControl ipbans;

	/**
	 * Cria uma nova instância do serviço para banimento de endereços IP.
	 * @param server servidor de acesso que irá utilizar esse serviço.
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
	 * Listener usado pelo temporizador que irá garantir que endereços de IP não fiquem banidos para sempre.
	 * Irá chamar o controle dos banimentos para endereços de IP e realizar uma limpeza no mesmo.
	 * A limpeza consiste em remover do cache e banco de dados todos os banimentos que estejam expirados.
	 */

	private TimerListener cleanup = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			try {

				logNotice("%d listas de endereços de IP banidos expirados.\n", ipbans.cleanup());

			} catch (RagnarokException e) {
				logError("falha durante a limpeza de endereços de IP banidos expirados:\n");
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
	 * @return true se o serviço estiver habilitado para ser utilizado ou false caso contrário.
	 */

	private boolean isEnabled()
	{
		return getConfigs().getBool(IPBAN_ENABLED);
	}

	/**
	 * Verifica se um endereço de IP especificado está banido do sistema em alguma das listas.
	 * Caso o serviço esteja configurado para não ser utilizado, este método não terá efeito.
	 * @param ip número inteiro que representa o endereço de IP (4 bytes | 4 dígitos).
	 * @return true se estiver contido em alguma lista de ban ou false caso contrário.
	 */

	public boolean isBanned(int ip)
	{
		if (!isEnabled())
			return false;

		return ipbans.addressBanned(ip);
	}

	/**
	 * Verifica quantas tentativas de acesso falha um determinado endereço de IP sofreu.
	 * Caso essa quantidade de tentativas tenha passado o limite permitido (configurado),
	 * será registrado o endereço passado por parâmetro como banido temporariamente.
	 * @param ipAddress endereço de IP formatado da seguinte forma: %d.%d.%d.%d.
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
	 * Verifica se o endereço de IP de uma conexão foi banida afim de recusar seu acesso.
	 * Essa operação só terá efeito se tiver sido habilitado o banimento por IP.
	 * @param fd referência da conexão do qual deseja verificar o banimento.
	 * @return true se estiver liberado o acesso ou false se estiver banido.
	 */

	public boolean parseBanTime(LFileDescriptor fd)
	{
		if (getConfigs().getBool("ipban.enabled") && isBanned(fd.getAddress()))
		{
			log("conexão recusada, ip não autorizado (ip: %s).\n", fd.getAddressString());

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

package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Conta Online</h1>
 *
 * <p>Através dessa classe o servidor de acesso poderá identificar quais as contas que estão online.
 * Além de saber quais estão online será possível determinar em qual servidor de personagem estão.
 * Tendo ainda um temporizador que será executado no caso da conta vir a ficar offline.</p>
 *
 * @see Timer
 *
 * @author Andrew
 */

public class OnlineLogin
{
	/**
	 * Código para servidor de personagem que ficou offline.
	 */
	public static final int CHAR_SERVER_OFFLINE = -2;

	/**
	 * Código para servidor de personagem que não foi definido.
	 */
	public static final int NO_CHAR_SERVER = -1;


	/**
	 * Código de identificação da conta do jogador.
	 */
	private int accountID;

	/**
	 * Código de identificação do servidor de personagem no sistema.
	 */
	private int charServerID;

	/**
	 * Temporizador que será executado no caso do jogador for desconectado.
	 */
	private Timer waitingDisconnect;

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
	 * @return aquisição do código de identificação do servidor de personagem acessado pela conta.
	 */

	public int getCharServerID()
	{
		return charServerID;
	}

	/**
	 * @param charServerID código de identificação do servidor de personagem acessado pela conta.
	 */

	public void setCharServer(int charServerID)
	{
		this.charServerID = charServerID;
	}

	/**
	 * @return aquisição do temporizador usado para desconectar o jogador do sistema.
	 */

	public Timer getWaitingDisconnect()
	{
		return waitingDisconnect;
	}

	/**
	 * @param waitingDisconnect temporizador usado para desconectar o jogador do sistema.
	 */

	public void setWaitingDisconnect(Timer waitingDisconnect)
	{
		this.waitingDisconnect = waitingDisconnect;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);
		description.append("charServerID", charServerID);

		if (waitingDisconnect != null)
			description.append("waitingDisconnect", waitingDisconnect.getID());

		return description.toString();
	}
}

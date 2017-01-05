package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Conta Online</h1>
 *
 * <p>Atrav�s dessa classe o servidor de acesso poder� identificar quais as contas que est�o online.
 * Al�m de saber quais est�o online ser� poss�vel determinar em qual servidor de personagem est�o.
 * Tendo ainda um temporizador que ser� executado no caso da conta vir a ficar offline.</p>
 *
 * @see Timer
 *
 * @author Andrew
 */

public class OnlineLogin
{
	/**
	 * C�digo para servidor de personagem que ficou offline.
	 */
	public static final int CHAR_SERVER_OFFLINE = -2;

	/**
	 * C�digo para servidor de personagem que n�o foi definido.
	 */
	public static final int NO_CHAR_SERVER = -1;


	/**
	 * C�digo de identifica��o da conta do jogador.
	 */
	private int accountID;

	/**
	 * C�digo de identifica��o do servidor de personagem no sistema.
	 */
	private int charServerID;

	/**
	 * Temporizador que ser� executado no caso do jogador for desconectado.
	 */
	private Timer waitingDisconnect;

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
	 * @return aquisi��o do c�digo de identifica��o do servidor de personagem acessado pela conta.
	 */

	public int getCharServerID()
	{
		return charServerID;
	}

	/**
	 * @param charServerID c�digo de identifica��o do servidor de personagem acessado pela conta.
	 */

	public void setCharServer(int charServerID)
	{
		this.charServerID = charServerID;
	}

	/**
	 * @return aquisi��o do temporizador usado para desconectar o jogador do sistema.
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

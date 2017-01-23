package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Dados de Personagem Online</h1>
 *
 * <p>Grava informa��es referente a um determinado personagem que est� online em um servidor de personagem.
 * As informa��es s�o referentes a conex�o estabelecida, c�digo de identifica��o (conta e personagem),
 * uma identifica��o do servidor de personagem do qual se encontra conectado, c�digo PIN e um temporizador.</p>
 *
 * @see CFileDescriptor
 *
 * @author Andrew
 */

public class OnlineCharData
{
	/**
	 * C�digo para definir que o servidor � desconhecido.
	 */
	public static final int UNKNOW_SERVER = -2;

	/**
	 * C�digo para definir que nenhum servidor foi selecionado.
	 */
	public static final int NO_SERVER = -1;


	/**
	 * Descritor de Arquivo da conex�o do jogador.
	 */
	private CFileDescriptor fd;

	/**
	 * C�digo de identifica��o da conta que o jogador acessou.
	 */
	private int accountID;

	/**
	 * C�digo de identifica��o do personagem que o jogador selecionou.
	 */
	private int charID;

	/**
	 * Temporizador para garantir que o personagem seja desconectado.
	 */
	private Timer waitingDisconnect;

	/**
	 * C�digo de identifica��o do servidor que o personagem se encontra online.
	 */
	private int server;

	/**
	 * Determina se o c�digo PIN foi inserido corretamente com sucesso.
	 */
	private boolean pincodeSuccess;

	/**
	 * @return aquisi��o do descritor de arquivo da conex�o do jogador.
	 */

	public CFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	/**
	 * @param fd descritor de arquivo da conex�o do jogador.
	 */

	public void setFileDescriptor(CFileDescriptor fd)
	{
		this.fd = fd;
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
		if (this.accountID == 0)
			this.accountID = accountID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do personagem selecionado.
	 */

	public int getCharID()
	{
		return charID;
	}

	/**
	 * @param charID c�digo de identifica��o do personagem selecionado.
	 */

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	/**
	 * @return aquisi��o do temporizador para garantir que o personagem seja desconectado.
	 */

	public Timer getWaitingDisconnect()
	{
		return waitingDisconnect;
	}

	/**
	 * @param waitingDisconnect temporizador para garantir que o personagem seja desconectado.
	 */

	public void setWaitingDisconnect(Timer waitingDisconnect)
	{
		this.waitingDisconnect = waitingDisconnect;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do servidor em que o personagem est� online.
	 */

	public int getServer()
	{
		return server;
	}

	/**
	 * @param server c�digo de identifica��o do servidor em que o personagem est� online.
	 */

	public void setServer(int server)
	{
		this.server = server;
	}

	/**
	 * @return true se o c�digo pin tiver sido validado ou false caso contr�rio.
	 */

	public boolean isPincodeSuccess()
	{
		return pincodeSuccess;
	}

	/**
	 * @param pincodeSuccess true se o c�digo pin tiver sido validado ou false caso contr�rio.
	 */

	public void setPincodeSuccess(boolean pincodeSuccess)
	{
		this.pincodeSuccess = pincodeSuccess;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("fd", fd != null ? fd.getID() : null);
		description.append("accountID", accountID);
		description.append("charID", charID);
		description.append("server", server);

		if (pincodeSuccess)
			description.append("pincodeSuccess");

		if (waitingDisconnect != null)
			description.append("waitingDisconnect", waitingDisconnect.getID());

		return description.toString();
	}
}

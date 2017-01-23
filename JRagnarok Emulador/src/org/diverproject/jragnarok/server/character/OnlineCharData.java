package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Dados de Personagem Online</h1>
 *
 * <p>Grava informações referente a um determinado personagem que está online em um servidor de personagem.
 * As informações são referentes a conexão estabelecida, código de identificação (conta e personagem),
 * uma identificação do servidor de personagem do qual se encontra conectado, código PIN e um temporizador.</p>
 *
 * @see CFileDescriptor
 *
 * @author Andrew
 */

public class OnlineCharData
{
	/**
	 * Código para definir que o servidor é desconhecido.
	 */
	public static final int UNKNOW_SERVER = -2;

	/**
	 * Código para definir que nenhum servidor foi selecionado.
	 */
	public static final int NO_SERVER = -1;


	/**
	 * Descritor de Arquivo da conexão do jogador.
	 */
	private CFileDescriptor fd;

	/**
	 * Código de identificação da conta que o jogador acessou.
	 */
	private int accountID;

	/**
	 * Código de identificação do personagem que o jogador selecionou.
	 */
	private int charID;

	/**
	 * Temporizador para garantir que o personagem seja desconectado.
	 */
	private Timer waitingDisconnect;

	/**
	 * Código de identificação do servidor que o personagem se encontra online.
	 */
	private int server;

	/**
	 * Determina se o código PIN foi inserido corretamente com sucesso.
	 */
	private boolean pincodeSuccess;

	/**
	 * @return aquisição do descritor de arquivo da conexão do jogador.
	 */

	public CFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	/**
	 * @param fd descritor de arquivo da conexão do jogador.
	 */

	public void setFileDescriptor(CFileDescriptor fd)
	{
		this.fd = fd;
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
		if (this.accountID == 0)
			this.accountID = accountID;
	}

	/**
	 * @return aquisição do código de identificação do personagem selecionado.
	 */

	public int getCharID()
	{
		return charID;
	}

	/**
	 * @param charID código de identificação do personagem selecionado.
	 */

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	/**
	 * @return aquisição do temporizador para garantir que o personagem seja desconectado.
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
	 * @return aquisição do código de identificação do servidor em que o personagem está online.
	 */

	public int getServer()
	{
		return server;
	}

	/**
	 * @param server código de identificação do servidor em que o personagem está online.
	 */

	public void setServer(int server)
	{
		this.server = server;
	}

	/**
	 * @return true se o código pin tiver sido validado ou false caso contrário.
	 */

	public boolean isPincodeSuccess()
	{
		return pincodeSuccess;
	}

	/**
	 * @param pincodeSuccess true se o código pin tiver sido validado ou false caso contrário.
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

package org.diverproject.jragnarok.server.common;

/**
 * Enumera��o para Jogador Desconectado
 *
 * Classe que enumera as formas em que um jogador pode ser desconectado do servidor de mapas.
 * O padr�o � apenas duas formas de remov�-lo: por inatividade ou for�ar a sua sa�da (kick).
 *
 * @author Andrew
 */

public enum DisconnectPlayer
{
	/**
	 * Desconectar jogador por inatividade.
	 */
	DP_KICK_OFFLINE(1),

	/**
	 * Desconectar jogador for�adamente (est� online).
	 */
	DP_KICK_ONLINE(2);

	/**
	 * C�digo de identifica��o �nico da enumera��o.
	 */
	public final int CODE;

	/**
	 * Cria uma nova inst�ncia de uma enumera��o para jogador desconectado.
	 * @param code c�digo de identifica��o da forma que o jogador � desconectado.
	 */

	private DisconnectPlayer(int code)
	{
		CODE = code;
	}
}

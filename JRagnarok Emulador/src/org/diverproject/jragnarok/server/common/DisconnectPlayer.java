package org.diverproject.jragnarok.server.common;

/**
 * Enumeração para Jogador Desconectado
 *
 * Classe que enumera as formas em que um jogador pode ser desconectado do servidor de mapas.
 * O padrão é apenas duas formas de removê-lo: por inatividade ou forçar a sua saída (kick).
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
	 * Desconectar jogador forçadamente (está online).
	 */
	DP_KICK_ONLINE(2);

	/**
	 * Código de identificação único da enumeração.
	 */
	public final int CODE;

	/**
	 * Cria uma nova instância de uma enumeração para jogador desconectado.
	 * @param code código de identificação da forma que o jogador é desconectado.
	 */

	private DisconnectPlayer(int code)
	{
		CODE = code;
	}
}

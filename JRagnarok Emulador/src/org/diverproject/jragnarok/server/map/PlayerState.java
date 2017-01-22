package org.diverproject.jragnarok.server.map;

/**
 * <h1>Estado do Jogador</h1>
 *
 * <p>Enumera algumas possibilidades de estado em que o jogador poder� se encontrar no servidor de mapas.
 * As enumera��es consistem em dizer que o jogador est� entrando no servidor (personagem selecionado),
 * saindo do servidor (fechou o jogo/retornou � sele��o de personagens) ou trocando de mapa.</p>
 *
 * @author Andrew
 */

public enum PlayerState
{
	/**
	 * Entrando no servidor de mapa.
	 */
	ST_LOGIN,

	/**
	 * Saindo do servidor de mapa.
	 */
	ST_LOGOUT,

	/**
	 * Trocando de mapa.
	 */
	ST_MAPCHANGE;
}

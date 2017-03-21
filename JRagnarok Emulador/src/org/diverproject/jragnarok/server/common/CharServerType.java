package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.s;

import org.diverproject.jragnarok.RagnarokRuntimeException;

/**
 * <h1>Tipo do Servidor de Personagem</h1>
 *
 * <p>Classe com a enumera��o dos tipos de servidores de personagem existentes no sistema JRagnarok.
 * Os tipos foram definidos conforme os c�digos que sofrem efeitos no cliente quando passados ao mesmo.
 * Outros tipos de servidores de personagem n�o ter�o influ�ncia visual no cliente do jogador.</p>
 *
 * @author Andrew
 */

public enum CharServerType
{
	/**
	 * Servidor normal (padr�o).
	 */
	NORMAL(0),

	/**
	 * Servidor em manuten��o (prov�vel acesso restrito aos desenvolvedores).
	 */
	MAINTANCE(1),

	/**
	 * Servidor apenas para maiores de idade.
	 */
	OVER_AGE(2),

	/**
	 * Servidor que se deve pagar para jogar.
	 */
	PAY_TO_PLAY(3),

	/**
	 * Servidor gratuito para jogar.
	 */
	PREE_TO_PLAY(4);

	/**
	 * C�digo de identifica��o do tipo do servidor de personagem configurado no mesmo.
	 */

	public final short CODE;

	/**
	 * Cria uma nova inst�ncia de uma enumera��o para definir um tipo do servidor de personagem no sistema.
	 * @param code c�digo de identifica��o do tipo do servidor de personagem configurado no mesmo.
	 */

	private CharServerType(int code)
	{
		CODE = s(code);
	}

	/**
	 * Procedimento que permite realizar a an�lise de um c�digo especificado e obter o tipo do servidor de personagem.
	 * @param b c�digo de identifica��o do tipo do servidor de personagem do qual deseja selecionar a enumera��o.
	 * @return aquisi��o da enumera��o do tipo do servidor de personagem conforme o seu c�digo de identifica��o.
	 */

	public static CharServerType parse(int code)
	{
		switch (code)
		{
			case 0: return NORMAL;
			case 1: return MAINTANCE;
			case 2: return OVER_AGE;
			case 3: return PAY_TO_PLAY;
			case 4: return PREE_TO_PLAY;
		}

		throw new RagnarokRuntimeException("code n�o � CharServerType");
	}
}

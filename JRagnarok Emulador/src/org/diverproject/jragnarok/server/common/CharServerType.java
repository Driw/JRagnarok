package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.s;

import org.diverproject.jragnarok.RagnarokRuntimeException;

/**
 * <h1>Tipo do Servidor de Personagem</h1>
 *
 * <p>Classe com a enumeração dos tipos de servidores de personagem existentes no sistema JRagnarok.
 * Os tipos foram definidos conforme os códigos que sofrem efeitos no cliente quando passados ao mesmo.
 * Outros tipos de servidores de personagem não terão influência visual no cliente do jogador.</p>
 *
 * @author Andrew
 */

public enum CharServerType
{
	/**
	 * Servidor normal (padrão).
	 */
	NORMAL(0),

	/**
	 * Servidor em manutenção (provável acesso restrito aos desenvolvedores).
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
	 * Código de identificação do tipo do servidor de personagem configurado no mesmo.
	 */

	public final short CODE;

	/**
	 * Cria uma nova instância de uma enumeração para definir um tipo do servidor de personagem no sistema.
	 * @param code código de identificação do tipo do servidor de personagem configurado no mesmo.
	 */

	private CharServerType(int code)
	{
		CODE = s(code);
	}

	/**
	 * Procedimento que permite realizar a análise de um código especificado e obter o tipo do servidor de personagem.
	 * @param b código de identificação do tipo do servidor de personagem do qual deseja selecionar a enumeração.
	 * @return aquisição da enumeração do tipo do servidor de personagem conforme o seu código de identificação.
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

		throw new RagnarokRuntimeException("code não é CharServerType");
	}
}

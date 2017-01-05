package org.diverproject.jragnarok.console;

/**
 * <h1>Enumera��o para Tipos de Mensagens</h1>
 *
 * <p>Os tipos de mensagens permitem definir os pr�-fixos que ser�o vistos a frente das mensagens.
 * Atrav�s dos tipos tamb�m ser� poss�vel especificar como estes pr�-fixos ser�o exibidos,
 * tal como o seu nome conforme o tipo e uma colora��o na sua formata��o quando exibido no console.</p>
 *
 * @author Andrew
 */

public enum MessageType
{
	/**
	 * Mensagens exibidas apenas no modo depura��o.
	 */
	DEBUG("\fclDebug\fcw"),

	/**
	 * Mensagens exibidas para informa��es relacionadas a pacotes no servidor.
	 */
	INFO("\fcnInfo\fcw"),

	/**
	 * Mensagens exibidas para informa��es de processamentos no servidor.
	 */
	NOTICE("\fcgNotice\fcw"),

	/**
	 * Mensagens exibidas de avisos sobre processamentos que devem ser evitados.
	 */
	WARNING("\fcoWarning\fcw"),

	/**
	 * Mensagens exibidas de avisos sobre processamentos que n�o foram bem sucedidos.
	 */
	ERROR("\fcrErro\fcw"),

	/**
	 * Mensagens exibidas de avisos sobre processamentos que podem danificar a opera��o do servidor.
	 */
	FATAL("\fcmFatal\fcw"),

	/**
	 * Mensagens exibidas de avisos sobre processamentos que causam o fechamento de conex�es.
	 */
	EXCEPTION("\fcmException\fcw");

	/**
	 * Pr�-fixo que ser� exibido antes do conte�do da mensagem do log.
	 */
	public final String prefix;

	/**
	 * Cria uma nova inst�ncia para enumerar um tipo de mensagem que ser� exibida no console.
	 * @param prefix formata��o do pr�-fixo que dever� ser exibido antes da mensagem.
	 */

	private MessageType(String prefix)
	{
		this.prefix = prefix;
	}
}

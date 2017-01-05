package org.diverproject.jragnarok.console;

/**
 * <h1>Enumeração para Tipos de Mensagens</h1>
 *
 * <p>Os tipos de mensagens permitem definir os pré-fixos que serão vistos a frente das mensagens.
 * Através dos tipos também será possível especificar como estes pré-fixos serão exibidos,
 * tal como o seu nome conforme o tipo e uma coloração na sua formatação quando exibido no console.</p>
 *
 * @author Andrew
 */

public enum MessageType
{
	/**
	 * Mensagens exibidas apenas no modo depuração.
	 */
	DEBUG("\fclDebug\fcw"),

	/**
	 * Mensagens exibidas para informações relacionadas a pacotes no servidor.
	 */
	INFO("\fcnInfo\fcw"),

	/**
	 * Mensagens exibidas para informações de processamentos no servidor.
	 */
	NOTICE("\fcgNotice\fcw"),

	/**
	 * Mensagens exibidas de avisos sobre processamentos que devem ser evitados.
	 */
	WARNING("\fcoWarning\fcw"),

	/**
	 * Mensagens exibidas de avisos sobre processamentos que não foram bem sucedidos.
	 */
	ERROR("\fcrErro\fcw"),

	/**
	 * Mensagens exibidas de avisos sobre processamentos que podem danificar a operação do servidor.
	 */
	FATAL("\fcmFatal\fcw"),

	/**
	 * Mensagens exibidas de avisos sobre processamentos que causam o fechamento de conexões.
	 */
	EXCEPTION("\fcmException\fcw");

	/**
	 * Pré-fixo que será exibido antes do conteúdo da mensagem do log.
	 */
	public final String prefix;

	/**
	 * Cria uma nova instância para enumerar um tipo de mensagem que será exibida no console.
	 * @param prefix formatação do pré-fixo que deverá ser exibido antes da mensagem.
	 */

	private MessageType(String prefix)
	{
		this.prefix = prefix;
	}
}

package org.diverproject.jragnarok.server;

/**
 * <h1>Ação para Descritor de Arquivo</h1>
 *
 * <p>Implementa um método que será executado por cada descritor de arquivo encontrado.
 * Essa interface é usada por FileDescriptor para executar ações específicas em todo sistema.
 * Assim é possível que todo cliente que está conectado ao servidor receba dados específicos.</p>
 *
 * @see FileDescriptor
 *
 * @author Andrew Mello
 */

public interface FileDescriptorAction
{
	/**
	 * Procedimento que será executado por cada descritor de arquivo encontrado.
	 * Através do descritor de arquivo é possível receber ou enviar dados ao cliente.
	 * @param fd referência do descritor de arquivo que está sendo iterado.
	 */

	void execute(FileDescriptor fd);
}

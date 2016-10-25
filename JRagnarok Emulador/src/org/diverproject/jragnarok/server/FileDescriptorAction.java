package org.diverproject.jragnarok.server;

/**
 * <h1>A��o para Descritor de Arquivo</h1>
 *
 * <p>Implementa um m�todo que ser� executado por cada descritor de arquivo encontrado.
 * Essa interface � usada por FileDescriptor para executar a��es espec�ficas em todo sistema.
 * Assim � poss�vel que todo cliente que est� conectado ao servidor receba dados espec�ficos.</p>
 *
 * @see FileDescriptor
 *
 * @author Andrew Mello
 */

public interface FileDescriptorAction
{
	/**
	 * Procedimento que ser� executado por cada descritor de arquivo encontrado.
	 * Atrav�s do descritor de arquivo � poss�vel receber ou enviar dados ao cliente.
	 * @param fd refer�ncia do descritor de arquivo que est� sendo iterado.
	 */

	void execute(FileDescriptor fd);
}

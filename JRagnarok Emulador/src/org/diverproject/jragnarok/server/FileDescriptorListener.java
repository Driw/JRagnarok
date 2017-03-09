package org.diverproject.jragnarok.server;

import org.diverproject.jragnarok.RagnarokException;

/**
 * <h1>Listener para Arquivo Descritor</h1>
 *
 * <p>Esse listener permite definir um m�todo que recebe o Arquivo Descritor.
 * Nesse m�todo dever� especificar opera��es conforme foi definido no descritor.</p>
 *
 * @author Andrew
 */

public interface FileDescriptorListener
{
	/**
	 * Chamado toda vez que for solicitado a atualiza��o dos Arquivos Descritores.
	 * Esse m�todo ter� uma das tr�s fun��es a seguir: receber, enviar ou analisar dados.
	 * Os dados s�o referentes ao descritor, atrav�s da stream de input/output do socket.
	 * @param fd refer�ncia do Arquivo Descritor para criar as streams de input ou output.
	 * @return true se deve continuar com o mesmo listener ou false caso contr�rio.
	 * @throws RagnarokException qualquer problema que n�o deve passar para registrar.
	 */

	boolean onCall(FileDescriptor fd) throws RagnarokException;
}

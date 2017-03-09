package org.diverproject.jragnarok.server;

import org.diverproject.jragnarok.RagnarokException;

/**
 * <h1>Listener para Arquivo Descritor</h1>
 *
 * <p>Esse listener permite definir um método que recebe o Arquivo Descritor.
 * Nesse método deverá especificar operações conforme foi definido no descritor.</p>
 *
 * @author Andrew
 */

public interface FileDescriptorListener
{
	/**
	 * Chamado toda vez que for solicitado a atualização dos Arquivos Descritores.
	 * Esse método terá uma das três funções a seguir: receber, enviar ou analisar dados.
	 * Os dados são referentes ao descritor, através da stream de input/output do socket.
	 * @param fd referência do Arquivo Descritor para criar as streams de input ou output.
	 * @return true se deve continuar com o mesmo listener ou false caso contrário.
	 * @throws RagnarokException qualquer problema que não deve passar para registrar.
	 */

	boolean onCall(FileDescriptor fd) throws RagnarokException;
}

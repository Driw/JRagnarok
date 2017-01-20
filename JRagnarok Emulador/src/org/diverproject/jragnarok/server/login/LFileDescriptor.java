package org.diverproject.jragnarok.server.login;

import java.net.Socket;

import org.diverproject.jragnarok.server.FileDescriptor;

/**
 * <h1>Descritor de Arquivo para Servidor de Acesso</h1>
 *
 * <p>Possui as mesmas funcionalidades e finalidade de um descritor de arquivo.
 * Porém irá possuir um objeto contendo dados da sessão no servidor de acesso.
 * Esses dados são necessários para boa parte das operações realizadas.</p>
 *
 * @see FileDescriptor
 * @see LoginSessionData
 *
 * @author Andrew
 */

class LFileDescriptor extends FileDescriptor
{
	/**
	 * Dados da sessão no servidor de acesso.
	 */
	private LoginSessionData session;

	/**
	 * Cria uma nova instância de um arquivo de descritor para ser usado no servidor de acesso.
	 * @param socket conexão socket recebido do servidor solicitado pelo cliente.
	 */

	public LFileDescriptor(Socket socket)
	{
		super(socket);

		session = new LoginSessionData();
	}

	@Override
	public LoginSessionData getSessionData()
	{
		return session;
	}
}

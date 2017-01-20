package org.diverproject.jragnarok.server.login;

import java.net.Socket;

import org.diverproject.jragnarok.server.FileDescriptor;

/**
 * <h1>Descritor de Arquivo para Servidor de Acesso</h1>
 *
 * <p>Possui as mesmas funcionalidades e finalidade de um descritor de arquivo.
 * Por�m ir� possuir um objeto contendo dados da sess�o no servidor de acesso.
 * Esses dados s�o necess�rios para boa parte das opera��es realizadas.</p>
 *
 * @see FileDescriptor
 * @see LoginSessionData
 *
 * @author Andrew
 */

class LFileDescriptor extends FileDescriptor
{
	/**
	 * Dados da sess�o no servidor de acesso.
	 */
	private LoginSessionData session;

	/**
	 * Cria uma nova inst�ncia de um arquivo de descritor para ser usado no servidor de acesso.
	 * @param socket conex�o socket recebido do servidor solicitado pelo cliente.
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

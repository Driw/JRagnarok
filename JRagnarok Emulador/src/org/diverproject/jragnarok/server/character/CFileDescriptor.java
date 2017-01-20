package org.diverproject.jragnarok.server.character;

import java.net.Socket;

import org.diverproject.jragnarok.server.FileDescriptor;

/**
 * <h1>Descritor de Arquivo para Servidor de Personagem</h1>
 *
 * <p>Possui as mesmas funcionalidades e finalidade de um descritor de arquivo.
 * Por�m ir� possuir um objeto contendo dados da sess�o no servidor de personagem.
 * Esses dados s�o necess�rios para boa parte das opera��es realizadas.</p>
 *
 * @see FileDescriptor
 * @see CharSessionData
 *
 * @author Andrew
 */

public class CFileDescriptor extends FileDescriptor
{
	/**
	 * Dados da sess�o no servidor de personagem.
	 */
	private CharSessionData session;

	/**
	 * Cria uma nova inst�ncia de um arquivo de descritor para ser usado no servidor de personagem.
	 * @param socket conex�o socket recebido do servidor solicitado pelo cliente.
	 */

	public CFileDescriptor(Socket socket)
	{
		super(socket);

		session = new CharSessionData();
	}

	@Override
	public CharSessionData getSessionData()
	{
		return session;
	}
}

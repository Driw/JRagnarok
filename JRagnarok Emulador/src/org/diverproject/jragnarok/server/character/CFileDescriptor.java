package org.diverproject.jragnarok.server.character;

import java.net.Socket;

import org.diverproject.jragnarok.server.FileDescriptor;

/**
 * <h1>Descritor de Arquivo para Servidor de Personagem</h1>
 *
 * <p>Possui as mesmas funcionalidades e finalidade de um descritor de arquivo.
 * Porém irá possuir um objeto contendo dados da sessão no servidor de personagem.
 * Esses dados são necessários para boa parte das operações realizadas.</p>
 *
 * @see FileDescriptor
 * @see CharSessionData
 *
 * @author Andrew
 */

public class CFileDescriptor extends FileDescriptor
{
	/**
	 * Dados da sessão no servidor de personagem.
	 */
	private CharSessionData session;

	/**
	 * Cria uma nova instância de um arquivo de descritor para ser usado no servidor de personagem.
	 * @param socket conexão socket recebido do servidor solicitado pelo cliente.
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

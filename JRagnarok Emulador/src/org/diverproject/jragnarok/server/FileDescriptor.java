package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokConstants.FD_SETSIZE;
import static org.diverproject.jragnarok.JRagnarokUtil.indexOn;
import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.setUpSource;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.LoopList;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.implementation.input.InputPacket;
import org.diverproject.util.stream.implementation.output.OutputPacket;

/**
 * <h1>Descritor de Arquivo</h1>
 *
 * <p>Classe usada para trabalhar com conex�es socket recebido de clientes dos servidores.
 * Atrav�s dele ser� especificado para onde as conex�es ser�o repassadas para operarem.
 * Al�m disso nele ser� poss�vel criar objetos que trabalhem com entrada e sa�da de dados.</p>
 *
 * @see Socket
 * @see InternetProtocol
 * @see FileDescriptorListener
 *
 * @author Andrew
 */

public class FileDescriptor
{
	/**
	 * Descritor fechado.
	 */
	public static final int FLAG_EOF = 1;

	/**
	 * Descritor � um servidor.
	 */
	public static final int FLAG_SERVER = 2;

	/**
	 * Descritor � um ping.
	 */
	public static final int FLAG_PING = 3;

	/**
	 * Tempo limite em milissegundos aceito por ociosidade.
	 */
	public static final int DEFAULT_TIMEOUT = 60000;


	/**
	 * C�digo de identifica��o do descritor no sistema.
	 */
	private int id;

	/**
	 * Flag que determina o tipo de descritor.
	 */
	private int flag;

	/**
	 * Tempo restante para que o descritor seja considerado inativo.
	 */
	private int timeout;

	/**
	 * Conex�o socket do cliente com o servidor.
	 */
	private Socket socket;

	/**
	 * Objeto para armazenar detalhadamente o endere�o da conex�o socket.
	 */
	private InternetProtocol address;

	/**
	 * Listener para receber pacotes de dados.
	 */
	private FileDescriptorListener receiveListener;

	/**
	 * Listener para enviar pacotes de dados.
	 */
	private FileDescriptorListener sendListener;

	/**
	 * Listener para validar conex�o.
	 */
	private FileDescriptorListener parseListener;

	/**
	 * Objeto em cache utilizado por esse descritor.
	 */
	private Object cache;

	/**
	 * Cria um novo Descriptor de Arquivo baseado em uma conex�o socket.
	 * @param socket refer�ncia da conex�o do cliente com o servidor.
	 */

	private FileDescriptor(Socket socket)
	{
		this.socket = socket;
		this.address = new InternetProtocol(socket);
	}

	/**
	 * A identifica��o do descritor � �nica por cliente conectado.
	 * @return aquisi��o da identifica��o do cliente com o servidor.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * Flag define o tipo de conex�o do descritor (EOF, SERVER ou PING).
	 * @return aquisi��o da rela��o do descritor com o servidor.
	 */

	public int getFlag()
	{
		return flag;
	}

	/**
	 * Permite definir o tipo da conex�o do descritor (EOF, SERVER ou PING).
	 * @param flag rela��o do descritor com o servidor:
	 * FLAG_EOF, FLAG_SERVER ou FLAG_PING, encontrado em FileDescriptor.
	 */

	public void setFlag(int flag)
	{
		if (flag >= FLAG_EOF && flag <= FLAG_PING)
			this.flag = flag;
	}

	/**
	 * O tempo de expira��o da conex�o � usado para garantir que n�o haja conex�es ociosas.
	 * A ociosidade � feita a partir da cria��o de pacotes para entrada ou sa�da de dados.
	 * @return aquisi��o do tick em que o descriptor ser� considerado ocioso no servidor.
	 */

	public int getTimeout()
	{
		return timeout;
	}

	/**
	 * O tempo de expira��o da conex�o � usado para garantir que n�o haja conex�es ociosas.
	 * A ociosidade � feita a partir da cria��o de pacotes para entrada ou sa�da de dados.
	 * @param timeout tick em que o descriptor ser� considerado ocioso no servidor.
	 */

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	/**
	 * Endere�o � referente ao IP da conex�o socket do cliente estabeleceu com o servidor.
	 * @return aquisi��o do endere�o de IP armazenado em um n�mero inteiro.
	 */

	public int getAddress()
	{
		return address.get();
	}

	/**
	 * Endere�o � referente ao IP da conex�o socket do cliente estabeleceu com o servidor.
	 * @return aquisi��o do endere�o de IP da conex�o formatada em String.
	 */

	public String getAddressString()
	{
		return address.getString();
	}

	/**
	 * Listener de recebimento possui um m�todo para receber dados do socket.
	 * @return aquisi��o do listener usado para receber dados do socket.
	 */

	public FileDescriptorListener getReceiveListener()
	{
		return receiveListener;
	}

	/**
	 * Listener de recebimento possui um m�todo para receber dados do socket.
	 * @param receiveListener listener usado para receber dados do socket.
	 */

	public void setReceiveListener(FileDescriptorListener receiveListener)
	{
		this.receiveListener = receiveListener;
	}

	/**
	 * Listener de recebimento possui um m�todo para enviar dados por socket.
	 * @return aquisi��o do listener usado para receber dados por socket.
	 */

	public FileDescriptorListener getSendListener()
	{
		return sendListener;
	}

	/**
	 * Listener de recebimento possui um m�todo para enviar dados por socket.
	 * @param sendListener listener usado para receber dados por socket.
	 */

	public void setSendListener(FileDescriptorListener sendListener)
	{
		this.sendListener = sendListener;
	}

	/**
	 * Listener que possui um m�todo para analisar o despache do socket no servidor.
	 * @return aquisi��o do listener usado para despachar o socket no servidor.
	 */

	public FileDescriptorListener getParseListener()
	{
		return parseListener;
	}

	/**
	 * Listener que possui um m�todo para analisar o despache do socket no servidor.
	 * @param parseListener listener usado para despachar o socket no servidor.
	 */

	public void setParseListener(FileDescriptorListener parseListener)
	{
		this.parseListener = parseListener;
	}

	/**
	 * Cache permite vincular um determinado objeto para que possa ser usado.
	 * @return aquisi��o do objeto em cache no descritor.
	 */

	public Object getCache()
	{
		return cache;
	}

	/**
	 * Cache permite vincular um determinado objeto para que possa ser usado.
	 * @param cache refer�ncia do objeto a armazenar em cache.
	 */

	public void setCache(Object cache)
	{
		this.cache = cache;
	}

	/**
	 * Instancia uma nova stream para criar uma entrada de dados por socket.
	 * @param name nome que ser� usado para identificar o pacote.
	 * @return aquisi��o de um objeto para entrada de dados por socket.
	 */

	public InputPacket newInput(String name)
	{
		timeout = TimerSystem.getInstance().getLastTickCount() + DEFAULT_TIMEOUT;

		try {
			return new InputPacket(socket, 0, name);
		} catch (StreamException e ) {
			logError("falha ao criar InputPacket para %s (ip: %s)", name, address.getString());
			throw new RagnarokRuntimeException(e.getMessage());
		}
	}

	/**
	 * Instancia uma nova stream para criar uma sa�da de dados por socket.
	 * @param name nome que ser� usado para identificar o pacote.
	 * @return aquisi��o de um objeto para sa�da de dados por socket.
	 */

	public OutputPacket newOutput(String name)
	{
		timeout = TimerSystem.getInstance().getLastTickCount() + DEFAULT_TIMEOUT;

		try {
			return new OutputPacket(socket, 0, name);
		} catch (StreamException e ) {
			logError("falha ao criar OutputPacket para %s (ip: %s)", name, address.getString());
			throw new RagnarokRuntimeException(e.getMessage());
		}
	}

	/**
	 * Solicita o fechamento da conex�o socket do cliente com o servidor.
	 * Tamb�m remove o descritor das sess�es existentes no sistema.
	 */

	public void close()
	{
		try {

			socket.close();
			socket = null;
			cache = null;
			sendListener = null;
			receiveListener = null;
			parseListener = null;

			SESSIONS.remove(id);

			id = 0;

		} catch (IOException e) {
			logExeception(e);
		}
	}

	/**
	 * Procedimento usado para garantir que a conex�o socket ainda existe.
	 * @return true se o socket estiver conectado ou false caso contr�rio.
	 */

	public boolean isConnected()
	{
		return socket != null && socket.isConnected();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("address", address.getString());

		description.append("receive", receiveListener);
		description.append("send", sendListener);
		description.append("parse", parseListener);

		if (cache != null)
			description.append("cache", nameOf(cache));

		return description.toString();
	}

	/**
	 * Lista contendo todas as conex�es sockets.
	 */
	private static final List<FileDescriptor> SESSIONS = new LoopList<>(FD_SETSIZE);

	/**
	 * Cria um novo Arquivo Descritor a partir de uma conex�o socket.
	 * @param socket refer�ncia da conex�o socket a considerar.
	 * @return aquisi��o de uma novo Arquivo Descritor.
	 */

	public static FileDescriptor newFileDecriptor(Socket socket)
	{
		FileDescriptor fd = new FileDescriptor(socket);

		if (!SESSIONS.add(fd))
		{
			fd.close();

			return null;
		}

		fd.id = indexOn(SESSIONS, fd);

		return fd;
	}

	/**
	 * Procedimento est�tico usado para atualizar todos os Arquivos Descritores.
	 * Dever� garantir que todas conex�es sejam processadas igualmente.
	 * @param next milissegundos para expirar o pr�ximo temporizador.
	 */

	public static void update(long next)
	{
		TimerSystem timer = TimerSystem.getInstance();
		long lastTick = timer.getLastTickCount();

		for (FileDescriptor fd : SESSIONS)
		{
			if (fd.getTimeout() > 0 && (lastTick - fd.getTimeout()) > 60)
			{
				if (fd.getFlag() == FLAG_SERVER && fd.flag != 2)
					fd.setFlag(FLAG_PING);
				else
				{
					log("sess�o #%d terminou (ip: %s).\n", fd.getID(), fd.getAddressString());
					fd.close();
				}
			}

			try {

				fd.getParseListener().onCall(fd);
				fd.setTimeout(60);

			} catch (RagnarokException e) {

				setUpSource(1);
				logError("processamento inv�lido encontrado:\n");
				logExeception(e);

			} catch (RagnarokRuntimeException e) {

				setUpSource(1);
				logError("informa��o inv�lida encontrada:\n");
				logExeception(e);

			} catch (Exception e) {

				setUpSource(1);
				logError("erro inesperado ocorrido:\n");
				logExeception(e);

			}
		}
	}
}

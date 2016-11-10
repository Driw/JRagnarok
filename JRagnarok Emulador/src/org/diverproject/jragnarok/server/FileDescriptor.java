package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.log.LogSystem.logExeception;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.implementation.PacketBuilder;

/**
 * <h1>Descritor de Arquivo</h1>
 *
 * <p>Um descritor de arquivo é criado a partir de uma conexão socket para ser usado no JRagnarok.
 * Esse descritor irá permitir a construção de pacotes para enviar dados ou receber dados do cliente.
 * Será possível ainda definir algumas propriedades (flag) como verificar se ainda há conexão.</p>
 *
 * <p>Ainda possui um atributo para funcionar como cache, permitindo transportar objetos.
 * Para que um descritor seja chamado para ser processado deve ser definido um listener.
 * Quando um listener estiver definido a atualização do servidor chamará esse listener.</p>
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
	 * Flag que define o fim do descritor.
	 */
	public static final int FLAG_EOF = 0x01;

	/**
	 * Flag que define o descritor como servidor.
	 */
	public static final int FLAG_SERVER = 0x02;

	/**
	 * Flag que define o descritor como ping.
	 */
	public static final int FLAG_PING = 0x04;

	/**
	 * Flag que define o descritor como ping enviado.
	 */
	public static final int FLAG_PING_SENT = 0x08;

	/**
	 * Vetor contendo o nome das flags disponíveis.
	 */
	public static final String FLAG_STRING[] = new String[] { "EOF", "SERVER", "PING" };

	/**
	 * Tempo limite em milissegundos aceito por ociosidade.
	 */
	public static final int DEFAULT_TIMEOUT = 30000;


	/**
	 * Sistema que criou esse Descritor de Arquivo.
	 */
	FileDescriptorSystem system;

	/**
	 * Código de identificação do descritor no sistema.
	 */
	int id;

	/**
	 * Flag que determina o tipo de descritor.
	 */
	private BitWise flag;

	/**
	 * Tempo restante para que o descritor seja considerado inativo.
	 */
	private int timeout;

	/**
	 * Conexão socket do cliente com o servidor.
	 */
	private Socket socket;

	/**
	 * Objeto para armazenar detalhadamente o endereço da conexão socket.
	 */
	private InternetProtocol address;

	/**
	 * Listener para validar conexão.
	 */
	private FileDescriptorListener parseListener;

	/**
	 * Objeto em cache utilizado por esse descritor.
	 */
	private Object cache;

	/**
	 * Criador de pacotes.
	 */
	private PacketBuilder packetBuilder;

	/**
	 * Cria um novo Descriptor de Arquivo baseado em uma conexão socket.
	 * @param socket referência da conexão do cliente com o servidor.
	 */

	FileDescriptor(Socket socket)
	{
		this.socket = socket;
		this.flag = new BitWise(FLAG_STRING);
		this.address = new InternetProtocol(socket);
		this.packetBuilder = new PacketBuilder(socket);
	}

	/**
	 * A identificação do descritor é única por cliente conectado.
	 * @return aquisição da identificação do cliente com o servidor.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * Flag define o tipo de conexão do descritor (FLAG_EOF, FLAG_SERVER ou FLAG_PING).
	 * @return aquisição da atribuição de comportamento do descritor no servidor.
	 */

	public BitWise getFlag()
	{
		return flag;
	}

	/**
	 * O tempo de expiração da conexão é usado para garantir que não haja conexões ociosas.
	 * A ociosidade é feita a partir da criação de pacotes para entrada ou saída de dados.
	 * @return aquisição do tick em que o descriptor será considerado ocioso no servidor.
	 */

	public int getTimeout()
	{
		return timeout;
	}

	/**
	 * O tempo de expiração da conexão é usado para garantir que não haja conexões ociosas.
	 * A ociosidade é feita a partir da criação de pacotes para entrada ou saída de dados.
	 * @param timeout tick em que o descriptor será considerado ocioso no servidor.
	 */

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	/**
	 * Endereço é referente ao IP da conexão socket do cliente estabeleceu com o servidor.
	 * @return aquisição do endereço de IP armazenado em um número inteiro.
	 */

	public int getAddress()
	{
		return address.get();
	}

	/**
	 * Endereço é referente ao IP da conexão socket do cliente estabeleceu com o servidor.
	 * @return aquisição do endereço de IP da conexão formatada em String.
	 */

	public String getAddressString()
	{
		return address.getString();
	}

	/**
	 * Listener que possui um método para analisar o despache do socket no servidor.
	 * @return aquisição do listener usado para despachar o socket no servidor.
	 */

	public FileDescriptorListener getParseListener()
	{
		return parseListener;
	}

	/**
	 * Listener que possui um método para analisar o despache do socket no servidor.
	 * @param parseListener listener usado para despachar o socket no servidor.
	 */

	public void setParseListener(FileDescriptorListener parseListener)
	{
		this.parseListener = parseListener;
	}

	/**
	 * Cache permite vincular um determinado objeto para que possa ser usado.
	 * @return aquisição do objeto em cache no descritor.
	 */

	public Object getCache()
	{
		return cache;
	}

	/**
	 * Cache permite vincular um determinado objeto para que possa ser usado.
	 * @param cache referência do objeto a armazenar em cache.
	 */

	public void setCache(Object cache)
	{
		this.cache = cache;
	}

	/**
	 * O construtor de pacotes irá permitir o recebimento e envio de dados por pacote.
	 * Através dele será possível dizer se é input/output, tamanho e nome do pacote.
	 * @return aquisição do criador de pacotes desse Descritor de Arquivo.
	 */

	public PacketBuilder getPacketBuilder()
	{
		timeout = system.getTimerSystem().getCurrentTime() + DEFAULT_TIMEOUT;

		return packetBuilder;
	}

	/**
	 * Solicita o fechamento da conexão socket do cliente com o servidor.
	 * Também remove o descritor das sessões existentes no sistema.
	 */

	public void close()
	{
		if (socket == null)
			return;

		try {

			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();

			socket = null;
			cache = null;
			parseListener = null;

			id = 0;

		} catch (IOException e) {
			logExeception(e);
		}
	}

	/**
	 * Procedimento usado para garantir que a conexão socket ainda existe.
	 * @return true se o socket estiver conectado ou false caso contrário.
	 */

	public boolean isConnected()
	{
		if (id <= 0 || flag.is(FLAG_EOF) || socket == null)
			return false;

		boolean connected = socket.isConnected();

		return connected;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("address", address.getString());
		description.append("parse", parseListener);

		if (cache != null)
			description.append("cache", nameOf(cache));

		return description.toString();
	}
}

package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.setUpSource;
import static org.diverproject.util.Util.seconds;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnarok.server.common.SessionData;
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

public abstract class FileDescriptor
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
	 * Flag que define o descritor como ping enviado.
	 */
	public static final int FLAG_PING = 0x04;

	/**
	 * Vetor contendo o nome das flags disponíveis.
	 */
	public static final String FLAG_STRING[] = new String[] { "EOF", "SERVER", "PING" };

	/**
	 * Tempo limite em milissegundos aceito por ociosidade.
	 */
	public static final int DEFAULT_TIMEOUT = seconds(30);


	/**
	 * Código de identificação do descritor no sistema.
	 */
	int id;

	/**
	 * Tick da última vez em que o descritor de arquivo foi atualizado.
	 */
	int lastTickUpdate;

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
	 * Listener para realizar o fechamento da conexão.
	 */
	private FileDescriptorListener closeListener;

	/**
	 * Criador de pacotes.
	 */
	private PacketBuilder packetBuilder;

	/**
	 * Cria um novo Descriptor de Arquivo baseado em uma conexão socket.
	 * @param socket referência da conexão do cliente com o servidor.
	 */

	public FileDescriptor(Socket socket)
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
	 * Listener que possui um método para processar ações após o fechamento do socket.
	 * @return aquisição do listener usado após a conexão ter sido fechada.
	 */

	public FileDescriptorListener getCloseListener()
	{
		return closeListener;
	}

	/**
	 * Listener que possui um método para processar ações após o fechamento do socket.
	 * @param closeListener listener usado após a conexão ter sido fechada.
	 */

	public void setCloseListener(FileDescriptorListener closeListener)
	{
		this.closeListener = closeListener;
	}

	/**
	 * O construtor de pacotes irá permitir o recebimento e envio de dados por pacote.
	 * Através dele será possível dizer se é input/output, tamanho e nome do pacote.
	 * @return aquisição do criador de pacotes desse Descritor de Arquivo.
	 */

	public PacketBuilder getPacketBuilder()
	{
		timeout = lastTickUpdate + DEFAULT_TIMEOUT;

		return packetBuilder;
	}

	/**
	 * Define o descritor de arquivo para ser fechado na próxima atualização do sistema.
	 */

	public void close()
	{
		setUpSource(1);
		logInfo("solicitado fechamento de conexão (fd: %d, ip: %s).\n", getID(), getAddressString());

		flag.set(FLAG_EOF);
	}

	/**
	 * Solicita o fechamento da conexão socket do cliente com o servidor.
	 * Também remove o descritor das sessões existentes no sistema.
	 */

	void closeSocket()
	{
		if (socket == null)
			return;

		try {

			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();

			socket = null;
			parseListener = null;

		} catch (IOException e) {
			logException(e);
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

	/**
	 * Verifica se existem dados para serem lidos através da conexão socket estabelecida.
	 * @return true se houver dados para ler ou false caso contrário.
	 */

	public boolean hasData()
	{
		try {
			return socket != null && socket.getInputStream().available() > 0;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Toda conexão estabelecida pode possuir dados relacionados a sua própria sessão.
	 * Esses dados podem consistir em nome de usuário, código da conta ou personagem.
	 * As informações nele contido irá depender do servidor do qual o está usando.
	 * @return aquisição dos dados da sessão respectivo a esse descritor de arquivo.
	 */

	public abstract SessionData getSessionData();

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("address", address.getString());
		description.append("flag", flag.toStringProperties());
		description.append("parse", parseListener);

		return description.toString();
	}
}

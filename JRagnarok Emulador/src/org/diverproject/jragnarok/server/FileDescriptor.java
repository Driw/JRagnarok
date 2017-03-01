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
 * <p>Um descritor de arquivo � criado a partir de uma conex�o socket para ser usado no JRagnarok.
 * Esse descritor ir� permitir a constru��o de pacotes para enviar dados ou receber dados do cliente.
 * Ser� poss�vel ainda definir algumas propriedades (flag) como verificar se ainda h� conex�o.</p>
 *
 * <p>Ainda possui um atributo para funcionar como cache, permitindo transportar objetos.
 * Para que um descritor seja chamado para ser processado deve ser definido um listener.
 * Quando um listener estiver definido a atualiza��o do servidor chamar� esse listener.</p>
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
	 * Vetor contendo o nome das flags dispon�veis.
	 */
	public static final String FLAG_STRING[] = new String[] { "EOF", "SERVER", "PING" };

	/**
	 * Tempo limite em milissegundos aceito por ociosidade.
	 */
	public static final int DEFAULT_TIMEOUT = seconds(30);


	/**
	 * C�digo de identifica��o do descritor no sistema.
	 */
	int id;

	/**
	 * Tick da �ltima vez em que o descritor de arquivo foi atualizado.
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
	 * Conex�o socket do cliente com o servidor.
	 */
	private Socket socket;

	/**
	 * Objeto para armazenar detalhadamente o endere�o da conex�o socket.
	 */
	private InternetProtocol address;

	/**
	 * Listener para validar conex�o.
	 */
	private FileDescriptorListener parseListener;

	/**
	 * Listener para realizar o fechamento da conex�o.
	 */
	private FileDescriptorListener closeListener;

	/**
	 * Criador de pacotes.
	 */
	private PacketBuilder packetBuilder;

	/**
	 * Cria um novo Descriptor de Arquivo baseado em uma conex�o socket.
	 * @param socket refer�ncia da conex�o do cliente com o servidor.
	 */

	public FileDescriptor(Socket socket)
	{
		this.socket = socket;
		this.flag = new BitWise(FLAG_STRING);
		this.address = new InternetProtocol(socket);
		this.packetBuilder = new PacketBuilder(socket);
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
	 * Flag define o tipo de conex�o do descritor (FLAG_EOF, FLAG_SERVER ou FLAG_PING).
	 * @return aquisi��o da atribui��o de comportamento do descritor no servidor.
	 */

	public BitWise getFlag()
	{
		return flag;
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
	 * Listener que possui um m�todo para processar a��es ap�s o fechamento do socket.
	 * @return aquisi��o do listener usado ap�s a conex�o ter sido fechada.
	 */

	public FileDescriptorListener getCloseListener()
	{
		return closeListener;
	}

	/**
	 * Listener que possui um m�todo para processar a��es ap�s o fechamento do socket.
	 * @param closeListener listener usado ap�s a conex�o ter sido fechada.
	 */

	public void setCloseListener(FileDescriptorListener closeListener)
	{
		this.closeListener = closeListener;
	}

	/**
	 * O construtor de pacotes ir� permitir o recebimento e envio de dados por pacote.
	 * Atrav�s dele ser� poss�vel dizer se � input/output, tamanho e nome do pacote.
	 * @return aquisi��o do criador de pacotes desse Descritor de Arquivo.
	 */

	public PacketBuilder getPacketBuilder()
	{
		timeout = lastTickUpdate + DEFAULT_TIMEOUT;

		return packetBuilder;
	}

	/**
	 * Define o descritor de arquivo para ser fechado na pr�xima atualiza��o do sistema.
	 */

	public void close()
	{
		setUpSource(1);
		logInfo("solicitado fechamento de conex�o (fd: %d, ip: %s).\n", getID(), getAddressString());

		flag.set(FLAG_EOF);
	}

	/**
	 * Solicita o fechamento da conex�o socket do cliente com o servidor.
	 * Tamb�m remove o descritor das sess�es existentes no sistema.
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
	 * Procedimento usado para garantir que a conex�o socket ainda existe.
	 * @return true se o socket estiver conectado ou false caso contr�rio.
	 */

	public boolean isConnected()
	{
		if (id <= 0 || flag.is(FLAG_EOF) || socket == null)
			return false;

		boolean connected = socket.isConnected();

		return connected;
	}

	/**
	 * Verifica se existem dados para serem lidos atrav�s da conex�o socket estabelecida.
	 * @return true se houver dados para ler ou false caso contr�rio.
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
	 * Toda conex�o estabelecida pode possuir dados relacionados a sua pr�pria sess�o.
	 * Esses dados podem consistir em nome de usu�rio, c�digo da conta ou personagem.
	 * As informa��es nele contido ir� depender do servidor do qual o est� usando.
	 * @return aquisi��o dos dados da sess�o respectivo a esse descritor de arquivo.
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

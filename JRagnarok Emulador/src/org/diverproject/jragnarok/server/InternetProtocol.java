package org.diverproject.jragnarok.server;

import java.net.Socket;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.lang.ByteUtil;
import org.diverproject.util.lang.IntUtil;
import org.w3c.dom.ranges.RangeException;

/**
 * <h1>Protocolo de Internet</h1>
 *
 * <p>Objeto que permite especificar um endere�o de IP atrav�s de n�meros inteiros.
 * A especifica��o torna o trabalho com endere�os de IP mais din�mico e vis�vel.
 * Os construtores e setters permitem definir o IP por n�meros inteiros de cada faixa.
 * Al�m disso permite converter o IP para um n�mero inteiro compacto ou string formatada.</p>
 *
 * @author Andrew
 */

public class InternetProtocol
{
	/**
	 * Primeiro n�mero do endere�o.
	 */
	private byte first;

	/**
	 * Segundo n�mero do endere�o.
	 */
	private byte second;

	/**
	 * Terceiro n�mero do endere�o.
	 */
	private byte third;

	/**
	 * Quarto n�mero do endere�o.
	 */
	private byte fourth;

	/**
	 * Cria um novo endere�o de IP usando o endere�o padr�o e local da m�quina.
	 * O valor padr�o/local das m�quinas sempre s�o 127.0.0.1 (localhost).
	 */

	public InternetProtocol()
	{
		this(127, 0, 0, 1);
	}

	/**
	 * Cria um novo endere�o IP atrav�s de n�meros inteiros.
	 * Cada n�mero ir� representar um valor do endere�o.
	 * @param first primeiro valor do endere�o de ip.
	 * @param second segundo valor do endere�o de ip.
	 * @param third terceiro valor do endere�o de ip.
	 * @param fourth quarto valor do endere�o de ip.
	 */

	public InternetProtocol(int first, int second, int third, int fourth)
	{
		set(first, second, third, fourth);
	}

	/**
	 * Permite criar um endere�o de IP atrav�s de uma conex�o socket estabelecida.
	 * @param socket conex�o socket que ser� usado o IP.
	 */

	public InternetProtocol(Socket socket)
	{
		String ip = SocketUtil.socketIP(socket);
		String numbers[] = ip.split("\\.");

		this.first = (byte) IntUtil.parse(numbers[0]);
		this.second = (byte) IntUtil.parse(numbers[1]);
		this.third = (byte) IntUtil.parse(numbers[2]);
		this.fourth = (byte) IntUtil.parse(numbers[3]);
	}

	/**
	 * Endere�os de IP podem ser armazenados em n�meros do tipo inteiros de 4 bytes.
	 * Onde cada byte ir� representar em valor num�rico uma parte do endere�o de IP.
	 * Assim � poss�vel ter o endere�o de IP em 4 n�meros de 0 a 255 como de fato �.
	 * @param intIP valor em n�mero inteiro referente ao endere�o de IP.
	 */

	public InternetProtocol(int intIP)
	{
		String ip = SocketUtil.socketIP(intIP);
		String numbers[] = ip.split("\\.");

		set(IntUtil.parse(numbers[0]), IntUtil.parse(numbers[1]),
			IntUtil.parse(numbers[2]), IntUtil.parse(numbers[3]));
	}

	/**
	 * Permite definir os valores do endere�o de IP por n�meros inteiro.
	 * @param first primeiro valor do endere�o de ip.
	 * @param second segundo valor do endere�o de ip.
	 * @param third terceiro valor do endere�o de ip.
	 * @param fourth quarto valor do endere�o de ip.
	 */

	public void set(int first, int second, int third, int fourth)
	{
		validate(first);
		validate(second);
		validate(third);
		validate(fourth);

		this.first = (byte) first;
		this.second = (byte) second;
		this.third = (byte) third;
		this.fourth = (byte) fourth;
	}

	/**
	 * Valida se um determinado n�mero inteiro est� dentro do limite de IP.
	 * Cada n�mero de um endere�o de IP deve estar entre 0 e 255.
	 * @param value valor num�rico que ser� verificado.
	 */

	private void validate(int value)
	{
		if (!IntUtil.interval(first, 0, 255))
			throw new RangeException((short) value, "valor aceito de 0 a 255 (value: " +value+ ")");
	}

	/**
	 * Cria uma string com o endere�o do IP separando os n�meros por pontos.
	 * @return aquisi��o da string formatada com o endere�o de ip definido.
	 */

	public String getString()
	{
		return String.format("%d.%d.%d.%d", first, second, third, fourth);
	}

	/**
	 * Converte os n�meros armazenados em bytes para um valor num�rico inteiro.
	 * @return aquisi��o do valor do endere�o de IP como n�mero inteiro.
	 */

	public int get()
	{
		return IntUtil.parseBytes(new byte[] { first, second, third, fourth });
	}

	/**
	 * Permite definir o endere�o do IP atrav�s de um n�mero inteiro.
	 * Converte o n�mero em 4 bytes representados de 1 a 255 formando o IP.
	 * @param intIP n�mero inteiro que ser� usado como endere�o de IP.
	 */

	public void set(int intIP)
	{
		byte numbers[] = ByteUtil.parseInt(intIP);

		first = numbers[0];
		second = numbers[1];
		third = numbers[2];
		fourth = numbers[3];
	}

	/**
	 * Copia as informa��es de um outro protocolo de internet para esse objeto.
	 * @param ip refer�ncia do protocolo de internet a ser copiado.
	 */

	public void copy(InternetProtocol ip)
	{
		first = ip.first;
		second = ip.second;
		third = ip.third;
		fourth = ip.fourth;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof InternetProtocol)
		{
			InternetProtocol ip = (InternetProtocol) obj;

			return	ip.first == first &&
					ip.second == second &&
					ip.third == third &&
					ip.fourth == fourth;
		}

		return false;
	}

	@Override
	protected InternetProtocol clone()
	{
		InternetProtocol ip = new InternetProtocol();
		ip.first = first;
		ip.second = second;
		ip.third = third;
		ip.fourth = fourth;

		return ip;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append(getString());

		return description.toString();
	}
}

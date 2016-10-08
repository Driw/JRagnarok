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
	 * 
	 */

	public InternetProtocol()
	{
		this(127, 0, 0, 1);
	}

	/**
	 * 
	 * @param first
	 * @param second
	 * @param third
	 * @param fourth
	 */

	public InternetProtocol(int first, int second, int third, int fourth)
	{
		set(first, second, third, fourth);
	}

	/**
	 * 
	 * @param socket
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
	 * 
	 * @param intIP
	 */

	public InternetProtocol(int intIP)
	{
		String ip = SocketUtil.socketIP(intIP);
		String numbers[] = ip.split(".");

		set(IntUtil.parse(numbers[0]), IntUtil.parse(numbers[1]),
			IntUtil.parse(numbers[2]), IntUtil.parse(numbers[3]));
	}

	/**
	 * @param first
	 * @param second
	 * @param third
	 * @param fourth
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

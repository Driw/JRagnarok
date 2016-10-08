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
 * <p>Objeto que permite especificar um endereço de IP através de números inteiros.
 * A especificação torna o trabalho com endereços de IP mais dinâmico e visível.
 * Os construtores e setters permitem definir o IP por números inteiros de cada faixa.
 * Além disso permite converter o IP para um número inteiro compacto ou string formatada.</p>
 *
 * @author Andrew
 */

public class InternetProtocol
{
	/**
	 * Primeiro número do endereço.
	 */
	private byte first;

	/**
	 * Segundo número do endereço.
	 */
	private byte second;

	/**
	 * Terceiro número do endereço.
	 */
	private byte third;

	/**
	 * Quarto número do endereço.
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
	 * Valida se um determinado número inteiro está dentro do limite de IP.
	 * Cada número de um endereço de IP deve estar entre 0 e 255.
	 * @param value valor numérico que será verificado.
	 */

	private void validate(int value)
	{
		if (!IntUtil.interval(first, 0, 255))
			throw new RangeException((short) value, "valor aceito de 0 a 255 (value: " +value+ ")");
	}

	/**
	 * Cria uma string com o endereço do IP separando os números por pontos.
	 * @return aquisição da string formatada com o endereço de ip definido.
	 */

	public String getString()
	{
		return String.format("%d.%d.%d.%d", first, second, third, fourth);
	}

	/**
	 * Converte os números armazenados em bytes para um valor numérico inteiro.
	 * @return aquisição do valor do endereço de IP como número inteiro.
	 */

	public int get()
	{
		return IntUtil.parseBytes(new byte[] { first, second, third, fourth });
	}

	/**
	 * Permite definir o endereço do IP através de um número inteiro.
	 * Converte o número em 4 bytes representados de 1 a 255 formando o IP.
	 * @param intIP número inteiro que será usado como endereço de IP.
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

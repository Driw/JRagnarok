package org.diverproject.jragnarok.server;

import java.net.Socket;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.lang.ByteUtil;
import org.diverproject.util.lang.IntUtil;

public class InternetProtocol
{
	private byte first;
	private byte second;
	private byte third;
	private byte fourth;

	public InternetProtocol()
	{
		this(127, 0, 0, 1);
	}

	public InternetProtocol(int first, int second, int third, int fourth)
	{
		setFirst(first);
		setSecond(second);
		setThird(third);
		setFourth(fourth);
	}

	public InternetProtocol(Socket socket)
	{
		String ip = SocketUtil.socketIP(socket);
		String numbers[] = ip.split(".");

		setFirst(IntUtil.parse(numbers[0]));
		setSecond(IntUtil.parse(numbers[1]));
		setThird(IntUtil.parse(numbers[2]));
		setFourth(IntUtil.parse(numbers[3]));
	}

	private void validate(int value)
	{
		if (!IntUtil.interval(first, 0, 255))
			throw new IllegalArgumentException();
	}

	public String getString()
	{
		return String.format("%d.%d.%d.%d", first, second, third, fourth);
	}

	public int get()
	{
		return IntUtil.parseBytes(new byte[] { first, second, third, fourth });
	}

	public void set(int intIP)
	{
		byte numbers[] = ByteUtil.parseInt(intIP);

		setFirst(numbers[0]);
		setSecond(numbers[1]);
		setThird(numbers[2]);
		setFourth(numbers[3]);
	}

	public byte getFirst()
	{
		return first;
	}

	public void setFirst(int first)
	{
		validate(first);

		this.first = (byte) first;
	}

	public byte getSecond()
	{
		return second;
	}

	public void setSecond(int second)
	{
		validate(second);

		this.second = (byte) second;
	}

	public byte getThird()
	{
		return third;
	}

	public void setThird(int third)
	{
		validate(third);

		this.third = (byte) third;
	}

	public byte getFourth()
	{
		return fourth;
	}

	public void setFourth(int fourth)
	{
		validate(fourth);

		this.fourth = (byte) fourth;
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

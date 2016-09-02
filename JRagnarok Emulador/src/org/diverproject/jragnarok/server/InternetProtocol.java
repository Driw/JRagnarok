package org.diverproject.jragnarok.server;

import org.diverproject.util.ObjectDescription;
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

	private void validate(int value)
	{
		if (!IntUtil.interval(first, 0, 255))
			throw new IllegalArgumentException();
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

	public String get()
	{
		return String.format("%d.%d.%d.%d", first, second, third, fourth);
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

		description.append(get());

		return description.toString();
	}
}

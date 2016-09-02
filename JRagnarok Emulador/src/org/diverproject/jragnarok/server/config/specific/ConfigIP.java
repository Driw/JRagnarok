package org.diverproject.jragnarok.server.config.specific;

import org.diverproject.jragnaork.configuration.ConfigObject;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.lang.Bits;
import org.diverproject.util.lang.StringUtil;

public class ConfigIP extends ConfigObject<InternetProtocol>
{
	public ConfigIP(String name)
	{
		super(name);
	}

	@Override
	public void setObject(Object object)
	{
		if (object instanceof String)
			setRawValue((String) object);
	}

	private void setRawValue(String string)
	{
		if (StringUtil.countOf(string, '.') != 3)
			throw new IllegalArgumentException();

		int ipAddress = SocketUtil.socketIPInt(string);

		getValue().setFirst(Bits.byteOf(ipAddress, 4));
		getValue().setSecond(Bits.byteOf(ipAddress, 3));
		getValue().setThird(Bits.byteOf(ipAddress, 2));
		getValue().setFourth(Bits.byteOf(ipAddress, 1));
	}
}

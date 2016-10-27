package org.diverproject.jragnarok.server.config.specific;

import org.diverproject.jragnaork.configuration.ConfigObject;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.SocketUtil;
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
		else
			super.setObject(object);
	}

	private void setRawValue(String string)
	{
		if (StringUtil.countOf(string, '.') != 3)
			throw new IllegalArgumentException();

		int ipAddress = SocketUtil.socketIPInt(string);

		getValue().set(ipAddress);
	}

	@Override
	public ConfigIP clone()
	{
		ConfigIP config = new ConfigIP(getName());
		config.setObject(getValue());

		return config;
	}
}

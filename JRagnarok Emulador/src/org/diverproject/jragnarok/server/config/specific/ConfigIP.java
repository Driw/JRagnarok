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
			setRaw((String) object);
	}

	@Override
	public boolean setRaw(String rawValue)
	{
		if (StringUtil.countOf(rawValue, '.') != 3)
			return false;

		int ipAddress = SocketUtil.socketIPInt(rawValue);

		getValue().set(ipAddress);

		return true;
	}

	@Override
	public ConfigIP clone()
	{
		ConfigIP config = new ConfigIP(getName());
		config.setObject(getValue());

		return config;
	}
}

package org.diverproject.jragnarok.server.config.specific;

import org.diverproject.jragnaork.configuration.ConfigObject;
import org.diverproject.jragnarok.server.login.structures.ClientHashNode;

public class ConfigClientHash extends ConfigObject<ClientHashNode>
{
	public ConfigClientHash(String name)
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
		// TODO
	}

	@Override
	public ConfigClientHash clone()
	{
		ConfigClientHash config = new ConfigClientHash(getName());
		config.setObject(getValue());

		return config;
	}
}

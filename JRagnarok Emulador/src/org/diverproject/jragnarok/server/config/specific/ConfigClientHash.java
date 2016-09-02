package org.diverproject.jragnarok.server.config.specific;

import org.diverproject.jragnaork.configuration.ConfigObject;
import org.diverproject.jragnarok.server.ClientHash;
import org.diverproject.util.collection.Node;

public class ConfigClientHash extends ConfigObject<Node<ClientHash>>
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

		super.setObject(object);
	}

	private void setRawValue(String string)
	{
		// TODO
	}
}

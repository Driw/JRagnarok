package org.diverproject.jragnarok.server.config.specific;

import org.diverproject.jragnaork.configuration.ConfigObject;
import org.diverproject.jragnarok.server.login.structures.ClientHash;
import org.diverproject.jragnarok.server.login.structures.ClientHashNode;
import org.diverproject.util.UtilException;
import org.diverproject.util.collection.Node;
import org.diverproject.util.lang.ByteUtil;

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
			setRaw((String) object);
	}

	@Override
	public boolean setRaw(String rawValue)
	{
		if (rawValue.isEmpty())
		{
			setValue(new ClientHashNode(null));
			return true;
		}

		String values[] = rawValue.split(";");

		ClientHash hash[] = new ClientHash[values.length];

		for (int i = 0; i < values.length; i++)
		{
			String value = values[i];
			String tempBytes[] = value.split(" ");

			if (tempBytes.length != ClientHash.SIZE)
				return false;

			byte bytes[] = new byte[ClientHash.SIZE];

			for (int j = 0; j < bytes.length; j++)
			{
				try {
					bytes[j] = ByteUtil.parseString(tempBytes[j]);
				} catch (UtilException e) {
					return false;
				}
			}

			hash[i] = new ClientHash();
			hash[i].set(bytes);
		}

		ClientHashNode root = new ClientHashNode(hash[0]);

		if (hash.length > 1)
		{
			ClientHashNode node = root;

			for (int i = 1; i < hash.length; i++)
			{
				ClientHashNode temp = new ClientHashNode(hash[i]);
				Node.attach(node, temp);
				node = temp;
			}
		}

		setValue(root);

		return true;
	}

	@Override
	public ConfigClientHash clone()
	{
		ConfigClientHash config = new ConfigClientHash(getName());
		config.setObject(getValue());

		return config;
	}
}

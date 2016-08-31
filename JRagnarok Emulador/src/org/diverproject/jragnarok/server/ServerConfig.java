package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigString;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

public abstract class ServerConfig
{
	private static final boolean DEFAULT_BOOLEAN = false;
	private static final int DEFAULT_INTEGER = 0;
	private static final String DEFAULT_STRING = "";

	private Map<String, Config<?>> configurations;

	public ServerConfig()
	{
		configurations = new StringSimpleMap<>();

		for (Config<?> config : getInitialConfigs())
			if (config != null && !config.getName().isEmpty() && config.getValue() != null)
				if (!configurations.add(config.getName(), config))
					logWarning("configuração '%s' repetindo.\n", config.getName());
	}

	public Map<String, Config<?>> getMap()
	{
		return configurations;
	}

	public boolean getBool(String name)
	{
		Config<?> config = configurations.get(name);

		if (config instanceof ConfigBoolean)
			return ((ConfigBoolean) config).getValue();

		return DEFAULT_BOOLEAN;
	}

	public int getInt(String name)
	{
		Config<?> config = configurations.get(name);

		if (config instanceof ConfigInt)
			return ((ConfigInt) config).getValue();

		return DEFAULT_INTEGER;
	}

	public String getString(String name)
	{
		Config<?> config = configurations.get(name);

		if (config instanceof ConfigString)
			return ((ConfigString) config).getValue();

		return DEFAULT_STRING;
	}

	protected abstract Config<?>[] getInitialConfigs();
}

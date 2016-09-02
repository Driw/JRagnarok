package org.diverproject.jragnarok.server.config;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigObject;
import org.diverproject.jragnarok.server.ClientHas;
import org.diverproject.util.collection.Node;

public class ConfigClient
{
	private static final ConfigInt HASH_CHECK;
	private static final Config<Node<ClientHas>> HASH_NODES;
	private static final ConfigInt CHAR_PER_ACCOUNT;
	private static final ConfigBoolean CHECK_VERSION;
	private static final ConfigInt VERSION;

	static
	{
		HASH_CHECK = new ConfigInt("client.hash_check");
		HASH_NODES = new ConfigObject<Node<ClientHas>>("client.hash_nodes");
		CHAR_PER_ACCOUNT = new ConfigInt("client.char_per_account");
		CHECK_VERSION = new ConfigBoolean("client.check_version");
		VERSION = new ConfigInt("client.version");
	}

	public static ConfigInt getHashCheck()
	{
		return HASH_CHECK;
	}

	public static Config<Node<ClientHas>> getHashNodes()
	{
		return HASH_NODES;
	}

	public static ConfigInt getCharPerAccount()
	{
		return CHAR_PER_ACCOUNT;
	}

	public static ConfigBoolean getCheckVersion()
	{
		return CHECK_VERSION;
	}

	public static ConfigInt getVersion()
	{
		return VERSION;
	}
}

package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_SERVERS;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.server.ServerState.CREATED;
import static org.diverproject.jragnarok.server.ServerState.NONE;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.ServerState.STOPED;
import static org.diverproject.log.LogSystem.logExeception;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.character.CharServer;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.map.MapServer;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Index;
import org.diverproject.util.collection.abstraction.StaticArray;

/**
 * Controlador de Servidores
 *
 * <p>Essa classe utiliza-se do padrão de projetos Singleton, afim de evitar instâncias desnecessárias.
 * Mantém uma indexação de servidores conforme o código de identificação limitado a <code>MAX_SERVERS</code>.
 * Além de indexar os servidores permite executar operações a todos os servidores.
 * Como por exemplo a criar, rodar, parar e destruir um servidor forçadamente ou não.</p>
 *
 * @see LoginServer
 * @see CharServer
 * @see MapServer
 * @see Index
 * @see StaticArray
 *
 * @author Andrew Mello
 */

public class ServerControl
{
	private static final ServerControl INSTANCE = new ServerControl();

	private final Index<LoginServer> loginServers;
	private final Index<CharServer> charServers;
	private final Index<MapServer> mapServers;

	private ServerControl()
	{
		loginServers = new StaticArray<>(MAX_SERVERS);
		charServers = new StaticArray<>(MAX_SERVERS);
		mapServers = new StaticArray<>(MAX_SERVERS);
	}

	private void setServerID(Server server, int id)
	{
		if (server.getID() == 0)
			server.setID(id);
	}

	public LoginServer getLoginServer(int id)
	{
		return loginServers.get(id);
	}

	public void add(LoginServer server)
	{
		if (!loginServers.contains(server))
		{
			setServerID(server, loginServers.size() + 1);
			loginServers.update(server.getID(), server);
		}
	}

	public CharServer getCharServer(int id)
	{
		return charServers.get(id);
	}

	public void add(CharServer server)
	{
		if (!charServers.contains(server))
		{
			setServerID(server, charServers.size() + 1);
			charServers.update(server.getID(), server);
		}
	}

	public MapServer getMapServer(int id)
	{
		return mapServers.get(id);
	}

	public void add(MapServer server)
	{
		if (!mapServers.contains(server))
		{
			setServerID(server, mapServers.size() + 1);
			mapServers.update(server.getID(), server);
		}
	}

	public void createAll(boolean force)
	{
		for (Server server : loginServers)
			try {
				if (force || server.isState(NONE))
					server.create();
			} catch (RagnarokException e) {
				logExeception(e);
			}

		for (Server server : charServers)
			try {
				if (force || server.isState(NONE))
					server.create();
			} catch (RagnarokException e) {
				logExeception(e);
			}

		for (Server server : mapServers)
			try {
				if (force || server.isState(NONE))
					server.create();
			} catch (RagnarokException e) {
				logExeception(e);
			}
	}

	public void runAll(boolean force)
	{
		for (Server server : loginServers)
			try {
				if (force || server.isState(CREATED))
					server.run();
			} catch (RagnarokException e) {
				logExeception(e);
			}

		for (Server server : charServers)
			try {
				if (force || server.isState(CREATED))
					server.run();
			} catch (RagnarokException e) {
				logExeception(e);
			}

		for (Server server : mapServers)
			try {
				if (force || server.isState(CREATED))
					server.run();
			} catch (RagnarokException e) {
				logExeception(e);
			}
	}

	public void stopAll(boolean force)
	{
		for (Server server : loginServers)
			try {
				if (force || server.isState(RUNNING))
					server.stop();
			} catch (RagnarokException e) {
				logExeception(e);
			}

		for (Server server : charServers)
			try {
				if (force || server.isState(RUNNING))
					server.stop();
			} catch (RagnarokException e) {
				logExeception(e);
			}

		for (Server server : mapServers)
			try {
				if (force || server.isState(RUNNING))
					server.stop();
			} catch (RagnarokException e) {
				logExeception(e);
			}
	}

	public void destroyAll(boolean force)
	{
		for (Server server : loginServers)
			try {
				if (force || server.isState(STOPED))
					server.destroy();
			} catch (RagnarokException e) {
				logExeception(e);
			}

		for (Server server : charServers)
			try {
				if (force || server.isState(STOPED))
					server.destroy();
			} catch (RagnarokException e) {
				logExeception(e);
			}

		for (Server server : mapServers)
			try {
				if (force || server.isState(STOPED))
					server.destroy();
			} catch (RagnarokException e) {
				logExeception(e);
			}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("login-servers", format("%d/%d", loginServers.size(), loginServers.length()));
		description.append("char-servers", format("%d/%d", charServers.size(), charServers.length()));
		description.append("map-servers", format("%d/%d", mapServers.size(), mapServers.length()));

		return description.toString();
	}

	public static ServerControl getInstance()
	{
		return INSTANCE;
	}
}

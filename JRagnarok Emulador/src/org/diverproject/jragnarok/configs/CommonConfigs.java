package org.diverproject.jragnarok.configs;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_MAP_INDEX;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_DATABASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_HOST;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_CHAR_SERVER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_CLIENT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_IPBAN;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_LAN;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_LOG;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_LOGIN_SERVER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_MAP_SERVER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_MESSAGES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_SQLCONNECTION;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_VIP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_CHARID;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_LOGINID;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_MAPID;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_NAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_DATABASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_HOST;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_LEGACY_DATETIME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_TIMEZONE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_CONFIG_TYPES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_LANGUAGE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_LOG_FILENAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_CHAR_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_LOGIN_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_MAP_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_USE_CONSOLE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_USE_LOG;
import static org.diverproject.util.Util.s;

import org.diverproject.jragnaork.configuration.Configurations;

public class CommonConfigs
{
	public final String language;
	public final String configTypes;
	public final String serverFiles;
	public final String serverFolder;
	public final String serverDefaultFiles;
	public final String serverDefaultLoginFiles;
	public final String serverDefaultCharFiles;
	public final String serverDefaultMapFiles;
	public final String serverDefaultFolder;
	public final boolean useConsole;
	public final boolean useLog;
	public final String logFilename;

	public final String name;
	public final String folder;
	public final String files;
	public final int loginServerID;
	public final int charServerID;
	public final int mapServerID;

	public final String sqlConnection;
	public final String client;
	public final String lan;
	public final String ipban;
	public final String log;
	public final String vip;
	public final String loginServer;
	public final String charServer;
	public final String mapServer;
	public final String messages;

	public final String host;
	public final String database;
	public final String username;
	public final String password;
	public final short port;
	public final boolean legacyDatetime;
	public final String timezone;

	public final String databaseFolder;
	public final short databasePort;
	public final String databaseHost;
	public final String databaseName;
	public final String databaseUsername;
	public final String databasePassword;

	public final String mapIndexes;

	public CommonConfigs(Configurations configs)
	{
		language = configs.getString(SYSTEM_LANGUAGE);
		configTypes = configs.getString(SYSTEM_CONFIG_TYPES);
		serverFiles = configs.getString(SYSTEM_SERVER_FILES);
		serverFolder = configs.getString(SYSTEM_SERVER_FOLDER);
		serverDefaultFiles = configs.getString(SYSTEM_SERVER_DEFAULT_FILES);
		serverDefaultLoginFiles = configs.getString(SYSTEM_SERVER_DEFAULT_LOGIN_FILES);
		serverDefaultCharFiles = configs.getString(SYSTEM_SERVER_DEFAULT_CHAR_FILES);
		serverDefaultMapFiles = configs.getString(SYSTEM_SERVER_DEFAULT_MAP_FILES);
		serverDefaultFolder = configs.getString(SYSTEM_SERVER_DEFAULT_FOLDER);
		useConsole = configs.getBool(SYSTEM_USE_CONSOLE);
		useLog = configs.getBool(SYSTEM_USE_LOG);
		logFilename = configs.getString(SYSTEM_LOG_FILENAME);

		name = configs.getString(SERVER_NAME);
		folder = configs.getString(SERVER_FOLDER);
		files = configs.getString(SERVER_FILES);
		loginServerID = configs.getInt(SERVER_LOGINID);
		charServerID = configs.getInt(SERVER_CHARID);
		mapServerID = configs.getInt(SERVER_MAPID);

		sqlConnection = configs.getString(FILE_SQLCONNECTION);
		client = configs.getString(FILE_CLIENT);
		lan = configs.getString(FILE_LAN);
		ipban = configs.getString(FILE_IPBAN);
		log = configs.getString(FILE_LOG);
		vip = configs.getString(FILE_VIP);
		loginServer = configs.getString(FILE_LOGIN_SERVER);
		charServer = configs.getString(FILE_CHAR_SERVER);
		mapServer = configs.getString(FILE_MAP_SERVER);
		messages = configs.getString(FILE_MESSAGES);

		host = configs.getString(SQL_HOST);
		database = configs.getString(SQL_DATABASE);
		username = configs.getString(SQL_USERNAME);
		password = configs.getString(SQL_PASSWORD);
		port = s(configs.getInt(SQL_PORT));
		legacyDatetime = configs.getBool(SQL_LEGACY_DATETIME);
		timezone = configs.getString(SQL_TIMEZONE);

		databaseFolder = configs.getString(DATABASE_FOLDER);
		databasePort = s(configs.getInt(DATABASE_SQL_PORT));
		databaseHost = configs.getString(DATABASE_SQL_HOST);
		databaseName = configs.getString(DATABASE_SQL_DATABASE);
		databaseUsername = configs.getString(DATABASE_SQL_USERNAME);
		databasePassword = configs.getString(DATABASE_SQL_PASSWORD);

		mapIndexes = configs.getString(DATABASE_MAP_INDEX);
	}
}

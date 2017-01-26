package org.diverproject.jragnarok.configs;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHAR_BILLING;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHAR_VIP;
import static org.diverproject.jragnarok.JRagnarokConstants.PACKETVER;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;

import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigString;
import org.diverproject.jragnaork.configuration.Configurations;

public class JRagnarokConfigs
{
	public static final String FILE_SYSTEM = "conf/System.conf";
	public static final String FILE_CONFIG_TYPES = "conf/ConfigTypes.conf";

	public static final String TYPES_CONFIGS = "types";

	public static final String SYSTEM_CONFIGS = "system";
	public static final String SYSTEM_LANGUAGE = SYSTEM_CONFIGS+ ".language";
	public static final String SYSTEM_CONFIG_TYPES = SYSTEM_CONFIGS+ ".types";
	public static final String SYSTEM_SERVER_FILES = SYSTEM_CONFIGS+ ".files";
	public static final String SYSTEM_SERVER_FOLDER = SYSTEM_CONFIGS+ ".folder";
	public static final String SYSTEM_USE_CONSOLE = SYSTEM_CONFIGS+ ".use_console";
	public static final String SYSTEM_USE_LOG = SYSTEM_CONFIGS+ ".use_log";
	public static final String SYSTEM_LOG_FILENAME = SYSTEM_CONFIGS+ ".log_filename";
	public static final String SYSTEM_SERVER_DEFAULT_FILES = SYSTEM_CONFIGS+ ".default_files";
	public static final String SYSTEM_SERVER_DEFAULT_LOGIN_FILES = SYSTEM_CONFIGS+ ".default_login_files";
	public static final String SYSTEM_SERVER_DEFAULT_CHAR_FILES = SYSTEM_CONFIGS+ ".default_char_files";
	public static final String SYSTEM_SERVER_DEFAULT_MAP_FILES = SYSTEM_CONFIGS+ ".default_map_files";
	public static final String SYSTEM_SERVER_DEFAULT_FOLDER = SYSTEM_CONFIGS+ ".default_folder";

	public static final String SERVER_CONFIGS = "server";
	public static final String SERVER_NAME = SERVER_CONFIGS+ ".name";
	public static final String SERVER_FOLDER = SERVER_CONFIGS+ ".folder";
	public static final String SERVER_FILES = SERVER_CONFIGS+ ".files";
	public static final String SERVER_LOGINID = SERVER_CONFIGS+ ".login_server_id";
	public static final String SERVER_CHARID = SERVER_CONFIGS+ ".char_server_id";
	public static final String SERVER_MAPID = SERVER_CONFIGS+ ".map_server_id";

	public static final String FILES_CONFIGS = "file";
	public static final String FILE_SQLCONNECTION = FILES_CONFIGS+ ".sql_connection";
	public static final String FILE_CLIENT = FILES_CONFIGS+ ".client";
	public static final String FILE_LAN = FILES_CONFIGS+ ".lan";
	public static final String FILE_IPBAN = FILES_CONFIGS+ ".ipban";
	public static final String FILE_LOG = FILES_CONFIGS+ ".lan";
	public static final String FILE_VIP = FILES_CONFIGS+ ".vip";
	public static final String FILE_LOGIN_SERVER = FILES_CONFIGS+ ".login_server";
	public static final String FILE_CHAR_SERVER = FILES_CONFIGS+ ".char_server";
	public static final String FILE_MAP_SERVER = FILES_CONFIGS+ ".map_server";
	public static final String FILE_MESSAGES = FILES_CONFIGS+ ".messages";

	public static final String SQL_CONFIGS = "sql";
	public static final String SQL_HOST = SQL_CONFIGS+ ".host";
	public static final String SQL_PORT = SQL_CONFIGS+ ".port";
	public static final String SQL_DATABASE = SQL_CONFIGS+ ".database";
	public static final String SQL_USERNAME = SQL_CONFIGS+ ".username";
	public static final String SQL_PASSWORD = SQL_CONFIGS+ ".password";
	public static final String SQL_LEGACY_DATETIME = SQL_CONFIGS+ ".legacydatetime";
	public static final String SQL_TIMEZONE = SQL_CONFIGS+ ".timezone";

	public static final String LOGIN_CONFIGS = "login";
	public static final String LOGIN_IP = LOGIN_CONFIGS+ ".ip";
	public static final String LOGIN_PORT = LOGIN_CONFIGS+ ".port";
	public static final String LOGIN_USERNAME = LOGIN_CONFIGS+ ".username";
	public static final String LOGIN_PASSWORD = LOGIN_CONFIGS+ ".password";
	public static final String LOGIN_IP_SYNC_INTERVAL = LOGIN_CONFIGS+ ".ip_sync_interval";
	public static final String LOGIN_ONLINE_CLEANUP_INTERVAL = LOGIN_CONFIGS+ ".online_cleanup_interval";
	public static final String LOGIN_DATE_FORMAT = LOGIN_CONFIGS+ ".date_format";
	public static final String LOGIN_USE_MD5_PASSWORD = LOGIN_CONFIGS+ ".use_md5_password";
	public static final String LOGIN_GROUP_TO_CONNECT = LOGIN_CONFIGS+ ".group_to_connnect";
	public static final String LOGIN_MIN_GROUP_TO_CONNECT = LOGIN_CONFIGS+ ".min_group_to_connect";
	public static final String LOGIN_ALLOWED_REGS = LOGIN_CONFIGS+ ".allowed_regs";
	public static final String LOGIN_TIME_ALLOWED = LOGIN_CONFIGS+ ".time_allowed";

	public static final String CHAR_CONFIGS = "char";
	public static final String CHAR_IP = CHAR_CONFIGS+ ".ip";
	public static final String CHAR_PORT = CHAR_CONFIGS+ ".port";
	public static final String CHAR_USERNAME = CHAR_CONFIGS+ ".username";
	public static final String CHAR_PASSWORD = CHAR_CONFIGS+ ".password";
	public static final String CHAR_SERVER_NAME = CHAR_CONFIGS+ ".name";
	public static final String CHAR_WISP_SERVER_NAME = CHAR_CONFIGS+ ".wisp_server_name";
	public static final String CHAR_MAINTANCE = CHAR_CONFIGS+ ".maintance";
	public static final String CHAR_NEW_DISPLAY = CHAR_CONFIGS+ ".new_display";
	public static final String CHAR_MAX_USERS = CHAR_CONFIGS+ ".max_users";
	public static final String CHAR_OVERLOAD_BYPASS = CHAR_CONFIGS+ ".overload_bypass";
	public static final String CHAR_MOVE_ENABLED = CHAR_CONFIGS+ ".move_enabled";
	public static final String CHAR_MOVE_TO_USED = CHAR_CONFIGS+ ".movetoused";
	public static final String CHAR_MOVE_UNLIMITED = CHAR_CONFIGS+ ".moves_unlimited";
	public static final String CHAR_CHECKDB = CHAR_CONFIGS+ ".checkdb";
	public static final String CHAR_DEFAULT_MAP = CHAR_CONFIGS+ ".default_map";
	public static final String CHAR_DEFAULT_MAP_X = CHAR_CONFIGS+ ".default_map_x";
	public static final String CHAR_DEFAULT_MAP_Y = CHAR_CONFIGS+ ".default_map_y";

	public static final String MAP_CONFIGS = "map";
	public static final String MAP_IP = MAP_CONFIGS+ ".ip";
	public static final String MAP_PORT = MAP_CONFIGS+ ".port";
	public static final String MAP_USERNAME = MAP_CONFIGS+ ".username";
	public static final String MAP_PASSWORD = MAP_CONFIGS+ ".password";

	public static final String DATABASE_CONFIGS = "database";
	public static final String DATABASE_SQL_HOST = DATABASE_CONFIGS+ ".sql_host";
	public static final String DATABASE_SQL_PORT = DATABASE_CONFIGS+ ".sql_port";
	public static final String DATABASE_SQL_DATABASE = DATABASE_CONFIGS+ ".sql_database";
	public static final String DATABASE_SQL_USERNAME = DATABASE_CONFIGS+ ".sql_username";
	public static final String DATABASE_SQL_PASSWORD = DATABASE_CONFIGS+ ".sql_password";
	public static final String DATABASE_FOLDER = DATABASE_CONFIGS+ ".folder";
	public static final String DATABASE_MAP_INDEX = DATABASE_CONFIGS+ ".map_index";

	public static final String LOG_CONFIGS = "log";
	public static final String LOG_LOGIN = LOG_CONFIGS+ ".login";

	public static final String IPBAN_CONFIGS = "ipban";
	public static final String IPBAN_ENABLED = IPBAN_CONFIGS+ ".enabled";
	public static final String IPBAN_CLEANUP_INTERVAL = IPBAN_CONFIGS+ ".cleanup_interval";
	public static final String IPBAN_PASS_FAILURE = IPBAN_CONFIGS+ ".pass_failure";
	public static final String IPBAN_PASS_FAILURE_INTERVAL = IPBAN_CONFIGS+ ".pass_failure_interval";
	public static final String IPBAN_PASS_FAILURE_LIMIT = IPBAN_CONFIGS+ ".pass_failure_limit";
	public static final String IPBAN_PASS_FAILURE_DURATION = IPBAN_CONFIGS+ ".pass_failure_duration";

	public static final String CLIENT_CONFIGS = "client";
	public static final String CLIENT_HASH_CHECK = CLIENT_CONFIGS+ ".hash_check";
	public static final String CLIENT_HASH_NODES = CLIENT_CONFIGS+ ".hash_nodes";
	public static final String CLIENT_CHAR_PER_ACCOUNT = CLIENT_CONFIGS+ ".char_per_account";
	public static final String CLIENT_CHECK_VERSION = CLIENT_CONFIGS+ ".check_version";
	public static final String CLIENT_VERSION = CLIENT_CONFIGS+ ".version";

	public static final String PINCODE_CONFIGS = "pincode";
	public static final String PINCODE_ENABLED = PINCODE_CONFIGS+ ".enabled";
	public static final String PINCODE_CHANGE_TIME = PINCODE_CONFIGS+ ".change_time";
	public static final String PINCODE_MAXTRY = PINCODE_CONFIGS+ ".maxtry";
	public static final String PINCODE_FORCE = PINCODE_CONFIGS+ ".force";
	public static final String PINCODE_ALLOW_REPEATED = PINCODE_CONFIGS+ ".allow_repeated";
	public static final String PINCODE_ALLOW_SEQUENTIAL = PINCODE_CONFIGS+ ".allow_sequential";

	public static final String CHARACTER_CONFIGS = "character";
	public static final String CHARACTER_CREATE = CHARACTER_CONFIGS+ ".create";
	public static final String CHARACTER_PER_ACCOUNT = CHARACTER_CONFIGS+ ".per_account";
	public static final String CHARACTER_IGNORING_CASE = CHARACTER_CONFIGS+ ".name_ignoring_case";
	public static final String CHARACTER_UNKNOW_CHAR_NAME = CHARACTER_CONFIGS+ ".unknow_char_name";
	public static final String CHARACTER_NAME_LETTERS = CHARACTER_CONFIGS+ ".name_letters";
	public static final String CHARACTER_NAME_OPTION = CHARACTER_CONFIGS+ ".name_option";
	public static final String CHARACTER_DELETE_OPTION = CHARACTER_CONFIGS+ ".delete_option";
	public static final String CHARACTER_DELETE_LEVEL = CHARACTER_CONFIGS+ ".delete_level";
	public static final String CHARACTER_DELETE_DELAY = CHARACTER_CONFIGS+ ".delete_delay";

	public static final String VIP_CONFIGS = "vip";
	public static final String VIP_GROUPID = VIP_CONFIGS+ ".groupid";
	public static final String VIP_CHAR_INCREASE = VIP_CONFIGS+ ".char_increase";

	public static final String SERVER_THREAD_PRIORITY = SERVER_CONFIGS+ ".thread_priority";

	public static final Configurations newSystemConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigString(SYSTEM_LANGUAGE));
		configurations.add(new ConfigString(SYSTEM_CONFIG_TYPES, "ConfigTypes.conf"));
		configurations.add(new ConfigString(SYSTEM_SERVER_FILES));
		configurations.add(new ConfigString(SYSTEM_SERVER_FOLDER, "Servers"));
		configurations.add(new ConfigString(SYSTEM_SERVER_DEFAULT_FILES, "Log.conf"));
		configurations.add(new ConfigString(SYSTEM_SERVER_DEFAULT_LOGIN_FILES, "IpBan.conf,Client.conf,LoginServer.conf"));
		configurations.add(new ConfigString(SYSTEM_SERVER_DEFAULT_CHAR_FILES, "CharServer.conf"));
		configurations.add(new ConfigString(SYSTEM_SERVER_DEFAULT_MAP_FILES, "MapServer.conf"));
		configurations.add(new ConfigString(SYSTEM_SERVER_DEFAULT_FOLDER, "Default"));
		configurations.add(new ConfigBoolean(SYSTEM_USE_CONSOLE, true));
		configurations.add(new ConfigBoolean(SYSTEM_USE_LOG, true));
		configurations.add(new ConfigString(SYSTEM_LOG_FILENAME, "log"));

		return configurations;
	}

	public static Configurations newServerConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigString(SERVER_NAME, "Server"));
		configurations.add(new ConfigString(SERVER_FOLDER, "ServerFolder"));
		configurations.add(new ConfigString(SERVER_FILES, "SqlConnection.conf"));
		configurations.add(new ConfigInt(SERVER_LOGINID, 1));
		configurations.add(new ConfigInt(SERVER_CHARID, 1));
		configurations.add(new ConfigInt(SERVER_MAPID, 1));

		return configurations;
	}

	public static Configurations newFileConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigString(FILE_SQLCONNECTION, "SqlConnection.conf"));
		configurations.add(new ConfigString(FILE_CLIENT, "Client.conf"));
		configurations.add(new ConfigString(FILE_LAN, "Lan.conf"));
		configurations.add(new ConfigString(FILE_IPBAN, "IpBan.conf"));
		configurations.add(new ConfigString(FILE_LOG, "Log.conf"));
		configurations.add(new ConfigString(FILE_VIP, "Vip.conf"));
		configurations.add(new ConfigString(FILE_LOGIN_SERVER, "LoginServer.conf"));
		configurations.add(new ConfigString(FILE_CHAR_SERVER, "CharServer.conf"));
		configurations.add(new ConfigString(FILE_MAP_SERVER, "MapServer.conf"));
		configurations.add(new ConfigString(FILE_MESSAGES, "Messages.conf"));

		return configurations;
	}

	public static Configurations newSqlConnectionConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigString(SQL_HOST, "localhost"));
		configurations.add(new ConfigString(SQL_DATABASE, "jragnarok"));
		configurations.add(new ConfigString(SQL_USERNAME, "jragnarok"));
		configurations.add(new ConfigString(SQL_PASSWORD, "jragnarok"));
		configurations.add(new ConfigInt(SQL_PORT, 3306));
		configurations.add(new ConfigBoolean(SQL_LEGACY_DATETIME, false));
		configurations.add(new ConfigString(SQL_TIMEZONE, "Africa/Abidjan"));

		configurations.add(new ConfigString(DATABASE_FOLDER, "database"));
		configurations.add(new ConfigInt(DATABASE_SQL_PORT, 3306));
		configurations.add(new ConfigString(DATABASE_SQL_HOST, "localhost"));
		configurations.add(new ConfigString(DATABASE_SQL_DATABASE, "jragnarok_db"));
		configurations.add(new ConfigString(DATABASE_SQL_USERNAME, "server"));
		configurations.add(new ConfigString(DATABASE_SQL_PASSWORD, "passwd"));
		configurations.add(new ConfigString(DATABASE_MAP_INDEX, "map_index.txt"));

		return configurations;
	}

	public static Configurations newLoginServerConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigString(LOGIN_IP, "localhost"));
		configurations.add(new ConfigInt(LOGIN_PORT, 6900));
		configurations.add(new ConfigString(LOGIN_USERNAME, "server"));
		configurations.add(new ConfigString(LOGIN_PASSWORD, "passwd"));
		configurations.add(new ConfigInt(LOGIN_IP_SYNC_INTERVAL, 10));
		configurations.add(new ConfigInt(LOGIN_ONLINE_CLEANUP_INTERVAL, 30));
		configurations.add(new ConfigString(LOGIN_DATE_FORMAT, "YY-mm-dd HH:MM:SS"));
		configurations.add(new ConfigBoolean(LOGIN_USE_MD5_PASSWORD, false));
		configurations.add(new ConfigInt(LOGIN_GROUP_TO_CONNECT, 0));
		configurations.add(new ConfigInt(LOGIN_MIN_GROUP_TO_CONNECT, 0));
		configurations.add(new ConfigInt(LOGIN_ALLOWED_REGS, 1));
		configurations.add(new ConfigInt(LOGIN_TIME_ALLOWED, 10));

		configurations.add(new ConfigInt(VIP_GROUPID, 4));
		configurations.add(new ConfigInt(VIP_CHAR_INCREASE, MAX_CHAR_VIP));

		return configurations;
	}

	public static Configurations newCharServerConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigString(CHAR_IP, "localhost"));
		configurations.add(new ConfigInt(CHAR_PORT, 6900));
		configurations.add(new ConfigString(CHAR_USERNAME, "server"));
		configurations.add(new ConfigString(CHAR_PASSWORD, "passwd"));
		configurations.add(new ConfigString(CHAR_SERVER_NAME, "Servidor de Personagem"));
		configurations.add(new ConfigString(CHAR_WISP_SERVER_NAME, "Servidor"));
		configurations.add(new ConfigInt(CHAR_MAINTANCE, 0));
		configurations.add(new ConfigBoolean(CHAR_NEW_DISPLAY, false));
		configurations.add(new ConfigInt(CHAR_MAX_USERS, -1));
		configurations.add(new ConfigInt(CHAR_OVERLOAD_BYPASS, 99));
		configurations.add(new ConfigBoolean(CHAR_MOVE_ENABLED, true));
		configurations.add(new ConfigBoolean(CHAR_MOVE_TO_USED, true));
		configurations.add(new ConfigBoolean(CHAR_MOVE_UNLIMITED, false));
		configurations.add(new ConfigBoolean(CHAR_CHECKDB, false));
		configurations.add(new ConfigString(CHAR_DEFAULT_MAP, "prontera"));
		configurations.add(new ConfigInt(CHAR_DEFAULT_MAP_X, 156));
		configurations.add(new ConfigInt(CHAR_DEFAULT_MAP_Y, 191));

		configurations.add(new ConfigBoolean(PINCODE_ENABLED, true));
		configurations.add(new ConfigInt(PINCODE_CHANGE_TIME, 0));
		configurations.add(new ConfigInt(PINCODE_MAXTRY, 3));
		configurations.add(new ConfigBoolean(PINCODE_FORCE, true));
		configurations.add(new ConfigBoolean(PINCODE_ALLOW_REPEATED, false));
		configurations.add(new ConfigBoolean(PINCODE_ALLOW_SEQUENTIAL, false));

		configurations.add(new ConfigBoolean(CHARACTER_CREATE, true));
		configurations.add(new ConfigInt(CHARACTER_PER_ACCOUNT, 0));
		configurations.add(new ConfigBoolean(CHARACTER_IGNORING_CASE, false));
		configurations.add(new ConfigString(CHARACTER_UNKNOW_CHAR_NAME, "Desconhecido"));
		configurations.add(new ConfigString(CHARACTER_NAME_LETTERS, "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"));
		configurations.add(new ConfigInt(CHARACTER_NAME_OPTION, 1));
		configurations.add(new ConfigInt(CHARACTER_DELETE_OPTION, 2));
		configurations.add(new ConfigInt(CHARACTER_DELETE_LEVEL, 150));
		configurations.add(new ConfigInt(CHARACTER_DELETE_DELAY, 86400));

		return configurations;
	}

	public static Configurations newMapServerConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigString(MAP_IP, "localhost"));
		configurations.add(new ConfigInt(MAP_PORT, 6900));
		configurations.add(new ConfigString(MAP_USERNAME, "server"));
		configurations.add(new ConfigString(MAP_PASSWORD, "passwd"));

		return configurations;
	}

	public static Configurations newLogConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigBoolean(LOG_LOGIN, true));

		return configurations;
	}

	public static Configurations newIPBanConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigBoolean(IPBAN_ENABLED, true));
		configurations.add(new ConfigInt(IPBAN_CLEANUP_INTERVAL, 60));
		configurations.add(new ConfigBoolean(IPBAN_PASS_FAILURE, true));
		configurations.add(new ConfigInt(IPBAN_PASS_FAILURE_INTERVAL, 5));
		configurations.add(new ConfigInt(IPBAN_PASS_FAILURE_LIMIT, 7));
		configurations.add(new ConfigInt(IPBAN_PASS_FAILURE_DURATION, 5));

		return configurations;
	}

	public static Configurations newClientConfigs()
	{
		Configurations configurations = new Configurations();
		configurations.add(new ConfigBoolean(CLIENT_HASH_CHECK, false));
		configurations.add(new ConfigString(CLIENT_HASH_NODES, ""));
		configurations.add(new ConfigInt(CLIENT_CHAR_PER_ACCOUNT, MAX_CHARS - MAX_CHAR_VIP - MAX_CHAR_BILLING));
		configurations.add(new ConfigBoolean(CLIENT_CHECK_VERSION, false));
		configurations.add(new ConfigInt(CLIENT_VERSION, dateToVersion(PACKETVER)));

		return configurations;
	}
}

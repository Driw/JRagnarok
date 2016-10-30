package org.diverproject.jragnarok;

public class JRagnarokConfigs
{
	public static final String FILE_SYSTEM = "conf/System.conf";
	public static final String FILE_CONFIG_TYPES = "conf/ConfigTypes.conf";

	public static final String TYPES_CONFIGS = "types";

	public static final String SYSTEM_CONFIGS = "system";
	public static final String SYSTEM_CONFIG_TYPES = SYSTEM_CONFIGS+ ".types";
	public static final String SYSTEM_SERVER_FILES = SYSTEM_CONFIGS+ ".files";
	public static final String SYSTEM_SERVER_FOLDER = SYSTEM_CONFIGS+ ".folder";
	public static final String SYSTEM_USE_CONSOLE = SYSTEM_CONFIGS+ ".use_console";
	public static final String SYSTEM_USE_LOG = SYSTEM_CONFIGS+ ".use_log";
	public static final String SYSTEM_LOG_FILENAME = SYSTEM_CONFIGS+ ".log_filename";
	public static final String SYSTEM_SERVER_DEFAULT_FILES = SYSTEM_CONFIGS+ ".default_files";
	public static final String SYSTEM_SERVER_DEFAULT_FOLDER = SYSTEM_CONFIGS+ ".default_folder";

	public static final String SERVER_CONFIGS = "server";
	public static final String SERVER_NAME = SERVER_CONFIGS+ ".name";
	public static final String SERVER_FOLDER = SERVER_CONFIGS+ ".folder";
	public static final String SERVER_FILES = SERVER_CONFIGS+ ".files";
	public static final String SERVER_LOGINID = SERVER_CONFIGS+ "login_server_id";
	public static final String SERVER_CHARID = SERVER_CONFIGS+ "char_server_id";
	public static final String SERVER_MAPID = SERVER_CONFIGS+ ".map_server_id";

	public static final String SERVER_HOST = SERVER_CONFIGS+ ".host";
	public static final String SERVER_PORT = SERVER_CONFIGS+ ".port";
	public static final String SERVER_THREAD_PRIORITY = SERVER_CONFIGS+ ".thread_priority";

	public static final String CHAR_CONFIGS = "char";
	public static final String CHAR_HOST = CHAR_CONFIGS+ ".host";
	public static final String CHAR_PORT = CHAR_CONFIGS+ ".port";

	public static final String MAP_CONFIGS = "map";
	public static final String MAP_HOST = MAP_CONFIGS+ ".host";
	public static final String MAP_PORT = MAP_CONFIGS+ ".port";
}

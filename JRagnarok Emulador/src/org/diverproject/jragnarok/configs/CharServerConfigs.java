package org.diverproject.jragnarok.configs;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_DEFAULT_MAP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_DEFAULT_MAP_X;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_DEFAULT_MAP_Y;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_CREATE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_DELETE_DELAY;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_DELETE_LEVEL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_DELETE_OPTION;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_IGNORING_CASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_NAME_LETTERS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_NAME_OPTION;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_PER_ACCOUNT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHARACTER_UNKNOW_CHAR_NAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_CHECKDB;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MAINTANCE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MAX_USERS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_TO_USED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MOVE_UNLIMITED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_NEW_DISPLAY;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_OVERLOAD_BYPASS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_SERVER_NAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_WISP_SERVER_NAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_ALLOW_REPEATED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_ALLOW_SEQUENTIAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_CHANGE_TIME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_FORCE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_MAXTRY;
import static org.diverproject.util.Util.s;

import org.diverproject.jragnaork.configuration.Configurations;

public class CharServerConfigs extends CommonConfigs
{
	public final String ip;
	public final short port;
	public final String username;
	public final String password;
	public final String serverName;
	public final String wispServerName;
	public final int maintance;
	public final boolean newDisplay;
	public final int maxUsers;
	public final int overloadBypass;
	public final boolean moveEnabled;
	public final boolean moveToUsed;
	public final boolean moveUnlimited;
	public final boolean checkDB;
	public final String defaultMap;
	public final int defaultMapX;
	public final int defaultMapY;

	public final boolean pincodeEnabled;
	public final int pincodeChangeTime;
	public final int pincodeMaxTry;
	public final boolean pincodeForce;
	public final boolean pincodeAllowRepeated;
	public final boolean pincodeAllowSequential;

	public final boolean charCreate;
	public final int charPerAccount;
	public final boolean charIgnoringCase;
	public final String charUnknowCharName;
	public final String charNameLetters;
	public final int charNameOption;
	public final int charDeleteOption;
	public final int charDeleteLevel;
	public final int charDeleteDelay;

	public CharServerConfigs(Configurations configs)
	{
		super(configs);

		ip = configs.getString(CHAR_IP);
		port = s(configs.getInt(CHAR_PORT));
		username = configs.getString(CHAR_USERNAME);
		password = configs.getString(CHAR_PASSWORD);
		serverName = configs.getString(CHAR_SERVER_NAME);
		wispServerName = configs.getString(CHAR_WISP_SERVER_NAME);
		maintance = configs.getInt(CHAR_MAINTANCE);
		newDisplay = configs.getBool(CHAR_NEW_DISPLAY);
		maxUsers = configs.getInt(CHAR_MAX_USERS);
		overloadBypass = configs.getInt(CHAR_OVERLOAD_BYPASS);
		moveEnabled = configs.getBool(CHAR_MOVE_ENABLED);
		moveToUsed = configs.getBool(CHAR_MOVE_TO_USED);
		moveUnlimited = configs.getBool(CHAR_MOVE_UNLIMITED);
		checkDB = configs.getBool(CHAR_CHECKDB);
		defaultMap = configs.getString(CHAR_DEFAULT_MAP);
		defaultMapX = configs.getInt(CHAR_DEFAULT_MAP_X);
		defaultMapY = configs.getInt(CHAR_DEFAULT_MAP_Y);

		pincodeEnabled = configs.getBool(PINCODE_ENABLED);
		pincodeChangeTime = configs.getInt(PINCODE_CHANGE_TIME);
		pincodeMaxTry = configs.getInt(PINCODE_MAXTRY);
		pincodeForce = configs.getBool(PINCODE_FORCE);
		pincodeAllowRepeated = configs.getBool(PINCODE_ALLOW_REPEATED);
		pincodeAllowSequential = configs.getBool(PINCODE_ALLOW_SEQUENTIAL);

		charCreate = configs.getBool(CHARACTER_CREATE);
		charPerAccount = configs.getInt(CHARACTER_PER_ACCOUNT);
		charIgnoringCase = configs.getBool(CHARACTER_IGNORING_CASE);
		charUnknowCharName = configs.getString(CHARACTER_UNKNOW_CHAR_NAME);
		charNameLetters = configs.getString(CHARACTER_NAME_LETTERS);
		charNameOption = configs.getInt(CHARACTER_NAME_OPTION);
		charDeleteOption = configs.getInt(CHARACTER_DELETE_OPTION);
		charDeleteLevel = configs.getInt(CHARACTER_DELETE_LEVEL);
		charDeleteDelay = configs.getInt(CHARACTER_DELETE_DELAY);
	}
}

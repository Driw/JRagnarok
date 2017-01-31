package org.diverproject.jragnarok.configs;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CLIENT_CHAR_PER_ACCOUNT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CLIENT_CHECK_VERSION;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CLIENT_HASH_CHECK;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CLIENT_HASH_NODES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CLIENT_VERSION;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_CLEANUP_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_DURATION;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_LIMIT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_ALLOWED_REGS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_DATE_FORMAT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_GROUP_TO_CONNECT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP_SYNC_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_MIN_GROUP_TO_CONNECT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_ONLINE_CLEANUP_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_TIME_ALLOWED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_USE_MD5_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.VIP_CHAR_INCREASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.VIP_GROUPID;
import static org.diverproject.util.Util.s;

import org.diverproject.jragnaork.configuration.Configurations;

public class LoginServerConfigs extends CommonConfigs
{
	public final String ip;
	public final short port;
	public final String username;
	public final String password;
	public final int ipSyncInterval;
	public final int onlineCleanupInterval;
	public final String dateFormat;
	public final boolean useMD5Password;
	public final int grupToConnect;
	public final int minGroupToConnect;
	public final int AllowedRegs;
	public final int timeAllowed;

	public final int vipGroupID;
	public final int vipCharIncrease;

	public final boolean logLogin;

	public final boolean ipbanEnabled;
	public final int ipbanCleanupInterval;
	public final boolean ipbanPassFailure;
	public final int ipbanPassFailureInterval;
	public final int ipbanPassFailureLimit;
	public final int ipbanPassFailureDuration;

	public final boolean hashCheck;
	public final String hashNodes;
	public final int charPerAccount;
	public final boolean checkVersion;
	public final int version;

	public LoginServerConfigs(Configurations configs)
	{
		super(configs);

		ip = configs.getString(LOGIN_IP);
		port = s(configs.getInt(LOGIN_PORT));
		username = configs.getString(LOGIN_USERNAME);
		password = configs.getString(LOGIN_PASSWORD);
		ipSyncInterval = configs.getInt(LOGIN_IP_SYNC_INTERVAL);
		onlineCleanupInterval = configs.getInt(LOGIN_ONLINE_CLEANUP_INTERVAL);
		dateFormat = configs.getString(LOGIN_DATE_FORMAT);
		useMD5Password = configs.getBool(LOGIN_USE_MD5_PASSWORD);
		grupToConnect = configs.getInt(LOGIN_GROUP_TO_CONNECT);
		minGroupToConnect = configs.getInt(LOGIN_MIN_GROUP_TO_CONNECT);
		AllowedRegs = configs.getInt(LOGIN_ALLOWED_REGS);
		timeAllowed = configs.getInt(LOGIN_TIME_ALLOWED);

		vipGroupID = configs.getInt(VIP_GROUPID);
		vipCharIncrease = configs.getInt(VIP_CHAR_INCREASE);

		logLogin = configs.getBool(IPBAN_ENABLED);

		ipbanEnabled = configs.getBool(IPBAN_ENABLED);
		ipbanCleanupInterval = configs.getInt(IPBAN_CLEANUP_INTERVAL);
		ipbanPassFailure = configs.getBool(IPBAN_PASS_FAILURE);
		ipbanPassFailureInterval = configs.getInt(IPBAN_PASS_FAILURE_INTERVAL);
		ipbanPassFailureLimit = configs.getInt(IPBAN_PASS_FAILURE_LIMIT);
		ipbanPassFailureDuration = configs.getInt(IPBAN_PASS_FAILURE_DURATION);

		hashCheck = configs.getBool(CLIENT_HASH_CHECK);
		hashNodes = configs.getString(CLIENT_HASH_NODES);
		charPerAccount = configs.getInt(CLIENT_CHAR_PER_ACCOUNT);
		checkVersion = configs.getBool(CLIENT_CHECK_VERSION);
		version = configs.getInt(CLIENT_VERSION);
	}
}

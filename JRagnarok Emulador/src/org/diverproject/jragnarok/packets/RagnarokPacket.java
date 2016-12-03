package org.diverproject.jragnarok.packets;

public class RagnarokPacket
{
	public static final short PACKET_LOGIN = 0x0064;
	public static final short PACKET_CHAR_SERVER_SELECTED = 0x0065;
	public static final short PACKET_REFUSE_LOGIN = 0x006A;
	public static final short PACKET_LIST_SERVERS = 0x0069;
	public static final short PACKET_REFUSE_ENTER = 0x006C;
	public static final short PACKET_NOTIFY_AUTH = 0x0081;
	public static final short PACKET_KEEP_ALIVE = 0x0200;
	public static final short PACKET_UPDATE_CLIENT_HASH = 0x0204;
	public static final short PACKET_LOGIN_PCBANG = 0x0277;
	public static final short PACKET_LOGIN_HAN = 0x02B0;
	public static final short PACKET_ACKNOWLEDGE_HASH = 0x01DC;
	public static final short PACKET_LOGIN_MD5 = 0x01DD;
	public static final short PACKET_LOGIN_MD5INFO = 0x01FA;
	public static final short PACKET_LOGIN_MD5MAC = 0x027C;
	public static final short PACKET_LOGIN_SSO = 0x0825;
	public static final short PACKET_REFUSE_LOGIN_R2 = 0x083E;
	public static final short PACKET_REQ_HASH = 0x01DB;

	public static final short PACKET_REQ_CHAR_SERVER_CONNECT = 0x2710;
	public static final short PACKET_RES_CHAR_SERVER_CONNECT = 0x2711;
	public static final short PACKET_REQ_AUTH_ACCOUNT = 0x2712;
	public static final short PACKET_RES_AUTH_ACCOUNT = 0x2713;
	public static final short PACKET_UPDATE_USER_COUNT = 0x2714;
	// 0x2715
	public static final short PACKET_REQ_ACCOUNT_DATA = 0x2716;
	public static final short PACKET_RES_ACCOUNT_DATA = 0x2717;
	public static final short PACKET_REQ_KEEP_ALIVE = 0x2718;
	public static final short PACKET_RES_KEEP_ALIVE = 0x2719;
	public static final short PACKET_REQ_ACCOUNT_INFO = 0x2720;
	public static final short PACKET_RES_ACCOUNT_INFO = 0x2721;
	public static final short PACKET_REQ_CHANGE_EMAIL = 0x2722;
	public static final short PACKET_ACCOUNT_STATE_UPDATE = 0x2724;
	public static final short PACKET_ACCOUNT_STATE_NOTIFY = 0x2725;
	public static final short PACKET_REQ_GLOBAL_ACCREG = 0x2726;
	public static final short PACKET_SET_ACCOUNT_ONLINE = 0x272B;
	public static final short PACKET_ALREADY_ONLINE = 0x2734;
	public static final short PACKET_SYNCRONIZE_IPADDRESS = 0x2735;

}

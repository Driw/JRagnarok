package org.diverproject.jragnarok.packets;

public class RagnarokPacketList
{
	private RagnarokPacketList() { }

	public static final short PACKET_LOGIN = 0x0064;
	public static final short PACKET_REFUSE_LOGIN = 0x006A;
	public static final short PACKET_LIST_SERVERS = 0x0069;
	public static final short PACKET_REFUSE_ENTER = 0x006C;
	public static final short PACKET_NOTIFY_BAN = 0x0081;
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

	public static final short PACKET_REQ_CHAR_CONNECT = 0x2710;
	public static final short PACKET_RES_CHAR_CONNECT = 0x2711;
	public static final short PACKET_REC_AUTH_ACCOUNT = 0x2712;
	public static final short PACKET_RES_AUTH_ACCOUNT = 0x2713;
	public static final short PACKET_UPDATE_USER_COUNT = 0x2714;
	public static final short PACKET_REQ_ACCOUNT_DATA = 0x2716;
	public static final short PACKET_RES_ACCOUNT_DATA = 0x2717;
	public static final short PACKET_PING_RESPONSE = 0x2718;
	public static final short PACKET_PING_REQUEST = 0x2719;
	public static final short PACKET_ACK_ACCOUNT_INFO = 0x2721;
	public static final short PACKET_ACK_CHANGE_SEX = 0x2723;
	public static final short PACKET_ACK_GLOBAL_ACCREG = 0x2726;
	public static final short PACKET_ACCOUNT_BAN_NOTIFICATION = 0x2731;
	public static final short PACKET_ALREADY_ONLINE = 0x2734;
	public static final short PACKET_SYNCRONIZE_IPADDRESS = 0x2735;

	public static final short PACKET_ACK_VIP = 0x2743;
}

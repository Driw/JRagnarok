package org.diverproject.jragnarok.packets;

public class RagnarokPacket
{
	// -- RAGNAROK PACKET ID : SERVER TO CLIENT (SC) --
	public static final short PACKET_SC_NOTIFY_BAN           = 0x0081;

	// -- RAGNAROK PACKET ID : CLIENT TO LOGIN (CA) --
	public static final short PACKET_CA_LOGIN                = 0x0064;
	public static final short PACKET_CA_LOGIN2               = 0x01DD;
	public static final short PACKET_CA_LOGIN3               = 0x01FA;
	public static final short PACKET_CA_CONNECT_INFO_CHANGED = 0x0200;
	public static final short PACKET_CA_EXE_HASHCHECK        = 0x0204;
	public static final short PACKET_CA_LOGIN_PCBANG         = 0x0277;
	public static final short PACKET_CA_LOGIN4               = 0x027C;
	public static final short PACKET_CA_LOGIN_HAN            = 0x02B0;
	public static final short PACKET_CA_SSO_LOGIN_REQ        = 0x0825;
	public static final short PACKET_CA_REQ_HASH             = 0x01DB;
	public static final short PACKET_CA_CHARSERVERCONNECT    = 0x2710;

	// -- RAGNAROK PACKET ID : LOGIN TO CLIENT (AC) --
	public static final short PACKET_AC_ACCEPT_LOGIN         = 0x0069;
	public static final short PACKET_AC_REFUSE_LOGIN         = 0x006A;
	public static final short PACKET_AC_ACK_HASH             = 0x01DC;
	public static final short PACKET_AC_REFUSE_LOGIN_R2      = 0x083E;

	// ------

	public static final short PACKET_CHAR_SERVER_SELECTED = 0x0065;
	public static final short PACKET_SEND_ACCOUNT_CHARS = 0x006B;
	public static final short PACKET_REFUSE_ENTER = 0x006C;
	public static final short PACKET_SEND_ACCOUNT_SLOT = 0x082D;
	public static final short PACKET_PINCODE_SEND_STATE = 0x08B9;
	public static final short PACKET_SEND_CHARS_PER_PAGE = 0x099D;
	public static final short PACKET_SEND_CHAR_PAGE_COUNT = 0x09A0;
	public static final short PACKET_REQ_CHARLIST = 0x09A1;

	// -- SERVIDORES (LOGIN|CHAR|MAP SERVERS) --

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
	public static final short PACKET_REQ_CHANGE_SEX = 0x2723;
	public static final short PACKET_ACCOUNT_STATE_UPDATE = 0x2724;
	public static final short PACKET_ACCOUNT_STATE_NOTIFY = 0x2725;
	public static final short PACKET_RES_GLOBAL_REGISTERS = 0x2726;
	// 0x2727
	public static final short PACKET_UPDATE_REGISTER = 0x2728;
	// 0x2729
	// 0x2720
	public static final short PACKET_REQ_UNBAN_ACCOUNT = 0x272A;
	public static final short PACKET_SET_ACCOUNT_ONLINE = 0x272B;
	public static final short PACKET_SET_ACCOUNT_OFFLINE = 0x272C;
	public static final short PACKET_SEND_ACCOUNTS = 0x272D;
	public static final short PACKET_REQ_GLOBAL_REGISTER = 0x272E;
	// 0x272F
	// 0x2730
	public static final short PACKET_REQ_BAN_NOTIFICATION = 0x2731;
	// 0x2732
	// 0x2733
	public static final short PACKET_ALREADY_ONLINE = 0x2734;
	public static final short PACKET_SYNCRONIZE_IPADDRESS = 0x2735;
	public static final short PACKET_UPDATE_IP = 0x2736;
	public static final short PACKET_SET_ALL_ACC_OFFLINE = 0x2737;
	public static final short PACKET_NOTIFY_PIN_UPDATE = 0x2738;
	public static final short PACKET_NOTIFY_PIN_ERROR = 0x2739;
	// 0x273A
	// 0x273B
	// 0x273C
	// 0x273D
	// 0x273E
	// 0x273F
	// 0x2740
	// 0x2741
	public static final short PACKET_REQ_VIP_DATA = 0x2742;
	public static final short PACKET_RES_VIP_DATA = 0x2743;

	// 0x2AF9
	// 0x2AF0
	// 0x2AFA
	// 0x2AFB
	// 0x2AFC
	// 0x2AFD
	// 0x2AFE
	// 0x2AFF
	public static final short PACKET_REQ_CHAR_MAP_USER_COUNT = 0x2B00;
	public static final short PACKET_CHANGE_SEX_NOTIFY = 0x2B0D;
	public static final short PACKET_RES_BAN_NOTIFICATION = 0x2B14;
	public static final short PACKET_BROADCAST_UPDATE_IP = 0x2B1E;
}

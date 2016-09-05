package org.diverproject.jragnarok.packets;

public class RagnarokPacketList
{
	private RagnarokPacketList() { }

	public static final short PACKET_CA_LOGIN = 0x0064;
	public static final short PACKET_AC_REFUSE_LOGIN = 0x006A;
	public static final short PACKET_CA_CONNECT_INFO_CHANGED = 0x200;
	public static final short PACKET_CA_EXE_HASHCHECK = 0x0204;
	public static final short PACKET_CA_LOGIN_PCBANG = 0x0277;
	public static final short PACKET_CA_LOGIN_HAN = 0x02B0;
	public static final short PACKET_AC_ACK_HASH = 0x01DC;
	public static final short PACKET_CA_LOGIN2 = 0x01DD;
	public static final short PACKET_CA_LOGIN3 = 0x01FA;
	public static final short PACKET_CA_LOGIN4 = 0x027C;
	public static final short PACKET_CA_SSO_LOGIN_REQ = 0x0825;
	public static final short PACKET_CA_REQ_HASH = 0x01DB;
	public static final short PACKET_CA_REQ_CHAR_CONNECT = 0x2710;
	public static final short PACKET_CA_RES_CHAR_CONNECT = 0x2711;
	public static final short PACKET_SYNCRONIZE_IPADDRESS = 0x2735;
}

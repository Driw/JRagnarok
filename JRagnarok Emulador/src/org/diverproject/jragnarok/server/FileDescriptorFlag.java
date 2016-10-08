package org.diverproject.jragnarok.server;

public class FileDescriptorFlag
{
	private byte eof;
	private byte server;
	private byte ping;

	public byte getEOF()
	{
		return eof;
	}

	public void setEOF(byte eof)
	{
		this.eof = eof;
	}

	public byte getServer()
	{
		return server;
	}

	public void setServer(byte server)
	{
		this.server = server;
	}

	public byte getPing()
	{
		return ping;
	}

	public void setPing(byte ping)
	{
		this.ping = ping;
	}
}

package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokConstants.PINCODE_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.util.Time;

public class Pincode
{
	private int id;
	private boolean enabled;
	private String code;
	private Time changed;

	public Pincode()
	{
		changed = new Time();
	}

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		if (id > 0)
			this.id = id;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = strcap(code, PINCODE_LENGTH);
	}

	public Time getChanged()
	{
		return changed;
	}

	public void setChangedNow()
	{
		changed.set(System.currentTimeMillis());
	}
}

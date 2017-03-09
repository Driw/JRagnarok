package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokConstants.PINCODE_LENGTH;
import static org.diverproject.util.Util.random;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class Pincode
{
	private boolean enabled;
	private String code;
	private Time changed;
	private int seed;

	public Pincode()
	{
		changed = new Time();
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
		if (code == null || code.length() == PINCODE_LENGTH)
			this.code = code;
	}

	public Time getChanged()
	{
		return changed;
	}

	public void setChangedNow()
	{
		changed.set(System.currentTimeMillis());
	}

	public int getSeed()
	{
		return seed;
	}

	public void genSeed()
	{
		seed = random() % 0xFFFF;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("enabled", enabled);
		description.append("code", code);
		description.append("changed", changed);

		return description.toString();
	}
}

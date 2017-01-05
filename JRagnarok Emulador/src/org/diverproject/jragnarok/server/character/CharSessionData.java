package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.common.SessionData;
import org.diverproject.jragnarok.server.login.entities.Group;
import org.diverproject.jragnarok.server.login.entities.Pincode;
import org.diverproject.jragnarok.server.login.entities.Vip;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class CharSessionData extends SessionData
{
	private boolean auth;

	private LoginSeed seed;

	private String newName;
	private String email;
	private String birthdate;
	private Time expiration;

	private Vip vip;
	private Group group;
	private Pincode pincode;

	private byte charSlots;
	private CharData chars[];

	private BitWise flag;
	private Timer charBlockTime;

	private int charactersMove;

	public CharSessionData()
	{
		this.expiration = new Time();
		this.flag = new BitWise();
		this.chars = new CharData[MAX_CHARS];
		this.seed = new LoginSeed();
		this.pincode = new Pincode();
		this.group = new Group();
		this.vip = new Vip();
	}

	public boolean isAuth()
	{
		return auth;
	}

	public void setAuth(boolean auth)
	{
		this.auth = auth;
	}

	public LoginSeed getSeed()
	{
		return seed;
	}

	public String getNewName()
	{
		return newName;
	}

	public void setNewName(String newName)
	{
		this.newName = strcap(newName, NAME_LENGTH);
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = strcap(email, EMAIL_LENGTH);
	}

	public String getBirthdate()
	{
		return birthdate;
	}

	public void setBirthdate(String birthdate)
	{
		this.birthdate = strcap(birthdate, 10);
	}

	public Time getExpiration()
	{
		return expiration;
	}

	public Vip getVip()
	{
		return vip;
	}

	public void setVip(Vip vip)
	{
		this.vip = vip;
	}

	public Group getGroup()
	{
		return group;
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	public Pincode getPincode()
	{
		return pincode;
	}

	public void setPincode(Pincode pincode)
	{
		this.pincode = pincode;
	}

	public byte getCharSlots()
	{
		return charSlots;
	}

	public void setCharSlots(byte charSlots)
	{
		this.charSlots = charSlots;
	}

	public CharData getCharData(int index)
	{
		return interval(index, 0, chars.length - 1) ? chars[index] : null;
	}

	public void setCharData(CharData data, int index)
	{
		if (interval(index, 0, chars.length - 1))
			chars[index] = data;
	}

	public BitWise getFlag()
	{
		return flag;
	}

	public Timer getCharBlockTime()
	{
		return charBlockTime;
	}

	public void setCharBlockTime(Timer charBlockTime)
	{
		this.charBlockTime = charBlockTime;
	}

	public int getCharactersMove()
	{
		return charactersMove;
	}

	public void setCharactersMove(int charactersMove)
	{
		this.charactersMove = charactersMove;
	}

	@Override
	public void toString(ObjectDescription description)
	{
		if (auth)
			description.append("auth");

		if (seed != null)
			description.append("seed", format("%d %d", seed.getFirst(), seed.getSecond()));

		description.append("newName", newName);
		description.append("email", email);
		description.append("birthdate", birthdate);
		description.append("expiration", expiration);

		if (vip != null)	description.append("vip", vip.getName());
		if (group != null)	description.append("group", group.getName());
		if (pincode != null)description.append("pincode", pincode.getCode());

		for (int i = 0, j = 0; i < chars.length; i++)
		{
			if (chars[i] != null)
				j++;

			if (i == chars.length - 1)
				description.append("chars", j);
		}

		description.append("flag", flag.toStringProperties());
		description.append("charBlockTime", charBlockTime);
	}
}

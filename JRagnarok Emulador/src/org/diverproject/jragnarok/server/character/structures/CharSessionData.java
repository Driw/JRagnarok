package org.diverproject.jragnarok.server.character.structures;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.login.entities.Group;
import org.diverproject.jragnarok.server.login.entities.Pincode;
import org.diverproject.jragnarok.server.login.entities.Vip;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class CharSessionData
{
	private FileDescriptor fileDescriptor;

	private int accountID;
	private boolean auth;

	private LoginSeed seed;

	private String newName;
	private String email;
	private String birthdate;
	private Time expiration;

	private Vip vip;
	private Group group;
	private Pincode pincode;

	private CharData chars[];

	private BitWise flag;
	private Time charBlockTime;
	private int version;
	private ClientType clientType;

	public CharSessionData(FileDescriptor fileDecriptor)
	{
		this.fileDescriptor = fileDecriptor;

		this.charBlockTime = new Time();
		this.expiration = new Time();
		this.flag = new BitWise();
		this.chars = new CharData[MAX_CHARS];
	}

	public FileDescriptor getFileDescriptor()
	{
		return fileDescriptor;
	}

	void setFileDescriptor(FileDescriptor fileDecriptor)
	{
		this.fileDescriptor = fileDecriptor;
	}

	public int getAddress()
	{
		return fileDescriptor.getAddress();
	}

	public String getAddressString()
	{
		return fileDescriptor.getAddressString();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
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

	public void setSeed(LoginSeed seed)
	{
		this.seed = seed;
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

	public void setExpiration(Time expiration)
	{
		this.expiration = expiration;
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

	public CharData getChars(int index)
	{
		return interval(index, 0, chars.length - 1) ? chars[index] : null;
	}

	public void setChars(CharData... chars)
	{
		for (int i = 0; i < chars.length; i++)
			this.chars[i] = chars[i];

		for (int i = chars.length; i < this.chars.length; i++)
			this.chars[i] = null;
	}

	public BitWise getFlag()
	{
		return flag;
	}

	public Time getCharBlockTime()
	{
		return charBlockTime;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public ClientType getClientType()
	{
		return clientType;
	}

	public void setClientType(ClientType clientType)
	{
		this.clientType = clientType;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("fd", fileDescriptor.getID());

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
		description.append("version", version);
		description.append("clientType", clientType);

		return description.toString();
	}
}

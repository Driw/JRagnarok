package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.PASSWORD_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.PINCODE_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.USERNAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_ACCOUNT_INFO;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_AccountInfo extends RequestPacket
{
	private int serverFD;
	private int userFD;
	private int accountID;
	private boolean useData;
	private int groupID;
	private int loginCount;
	private int state;
	private String email;
	private int lastIP;
	private int lastLogin;
	private String birthdate;
	private String password;
	private String pincode;
	private String username;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInts(serverFD, userFD, accountID);
		output.putByte(b(useData ? 1 : 0));

		if (useData)
		{
			output.putInts(groupID, loginCount, state);
			output.putString(email, EMAIL_LENGTH);
			output.putInts(lastIP, lastLogin);
			output.putString(birthdate, 10);
			output.putString(password, PASSWORD_LENGTH);
			output.putString(pincode, PINCODE_LENGTH);
			output.putString(username, USERNAME_LENGTH);
		}
	}

	@Override
	protected void receiveInput(Input input)
	{
		serverFD = input.getInt();
		userFD = input.getInt();
		accountID = input.getInt();
		useData = input.getByte() == 1;
	}

	public int getMapFD()
	{
		return serverFD;
	}

	public void setMapFD(int mapfd)
	{
		if (mapfd > 0)
			this.serverFD = mapfd;
	}

	public int getUFD()
	{
		return userFD;
	}

	public void setUFD(int ufd)
	{
		if (ufd > 0)
			this.userFD = ufd;
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		if (accountID > 0)
			this.accountID = accountID;
	}

	public boolean hasData()
	{
		return useData;
	}

	public void setData(boolean use)
	{
		this.useData = use;
	}

	public int getGroupID()
	{
		return groupID;
	}

	public void setGroupID(int groupID)
	{
		if (groupID > 0)
			this.groupID = groupID;
	}

	public int getLoginCount()
	{
		return loginCount;
	}

	public void setLoginCount(int loginCount)
	{
		if (loginCount > 0)
			this.loginCount = loginCount;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		if (state > 0)
			this.state = state;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = strcap(email, EMAIL_LENGTH);
	}

	public int getLastIP()
	{
		return lastIP;
	}

	public void setLastIP(int lastIP)
	{
		this.lastIP = lastIP;
	}

	public int getLastLogin()
	{
		return lastLogin;
	}

	public void setLastLogin(int lastLogin)
	{
		if (lastLogin > 0)
			this.lastLogin = lastLogin;
	}

	public String getBirthdate()
	{
		return birthdate;
	}

	public void setBirthdate(String birthdate)
	{
		if (birthdate.length() == 10)
			this.birthdate = birthdate;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = strcap(password, PASSWORD_LENGTH);
	}

	public String getPincode()
	{
		return pincode;
	}

	public void setPincode(String pincode)
	{
		if (pincode.length() == PINCODE_LENGTH)
			this.pincode = pincode;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = strcap(username, USERNAME_LENGTH);
	}

	@Override
	public String getName()
	{
		return "AH_ACCOUNT_INFO";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_ACCOUNT_INFO;
	}

	@Override
	protected int length()
	{
		return DYNAMIC_PACKET_LENGTH;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("serverFD", serverFD);
		description.append("userFD", userFD);
		description.append("accountID", accountID);

		if (useData)
		{
			description.append("groupID", groupID);
			description.append("loginCount", loginCount);
			description.append("state", state);
			description.append("email", email);
			description.append("lastIP", lastIP);
			description.append("lastLogin", lastLogin);
			description.append("birthdate", birthdate);
			description.append("password", password);
			description.append("pincode", pincode);
			description.append("username", username);
		}
	}
}

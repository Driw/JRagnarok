package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.PASSWORD_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.jragnarok.server.FileDecriptor;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;

public class LoginSessionData
{
	public static final int DENCRYPT = 1;
	public static final int DENCRYPT2 = 2;

	private static final String DENCRYPT_STRING[] = new String[]
	{
		"DENCRYPT", "DENCRYPT2"
	};

	private FileDecriptor fileDecriptor;
	private int id;
	private String username;
	private String password;
	private Sex sex;
	private String lastLogin;
	private byte group;
	private ClientType clientType;
	private int version;
	private BitWise passDencrypt;
	private LoginSeed seed;
	private ClientHash clientHash;

	public LoginSessionData(FileDecriptor fileDecriptor)
	{
		this.fileDecriptor = fileDecriptor;
		this.sex = Sex.SERVER;
		this.passDencrypt = new BitWise(DENCRYPT_STRING);
	}

	public FileDecriptor getFileDecriptor()
	{
		return fileDecriptor;
	}

	void setFileDecriptor(FileDecriptor fileDecriptor)
	{
		this.fileDecriptor = fileDecriptor;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = strcap(username, NAME_LENGTH);
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = strcap(password, PASSWORD_LENGTH);
	}

	public Sex getSex()
	{
		return sex;
	}

	public void setSex(Sex sex)
	{
		if (sex != null)
			this.sex = sex;
	}

	public String getLastLogin()
	{
		return lastLogin;
	}

	public void setLastLogin(String lastLogin)
	{
		this.lastLogin = strcap(lastLogin, 24);
	}

	public byte getGroup()
	{
		return group;
	}

	public void setGroup(byte group)
	{
		this.group = group;
	}

	public ClientType getClientType()
	{
		return clientType;
	}

	public void setClientType(ClientType clientType)
	{
		this.clientType = clientType;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public BitWise getPassDencrypt()
	{
		return passDencrypt;
	}

	public void setPassDencrypt(BitWise passDencrypt)
	{
		this.passDencrypt = passDencrypt;
	}

	public LoginSeed getSeed()
	{
		return seed;
	}

	public ClientHash getClientHash()
	{
		return clientHash;
	}

	public void setClientHash(ClientHash clientHash)
	{
		this.clientHash = clientHash;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("fd", fileDecriptor.getID());
		description.append("id", id);
		description.append("username", username);
		description.append("password", password);
		description.append("sex", sex);
		description.append("lastLogin", lastLogin);
		description.append("group", group);
		description.append("clientType", clientType);
		description.append("version", version);
		description.append("passDencrypt", passDencrypt);

		if (clientHash != null)
			description.append("clientHash", new String(clientHash.getHash()));

		if (seed != null)
			description.append("seed", format("%d %d", seed.getFirst(), seed.getSecond()));

		return description.toString();
	}
}

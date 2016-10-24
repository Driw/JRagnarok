package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.login.entities.Group;
import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;

public class LoginSessionData extends Login
{
	public static final int PASSWORD_DENCRYPT = 1;
	public static final int PASSWORD_DENCRYPT2 = 2;

	private static final String DENCRYPT_STRING[] = new String[]
	{
		"DENCRYPT", "DENCRYPT2"
	};

	private FileDescriptor fileDecriptor;
	private Group group;
	private ClientType clientType;
	private int version;
	private BitWise passDencrypt;
	private LoginSeed seed;
	private ClientHash clientHash;
	private short md5KeyLenght;
	private String md5Key;

	public LoginSessionData(FileDescriptor fileDecriptor)
	{
		this.fileDecriptor = fileDecriptor;
		this.passDencrypt = new BitWise(DENCRYPT_STRING);
		this.seed = new LoginSeed();
	}

	public FileDescriptor getFileDecriptor()
	{
		return fileDecriptor;
	}

	void setFileDecriptor(FileDescriptor fileDecriptor)
	{
		this.fileDecriptor = fileDecriptor;
	}

	public int getAddress()
	{
		return fileDecriptor.getID();
	}

	public String getAddressString()
	{
		return fileDecriptor.getAddressString();
	}

	public Group getGroup()
	{
		return group;
	}

	public void setGroup(Group group)
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

	public short getMd5KeyLenght()
	{
		return md5KeyLenght;
	}

	public void setMd5KeyLenght(short md5KeyLenght)
	{
		this.md5KeyLenght = md5KeyLenght;
	}

	public String getMd5Key()
	{
		return md5Key;
	}

	public void setMd5Key(String md5Key)
	{
		this.md5Key = strcap(md5Key, 20);
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		description.append("fd", fileDecriptor.getID());

		super.toString(description);

		description.append("group", group);
		description.append("clientType", clientType);
		description.append("version", version);
		description.append("passDencrypt", passDencrypt);

		if (clientHash != null)
			description.append("clientHash", new String(clientHash.getHash()));

		if (seed != null)
			description.append("seed", format("%d %d", seed.getFirst(), seed.getSecond()));

		if (md5KeyLenght > 0)
		{
			description.append("md5KeyLenght", md5KeyLenght);
			description.append("md5Key", md5Key);
		}
	}
}

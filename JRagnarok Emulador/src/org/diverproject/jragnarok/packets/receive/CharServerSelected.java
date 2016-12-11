package org.diverproject.jragnarok.packets.receive;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CHAR_SERVER_SELECTED;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CharServerSelected extends ReceivePacket
{
	private int accountID;
	private int firstSeed;
	private int secondSeed;
	private ClientType clientType;
	private char sex;

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		firstSeed = input.getInt();
		secondSeed = input.getInt();
		clientType = ClientType.parse(b(input.getShort()));
		sex = (char) input.getByte();
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o da conta.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @return aquisi��o do primeiro c�digo da seed do acesso.
	 */

	public int getFirstSeed()
	{
		return firstSeed;
	}

	/**
	 * @return aquisi��o do segundo c�digo da seed do acesso.
	 */

	public int getSecondSeed()
	{
		return secondSeed;
	}

	/**
	 * @return aquisi��o do tipo de cliente.
	 */

	public ClientType getClientType()
	{
		return clientType;
	}

	/**
	 * @return aquisi��o do sexo da conta acessada.
	 */

	public char getSex()
	{
		return sex;
	}

	@Override
	public String getName()
	{
		return "PACKET_CHAR_SERVER_SELECTED";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CHAR_SERVER_SELECTED;
	}

	@Override
	protected int length()
	{
		return 15;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("firstSeed", firstSeed);
		description.append("secondSeed", secondSeed);
		description.append("clientType", clientType);
		description.append("sex", sex);
	}
}

package org.diverproject.jragnarok.packets.receive;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CHAR_SERVER_SELECTED;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.Input;

public class CharServerSelected extends ReceivePacket
{
	private int accountID;
	private int firstSeed;
	private int secondSeed;
	private char sex;

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		firstSeed = input.getInt();
		secondSeed = input.getInt();

		input.skipe(2);

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
		return 0;
	}
}

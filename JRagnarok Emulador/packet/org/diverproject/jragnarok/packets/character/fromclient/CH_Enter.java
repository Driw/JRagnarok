package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_ENTER;
import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CH_Enter extends ReceivePacket
{
	private int accountID;
	private int firstSeed;
	private int secondSeed;
	private ClientType clientType;
	private Sex sex;

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		firstSeed = input.getInt();
		secondSeed = input.getInt();
		clientType = ClientType.parse(b(input.getShort()));
		sex = Sex.parse(input.getByte());
	}

	/**
	 * @return aquisição do código de identificação da conta.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @return aquisição do primeiro código da seed do acesso.
	 */

	public int getFirstSeed()
	{
		return firstSeed;
	}

	/**
	 * @return aquisição do segundo código da seed do acesso.
	 */

	public int getSecondSeed()
	{
		return secondSeed;
	}

	/**
	 * @return aquisição do tipo de cliente.
	 */

	public ClientType getClientType()
	{
		return clientType;
	}

	/**
	 * @return aquisição do sexo do servidor acessado (espero SERVER).
	 */

	public Sex getSex()
	{
		return sex;
	}

	@Override
	public String getName()
	{
		return "CH_ENTER";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_ENTER;
	}

	@Override
	protected int length()
	{
		return 17;
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

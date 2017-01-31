package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_GLOBAL_REGISTERS;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.GlobalRegister;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_GlobalRegisters extends RequestPacket
{
	public static final byte NONE_TYPE = 0;
	public static final byte INTEGER_TYPE = 1;
	public static final byte STRING_TYPE = 2;	

	private int accountID;
	private int charID;
	private Queue<GlobalRegister<?>> registers;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInts(accountID, charID, registers.size());

		while (!registers.isEmpty())
		{
			GlobalRegister<?> register = registers.poll();

			if (register.getValue() instanceof Integer)
			{
				output.putByte(INTEGER_TYPE);
				output.putString(register.getKey());
				output.putInt((Integer) register.getValue());
			}

			else if (register.getValue() instanceof String)
			{
				output.putByte(STRING_TYPE);
				output.putString(register.getKey());
				output.putString((String) register.getValue());
			}

			else
				output.putByte(NONE_TYPE);
		}
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		charID = input.getInt();
		registers = new DynamicQueue<>();

		int size = input.getInt();

		for (int i = 0; i < size; i++)
		{
			byte type = input.getByte();

			if (type == INTEGER_TYPE)
			{
				GlobalRegister<Integer> register = new GlobalRegister<>(accountID, input.getString());
				register.setValue(input.getInt());
				register.setUpdatable(false);
				registers.offer(register);
			}

			else if (type == STRING_TYPE)
			{
				GlobalRegister<String> register = new GlobalRegister<>(accountID, input.getString());
				register.setValue(input.getString());
				register.setUpdatable(false);
				registers.offer(register);
			}
		}
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public int getCharID()
	{
		return charID;
	}

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	public Queue<GlobalRegister<?>> getRegisters()
	{
		return registers;
	}

	public void setRegisters(Queue<GlobalRegister<?>> registers)
	{
		this.registers = registers;
	}

	@Override
	public String getName()
	{
		return "HA_GLOBAL_REGISTERS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_GLOBAL_REGISTERS;
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

		description.append("accountID", accountID);
		description.append("charID", charID);

		if (registers != null)
			description.append("registers", registers.size());
	}
}

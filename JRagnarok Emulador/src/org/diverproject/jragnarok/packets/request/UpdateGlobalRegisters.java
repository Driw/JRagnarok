package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_UPDATE_REGISTER;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.GlobalAccountReg;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class UpdateGlobalRegisters extends RequestPacket
{
	public static final byte VALUE_NONE = 0;
	public static final byte VALUE_INTEGER = 1;
	public static final byte VALUE_STRING = 2;

	public static final int OPERATION_INT_REPLACE = 0;
	public static final int OPERATION_INT_DELETE = 1;
	public static final int OPERATION_STR_REPLACE = 2;
	public static final int OPERATION_STR_DELETE = 3;

	private int accountID;
	private Queue<GlobalAccountReg> registers;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putInt(registers.size());

		for (GlobalAccountReg register = registers.poll(); registers != null; register = registers.poll())
		{
			output.putString(register.getKey());
			output.putInt(register.getOperation());

			Object value = register.getValue();

			if (value instanceof String)
			{
				output.putByte(VALUE_STRING);
				output.putString((String) value);
			}

			else if (value instanceof Integer)
			{
				output.putByte(VALUE_INTEGER);
				output.putInt((Integer) value);
			}

			else
				output.putByte(VALUE_NONE);
		}
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		registers = new DynamicQueue<>();

		int size = input.getInt();

		for (int i = 0; i < size; i++)
		{
			GlobalAccountReg register = new GlobalAccountReg();
			register.setKey(input.getString());
			register.setOperation(input.getInt());

			byte type = input.getByte();

			switch (type)
			{
				case VALUE_NONE:
					register.setValue(null);
					break;

				case VALUE_INTEGER:
					register.setValue(input.getInt());
					break;

				case VALUE_STRING:
					register.setValue(input.getString());
					break;

				default:
					throw new RagnarokRuntimeException("'%d' não é um tipo de registro", type);
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

	public Queue<GlobalAccountReg> getRegisters()
	{
		return registers;
	}

	public void setRegisters(Queue<GlobalAccountReg> registers)
	{
		this.registers = registers;
	}

	@Override
	public String getName()
	{
		return "UPDATE_REGISTER";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_UPDATE_REGISTER;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

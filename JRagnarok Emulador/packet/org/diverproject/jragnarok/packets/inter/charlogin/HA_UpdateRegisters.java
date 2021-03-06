package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.util.Util.size;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_UPDATE_REGISTERS;

import org.diverproject.jragnarok.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.GlobalRegister;
import org.diverproject.jragnarok.server.common.GlobalRegisterOperation;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_UpdateRegisters extends RequestPacket
{
	public static final byte VALUE_NONE = 0;
	public static final byte VALUE_INTEGER = 1;
	public static final byte VALUE_STRING = 2;

	private int accountID;
	private Queue<GlobalRegisterOperation<?>> operations;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putInt(operations.size());

		while (!operations.isEmpty())
		{
			GlobalRegisterOperation<?> operation = operations.poll();

			if (operation.getRegister() != null)
			{
				output.putInt(operation.getOperation());
				output.putString(operation.getRegister().getKey());

				Object value = operation.getRegister().getValue();

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
			}

			output.putByte(VALUE_NONE);
		}
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		operations = new DynamicQueue<>();

		int size = input.getInt();

		for (int i = 0; i < size; i++)
		{
			@SuppressWarnings("rawtypes")
			GlobalRegisterOperation<?> operation = new GlobalRegisterOperation();
			operation.setOperation(input.getInt());
			operations.offer(operation);

			String key = input.getString();
			byte type = input.getByte();

			switch (type)
			{
				case VALUE_NONE:
					continue;

				case VALUE_INTEGER:
					GlobalRegister<Integer> integerRegister = new GlobalRegister<>(accountID, key);
					integerRegister.setValue(input.getInt());
					integerRegister.setUpdatable(false);
					break;

				case VALUE_STRING:
					GlobalRegister<String> stringRegister = new GlobalRegister<>(accountID, key);
					stringRegister.setValue(input.getString());
					stringRegister.setUpdatable(false);
					break;

				default:
					throw new RagnarokRuntimeException("'%d' n�o � um tipo de registro", type);
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

	public Queue<GlobalRegisterOperation<?>> getRegisters()
	{
		return operations;
	}

	public void setRegisters(Queue<GlobalRegisterOperation<?>> operations)
	{
		this.operations = operations;
	}

	@Override
	public String getName()
	{
		return "HA_UPDATE_REGISTERS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_UPDATE_REGISTERS;
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
		description.append("operations", size(operations));
	}
}

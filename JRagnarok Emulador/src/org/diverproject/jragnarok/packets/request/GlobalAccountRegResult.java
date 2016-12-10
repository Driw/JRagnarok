package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_GLOBAL_ACCREG;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.GlobalAccountReg;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class GlobalAccountRegResult extends RequestPacket
{
	private Queue<GlobalAccountReg> registers;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(registers.size());

		while (!registers.isEmpty())
		{
			GlobalAccountReg register = registers.poll();

			output.putInt(register.getIndex());
			output.putString(register.getKey());
			output.putInt(register.getOperation());
		}
	}

	@Override
	protected void receiveInput(Input input)
	{
		registers = new DynamicQueue<>();
		int size = input.getInt();

		for (int i = 0; i < size; i++)
		{
			GlobalAccountReg register = new GlobalAccountReg();
			register.setIndex(input.getInt());
			register.setKey(input.getString());
			register.setOperation(input.getInt());
			registers.offer(register);
		}
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
		return "RES_GLOBAL_ACCREG";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_RES_GLOBAL_ACCREG;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

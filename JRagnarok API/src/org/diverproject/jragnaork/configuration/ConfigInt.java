package org.diverproject.jragnaork.configuration;

public class ConfigInt extends Config<Integer>
{
	private int value;

	public ConfigInt(String name)
	{
		super(name);
	}

	public ConfigInt(String name, int defaultValue)
	{
		super(name);

		value = defaultValue;
	}

	@Override
	public Integer getValue()
	{
		return value;
	}

	@Override
	public void setValue(Integer value)
	{
		if (value == null)
			value = 0;

		this.value = value;
	}

	@Override
	public void setObject(Object object)
	{
		if (object != null && object instanceof Integer)
			setValue((Integer) object);
	}
}

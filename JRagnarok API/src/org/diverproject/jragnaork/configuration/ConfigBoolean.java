package org.diverproject.jragnaork.configuration;

public class ConfigBoolean extends Config<Boolean>
{
	private boolean value;

	public ConfigBoolean(String name)
	{
		super(name);
	}

	public ConfigBoolean(String name, boolean defaultValue)
	{
		super(name);

		this.value = defaultValue;
	}

	@Override
	public Boolean getValue()
	{
		return value;
	}

	@Override
	public void setValue(Boolean value)
	{
		if (value == null)
			value = false;

		this.value = value;
	}

	@Override
	public void setObject(Object object)
	{
		if (object != null && object instanceof Boolean)
			setValue((Boolean) object);
	}
}

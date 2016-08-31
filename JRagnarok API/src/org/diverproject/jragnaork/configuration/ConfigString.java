package org.diverproject.jragnaork.configuration;

public class ConfigString extends Config<String>
{
	private String value;

	public ConfigString(String name)
	{
		super(name);
	}
	public ConfigString(String name, String defaultValue)
	{
		super(name);

		value = defaultValue;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public void setObject(Object object)
	{
		if (object != null && object instanceof String)
			setValue((String) object);
	}
}

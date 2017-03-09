package org.diverproject.jragnarok.configuration;

import org.diverproject.util.UtilException;
import org.diverproject.util.lang.IntUtil;

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
		if (object != null)
		{
			if (object instanceof Integer)
				setValue((Integer) object);
			else if (object instanceof Integer)
				setRaw((String) object);
		}
	}

	@Override
	public boolean setRaw(String rawValue)
	{
		try {
			setValue(IntUtil.parseString(rawValue));
		} catch (UtilException e) {
			return false;
		}

		return true;
	}

	@Override
	public ConfigInt clone()
	{
		ConfigInt config = new ConfigInt(getName());
		config.setValue(getValue());

		return config;
	}
}

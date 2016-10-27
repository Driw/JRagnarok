package org.diverproject.jragnaork.configuration;

import static org.diverproject.log.LogSystem.logExeception;

public class ConfigObject<T> extends Config<T>
{
	private T value;

	public ConfigObject(String name)
	{
		super(name);
	}

	public ConfigObject(String name, T defaultValue)
	{
		super(name);

		this.value = defaultValue;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	@Override
	public void setValue(T value)
	{
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setObject(Object object)
	{
		try {	
			this.value = (T) object;
		} catch (Exception e) {
			logExeception(e);
		}
	}

	@Override
	public ConfigObject<Object> clone()
	{
		ConfigObject<Object> config = new ConfigObject<Object>(getName());
		config.setValue(getValue());

		return config;
	}
}

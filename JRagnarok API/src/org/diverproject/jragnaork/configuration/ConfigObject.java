package org.diverproject.jragnaork.configuration;

public abstract class ConfigObject<T> extends Config<T>
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

	@Override
	public abstract ConfigObject<T> clone();
}

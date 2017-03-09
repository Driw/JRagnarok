package org.diverproject.jragnarok.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.diverproject.jragnarok.RagnarokRuntimeException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

public abstract class Config<T>
{
	private String name;

	public Config(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public abstract T getValue();

	public abstract void setValue(T value);

	public abstract void setObject(Object object);

	public abstract boolean setRaw(String rawValue);

	@Override
	protected abstract Object clone();

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("name", name);
		description.append("value", getValue());

		return description.toString();
	}

	private static final Map<String, Class<?>> TYPES = new StringSimpleMap<>();

	static
	{
		add(ConfigString.class);
		add(ConfigBoolean.class);
		add(ConfigInt.class);
	}

	public static boolean add(Class<?> cls)
	{
		return TYPES.add(cls.getSimpleName(), cls);
	}

	public static Config<?> newConfig(String name, Object object)
	{
		if (name == null || object == null)
			throw new IllegalArgumentException("valores nulos");

		return newConfig(name, object.getClass());
	}

	public static Config<?> newConfig(String name, Class<?> cls)
	{
		Class<?> parent;

		for (parent = cls; parent != Object.class; parent = cls, cls = cls.getSuperclass())
		{
			Class<?> configClass = TYPES.get(cls.getSimpleName());

			if (configClass != null)
			{
				String classname = configClass.getSimpleName();

				try {

					Constructor<?> constructor = configClass.getDeclaredConstructor(String.class);
					Object instance = constructor.newInstance(name);
					Config<?> config = (Config<?>) instance;

					return config;

				} catch (NoSuchMethodException e) {
					throw new RagnarokRuntimeException("'%s' não possui construtor adequado", classname);
				} catch (SecurityException e) {
					throw new RagnarokRuntimeException("'%s' construtor esperado não público", classname);
				} catch (InstantiationException e) {
					throw new RagnarokRuntimeException("'%s' não pôde ser instanciado", classname);
				} catch (IllegalAccessException e) {
					throw new RagnarokRuntimeException("'%s' não tem acesso ao construtor", classname);
				} catch (IllegalArgumentException e) {
					throw new RagnarokRuntimeException("'%s' não recebe uma string", classname);
				} catch (InvocationTargetException e) {
					throw new RagnarokRuntimeException("'%s' %s", classname, e.getMessage());
				}
			}
		}

		throw new RagnarokRuntimeException("configuração para '%s' não existe", cls.getSimpleName());
	}
}

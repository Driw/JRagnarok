package org.diverproject.jragnaork.configuration;

import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;
import org.diverproject.util.lang.BooleanUtil;
import org.diverproject.util.lang.IntUtil;

public class ConfigLoad
{
	private String filePath;
	private Map<String, Config<?>> configurations;

	public ConfigLoad()
	{
		configurations = new StringSimpleMap<>();
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public Map<String, Config<?>> getConfigurations()
	{
		return configurations;
	}

	public void setConfigurations(Map<String, Config<?>> configurations)
	{
		this.configurations = configurations;
	}

	public void clearRead()
	{
		configurations.clear();
		filePath = null;
	}

	public void read() throws RagnarokException
	{
		if (filePath == null)
			throw new RagnarokException("arquivo não definido");

		File file = new File(filePath);

		if (!file.isDirectory())
			read(file);
		else
			for (File f : file.listFiles())
				read(f);
	}

	private void read(File file) throws RagnarokException
	{
		try {

			int read = 0;
			FileReader in = new FileReader(file);
			BufferedReader reader = new BufferedReader(in);

			for (int i = 1; reader.ready(); i++)
			{
				String line = reader.readLine();

				if (line.isEmpty() || line.startsWith("//"))
					continue;

				String columns[] = new String[]
				{
					line.substring(0, line.indexOf(':')),
					line.substring(line.indexOf(':') + 1),
				};

				if (columns.length != 2)
					logWarning("formato inválido (linha: %d).\n", i);

				else
				{
					String name = columns[0].trim();
					String value = columns[1].trim();

					Config<?> config = configurations.get(name);

					if (config == null)
						logWarning("configuração '%s' não encontrada.\n", name);

					else if (!isConfigType(config, value))
						logWarning("configuração '%s' não é %s.\n", name, config.getValue().getClass().getSimpleName());

					else
						read++;
				}
			}

			logInfo("%d configurações lidas de %s.\n", read, file.getPath());

			reader.close();

		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	private boolean isConfigType(Config<?> config, String value)
	{
		if (config instanceof ConfigString)
			config.setObject((Object) value);

		else if (config instanceof ConfigInt && IntUtil.isInteger(value))
			config.setObject((Object) IntUtil.parse(value));

		else if (config instanceof ConfigBoolean && BooleanUtil.isBoolean(value))
			config.setObject((Object) BooleanUtil.parse(value));

		else
			try {
				config.setObject((Object) value);
			} catch (IllegalArgumentException e) {
				return false;
			}

		return true;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("file", filePath);
		description.append("configCoutn", configurations.size());

		return description.toString();
	}
}

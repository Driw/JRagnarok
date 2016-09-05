package org.diverproject.jragnaork.messages;

import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;
import org.diverproject.util.lang.IntUtil;

public class MessageLoad
{
	private String filePath;
	private Map<Integer, String> configurations;

	public MessageLoad()
	{
		configurations = new IntegerLittleMap<>();
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public Map<Integer, String> getConfigurations()
	{
		return configurations;
	}

	public void setConfigurations(Map<Integer, String> configurations)
	{
		this.configurations = configurations;
	}

	public void clearRead()
	{
		configurations = new IntegerLittleMap<>();
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
					String idString = columns[0].trim();
					String message = columns[1].trim();
					int id = IntUtil.parse(idString, -1);

					if (id < 0)
						logWarning("mensagem com id inválido (linha: %d)", i);

					else
					{
						configurations.add(id, message);
						read++;
					}
				}
			}

			logInfo("%d mensagens lidas de %s.\n", read, file.getPath());

			reader.close();

		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("file", filePath);
		description.append("messagesCount", configurations.size());

		return description.toString();
	}
}

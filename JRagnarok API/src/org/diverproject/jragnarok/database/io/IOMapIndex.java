package org.diverproject.jragnarok.database.io;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.MIN_MAP_NAME_LENGTH;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.setUpSource;
import static org.diverproject.util.Util.format;
import static org.diverproject.util.lang.IntUtil.interval;
import static org.diverproject.util.lang.IntUtil.isInteger;
import static org.diverproject.util.lang.IntUtil.parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.jragnarok.RagnarokRuntimeException;
import org.diverproject.jragnarok.database.MapIndexes;
import org.diverproject.jragnarok.database.impl.MapIndex;

/**
 * <h1>IO para Map Index</h1>
 *
 * <p>Atrav�s desta classe ser� poss�vel efetuar a leitura e escrita da indexa��o dos mapas entre servidores.
 * Esses dados s�o armazenados em uma base de dados nomeadas por <b>Map Index</b>, seja por SQL ou TXT.
 * Este IO considera as especifica��es de prefer�ncias gen�ricas usados por todos os IO do sistema.</p>
 *
 * @see IODefault
 * @see MapIndexes
 * @see Connection
 *
 * @author Andrew
 */

public class IOMapIndex extends IODefault<MapIndexes>
{
	@Override
	public int readSQL(MapIndexes maps, Connection connection, String tablename) throws RagnarokException
	{
		int read = 0;
		String sql = format("SELECT id, map_name FROM %s ORDER BY id", tablename);

		try {

			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				MapIndex map = new MapIndex();

				try {

					map.setID(rs.getInt("id"));
					map.setMapName(rs.getString("map_name"));

					if (map.getID() > maps.length())
					{
						if (getPreferences().is(PREFERENCES_THROWS_NOTFOUND))
							newException("map index '%s' n�o pode ser considerado (length: %d, id: %d)", maps.length(), map.getID());
						continue;
					}

					if (map.getMapName() == null || !interval(map.getMapName().length(), MIN_MAP_NAME_LENGTH, MAX_MAP_NAME_LENGTH))
					{
						if (getPreferences().is(PREFERENCES_THROWS_UNEXPECTED))
							newException("map index '%s' n�o foi aceito (id: %d)", map.getMapName(), map.getID());
						continue;
					}

					if (maps.insert(map))
					{
						read++;

						if (getPreferences().is(PREFERENCES_INTERNAL_LOG_ALL))
						{
							setUpSource(2);
							logNotice("map index '%s' lido e definido com id '%d'.\n", map.getMapName(), map.getID());
						}
					}

				} catch (SQLException e) {
					if (!getPreferences().is(PREFERENCES_THROWS_FORMAT))
						newException("formato inv�lido (sql: %s)", e.getMessage());
				}
			}

		} catch (SQLException e) {
			throw new RagnarokRuntimeException(e.getMessage());
		}

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
		{
			setUpSource(2);
			logNotice("foi encontrado %d entradas em '%s'.\n", read, tablename);
		}

		return read;
	}

	@Override
	public int writeSQL(MapIndexes maps, Connection connection, String tablename) throws RagnarokException
	{
		int write = 0;
		int lastID = 0;

		String sql = format("REPLACE INTO %s (id, map_name) VALUES (?, ?)", tablename);

		for (int i = 0; i < maps.length(); i++)
		{
			MapIndex map = maps.get(i);

			if (map == null)
				continue;

			if (map.getID() <= lastID)
			{
				if (getPreferences().is(PREFERENCES_THROWS_NOTFOUND))
					newException("map index n�o pode ser considerado (mapid: %d)", map.getID());
				continue;
			}

			lastID = map.getID();

			if (map.getMapName() == null || !interval(map.getMapName().length(), MIN_MAP_NAME_LENGTH, MAX_MAP_NAME_LENGTH))
			{
				if (getPreferences().is(PREFERENCES_THROWS_UNEXPECTED))
					newException("map index '%s' n�o foi aceito (id: %d)", map.getMapName(), map.getID());
				continue;
			}


			try {

				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setInt(1, map.getID());
				ps.setString(3, map.getMapName());

				write++;

				if (getPreferences().is(PREFERENCES_INTERNAL_LOG_ALL))
				{
					setUpSource(2);

					switch (ps.executeUpdate())
					{
						case INSERTED: 
							logNotice("map index '%s' inserido (id: %d, table: %s).\n", map.getMapName(), map.getID(), tablename);
							break;

						case REPLACED:
							logNotice("map index '%s' atualizado (id: %d, table: %s).\n", map.getMapName(), map.getID(), tablename);
							break;
					}
				}

			} catch (SQLException e) {
				if (!getPreferences().is(PREFERENCES_THROWS_FORMAT))
					newException("formato inv�lido (sql: %s)", e.getMessage());
			}
		}

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
		{
			setUpSource(2);
			logNotice("foram salvos %d registros em '%s'.\n", write, tablename);
		}

		return write;
	}

	@Override
	public int readFile(MapIndexes maps, String filepath) throws RagnarokException
	{
		File file = new File(filepath);

		int read = 0;
		int id = 1;

		try {

			FileReader in = new FileReader(file);
			BufferedReader reader = new BufferedReader(in);

			for (int i = 1; reader.ready(); i++)
			{
				String line = reader.readLine().trim();

				if (line.startsWith("//") || line.isEmpty())
					continue;

				String columns[] = line.split(",");

				if (columns.length > 2 || columns[columns.length == 2 ? 1 : 0].contains(" |\t"))
				{
					if (getPreferences().is(PREFERENCES_THROWS_FORMAT))
						newException("fomarto inv�lido - limite de duas colunas e sem espa�amento (linha: %d)", i);
					continue;
				}

				if (!interval(columns[columns.length == 2 ? 1 : 0].length(), MIN_MAP_NAME_LENGTH, MAX_MAP_NAME_LENGTH) ||
					(columns.length == 2 && !isInteger(columns[0])))
				{
					if (getPreferences().is(PREFERENCES_THROWS_UNEXPECTED))
						newException("map index '%s' n�o foi aceito (linha: %d)", columns[0], i);
					continue;
				}

				if (columns.length == 2)
				{
					int currentID = parse(columns[0]);

					if (id > currentID)
					{
						if (getPreferences().is(PREFERENCES_THROWS_NOTFOUND))
							newException("map index '%s' n�o pode ter id '%d' (linha: %d)", columns[1], currentID, i);
						continue;
					}

					id = currentID;
				}

				MapIndex map = new MapIndex();
				map.setMapName(columns[columns.length == 2 ? 1 : 0]);
				map.setID(id++);

				if (maps.insert(map));
				{
					read++;

					if (getPreferences().is(PREFERENCES_INTERNAL_LOG_ALL))
						logNotice("map index '%s' foi lido e definido com id '%d'.\n", columns[0], id);
				}
			}

			reader.close();

		} catch (IOException e) {
			if (!getPreferences().is(PREFERENCES_THROWS_FORMAT))
				newException("formato inv�lido (sql: %s)", e.getMessage());
		}

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
		{
			setUpSource(2);
			logNotice("foi encontrado %d entradas em '%s'.\n", read, filepath);
		}

		return read;
	}

	@Override
	public int writeFile(MapIndexes maps, String filepath) throws RagnarokException
	{
		File file = new File(filepath);

		int write = 0;
		int lastID = 0;

		try {

			FileWriter out = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(out);

			for (MapIndex map : maps)
			{
				if (map.getID() <= lastID)
				{
					if (getPreferences().is(PREFERENCES_THROWS_NOTFOUND))
						newException("map index '%d' n�o pode ser considerado", map.getID());
					continue;
				}

				if (map.getMapName() == null || !interval(map.getMapName().length(), MIN_MAP_NAME_LENGTH, MAX_MAP_NAME_LENGTH))
				{
					if (getPreferences().is(PREFERENCES_THROWS_UNEXPECTED))
						newException("map index '%s' n�o foi aceito (id: %d)", map.getMapName(), map.getID());
					continue;
				}

				if (lastID == 0 || lastID + 1 < map.getID())
					writer.write(format("%d,%s", map.getID(), map.getMapName()));
				else
					writer.write(map.getMapName());

				write++;

				writer.newLine();
				writer.flush();

				lastID = map.getID();

				if (getPreferences().is(PREFERENCES_INTERNAL_LOG_ALL))
				{
					setUpSource(2);
					logNotice("map index '%s' inserido (id: %d, table: %s).\n", map.getMapName(), map.getID(), filepath);
				}
			}

			writer.close();

		} catch (IOException e) {
			if (!getPreferences().is(PREFERENCES_THROWS_FORMAT))
				newException("formato inv�lido (sql: %s)", e.getMessage());
		}

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
		{
			setUpSource(2);
			logNotice("foram salvos %d registros em '%s'.\n", write, filepath);
		}

		return write;
	}
}

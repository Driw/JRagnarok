package org.diverproject.jragnarok.database.io;

import static org.diverproject.jragnarok.JRagnarokConstants.MAP_NAME_LENGTH;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.format;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.Util.strclr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.jragnarok.database.MapCache;
import org.diverproject.jragnarok.database.MapIndexes;
import org.diverproject.jragnarok.database.impl.MapCell;
import org.diverproject.jragnarok.database.impl.MapData;
import org.diverproject.jragnarok.database.impl.MapIndex;
import org.diverproject.util.SizeUtil;
import org.diverproject.util.jzip.ZipUtil;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;
import org.diverproject.util.stream.implementation.input.InputMapped;
import org.diverproject.util.stream.implementation.output.OutputWriter;

/**
 * <h1>IO para Map Cache</h1>
 *
 * <p>Através desta classe será possível efetuar a leitura e escrita dos dados básicos dos mapas.
 * Esses dados são armazenados em uma base de dados nomeadas por <b>Map Cache</b>, seja por SQL ou TXT.
 * Este IO considera as especificações de preferências genéricas usados por todos os IO do sistema.</p>
 *
 * <p>Além das preferências básicas é possível definir um índice de mapas que poderão ser lidos.
 * Caso definido, apenas os mapas especificados no mesmo serão lidos, entretanto se não definido,
 * será considerado que o IO deverá ler/escrever todos os mapas que forem encontrados.</p>
 *
 * @see IODefault
 * @see MapCache
 * @see MapIndexes
 * @see Connection
 *
 * @author Andrew
 */

public class IOMapCache extends IODefault<MapCache>
{
	/**
	 * Valor padrão para o intervalo de chamada do GC.
	 */
	private static final int GARBAGE_COLLECTOR_DELAY = 50;


	/**
	 * Intervalo de iterações para ser chamado o GC.
	 */
	private int garbageCollectorDelay;

	/**
	 * Índice de mapas para serem considerados durante a IO de um Map cache.
	 */
	private MapIndexes indexes;

	/**
	 * Cria uma nova instância para um IO de Map Cache e define o intervalo do GC com o valor padrão.
	 */

	public IOMapCache()
	{
		garbageCollectorDelay = GARBAGE_COLLECTOR_DELAY;
	}

	/**
	 * @return aquisição do índice de mapas que está sendo utilizado (se houver).
	 */

	public MapIndexes getMapIndexes()
	{
		return indexes;
	}

	/**
	 * @param indexes índice de mapas que deve ser considerado na leitura/escrita.
	 */

	public void setMapIndexes(MapIndexes indexes)
	{
		this.indexes = indexes;
	}

	/**
	 * @return aquisição do intervalo de iterações para a chamada do GC.
	 */

	public int getGarbageCollectorDelay()
	{
		return garbageCollectorDelay;
	}

	/**
	 * Não definir um valor muito baixo caso contrário pode aumentar o tempo de processamento.
	 * Caso seja definido um valor muito alto pode causar o excesso de uso da memória do sistema.
	 * @param garbageCollectorDelay intervalo de iterações para a chamada do GC.
	 */

	public void setGarbageCollectorDelay(int garbageCollectorDelay)
	{
		if (garbageCollectorDelay > 0)
			this.garbageCollectorDelay = garbageCollectorDelay;
	}

	/**
	 * Procedimento interno usado para verificar se uma determinada iteração corresponde ao intervalo do GC.
	 * Sempre que corresponder ao intervalo será considerado que é necessário uma chamado do GC.s
	 * Através desta lógica, é possível evitar que o IO consuma muita memória do sistema e evita lentidão.
	 * @param i
	 */

	private void clearMemory(int i)
	{
		if (i % garbageCollectorDelay == 0)
			System.gc();		
	}

	@Override
	public int readSQL(MapCache mapCache, Connection connection, String tablename) throws RagnarokException
	{
		return indexes == null ? readAllSQL(mapCache, connection, tablename) : readAvaiable(mapCache, connection, tablename);
	}

	@Override
	public int writeSQL(MapCache mapCache, Connection connection, String tablename) throws RagnarokException
	{
		int write = 0;
		int notfound = 0;

		String sql = format("REPLACE %s (mapindex, mapname, width, length, cache) VALUES (?, ?, ?, ?, ?)");

		for (int i = 1; i <= mapCache.length(); i++)
		{
			if (!mapCache.contains(i))
				continue;

			MapData map = mapCache.select(i);

			if (indexes != null && !indexes.contains(map.getName()))
			{
				notfound++;
				continue;
			}

			try {

				InputStream inputstream = createInputStream(map);

				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setInt(1, map.getMapIndex());
				ps.setString(2, map.getName());
				ps.setInt(3, map.getDimension().getWidth());
				ps.setInt(4, map.getDimension().getLength());
				ps.setBinaryStream(5, inputstream);

				if (ps.executeUpdate() < 1)
				{
					if (getPreferences().is(PREFERENCES_THROWS_UNEXPECTED))
						logWarning("%s não foi alocado em %s\n", map.getName(), tablename);
				}

				else
				{
					if (getPreferences().is(PREFERENCES_INTERNAL_LOG_ALL))
						logInfo("%s alocado em %s\n", map.getName(), tablename);

					write++;
				}

				try {
					inputstream.close();
				} catch (IOException e) {
					newException(e.getMessage());
				}

				inputstream = null;

			} catch (SQLException e) {
				newException(e.getMessage());
			}

			clearMemory(i);
		}

		clearMemory(0);

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
			logInfo("%d mapas alocados em %s e %d não indexados\n", write, tablename, notfound);

		return write;
	}

	@Override
	public int readFile(MapCache mapCache, String filepath) throws RagnarokException
	{
		int read = 0;
		int notfound = 0;

		try {

			Input input = new InputMapped(filepath);
			input.setInvert(true);

			int fileSize = 0;

			if (input.space() != (fileSize = input.getInt()))
			{
				newException("mapcache corrompido (tem: %d bytes e espera %d bytes)", input.space() + 8, fileSize);
				return 0;
			}

			int mapCount = input.getInt();

			for (int i = 0, index = 1; i < mapCount; i++)
			{
				String mapname = strclr(input.getString(MAP_NAME_LENGTH));
				short width = input.getShort();
				short length = input.getShort();
				int gatBufferLength = input.getInt();
				byte gatBufferZipped[] = input.getBytes(gatBufferLength);
				byte gatBuffer[] = ZipUtil.unzip(gatBufferZipped);

				if (gatBuffer == null)
				{
					if (getPreferences().is(PREFERENCES_THROWS_FORMAT))
						logWarning("%s está com os dados GAT corrompidos\n", mapname);
					continue;
				}

				MapData data = new MapData();
				data.setID(index++);
				data.setMapIndex(indexes == null ? index : indexes.getMapID(mapname));
				data.setName(mapname);
				data.getDimension().setSize(width, length);
				data.initMapCell();

				setGATCells(data.getMapCell(), gatBuffer);

				if (commonInsert(mapCache, data))
					read++;
				else
				{
					notfound++;
					index--;
				}

				gatBuffer = null;
				clearMemory(i);
			}

			input.close();

		} catch (IOException e) {
			newException(e.getMessage());
		}

		clearMemory(0);

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
			logInfo("%d mapas lidos e %d não encontrados\n", read, notfound);

		return read;
	}

	@Override
	public int writeFile(MapCache mapCache, String filepath) throws RagnarokException
	{
		int mapCount = 0;
		int notfound = 0;

		try {

			String filepathTemp = filepath+ ".temp";
			Output output = new OutputWriter(filepathTemp);
			output.setInvert(true);

			for (int i = 1; i < (indexes == null ? mapCache.length() : indexes.length()); i++)
			{
				MapData data = null;

				if (indexes == null)
					data = mapCache.select(i);
				else
				{
					String mapname = indexes.getMapName(i);

					if (mapname == null)
						continue;

					data = mapCache.select(mapname);
				}

				if (data == null)
				{
					if (indexes != null)
						notfound++;
					continue;
				}

				writeMapData(output, data);
				clearMemory(i);
				mapCount++;
			}

			output.close();

			Input input = new InputMapped(filepathTemp);

			output = new OutputWriter(filepath);
			output.setInvert(true);
			output.putInt(input.space() + 8);
			output.putInt(mapCount);
			output.setInvert(false);
			output.flush();
			output.putBytes(input.getBytes(input.space()));
			output.close();
			input.close();

			new File(filepathTemp).delete();

		} catch (IOException e) {
			newException(e.getMessage());
		}

		clearMemory(0);

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
			logInfo("%d mapas escritos e %d não foi possível\n", mapCount, notfound);

		return mapCount;
	}

	/**
	 * Procedimento interno que fará a iteração para leitura dos mapas registrados no banco de dados SQL.
	 * @param mapCache referência da base de dados para armazenamento dos dados dos mapas (Map Cache).
	 * @param connection referência da conexão om o banco de dados SQL que contém os dados dos mapas.
	 * @param tablename nome da tabela onde se encontra os dados dos mapas que devem ser lidos.
	 * @return aquisição da quantidade de mapas que foram lidos e armazenados no Map Cache.
	 * @throws RagnarokException apenas se houver uma falha grave que impeça a continuação da leitura.
	 */

	private int readAllSQL(MapCache mapCache, Connection connection, String tablename) throws RagnarokException
	{
		String sql = format("SELECT mapindex, mapname, width, length, cache FROM %s", tablename);

		int read = 0;
		int notfound = 0;

		try {

			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			for (int i = 1, index = 0; rs.next(); i++)
			{
				if (parseResultSet(index++, mapCache, rs))
					read++;
				else
				{
					notfound++;
					index--;
				}

				clearMemory(i);
			}

		} catch (SQLException e) {
			newException(e.getMessage());
		} catch (IOException e) {
			newException(e.getMessage());
		}

		clearMemory(0);

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
			logInfo("%d mapas lidos e %d não encontrados\n", read, notfound);

		return read;
	}

	/**
	 * Procedimento interno que fará a iteração para leitura dos mapas disponíveis (listados no índice de mapas).
	 * @param mapCache referência da base de dados para armazenamento dos dados dos mapas (Map Cache).
	 * @param connection referência da conexão om o banco de dados SQL que contém os dados dos mapas.
	 * @param tablename nome da tabela onde se encontra os dados dos mapas que devem ser lidos.
	 * @return aquisição da quantidade de mapas que foram lidos e armazenados no Map Cache.
	 * @throws RagnarokException apenas se houver uma falha grave que impeça a continuação da leitura.
	 */

	private int readAvaiable(MapCache mapCache, Connection connection, String tablename) throws RagnarokException
	{
		int read = 0;
		int notfound = 0;

		for (int i = 1, index = 0; i <= indexes.length(); i++)
		{
			if (!indexes.contains(i))
				continue;

			MapIndex mapIndex = indexes.get(i);
			String sql = format("SELECT mapindex, width, length, cache FROM %s WHERE mapname = ?", tablename);

			try {

				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setString(1, mapIndex.getMapName());

				ResultSet rs = ps.executeQuery();

				if (rs.next() && parseResultSet(index++, mapCache, rs))
					read++;
				else
				{
					notfound++;
					index--;
				}

				clearMemory(i);

			} catch (SQLException e) {
				newException(e.getMessage());
			} catch (IOException e) {
				newException(e.getMessage());
			}
		}

		clearMemory(0);

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_READ))
			logInfo("%d mapas lidos e %d não encontrados\n", read, notfound);

		return read;
	}

	/**
	 * Procedimento interno usado para fazer uma análise do resultado obtido de uma consulta no banco de dados.
	 * Esse resultado deverá conter as informações de um dos registros da tabela que contém os dados do mapa.
	 * @param id número do índice do mapa que está sendo lido para ser especificado do Map Cache.
	 * @param mapCache referência da base de dados que contém os dados dos mapas (Map Cache).
	 * @param rs referência do resultado obtido da consulta feita no banco de dados.
	 * @return true se conseguir criar os dados do mapa e inseri-los no Map Cache ou false caso contrário.
	 * @throws SQLException quando houver problema durante a consulta com o banco de dados.
	 * @throws RagnarokException quando houver problema de dados incorretos obtidos.
	 * @throws IOException quando houver problema de dados incorretos obtidos.
	 */

	private boolean parseResultSet(int id, MapCache mapCache, ResultSet rs) throws SQLException, RagnarokException, IOException
	{
		MapData data = new MapData();
		data.setID(id);
		data.setMapIndex(rs.getShort("mapindex"));
		data.setName(rs.getString("mapname"));
		data.getDimension().setSize(rs.getInt("width"), rs.getInt("length"));
		data.initMapCell();

		try {

			setGATCells(data.getMapCell(), rs.getAsciiStream("cache"));

		} catch (MapCacheException e) {
			
			if (getPreferences().is(PREFERENCES_THROWS_FORMAT))
				logWarning("falha ao ler %s (%s)\n", data.getName(), e.getMessage());

			newException(e.getMessage());

			return false;
		}

		return commonInsert(mapCache, data);
	}

	/**
	 * Procedimento interno utilizado para definir as propriedades GAT de cada células do mapa.
	 * Neste passo será feito ainda a descompactação dos dados encontrados no input passado conforme:
	 * @param mapCell referência de um objeto que permite definir as propriedades de cada célula do mapa.
	 * @param stream referência da interface que permite uma leitura avançada de dados em bytes.
	 * @throws RagnarokException quando houver problema durante a definição das propriedades.
	 * @throws IOException quando não for possível efetuar a leitura dos dados do mapa.
	 * @throws MapCacheException quando o buffer especificado não corresponder ao tamanho do mapa.
	 */

	private void setGATCells(MapCell mapCell, InputStream stream) throws RagnarokException, IOException, MapCacheException
	{
		byte buffer[] = new byte[stream.available()];
		stream.read(buffer);

		byte unzipData[] = ZipUtil.unzip(buffer);
		buffer = null;
		stream.close();

		setGATCells(mapCell, unzipData);
	}

	/**
	 * Procedimento interno utilizado para definir as propriedades GAT de forma mais detalhada.
	 * Neste passo será atualizado as propriedades de cada célula, já que as informações são passadas:
	 * @param mapCell referência de um objeto que permite definir as propriedades de cada célula do mapa.
	 * @param gatBufferData vetor contendo o tipo de célula que foi especificado para aquele ponto do mapa.
	 * @throws MapCacheException quando o buffer especificado não corresponder ao tamanho do mapa.
	 */

	private void setGATCells(MapCell mapCell, byte[] gatBufferData) throws MapCacheException
	{
		int width = mapCell.getDimension().getWidth();
		int length = mapCell.getDimension().getLength();
		int mapBufferSize = width * length;

		if (gatBufferData.length != mapBufferSize)
			throw new MapCacheException("possui %s e esperado %s", SizeUtil.toString(gatBufferData.length), SizeUtil.toString(mapBufferSize));

		for (int x = 0, offset = 0; x < width; x++)
			for (int y = 0; y < length; y++)
				mapCell.setTerrainGAT(x, y, gatBufferData[offset++]);
	}

	/**
	 * Cria uma stream para entrada de dados onde os dados são as propriedade de um mapa especificado.
	 * Através de um mapa já carregado irá criar um buffer contendo os valores GAT de cada célula.
	 * O formato do buffer é o mesmo que é encontrado durante a leitura das propriedades GAT do mapa.
	 * @param map referência dos dados do mapa do qual deverá ser criada a stream de entrada dos dados.
	 * @return aquisição de uma nova stream de entrada dos dados GAT do mapa especificado.
	 */

	private InputStream createInputStream(MapData map)
	{
		byte gatBuffer[] = createGATBuffer(map.getMapCell());
		byte zipGatBuffer[] = ZipUtil.zip(gatBuffer);

		InputStream inputstream = new ByteArrayInputStream(zipGatBuffer);

		gatBuffer = null;
		zipGatBuffer = null;

		return inputstream;
	}

	/**
	 * Cria um novo vetor de bytes (buffer) contendo as propriedade GAT de cada célula do mapa.
	 * O índice é respectivo as coordenadas x e y do mapa sendo de um único índice para ambos:
	 * o cálculo desse offset é especificado por: <b>(y * width) + x</b>.
	 * @param mapCell referência dos dados de cada célula do mapa do qual será criado o buffer.
	 * @return aquisição do buffer em bytes contendo as propriedades GAT do mapa especificado.
	 */

	private byte[] createGATBuffer(MapCell mapCell)
	{
		int width = mapCell.getDimension().getWidth();
		int length = mapCell.getDimension().getLength();
		byte buffer[] = new byte[width * length];

		for (int x = 0, offset = 0; x < width; x++)
			for (int y = 0; y < length; y++)
				buffer[offset++] = mapCell.getTerrainGAT(x, y);

		return buffer;
	}

	/**
	 * Através desse método é feito a inserção no Map Cache de um mapa que foi lido de um SQL ou TXT.
	 * Considera as opções de preferências genéricas para registrar mensagens ou exceptions se houver.
	 * @param mapCache referência da base de dados que contém os dados dos mapas (Map Cache).
	 * @param data referência do objeto que contém todos os dados para criação do mapa no sistema.
	 * @return true se conseguir inserir na base de dados ou false caso contrário.
	 */

	private boolean commonInsert(MapCache mapCache, MapData data)
	{
		if (indexes != null && !indexes.contains(data.getName()))
		{
			if (getPreferences().is(PREFERENCES_THROWS_NOTFOUND))
				logWarning("%s não encontrado no MapIndexDB\n", data.getName());

			return false;
		}

		if (mapCache.insert(data))
		{
			if (getPreferences().is(PREFERENCES_INTERNAL_LOG_ALL))
				logInfo("%s lido (dimension: %dx%d, buffer: %s)\n", data.getName(), data.getDimension().getWidth(), data.getDimension().getLength(), SizeUtil.toString(data.getBufferSize()));

			return true;
		}

		if (mapCache.isFull())
		{
			if (getPreferences().is(PREFERENCES_THROWS_UNEXPECTED))
				logWarning("map cache não pode adicionar %s (cheio)\n", data.getName());
		}

		else if (getPreferences().is(PREFERENCES_THROWS_UNEXPECTED))
			logWarning("%s já se encontra em cache\n", data.getName());

		return false;
	}

	/**
	 * Permite escrever todas as informações de forma reversa em comparação a leitura dos mapas em um TXT.
	 * @param output referência da stream de um arquivo onde será feita a escrita dos dados do mapa.
	 * @param data referência do objeto que contém todos os dados do mapa para serem salvos no Map Cache.
	 */

	private void writeMapData(Output output, MapData data)
	{
		byte gatBuffer[] = createGATBuffer(data.getMapCell());
		byte gatBufferZipped[] = ZipUtil.zip(gatBuffer, 1);

		output.putString(data.getName(), MAP_NAME_LENGTH);
		output.putShort(s(data.getDimension().getWidth()));
		output.putShort(s(data.getDimension().getLength()));
		output.putInt(gatBufferZipped.length);
		output.setInvert(false);
		output.putBytes(gatBufferZipped);
		output.setInvert(true);

		if (getPreferences().is(PREFERENCES_INTERNAL_LOG_ALL))
			logInfo("%s compactado em %s (cache: %s)\n", data.getName(), SizeUtil.toString(gatBufferZipped.length), SizeUtil.toString(output.offset()));

		gatBufferZipped = null;
		gatBuffer = null;
	}
}

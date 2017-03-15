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
 * <p>Atrav�s desta classe ser� poss�vel efetuar a leitura e escrita dos dados b�sicos dos mapas.
 * Esses dados s�o armazenados em uma base de dados nomeadas por <b>Map Cache</b>, seja por SQL ou TXT.
 * Este IO considera as especifica��es de prefer�ncias gen�ricas usados por todos os IO do sistema.</p>
 *
 * <p>Al�m das prefer�ncias b�sicas � poss�vel definir um �ndice de mapas que poder�o ser lidos.
 * Caso definido, apenas os mapas especificados no mesmo ser�o lidos, entretanto se n�o definido,
 * ser� considerado que o IO dever� ler/escrever todos os mapas que forem encontrados.</p>
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
	 * Valor padr�o para o intervalo de chamada do GC.
	 */
	private static final int GARBAGE_COLLECTOR_DELAY = 50;


	/**
	 * Intervalo de itera��es para ser chamado o GC.
	 */
	private int garbageCollectorDelay;

	/**
	 * �ndice de mapas para serem considerados durante a IO de um Map cache.
	 */
	private MapIndexes indexes;

	/**
	 * Cria uma nova inst�ncia para um IO de Map Cache e define o intervalo do GC com o valor padr�o.
	 */

	public IOMapCache()
	{
		garbageCollectorDelay = GARBAGE_COLLECTOR_DELAY;
	}

	/**
	 * @return aquisi��o do �ndice de mapas que est� sendo utilizado (se houver).
	 */

	public MapIndexes getMapIndexes()
	{
		return indexes;
	}

	/**
	 * @param indexes �ndice de mapas que deve ser considerado na leitura/escrita.
	 */

	public void setMapIndexes(MapIndexes indexes)
	{
		this.indexes = indexes;
	}

	/**
	 * @return aquisi��o do intervalo de itera��es para a chamada do GC.
	 */

	public int getGarbageCollectorDelay()
	{
		return garbageCollectorDelay;
	}

	/**
	 * N�o definir um valor muito baixo caso contr�rio pode aumentar o tempo de processamento.
	 * Caso seja definido um valor muito alto pode causar o excesso de uso da mem�ria do sistema.
	 * @param garbageCollectorDelay intervalo de itera��es para a chamada do GC.
	 */

	public void setGarbageCollectorDelay(int garbageCollectorDelay)
	{
		if (garbageCollectorDelay > 0)
			this.garbageCollectorDelay = garbageCollectorDelay;
	}

	/**
	 * Procedimento interno usado para verificar se uma determinada itera��o corresponde ao intervalo do GC.
	 * Sempre que corresponder ao intervalo ser� considerado que � necess�rio uma chamado do GC.s
	 * Atrav�s desta l�gica, � poss�vel evitar que o IO consuma muita mem�ria do sistema e evita lentid�o.
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
						logWarning("%s n�o foi alocado em %s\n", map.getName(), tablename);
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
			logInfo("%d mapas alocados em %s e %d n�o indexados\n", write, tablename, notfound);

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
						logWarning("%s est� com os dados GAT corrompidos\n", mapname);
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
			logInfo("%d mapas lidos e %d n�o encontrados\n", read, notfound);

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
			logInfo("%d mapas escritos e %d n�o foi poss�vel\n", mapCount, notfound);

		return mapCount;
	}

	/**
	 * Procedimento interno que far� a itera��o para leitura dos mapas registrados no banco de dados SQL.
	 * @param mapCache refer�ncia da base de dados para armazenamento dos dados dos mapas (Map Cache).
	 * @param connection refer�ncia da conex�o om o banco de dados SQL que cont�m os dados dos mapas.
	 * @param tablename nome da tabela onde se encontra os dados dos mapas que devem ser lidos.
	 * @return aquisi��o da quantidade de mapas que foram lidos e armazenados no Map Cache.
	 * @throws RagnarokException apenas se houver uma falha grave que impe�a a continua��o da leitura.
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
			logInfo("%d mapas lidos e %d n�o encontrados\n", read, notfound);

		return read;
	}

	/**
	 * Procedimento interno que far� a itera��o para leitura dos mapas dispon�veis (listados no �ndice de mapas).
	 * @param mapCache refer�ncia da base de dados para armazenamento dos dados dos mapas (Map Cache).
	 * @param connection refer�ncia da conex�o om o banco de dados SQL que cont�m os dados dos mapas.
	 * @param tablename nome da tabela onde se encontra os dados dos mapas que devem ser lidos.
	 * @return aquisi��o da quantidade de mapas que foram lidos e armazenados no Map Cache.
	 * @throws RagnarokException apenas se houver uma falha grave que impe�a a continua��o da leitura.
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
			logInfo("%d mapas lidos e %d n�o encontrados\n", read, notfound);

		return read;
	}

	/**
	 * Procedimento interno usado para fazer uma an�lise do resultado obtido de uma consulta no banco de dados.
	 * Esse resultado dever� conter as informa��es de um dos registros da tabela que cont�m os dados do mapa.
	 * @param id n�mero do �ndice do mapa que est� sendo lido para ser especificado do Map Cache.
	 * @param mapCache refer�ncia da base de dados que cont�m os dados dos mapas (Map Cache).
	 * @param rs refer�ncia do resultado obtido da consulta feita no banco de dados.
	 * @return true se conseguir criar os dados do mapa e inseri-los no Map Cache ou false caso contr�rio.
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
	 * Procedimento interno utilizado para definir as propriedades GAT de cada c�lulas do mapa.
	 * Neste passo ser� feito ainda a descompacta��o dos dados encontrados no input passado conforme:
	 * @param mapCell refer�ncia de um objeto que permite definir as propriedades de cada c�lula do mapa.
	 * @param stream refer�ncia da interface que permite uma leitura avan�ada de dados em bytes.
	 * @throws RagnarokException quando houver problema durante a defini��o das propriedades.
	 * @throws IOException quando n�o for poss�vel efetuar a leitura dos dados do mapa.
	 * @throws MapCacheException quando o buffer especificado n�o corresponder ao tamanho do mapa.
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
	 * Neste passo ser� atualizado as propriedades de cada c�lula, j� que as informa��es s�o passadas:
	 * @param mapCell refer�ncia de um objeto que permite definir as propriedades de cada c�lula do mapa.
	 * @param gatBufferData vetor contendo o tipo de c�lula que foi especificado para aquele ponto do mapa.
	 * @throws MapCacheException quando o buffer especificado n�o corresponder ao tamanho do mapa.
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
	 * Cria uma stream para entrada de dados onde os dados s�o as propriedade de um mapa especificado.
	 * Atrav�s de um mapa j� carregado ir� criar um buffer contendo os valores GAT de cada c�lula.
	 * O formato do buffer � o mesmo que � encontrado durante a leitura das propriedades GAT do mapa.
	 * @param map refer�ncia dos dados do mapa do qual dever� ser criada a stream de entrada dos dados.
	 * @return aquisi��o de uma nova stream de entrada dos dados GAT do mapa especificado.
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
	 * Cria um novo vetor de bytes (buffer) contendo as propriedade GAT de cada c�lula do mapa.
	 * O �ndice � respectivo as coordenadas x e y do mapa sendo de um �nico �ndice para ambos:
	 * o c�lculo desse offset � especificado por: <b>(y * width) + x</b>.
	 * @param mapCell refer�ncia dos dados de cada c�lula do mapa do qual ser� criado o buffer.
	 * @return aquisi��o do buffer em bytes contendo as propriedades GAT do mapa especificado.
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
	 * Atrav�s desse m�todo � feito a inser��o no Map Cache de um mapa que foi lido de um SQL ou TXT.
	 * Considera as op��es de prefer�ncias gen�ricas para registrar mensagens ou exceptions se houver.
	 * @param mapCache refer�ncia da base de dados que cont�m os dados dos mapas (Map Cache).
	 * @param data refer�ncia do objeto que cont�m todos os dados para cria��o do mapa no sistema.
	 * @return true se conseguir inserir na base de dados ou false caso contr�rio.
	 */

	private boolean commonInsert(MapCache mapCache, MapData data)
	{
		if (indexes != null && !indexes.contains(data.getName()))
		{
			if (getPreferences().is(PREFERENCES_THROWS_NOTFOUND))
				logWarning("%s n�o encontrado no MapIndexDB\n", data.getName());

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
				logWarning("map cache n�o pode adicionar %s (cheio)\n", data.getName());
		}

		else if (getPreferences().is(PREFERENCES_THROWS_UNEXPECTED))
			logWarning("%s j� se encontra em cache\n", data.getName());

		return false;
	}

	/**
	 * Permite escrever todas as informa��es de forma reversa em compara��o a leitura dos mapas em um TXT.
	 * @param output refer�ncia da stream de um arquivo onde ser� feita a escrita dos dados do mapa.
	 * @param data refer�ncia do objeto que cont�m todos os dados do mapa para serem salvos no Map Cache.
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

package org.diverproject.jragnaork.database.io;

import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.log.LogSystem.setUpSource;
import static org.diverproject.util.Util.size;

import java.sql.Connection;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.ConfigReader;
import org.diverproject.jragnaork.database.AbstractDatabase;
import org.diverproject.jragnaork.database.MapIndexes;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;

public abstract class IODefault<D extends AbstractDatabase<?>>
{
	/**
	 * Armazenar as exceções quando encontradas porém não repassar.
	 */
	public static final int PREFERENCES_SAVE_EXCEPTIONS = 0x0001;

	/**
	 * Repassar as exceções durante a leitura (sobrescreve SAVE_EXCEPTIONS).
	 */
	public static final int PREFERENCES_THROWS_EXCEPTIONS = 0x0002;

	/**
	 * Criar exceções para as configurações que não forem encontradas.
	 */
	public static final int PREFERENCES_THROWS_NOTFOUND = 0x0004;

	/**
	 * Criar exceções para dados inesperados: formato de configuração inválido.
	 */
	public static final int PREFERENCES_THROWS_FORMAT = 0x0008;

	/**
	 * Criar exceções para dados inesperados: formado do valor da configuração inválida.
	 */
	public static final int PREFERENCES_THROWS_UNEXPECTED = 0x0010;

	/**
	 * Registar uma mensagem mostrando quantas configurações foram lidas.
	 */
	public static final int PREFERENCES_INTERNAL_LOG_READ = 0x0020;

	/**
	 * Registar uma mensagem mostrando quando uma configuração for lida.
	 */
	public static final int PREFERENCES_INTERNAL_LOG_ALL = 0x0040;

	/**
	 * Registar a mensagem de uma exceção quando for gerada (sobrescreve THROWS_*).
	 */
	public static final int PREFERENCES_LOG_EXCEPTIONS = 0x0080;

	/**
	 * Constante para aplicar todas as propriedades disponíveis.
	 */
	public static final int PREFERENCES_ALL = 0x0100 - 1;

	/**
	 * Vetor contendo o nome de todas as propriedades que podem ser aplicadas.
	 */
	public static final String PREFERENCES_STRINGS[] = new String[]
	{ "SAVE_EXCEPTIONS", "THROWS_EXCEPTIONS", "NOTFOUND", "UNEXPECTED" };

	/**
	 * Valor retornado da conexão SQL ao inserir um item na tabela.
	 */
	public static final int INSERTED = 1;

	/**
	 * Valor retornado da conexão SQL ao substituir um item da tabela.
	 */
	public static final int REPLACED = 2;

	/**
	 * Valor das preferências para definir o leitor de configurações:
	 * <code>INTERNAL_LOG_ALL</code>, <code>LOG_EXCEPTIONS</code>, <code>THROWS_FORMAT</code>,
	 * <code>THROWS_EXCEPTIONS</code>, <code>THROWS_NOTFOUND</code> e <code>THROWS_UNEXPECETED</code>.
	 */
	public static final int DEFAULT_PREFERENCES =
			ConfigReader.PREFERENCES_INTERNAL_LOG_READ +
			ConfigReader.PREFERENCES_LOG_EXCEPTIONS +
			ConfigReader.PREFERENCES_THROWS_FORMAT +
			ConfigReader.PREFERENCES_THROWS_EXCEPTIONS +
			ConfigReader.PREFERENCES_THROWS_NOTFOUND +
			ConfigReader.PREFERENCES_THROWS_UNEXPECTED;


	/**
	 * Fila das exceções geradas.
	 */
	private Queue<RagnarokException> exceptions;

	/**
	 * Preferências para reações na leitura de configurações.
	 */
	private BitWise preferences;

	/**
	 * Cria uma nova instância de uma entrada e saída padrão para base de dados do servidor.
	 * Inicializa a flag de preferências sem nenhuma preferência e a fila de exceptions.
	 */

	public IODefault()
	{
		preferences = new BitWise(PREFERENCES_STRINGS);
		exceptions = new DynamicQueue<>();
	}

	/**
	 * O leitor de configurações enfileira as exceções geradas durante a leitura do arquivo.
	 * @return aquisição da fila que contém as exceções geradas ao ler o arquivo.
	 */

	public Queue<RagnarokException> getExceptions()
	{
		return exceptions;
	}

	/**
	 * Para melhorar a dinâmica e funcionamento do carregador é possível definir preferências.
	 * Verificar todas as constantes disponíveis em ConfigRead por PREFERENCES_*.
	 * @return aquisição do configurador de preferências do leitor.
	 */

	public BitWise getPreferences()
	{
		return preferences;
	}

	public abstract int readSQL(MapIndexes indexes, Connection connection, String tablename) throws RagnarokException;
	public abstract int writeSQL(MapIndexes indexes, Connection connection, String tablename) throws RagnarokException;

	public abstract int readFile(MapIndexes indexes, String filepath) throws RagnarokException;
	public abstract int writeFile(MapIndexes indexes, String filepath) throws RagnarokException;

	/**
	 * Cria uma nova exceção e reage conforme as preferências definidas no leitor.
	 * @param format string contendo o formato da mensagem que será exibida.
	 * @param args argumentos respectivos a formatação da mensagem.
	 * @throws RagnarokException apenas se PREFERENCES_THROWS_EXCEPTIONS definido.
	 */

	void newException(String format, Object... args) throws RagnarokException
	{
		RagnarokException exception = new RagnarokException(format, args);

		if (preferences.is(PREFERENCES_LOG_EXCEPTIONS))
		{
			setUpSource(3);
			logWarning(exception.getMessage()+ "\n");
		}

		else if (preferences.is(PREFERENCES_THROWS_EXCEPTIONS))
			throw exception;

		else if (preferences.is(PREFERENCES_SAVE_EXCEPTIONS))
			exceptions.offer(exception);		
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("exceptions", size(exceptions));
		description.append("preferences", preferences.toStringProperties());

		return description.toString();
	}
}

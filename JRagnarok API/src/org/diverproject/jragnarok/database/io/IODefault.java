package org.diverproject.jragnarok.database.io;

import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.log.LogSystem.setUpSource;
import static org.diverproject.util.Util.size;

import java.sql.Connection;

import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.jragnarok.configuration.ConfigReader;
import org.diverproject.jragnarok.database.AbstractDatabase;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;

/**
 * <h1>IO Padr�o</h1>
 *
 * <p>Esta classe implementa algumas informa��es pertinentes a qualquer leitor de arquivo do sistema JRagnarok.
 * Possui uma cole��o que enfileira todas as exce��es que forem geradas durante sua utiliza��o evitando paradas.
 * Caso seja necess�rio interromper o processo poder� ser feito especificando nas prefer�ncias de utiliza��o.</p>
 *
 * <p>Ir� implementar m�todos abstratos para permitir a leitura e escrita de informa��es por arquivos ou conex�o SQL.
 * As IO especificadas que forem criadas dever�o determinar como ser� feito a sua leitura e suas regras.
 * Cada IO poder� ainda especificar outros atributos/prefer�ncias para facilitar, para ficar mais din�mico.</p>
 *
 * @see AbstractDatabase
 * @see BitWise
 * @see Connection
 *
 * @author Andrew
 *
 * @param <D> tipo de base de dados que ser� considerada em IO
 */

public abstract class IODefault<D extends AbstractDatabase<?>>
{
	/**
	 * Armazenar as exce��es quando encontradas por�m n�o repassar.
	 */
	public static final int PREFERENCES_SAVE_EXCEPTIONS = 0x0001;

	/**
	 * Repassar as exce��es durante a leitura (sobrescreve SAVE_EXCEPTIONS).
	 */
	public static final int PREFERENCES_THROWS_EXCEPTIONS = 0x0002;

	/**
	 * Criar exce��es para as configura��es que n�o forem encontradas.
	 */
	public static final int PREFERENCES_THROWS_NOTFOUND = 0x0004;

	/**
	 * Criar exce��es para dados inesperados: formato de configura��o inv�lido.
	 */
	public static final int PREFERENCES_THROWS_FORMAT = 0x0008;

	/**
	 * Criar exce��es para dados inesperados: formado do valor da configura��o inv�lida.
	 */
	public static final int PREFERENCES_THROWS_UNEXPECTED = 0x0010;

	/**
	 * Registar uma mensagem mostrando quantas configura��es foram lidas.
	 */
	public static final int PREFERENCES_INTERNAL_LOG_READ = 0x0020;

	/**
	 * Registar uma mensagem mostrando quando uma configura��o for lida.
	 */
	public static final int PREFERENCES_INTERNAL_LOG_ALL = 0x0040;

	/**
	 * Registar a mensagem de uma exce��o quando for gerada (sobrescreve THROWS_*).
	 */
	public static final int PREFERENCES_LOG_EXCEPTIONS = 0x0080;

	/**
	 * Constante para aplicar todas as propriedades dispon�veis.
	 */
	public static final int PREFERENCES_ALL = 0x0100 - 1;

	/**
	 * Vetor contendo o nome de todas as propriedades que podem ser aplicadas.
	 */
	public static final String PREFERENCES_STRINGS[] = new String[]
	{ "SAVE_EXCEPTIONS", "THROWS_EXCEPTIONS", "NOTFOUND", "UNEXPECTED" };

	/**
	 * Valor retornado da conex�o SQL ao inserir um item na tabela.
	 */
	public static final int INSERTED = 1;

	/**
	 * Valor retornado da conex�o SQL ao substituir um item da tabela.
	 */
	public static final int REPLACED = 2;

	/**
	 * Valor das prefer�ncias para definir o leitor de configura��es:
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
	 * Fila das exce��es geradas.
	 */
	private Queue<RagnarokException> exceptions;

	/**
	 * Prefer�ncias para rea��es na leitura de configura��es.
	 */
	private BitWise preferences;

	/**
	 * Cria uma nova inst�ncia de uma entrada e sa�da padr�o para base de dados do servidor.
	 * Inicializa a flag de prefer�ncias sem nenhuma prefer�ncia e a fila de exceptions.
	 */

	public IODefault()
	{
		preferences = new BitWise(PREFERENCES_STRINGS);
		exceptions = new DynamicQueue<>();
	}

	/**
	 * O leitor de configura��es enfileira as exce��es geradas durante a leitura do arquivo.
	 * @return aquisi��o da fila que cont�m as exce��es geradas ao ler o arquivo.
	 */

	public Queue<RagnarokException> getExceptions()
	{
		return exceptions;
	}

	/**
	 * Para melhorar a din�mica e funcionamento do carregador � poss�vel definir prefer�ncias.
	 * Verificar todas as constantes dispon�veis em ConfigRead por PREFERENCES_*.
	 * @return aquisi��o do configurador de prefer�ncias do leitor.
	 */

	public BitWise getPreferences()
	{
		return preferences;
	}

	public abstract int readSQL(D database, Connection connection, String tablename) throws RagnarokException;
	public abstract int writeSQL(D database, Connection connection, String tablename) throws RagnarokException;

	public abstract int readFile(D database, String filepath) throws RagnarokException;

	/**
	 * Procedimento que efetua a escrita das informa��es contidas dos itens contidos na base de dados abaixo:
	 * @param database refer�ncia da base de dados do qual ter� suas informa��es salvas em um arquivo.
	 * @param filepath nome parcial ou completo do arquivo no qual as informa��es ser�o salvas
	 * @return quantidade de itens da base de dados que foram salvas no arquivo especificado.
	 * @throws RagnarokException apenas se houver algum problema grave que deva interromper o processo.
	 */

	public abstract int writeFile(D database, String filepath) throws RagnarokException;

	/**
	 * Cria uma nova exce��o e reage conforme as prefer�ncias definidas no leitor.
	 * @param format string contendo o formato da mensagem que ser� exibida.
	 * @param args argumentos respectivos a formata��o da mensagem.
	 * @throws RagnarokException apenas se PREFERENCES_THROWS_EXCEPTIONS definido.
	 */

	void newException(String format, Object... args) throws RagnarokException
	{
		RagnarokException exception = new RagnarokException(format, args);

		if (preferences.is(PREFERENCES_LOG_EXCEPTIONS))
		{
			setUpSource(1);
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

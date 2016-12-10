package org.diverproject.jragnaork.configuration;

import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.log.LogSystem.setUpSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;

/**
 * <h1>Leitor de Configura��es</h1>
 *
 * <p>Classe usada para permitir o carregamento de configura��es definidas em arquivos de texto.
 * Para que as configura��es possam ser lidas elas devem estar alocadas em um conjunto.
 * Quando alocadas em um conjunto o nome da configura��o e o seu tipo estar�o definidos.</p>
 *
 * <p>O arquivo que for lido dever� apenas repassar o valor da configura��o a mesma.
 * Todos os tipos de configura��es devem validar um novo valor quando for do tipo string.
 * Assim � poss�vel que o leitor possa carregar valores din�micos a suas configura��es.</p>
 *
 * @see Queue
 * @see Configurations
 *
 * @author Andrew Mello
 */

public class ConfigReader
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
	 * Caminho referente ao arquivo que ser� lido.
	 */
	private String filePath;

	/**
	 * Conjunto de configura��es que ter� os valores atualizados.
	 */
	private Configurations configurations;

	/**
	 * Fila das exce��es geradas.
	 */
	private Queue<RagnarokException> exceptions;

	/**
	 * Prefer�ncias para rea��es na leitura de configura��es.
	 */
	private BitWise preferences;

	/**
	 * Cria um novo leitor de configura��es inicializando o conjunto de configura��es.
	 */

	public ConfigReader()
	{
		exceptions = new DynamicQueue<>();
		preferences = new BitWise(PREFERENCES_STRINGS);

		clearRead();
	}

	/**
	 * Define o caminho do arquivo do qual ser� lido o valor das configura��es.
	 * @param filePath caminho completo ou parcial do arquivo em disco.
	 */

	public void setFilePath(String filePath)
	{
		if (filePath != null)
			this.filePath = filePath;
	}

	/**
	 * O leitor de configura��es utiliza essas configura��es para atualizar seus valores.
	 * @return aquisi��o do conjunto de configura��es a ser atualizados.
	 */

	public Configurations getConfigurations()
	{
		return configurations;
	}

	/**
	 * Permite definir um conjunto de configura��es para atualizar seus valores.
	 * @param configurations refer�ncia do conjunto de configura��es a ser atualizados.
	 */

	public void setConfigurations(Configurations configurations)
	{
		if (configurations == null)
			configurations = new Configurations();

		this.configurations = configurations;
	}

	/**
	 * Limpa o leitor de configura��es, usando um conjunto de configura��es em branco.
	 * Al�m disso limpa todas as exce��es enfileiradas e o arquivo restabelecido.
	 */

	public void clearRead()
	{
		configurations = new Configurations();
		exceptions.clear();
		filePath = "";
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

	/**
	 * Efetua a leitura do arquivo atualizando as configura��es conforme os valores em arquivo.
	 * Caso tenha sido definido uma pasta ao inv�s de um arquivo l� todos os arquivos dentro.
	 * @return quantidade de configura��es que foram atualizados durante a leitura.
	 * @throws RagnarokException apenas se n�o conseguir ler o arquivo.
	 */

	public int read() throws RagnarokException
	{
		if (filePath == null)
			throw new RagnarokException("arquivo n�o definido");

		File file = new File(filePath);

		exceptions.clear();

		int read = 0;

		if (!file.isDirectory())
			read += read(file);
		else
			for (File f : file.listFiles())
				read(f);

		return read;
	}

	/**
	 * Efetua a leitura do arquivo atualizando as configura��es conforme os valores em arquivo.
	 * @param file arquivo do qual ter� os dados lidos para atualizar as configura��es.
	 * @return quantidade de configura��es que foram atualizados durante a leitura do arquivo.
	 * @throws RagnarokException apenas se n�o conseguir ler o arquivo.
	 */

	private int read(File file) throws RagnarokException
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
				{
					if (!preferences.is(PREFERENCES_THROWS_FORMAT))
						continue;

					newException("formato inv�lido (linha: %d)", i);
				}

				String name = columns[0].trim();
				String value = columns[1].trim();

				Config<?> config = configurations.get(name);

				if (config == null)
				{
					if (preferences.is(PREFERENCES_THROWS_NOTFOUND))
						newException("configura��o '%s' n�o encontrada (linha: %d)", name, i);
				}

				else if (!config.setRaw(value))
				{
					if (preferences.is(PREFERENCES_THROWS_UNEXPECTED))
						newException("configura��o '%s' n�o aceitou '%s' (linha: %d)", name, value, i);
				}

				else
				{
					read++;

					if (preferences.is(PREFERENCES_INTERNAL_LOG_ALL))
					{
						setUpSource(2);
						logNotice("configura��o '%s' definida em '%s'.\n", name, value);
					}
				}
			}

			reader.close();

			if (preferences.is(PREFERENCES_INTERNAL_LOG_READ))
			{
				setUpSource(2);
				logNotice("%d configura��es lidas de '%s'.\n", read, filePath);
			}

			return read;

		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Cria uma nova exce��o e reage conforme as prefer�ncias definidas no leitor.
	 * @param format string contendo o formato da mensagem que ser� exibida.
	 * @param args argumentos respectivos a formata��o da mensagem.
	 * @throws RagnarokException apenas se PREFERENCES_THROWS_EXCEPTIONS definido.
	 */

	private void newException(String format, Object... args) throws RagnarokException
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

		description.append("file", filePath);
		description.append("configCount", configurations.size());

		return description.toString();
	}
}

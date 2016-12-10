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
 * <h1>Leitor de Configurações</h1>
 *
 * <p>Classe usada para permitir o carregamento de configurações definidas em arquivos de texto.
 * Para que as configurações possam ser lidas elas devem estar alocadas em um conjunto.
 * Quando alocadas em um conjunto o nome da configuração e o seu tipo estarão definidos.</p>
 *
 * <p>O arquivo que for lido deverá apenas repassar o valor da configuração a mesma.
 * Todos os tipos de configurações devem validar um novo valor quando for do tipo string.
 * Assim é possível que o leitor possa carregar valores dinâmicos a suas configurações.</p>
 *
 * @see Queue
 * @see Configurations
 *
 * @author Andrew Mello
 */

public class ConfigReader
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
	 * Caminho referente ao arquivo que será lido.
	 */
	private String filePath;

	/**
	 * Conjunto de configurações que terá os valores atualizados.
	 */
	private Configurations configurations;

	/**
	 * Fila das exceções geradas.
	 */
	private Queue<RagnarokException> exceptions;

	/**
	 * Preferências para reações na leitura de configurações.
	 */
	private BitWise preferences;

	/**
	 * Cria um novo leitor de configurações inicializando o conjunto de configurações.
	 */

	public ConfigReader()
	{
		exceptions = new DynamicQueue<>();
		preferences = new BitWise(PREFERENCES_STRINGS);

		clearRead();
	}

	/**
	 * Define o caminho do arquivo do qual será lido o valor das configurações.
	 * @param filePath caminho completo ou parcial do arquivo em disco.
	 */

	public void setFilePath(String filePath)
	{
		if (filePath != null)
			this.filePath = filePath;
	}

	/**
	 * O leitor de configurações utiliza essas configurações para atualizar seus valores.
	 * @return aquisição do conjunto de configurações a ser atualizados.
	 */

	public Configurations getConfigurations()
	{
		return configurations;
	}

	/**
	 * Permite definir um conjunto de configurações para atualizar seus valores.
	 * @param configurations referência do conjunto de configurações a ser atualizados.
	 */

	public void setConfigurations(Configurations configurations)
	{
		if (configurations == null)
			configurations = new Configurations();

		this.configurations = configurations;
	}

	/**
	 * Limpa o leitor de configurações, usando um conjunto de configurações em branco.
	 * Além disso limpa todas as exceções enfileiradas e o arquivo restabelecido.
	 */

	public void clearRead()
	{
		configurations = new Configurations();
		exceptions.clear();
		filePath = "";
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

	/**
	 * Efetua a leitura do arquivo atualizando as configurações conforme os valores em arquivo.
	 * Caso tenha sido definido uma pasta ao invés de um arquivo lê todos os arquivos dentro.
	 * @return quantidade de configurações que foram atualizados durante a leitura.
	 * @throws RagnarokException apenas se não conseguir ler o arquivo.
	 */

	public int read() throws RagnarokException
	{
		if (filePath == null)
			throw new RagnarokException("arquivo não definido");

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
	 * Efetua a leitura do arquivo atualizando as configurações conforme os valores em arquivo.
	 * @param file arquivo do qual terá os dados lidos para atualizar as configurações.
	 * @return quantidade de configurações que foram atualizados durante a leitura do arquivo.
	 * @throws RagnarokException apenas se não conseguir ler o arquivo.
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

					newException("formato inválido (linha: %d)", i);
				}

				String name = columns[0].trim();
				String value = columns[1].trim();

				Config<?> config = configurations.get(name);

				if (config == null)
				{
					if (preferences.is(PREFERENCES_THROWS_NOTFOUND))
						newException("configuração '%s' não encontrada (linha: %d)", name, i);
				}

				else if (!config.setRaw(value))
				{
					if (preferences.is(PREFERENCES_THROWS_UNEXPECTED))
						newException("configuração '%s' não aceitou '%s' (linha: %d)", name, value, i);
				}

				else
				{
					read++;

					if (preferences.is(PREFERENCES_INTERNAL_LOG_ALL))
					{
						setUpSource(2);
						logNotice("configuração '%s' definida em '%s'.\n", name, value);
					}
				}
			}

			reader.close();

			if (preferences.is(PREFERENCES_INTERNAL_LOG_READ))
			{
				setUpSource(2);
				logNotice("%d configurações lidas de '%s'.\n", read, filePath);
			}

			return read;

		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Cria uma nova exceção e reage conforme as preferências definidas no leitor.
	 * @param format string contendo o formato da mensagem que será exibida.
	 * @param args argumentos respectivos a formatação da mensagem.
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

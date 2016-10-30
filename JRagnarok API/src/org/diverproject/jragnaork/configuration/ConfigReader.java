package org.diverproject.jragnaork.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Queue;

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
	 * Cria um novo leitor de configurações inicializando o conjunto de configurações.
	 */

	public ConfigReader()
	{
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
					exceptions.offer(new RagnarokException("formato inválido (linha: %d).\n", i));

				else
				{
					String name = columns[0].trim();
					String value = columns[1].trim();

					Config<?> config = configurations.get(name);

					if (config != null && config.setRaw(value))
						read++;
				}
			}

			reader.close();

			return read;

		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
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

package org.diverproject.jragnaork.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Queue;

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
	 * Cria um novo leitor de configura��es inicializando o conjunto de configura��es.
	 */

	public ConfigReader()
	{
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
					exceptions.offer(new RagnarokException("formato inv�lido (linha: %d).\n", i));

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

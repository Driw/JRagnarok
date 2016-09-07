package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigString;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

/**
 * <h1>Configuração de servidor</h1>
 *
 * <p>Classe usada para armazenar todos os tipos de configurações de um servidor.
 * O armazenamento é feito de forma mapeada, onde a chave é o nome da configuração.</p>
 *
 * @author Andrew
 */

public abstract class ServerConfig
{
	/**
	 * Valor padrão de configurações booleanas não encontradas.
	 */
	private static final boolean DEFAULT_BOOLEAN = false;

	/**
	 * Valor padrão de configurações numéricas não encontradas.
	 */
	private static final int DEFAULT_INTEGER = 0;

	/**
	 * Valor padrão de configurações alfabéticas não encontradas.
	 */
	private static final String DEFAULT_STRING = "";

	/**
	 * Valor padrão de configurações que criam objetos não encontradas.
	 */
	private static final Object DEFAULT_OBJECT = new Object();


	/**
	 * Mapa contendo as configurações do servidor.
	 */
	private Map<String, Config<?>> configurations;

	/**
	 * Cria um novo objeto para armazenar as configurações de um servidor.
	 * Inicializa o mapa onde ficará as configurações e adiciona as mesmas.
	 * As configurações serão obtidas a partir do método getInitialConfigs().
	 */

	public ServerConfig()
	{
		configurations = new StringSimpleMap<>();

		for (Config<?> config : getInitialConfigs())
			if (config != null && !config.getName().isEmpty())
				if (!configurations.add(config.getName(), config))
					logWarning("configuração '%s' repetindo.\n", config.getName());

	}

	/**
	 * Permite obter o mapa que armazena as configurações do servidor diretamente.
	 * O carregador de configurações irá usar um mapa para alocar as configurações lidas.
	 * Através dele também é possível obter, iterar, adicionar, alterar e adicionar.
	 * @return
	 */

	public Map<String, Config<?>> getMap()
	{
		return configurations;
	}

	/**
	 * Permite obter uma configuração do tipo booleana se esta existir.
	 * @param name nome que foi dada a configuração (identificação única).
	 * @return valor da configuração ou <code>DEFAULT_BOOLEAN</code> caso contrário.
	 */

	public boolean getBool(String name)
	{
		Config<?> config = configurations.get(name);

		if (config instanceof ConfigBoolean)
			return ((ConfigBoolean) config).getValue();

		return DEFAULT_BOOLEAN;
	}

	/**
	 * Permite obter uma configuração do tipo numérico se esta existir.
	 * @param name nome que foi dada a configuração (identificação única).
	 * @return valor da configuração ou <code>DEFAULT_INTEGER</code> caso contrário.
	 */

	public int getInt(String name)
	{
		Config<?> config = configurations.get(name);

		if (config instanceof ConfigInt)
			return ((ConfigInt) config).getValue();

		return DEFAULT_INTEGER;
	}

	/**
	 * Permite obter uma configuração do tipo string se esta existir.
	 * @param name nome que foi dada a configuração (identificação única).
	 * @return valor da configuração ou <code>DEFAULT_STRING</code> caso contrário.
	 */

	public String getString(String name)
	{
		Config<?> config = configurations.get(name);

		if (config instanceof ConfigString)
			return ((ConfigString) config).getValue();

		return DEFAULT_STRING;
	}

	/**
	 * Permite obter uma configuração do tipo objeto se esta existir.
	 * @param name nome que foi dada a configuração (identificação única).
	 * @return valor da configuração ou <code>DEFAULT_OBJECT</code> caso contrário.
	 */

	public Object getObject(String name)
	{
		Config<?> config = configurations.get(name);

		if (config instanceof Config)
			return (Object) config.getValue();

		return DEFAULT_OBJECT;
	}

	/**
	 * Método interno usado pelo construtor para saber quais são as configurações.
	 * Irá iterar essas configurações e adicionar uma por uma no mapeador.
	 * @return aquisição de um vetor contendo todas as configurações válidas.
	 */

	protected abstract Config<?>[] getInitialConfigs();
}

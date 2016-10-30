package org.diverproject.jragnaork.configuration;

import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.Map.MapItem;
import org.diverproject.util.collection.abstraction.StringSimpleMap;
import org.diverproject.util.lang.StringUtil;

/**
 * <h1>Configuração</h1>
 *
 * <p>Classe usada para armazenar todos os tipos de configurações necessárias a serem agrupados.
 * O armazenamento é feito de forma mapeada, onde a chave é o nome completo da configuração.
 * As configurações são vinculadas através de um nome que identifica o grupo e chave.</p>
 *
 * @author Andrew
 */

public class Configurations
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
	 * Mapa contendo as configurações desse agrupamento.
	 */
	private StringSimpleMap<StringSimpleMap<Config<?>>> configs;

	/**
	 * Cria um novo objeto para armazenar as configurações de um servidor.
	 * Inicializa o mapa onde ficará as configurações e adiciona as mesmas.
	 * As configurações serão obtidas a partir do método getInitialConfigs().
	 */

	public Configurations()
	{
		configs = new StringSimpleMap<>();

		for (Config<?> config : getInitialConfigs())
			if (config != null && !config.getName().isEmpty())
				if (!add(config))
					logWarning("configuração '%s' duplicada.\n", config.getName());
	}

	/**
	 * Procedimento interno que separa o nome de uma configuração afim de localizá-la.
	 * A separação é feita através de um único ponto, a primeira parte identifica o
	 * grupo da configuração e o segundo o nome da configuração em si.
	 * @param name nome completo da configuração para localizá-la no mapa.
	 * @return aquisição de um vetor com {GRUPO, CHAVE} referente ao nome.
	 */

	private String[] keysOf(String name)
	{
		if (name != null && StringUtil.countOf(name, '.') == 1)
			return name.split("\\.");

		return null;
	}

	/**
	 * Atualiza uma configuração passada por parâmetro para usar um determinado valor.
	 * @param config referência da configuração que deverá ser atualizada.
	 * @param value valor em forma de objeto que será alocado na configuração.
	 * @return true se atualizar o valor ou false se for de tipo diferente.
	 */

	private boolean set(Config<?> config, Object value)
	{
		try {

			if (config != null)
			{
				config.setObject(value);
				return true;
			}

		} catch (ClassCastException e) {
		}

		return false;
	}

	/**
	 * Seleciona um determinado mapa de configurações conforme o nome do agrupamento.
	 * @param name nome da configuração que deve conter o grupo e chave do mesmo.
	 * @return aquisição do mapa contendo as configurações do agrupamento acima,
	 * caso não haja um mapa para tal será criado, adicionado e retornado.
	 */

	private Map<String, Config<?>> mapOf(String name)
	{
		String keys[] = keysOf(name);
		StringSimpleMap<Config<?>> map = configs.get(keys[0]);

		if (map == null)
		{
			map = new StringSimpleMap<Config<?>>();
			configs.add(keys[0], map);
		}

		return map;
	}

	/**
	 * Permite selecionar uma configuração conforme o seu grupo e chave.
	 * @param name nome para localização da configuração desejada.
	 * @return aquisição da configuração conforme o nome da configuração.
	 */

	public Config<?> get(String name)
	{
		String keys[] = keysOf(name);
		Map<String, Config<?>> map = configs.get(keys[0]);

		if (map == null)
			return null;

		Config<?> config = map.get(keys[1]);

		return config;
	}

	/**
	 * Permite definir um valor em objeto para um determinada configuração.
	 * @param name nome para localização da configuração a ser atualizada.
	 * @param value referência do objeto contendo o novo valor da configuração.
	 * @return true se conseguir atualizar ou false caso contrário,
	 * pode falhar por não existir ou por ser de um tipo diferente.
	 */

	public boolean set(String name, Object value)
	{
		Config<?> config = get(name);

		return set(config, value);
	}

	/**
	 * Cria uma configuração internamente e adiciona ao mapa com o valor passado.
	 * Para que o objeto seja vinculado a uma configuração o seu tipo deve ser registrado.
	 * @param name nome para localização da configuração a ser adicionada.
	 * @param value referência do objeto contendo o valor da configuração.
	 * @return true se conseguir atualizar ou false se já existir.
	 * @see Config
	 */

	public boolean add(String name, Object value)
	{
		return add(name, value, false);
	}

	/**
	 * Cria uma configuração internamente e adiciona ao mapa com o valor passado.
	 * Para que o objeto seja vinculado a uma configuração o seu tipo deve ser registrado.
	 * @param name nome para localização da configuração a ser adicionada.
	 * @param value referência do objeto contendo o valor da configuração.
	 * @param override true para sobrescrever se existir ou false caso contrário.
	 * @return true se conseguir atualizar ou false se já existir e não estiver em override.
	 * @see Config
	 */

	public boolean add(String name, Object value, boolean override)
	{
		Config<?> config = get(name);

		if (config != null)
			return override && set(config, value);

		Map<String, Config<?>> map = mapOf(name);

		name = keysOf(name)[1];

		config = Config.newConfig(name, value);
		config.setObject(value);

		return map.add(name, config);
	}

	/**
	 * Adiciona uma nova configuração já definida em um objeto de configuração.
	 * @param config referência do objeto contendo os dados da configuração.
	 * @return true se adicionar ou false se já existir a configuração.
	 */

	public boolean add(Config<?> config)
	{
		Map<String, Config<?>> map = mapOf(config.getName());
		String keys[] = keysOf(config.getName());

		return map.add(keys[1], config);
	}

	/**
	 * @return aquisição da quantidade de configurações aqui adicionadas.
	 */

	public int size()
	{
		int size = 0;

		for (Map<String, Config<?>> map : configs)
			if (map != null)
				size += map.size();

		return size;
	}

	/**
	 * Limpa todas as configurações removendo até mesmo os grupos de configurações.
	 */

	public void clear()
	{
		configs.clear();
	}

	/**
	 * Limpa todas as configurações mantendo os grupos de configurações.
	 */

	public void clearConfigs()
	{
		for (Map<String, Config<?>> map : configs)
			map.clear();
	}

	/**
	 * Permite obter uma configuração do tipo booleana se esta existir.
	 * @param name nome que foi dada a configuração (identificação única).
	 * @return valor da configuração ou <code>DEFAULT_BOOLEAN</code> caso contrário.
	 */

	public boolean getBool(String name)
	{
		Config<?> config = get(name);

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
		Config<?> config = get(name);

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
		Config<?> config = get(name);

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
		Config<?> config = get(name);

		if (config instanceof Config)
			return (Object) config.getValue();

		return DEFAULT_OBJECT;
	}

	/**
	 * Método interno usado pelo construtor para saber quais são as configurações.
	 * Irá iterar essas configurações e adicionar uma por uma no mapeador.
	 * @return aquisição de um vetor contendo todas as configurações válidas.
	 */

	protected Config<?>[] getInitialConfigs()
	{
		return new Config[0];
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		for (MapItem<String, StringSimpleMap<Config<?>>> item : configs.iterateItems())
			for (String key : item.value.iterateKey())
				description.append(item.key+ "." +key);

		return description.toString();
	}
}

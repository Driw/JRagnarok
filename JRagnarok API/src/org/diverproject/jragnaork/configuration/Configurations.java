package org.diverproject.jragnaork.configuration;

import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.Map.MapItem;
import org.diverproject.util.collection.abstraction.StringSimpleMap;
import org.diverproject.util.lang.StringUtil;

/**
 * <h1>Configura��o</h1>
 *
 * <p>Classe usada para armazenar todos os tipos de configura��es necess�rias a serem agrupados.
 * O armazenamento � feito de forma mapeada, onde a chave � o nome completo da configura��o.
 * As configura��es s�o vinculadas atrav�s de um nome que identifica o grupo e chave.</p>
 *
 * @author Andrew
 */

public class Configurations
{
	/**
	 * Valor padr�o de configura��es booleanas n�o encontradas.
	 */
	private static final boolean DEFAULT_BOOLEAN = false;

	/**
	 * Valor padr�o de configura��es num�ricas n�o encontradas.
	 */
	private static final int DEFAULT_INTEGER = 0;

	/**
	 * Valor padr�o de configura��es alfab�ticas n�o encontradas.
	 */
	private static final String DEFAULT_STRING = "";

	/**
	 * Valor padr�o de configura��es que criam objetos n�o encontradas.
	 */
	private static final Object DEFAULT_OBJECT = new Object();


	/**
	 * Mapa contendo as configura��es desse agrupamento.
	 */
	private StringSimpleMap<StringSimpleMap<Config<?>>> configs;

	/**
	 * Cria um novo objeto para armazenar as configura��es de um servidor.
	 * Inicializa o mapa onde ficar� as configura��es e adiciona as mesmas.
	 * As configura��es ser�o obtidas a partir do m�todo getInitialConfigs().
	 */

	public Configurations()
	{
		configs = new StringSimpleMap<>();

		for (Config<?> config : getInitialConfigs())
			if (config != null && !config.getName().isEmpty())
				if (!add(config))
					logWarning("configura��o '%s' duplicada.\n", config.getName());
	}

	/**
	 * Procedimento interno que separa o nome de uma configura��o afim de localiz�-la.
	 * A separa��o � feita atrav�s de um �nico ponto, a primeira parte identifica o
	 * grupo da configura��o e o segundo o nome da configura��o em si.
	 * @param name nome completo da configura��o para localiz�-la no mapa.
	 * @return aquisi��o de um vetor com {GRUPO, CHAVE} referente ao nome.
	 */

	private String[] keysOf(String name)
	{
		if (name != null && StringUtil.countOf(name, '.') == 1)
			return name.split("\\.");

		return null;
	}

	/**
	 * Atualiza uma configura��o passada por par�metro para usar um determinado valor.
	 * @param config refer�ncia da configura��o que dever� ser atualizada.
	 * @param value valor em forma de objeto que ser� alocado na configura��o.
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
	 * Seleciona um determinado mapa de configura��es conforme o nome do agrupamento.
	 * @param name nome da configura��o que deve conter o grupo e chave do mesmo.
	 * @return aquisi��o do mapa contendo as configura��es do agrupamento acima,
	 * caso n�o haja um mapa para tal ser� criado, adicionado e retornado.
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
	 * Permite selecionar uma configura��o conforme o seu grupo e chave.
	 * @param name nome para localiza��o da configura��o desejada.
	 * @return aquisi��o da configura��o conforme o nome da configura��o.
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
	 * Permite definir um valor em objeto para um determinada configura��o.
	 * @param name nome para localiza��o da configura��o a ser atualizada.
	 * @param value refer�ncia do objeto contendo o novo valor da configura��o.
	 * @return true se conseguir atualizar ou false caso contr�rio,
	 * pode falhar por n�o existir ou por ser de um tipo diferente.
	 */

	public boolean set(String name, Object value)
	{
		Config<?> config = get(name);

		return set(config, value);
	}

	/**
	 * Cria uma configura��o internamente e adiciona ao mapa com o valor passado.
	 * Para que o objeto seja vinculado a uma configura��o o seu tipo deve ser registrado.
	 * @param name nome para localiza��o da configura��o a ser adicionada.
	 * @param value refer�ncia do objeto contendo o valor da configura��o.
	 * @return true se conseguir atualizar ou false se j� existir.
	 * @see Config
	 */

	public boolean add(String name, Object value)
	{
		return add(name, value, false);
	}

	/**
	 * Cria uma configura��o internamente e adiciona ao mapa com o valor passado.
	 * Para que o objeto seja vinculado a uma configura��o o seu tipo deve ser registrado.
	 * @param name nome para localiza��o da configura��o a ser adicionada.
	 * @param value refer�ncia do objeto contendo o valor da configura��o.
	 * @param override true para sobrescrever se existir ou false caso contr�rio.
	 * @return true se conseguir atualizar ou false se j� existir e n�o estiver em override.
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
	 * Adiciona uma nova configura��o j� definida em um objeto de configura��o.
	 * @param config refer�ncia do objeto contendo os dados da configura��o.
	 * @return true se adicionar ou false se j� existir a configura��o.
	 */

	public boolean add(Config<?> config)
	{
		Map<String, Config<?>> map = mapOf(config.getName());
		String keys[] = keysOf(config.getName());

		return map.add(keys[1], config);
	}

	/**
	 * @return aquisi��o da quantidade de configura��es aqui adicionadas.
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
	 * Limpa todas as configura��es removendo at� mesmo os grupos de configura��es.
	 */

	public void clear()
	{
		configs.clear();
	}

	/**
	 * Limpa todas as configura��es mantendo os grupos de configura��es.
	 */

	public void clearConfigs()
	{
		for (Map<String, Config<?>> map : configs)
			map.clear();
	}

	/**
	 * Permite obter uma configura��o do tipo booleana se esta existir.
	 * @param name nome que foi dada a configura��o (identifica��o �nica).
	 * @return valor da configura��o ou <code>DEFAULT_BOOLEAN</code> caso contr�rio.
	 */

	public boolean getBool(String name)
	{
		Config<?> config = get(name);

		if (config instanceof ConfigBoolean)
			return ((ConfigBoolean) config).getValue();

		return DEFAULT_BOOLEAN;
	}

	/**
	 * Permite obter uma configura��o do tipo num�rico se esta existir.
	 * @param name nome que foi dada a configura��o (identifica��o �nica).
	 * @return valor da configura��o ou <code>DEFAULT_INTEGER</code> caso contr�rio.
	 */

	public int getInt(String name)
	{
		Config<?> config = get(name);

		if (config instanceof ConfigInt)
			return ((ConfigInt) config).getValue();

		return DEFAULT_INTEGER;
	}

	/**
	 * Permite obter uma configura��o do tipo string se esta existir.
	 * @param name nome que foi dada a configura��o (identifica��o �nica).
	 * @return valor da configura��o ou <code>DEFAULT_STRING</code> caso contr�rio.
	 */

	public String getString(String name)
	{
		Config<?> config = get(name);

		if (config instanceof ConfigString)
			return ((ConfigString) config).getValue();

		return DEFAULT_STRING;
	}

	/**
	 * Permite obter uma configura��o do tipo objeto se esta existir.
	 * @param name nome que foi dada a configura��o (identifica��o �nica).
	 * @return valor da configura��o ou <code>DEFAULT_OBJECT</code> caso contr�rio.
	 */

	public Object getObject(String name)
	{
		Config<?> config = get(name);

		if (config instanceof Config)
			return (Object) config.getValue();

		return DEFAULT_OBJECT;
	}

	/**
	 * M�todo interno usado pelo construtor para saber quais s�o as configura��es.
	 * Ir� iterar essas configura��es e adicionar uma por uma no mapeador.
	 * @return aquisi��o de um vetor contendo todas as configura��es v�lidas.
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

package org.diverproject.jragnaork.configuration;

import org.diverproject.util.collection.abstraction.StringSimpleMap;

/**
 * <h1>Sistema de Configurações</h1>
 *
 * <p>O sistema de configurações possui uma instância para mapear as configurações existentes.
 * Assim é possível agrupar as configurações em um mesmo lugar para diversos servidores.</p>
 *
 * @see StringSimpleMap
 *
 * @author Andrew Mello
 */

public class ConfigSystem
{
	/**
	 * Instância do sistema de configurações.
	 */
	private static final ConfigSystem INSTANCE = new ConfigSystem();


	/**
	 * Mapeamento das configurações existentes por nome.
	 */
	private StringSimpleMap<Configurations> configs;

	/**
	 * Cria uma nova instância para o sistema de configurações.
	 * Inicializa o mapeamento das configurações por chave de String.
	 */

	private ConfigSystem()
	{
		configs = new StringSimpleMap<>();
	}

	/**
	 * Adiciona um novo conjunto de configurações ao sistema de configurações.
	 * @param key chave referente ao nome que será dado as configurações.
	 * @param configurations referência do objeto contendo as configurações.
	 * @return true se adicionar ou false se a chave estiver sendo usada.
	 */

	public boolean add(String key, Configurations configurations)
	{
		return configs.add(key, configurations);
	}

	/**
	 * Obtém um conjunto de configurações através da sua chave (nome).
	 * @param key chave referente ao nome que foi dado as configurações.
	 * @return aquisição do conjunto de configurações referente a chave.
	 */

	public Configurations get(String key)
	{
		return configs.get(key);
	}

	/**
	 * Limpa o mapeamento de configurações removendo todos os conjuntos adicionados.
	 * Esse procedimento não irá limpar as configurações dentro dos conjuntos.
	 */

	public void clear()
	{
		configs.clear();
	}

	/**
	 * Limpa todas as configurações existentes dentro dos conjuntos, deixando-os limpos.
	 * Entretanto os conjuntos permanecem alocados no sistema de configurações.
	 */

	public void clearConfigs()
	{
		for (Configurations configurations : configs)
			configurations.clear();
	}

	/**
	 * O Sistema de configurações utiliza-se do padrão de projetos Singleton.
	 * Assim é possível que apenas um instância do mesmo possa existir.
	 * Através desse método é possível obter o objeto referente a este.
	 * @return aquisição da instância do sistema de configurações.
	 */

	public static ConfigSystem getInstance()
	{
		return INSTANCE;
	}
}

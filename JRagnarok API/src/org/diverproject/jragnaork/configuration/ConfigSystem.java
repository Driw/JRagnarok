package org.diverproject.jragnaork.configuration;

import org.diverproject.util.collection.abstraction.StringSimpleMap;

/**
 * <h1>Sistema de Configura��es</h1>
 *
 * <p>O sistema de configura��es possui uma inst�ncia para mapear as configura��es existentes.
 * Assim � poss�vel agrupar as configura��es em um mesmo lugar para diversos servidores.</p>
 *
 * @see StringSimpleMap
 *
 * @author Andrew Mello
 */

public class ConfigSystem
{
	/**
	 * Inst�ncia do sistema de configura��es.
	 */
	private static final ConfigSystem INSTANCE = new ConfigSystem();


	/**
	 * Mapeamento das configura��es existentes por nome.
	 */
	private StringSimpleMap<Configurations> configs;

	/**
	 * Cria uma nova inst�ncia para o sistema de configura��es.
	 * Inicializa o mapeamento das configura��es por chave de String.
	 */

	private ConfigSystem()
	{
		configs = new StringSimpleMap<>();
	}

	/**
	 * Adiciona um novo conjunto de configura��es ao sistema de configura��es.
	 * @param key chave referente ao nome que ser� dado as configura��es.
	 * @param configurations refer�ncia do objeto contendo as configura��es.
	 * @return true se adicionar ou false se a chave estiver sendo usada.
	 */

	public boolean add(String key, Configurations configurations)
	{
		return configs.add(key, configurations);
	}

	/**
	 * Obt�m um conjunto de configura��es atrav�s da sua chave (nome).
	 * @param key chave referente ao nome que foi dado as configura��es.
	 * @return aquisi��o do conjunto de configura��es referente a chave.
	 */

	public Configurations get(String key)
	{
		return configs.get(key);
	}

	/**
	 * Limpa o mapeamento de configura��es removendo todos os conjuntos adicionados.
	 * Esse procedimento n�o ir� limpar as configura��es dentro dos conjuntos.
	 */

	public void clear()
	{
		configs.clear();
	}

	/**
	 * Limpa todas as configura��es existentes dentro dos conjuntos, deixando-os limpos.
	 * Entretanto os conjuntos permanecem alocados no sistema de configura��es.
	 */

	public void clearConfigs()
	{
		for (Configurations configurations : configs)
			configurations.clear();
	}

	/**
	 * O Sistema de configura��es utiliza-se do padr�o de projetos Singleton.
	 * Assim � poss�vel que apenas um inst�ncia do mesmo possa existir.
	 * Atrav�s desse m�todo � poss�vel obter o objeto referente a este.
	 * @return aquisi��o da inst�ncia do sistema de configura��es.
	 */

	public static ConfigSystem getInstance()
	{
		return INSTANCE;
	}
}

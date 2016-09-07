package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigString;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

/**
 * <h1>Configura��o de servidor</h1>
 *
 * <p>Classe usada para armazenar todos os tipos de configura��es de um servidor.
 * O armazenamento � feito de forma mapeada, onde a chave � o nome da configura��o.</p>
 *
 * @author Andrew
 */

public abstract class ServerConfig
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
	 * Mapa contendo as configura��es do servidor.
	 */
	private Map<String, Config<?>> configurations;

	/**
	 * Cria um novo objeto para armazenar as configura��es de um servidor.
	 * Inicializa o mapa onde ficar� as configura��es e adiciona as mesmas.
	 * As configura��es ser�o obtidas a partir do m�todo getInitialConfigs().
	 */

	public ServerConfig()
	{
		configurations = new StringSimpleMap<>();

		for (Config<?> config : getInitialConfigs())
			if (config != null && !config.getName().isEmpty())
				if (!configurations.add(config.getName(), config))
					logWarning("configura��o '%s' repetindo.\n", config.getName());

	}

	/**
	 * Permite obter o mapa que armazena as configura��es do servidor diretamente.
	 * O carregador de configura��es ir� usar um mapa para alocar as configura��es lidas.
	 * Atrav�s dele tamb�m � poss�vel obter, iterar, adicionar, alterar e adicionar.
	 * @return
	 */

	public Map<String, Config<?>> getMap()
	{
		return configurations;
	}

	/**
	 * Permite obter uma configura��o do tipo booleana se esta existir.
	 * @param name nome que foi dada a configura��o (identifica��o �nica).
	 * @return valor da configura��o ou <code>DEFAULT_BOOLEAN</code> caso contr�rio.
	 */

	public boolean getBool(String name)
	{
		Config<?> config = configurations.get(name);

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
		Config<?> config = configurations.get(name);

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
		Config<?> config = configurations.get(name);

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
		Config<?> config = configurations.get(name);

		if (config instanceof Config)
			return (Object) config.getValue();

		return DEFAULT_OBJECT;
	}

	/**
	 * M�todo interno usado pelo construtor para saber quais s�o as configura��es.
	 * Ir� iterar essas configura��es e adicionar uma por uma no mapeador.
	 * @return aquisi��o de um vetor contendo todas as configura��es v�lidas.
	 */

	protected abstract Config<?>[] getInitialConfigs();
}

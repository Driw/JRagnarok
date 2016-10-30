package org.diverproject.jragnarok;

import static org.diverproject.jragnarok.JRagnarokConfigs.TYPES_CONFIGS;
import static org.diverproject.jragnarok.JRagnarokConfigs.USE_CONSOLE;
import static org.diverproject.jragnarok.JRagnarokConfigs.USE_LOG;
import static org.diverproject.jragnarok.JRagnarokConfigs.CONFIG_TYPES;
import static org.diverproject.jragnarok.JRagnarokConfigs.FILE_CONFIG_TYPES;
import static org.diverproject.jragnarok.JRagnarokConfigs.FILE_SYSTEM;
import static org.diverproject.jragnarok.JRagnarokConfigs.LOG_FILENAME;
import static org.diverproject.jragnarok.JRagnarokConfigs.SERVER_FILES;
import static org.diverproject.jragnarok.JRagnarokConfigs.SERVER_FOLDER;
import static org.diverproject.jragnarok.JRagnarokConfigs.SYSTEM_CONFIGS;
import static org.diverproject.log.LogSystem.logExeceptionSource;
import static org.diverproject.util.MessageUtil.die;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigLoad;
import org.diverproject.jragnaork.configuration.ConfigString;
import org.diverproject.jragnaork.configuration.ConfigSystem;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.log.LogPreferences;
import org.diverproject.log.LogSystem;
import org.diverproject.util.collection.abstraction.StringSimpleMap;
import org.diverproject.util.lang.StringUtil;

/**
 * <h1>JRagnarok</h1>
 *
 * <p>JRagnarok � um emulador que ir� funcionar como servidor(es) de Ragnarok Online.
 * Desenvolvido em Java e visando a melhor din�mica no funcionamento do mesmo.</p>
 *
 * <p>Essa classe � a classe principal por onde o servidor ser� rodado.
 * Atrav�s dele ser� feito a inicializa��o base do sistema, detectando arquivos e outros.
 * Com essa detec��o ser� poss�vel identificar quantos e quais os servidores a serem rodados.</p>
 *
 * <p>JRagnarok funciona como um servidor macro que ir� rodar micro servidores.
 * Cada micro servidor pode ser um servidor de acesso, personagens ou mapa.
 * Especificando nos arquivos do servidor macro podemos encontrar os micros.</p>
 *
 * @see StringSimpleMap
 * @see Configurations
 * @see ConfigLoad
 * @see Config
 * @see LogPreferences
 * @see LogSystem
 *
 * @author Andrew Mello
 */

public class JRagnarok
{
	/**
	 * Agrupamento das configura��es geradas por argumentos.
	 */
	public static final Configurations ARGUMENT_CONFIGS = new Configurations();

	/**
	 * Mapa contendo todos os argumentos encontrados na aplica��o.
	 */
	public static final StringSimpleMap<String> ARGUMENTS = new StringSimpleMap<>();

	/**
	 * Procedimento prim�rio que ser� chamado quando a aplica��o for aberta.
	 * Atrav�s dele dever� chamar outros m�todos para inicializa��o b�sica do sistema.
	 * Por fim dever� garantir que todos os micro servidores sejam inicializados.
	 * @param args vetor contendo todos os argumentos usados ao rodar a aplica��o.
	 */

	public static void main(String[] args)
	{
		prepareSystemConfig();
		prepareArguments();
		readArguments(args);
		prepareLog();
		prepareConsole();
		loadSystemConfig();
	}

	/**
	 * Prepara as configura��es m�nimas necess�rias para funcionamento do sistema.
	 * Dentre elas pode estar sendo definido servi�os que podem ser utilizados.
	 * Como tamb�m propriedades e nome de arquivos a serem lidos.
	 */

	private static void prepareSystemConfig()
	{
		Configurations system = new Configurations();
		system.add(new ConfigString(CONFIG_TYPES, "ConfigTypes.conf"));
		system.add(new ConfigString(SERVER_FILES));
		system.add(new ConfigString(SERVER_FOLDER, "Servers"));
		system.add(new ConfigBoolean(USE_CONSOLE, true));
		system.add(new ConfigBoolean(USE_LOG, true));
		system.add(new ConfigString(LOG_FILENAME, "log"));

		ConfigSystem configs = ConfigSystem.getInstance();
		configs.add(SYSTEM_CONFIGS, system);
		configs.add(TYPES_CONFIGS, new Configurations());
	}

	/**
	 * Prepara os argumentos m�nimos necess�rios para funcionamento do sistema.
	 * Al�m de identificar os argumentos b�sicos cria uma configura��o para eles.
	 */

	private static void prepareArguments()
	{
		ARGUMENTS.add("fs", FILE_SYSTEM);
		ARGUMENTS.add("fct", FILE_CONFIG_TYPES);
		ARGUMENTS.add("c", USE_CONSOLE);
		ARGUMENTS.add("l", USE_LOG);
		ARGUMENTS.add("lf", LOG_FILENAME);

		ARGUMENT_CONFIGS.add(new ConfigString("arg.fs"));
		ARGUMENT_CONFIGS.add(new ConfigString("arg.fct"));
		ARGUMENT_CONFIGS.add(new ConfigBoolean("arg.c"));
		ARGUMENT_CONFIGS.add(new ConfigBoolean("arg.l"));
		ARGUMENT_CONFIGS.add(new ConfigString("arg.lf"));
	}

	/**
	 * Efetua a leitura dos argumentos que foram identificados ao rodar a aplica��o.
	 * Os argumentos s�o separados por espa�o e podem ser definidos de duas formas.
	 * A primeira � como valor booleano usando um tra�o (-) a frente do argumento.
	 * Na segunda forma � usar o nome do argumento seguido de igual (=) e seu valor.
	 * @param args vetor contendo todos os argumentos que foram identificados.
	 */

	private static void readArguments(String[] args)
	{
		if (args == null || args.length == 0)
			return;

		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];

			if (arg.startsWith("-"))
			{
				Config<?> config = ARGUMENT_CONFIGS.get("arg." +arg.substring(1));

				if (config instanceof ConfigBoolean)
					((ConfigBoolean) config).setValue(true);
			}

			else if (StringUtil.countOf(arg, '=') == 1)
			{
				String temp[] = arg.split("=");
				String name = temp[0];
				String value = temp[1];

				Config<?> config = ARGUMENT_CONFIGS.get("arg." +name);
				config.setObject(value);
			}

			else
				die(new IllegalArgumentException("argumento " +(i+1)+ " '" +arg+ "' inv�lido"));
		}
	}

	/**
	 * Realiza o preparamento da inicializa��o do servi�o para registros de mensagens.
	 * Esse servi�o permite registrar diversas mensagens que ajudam no acompanhamento do sistema.
	 * Atrav�s dos registros � poss�vel saber onde um problema ou a��o foi executada.
	 */

	private static void prepareLog()
	{
		if (ARGUMENT_CONFIGS.getBool("arg.l"))
		{
			String filename = ARGUMENT_CONFIGS.getString("arg.lf");

			if (filename.isEmpty())
				filename = "log.txt";

			LogPreferences.setUseAll();
			LogPreferences.setFile(filename);
			LogSystem.initialize();
			LogSystem.addListener(JRagnarokLogListener.getInstance());
		}
	}

	/**
	 * Realiza o preparamento da inicializa��o do servi�o para exibi��o de registros.
	 * O console dever� permitir a exibi��o das mensagens registraras e entrar com comandos.
	 * Os comandos permitem mudan�as tempor�rias ou fixas no sistema conforme programadas.
	 */

	private static void prepareConsole()
	{
		if (ARGUMENT_CONFIGS.getBool("arg.c"))
		{
			// TODO instanciar, configurar e inicializar o console
		}
	}

	/**
	 * Deve efetuar o carregamento do arquivo de configura��es m�nimas do sistema.
	 * As configura��es carregadas ser�o alocadas no conjunto apropriado.
	 */

	private static void loadSystemConfig()
	{
		String filePath = ARGUMENT_CONFIGS.getString("arg.fs");

		if (filePath.isEmpty())
			filePath = "Config/System.conf";

		try {

			ConfigSystem system = ConfigSystem.getInstance();
			Configurations configurations = system.get(SYSTEM_CONFIGS);

			ConfigLoad load = new ConfigLoad();
			load.setConfigurations(configurations);
			load.setFilePath(filePath);
			load.read();

		} catch (RagnarokException e) {
			logExeceptionSource(e);
			die(e);
		}
	}
}

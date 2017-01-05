package org.diverproject.jragnarok;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.TYPES_CONFIGS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newServerConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newSystemConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_USE_CONSOLE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_USE_LOG;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_CONFIG_TYPES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.FILE_SYSTEM;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_LOGINID;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_LOG_FILENAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_CHAR_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_LOGIN_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_MAP_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_CONFIGS;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logExeceptionSource;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.MessageUtil.die;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigReader;
import org.diverproject.jragnaork.configuration.ConfigString;
import org.diverproject.jragnaork.configuration.ConfigSystem;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.console.JRagnarokConsole;
import org.diverproject.jragnarok.server.ServerControl;
import org.diverproject.jragnarok.server.character.CharServer;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.map.MapServer;
import org.diverproject.log.LogPreferences;
import org.diverproject.log.LogSystem;
import org.diverproject.util.SystemUtil;
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
 * @see ConfigReader
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
	 * Console utilizado para intera��o do administrador com o servidor.
	 */
	public static final JRagnarokConsole CONSOLE = new JRagnarokConsole();

	/**
	 * Controlador de servidores.
	 */
	private static final ServerControl SERVERS = ServerControl.getInstance();


	private JRagnarok()
	{
		
	}

	/**
	 * Procedimento prim�rio que ser� chamado quando a aplica��o for aberta.
	 * Atrav�s dele dever� chamar outros m�todos para inicializa��o b�sica do sistema.
	 * Por fim dever� garantir que todos os micro servidores sejam inicializados.
	 * @param args vetor contendo todos os argumentos usados ao rodar a aplica��o.
	 */

	public static void main(String[] args)
	{
		SystemUtil.setWindowsInterface();

		prepareSystemConfig();
		prepareArguments();
		readArguments(args);
		prepareLog();
		prepareConsole();
		loadSystemConfig();
		prepareConfigTypes();
		prepareServers();
		runServers();
	}

	/**
	 * Prepara as configura��es m�nimas necess�rias para funcionamento do sistema.
	 * Dentre elas pode estar sendo definido servi�os que podem ser utilizados.
	 * Como tamb�m propriedades e nome de arquivos a serem lidos.
	 */

	private static void prepareSystemConfig()
	{
		ConfigSystem configs = ConfigSystem.getInstance();
		configs.add(SYSTEM_CONFIGS, newSystemConfigs());
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
		ARGUMENTS.add("c", SYSTEM_USE_CONSOLE);
		ARGUMENTS.add("l", SYSTEM_USE_LOG);
		ARGUMENTS.add("lf", SYSTEM_LOG_FILENAME);

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
			CONSOLE.show();
			CONSOLE.setDefaultConsole();
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

			ConfigReader read = new ConfigReader();
			read.setConfigurations(configurations);
			read.setFilePath(filePath);
			read.read();

		} catch (RagnarokException e) {
			logExeceptionSource(e);
			die(e);
		}
	}

	/**
	 * Realiza a leitura do arquivo de configura��es que listam os tipos de configura��es.
	 * Essa lista dever� conter apenas as configura��es que n�o forem padr�es da API.
	 * Assim uma nova configura��o poder� ser lida corretamente sem exceptions.
	 */

	private static void prepareConfigTypes()
	{
		String filePath = ARGUMENT_CONFIGS.getString("arg.fct");

		try {

			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);

			while (br.ready())
			{
				String className = br.readLine();

				try {

					Class<?> cls = Class.forName(className);

					if (Config.add(cls))
						log("classe de configura��o '%s' vinculada.\n", cls.getSimpleName());
					else
						logWarning("classe de configura��o '%s' j� foi vinculada.\n", cls.getSimpleName());

				} catch (ClassNotFoundException e) {
					logError("configura��o '%s' n�o encontrada.\n", className);
				}
			}

			br.close();

		} catch (IOException e) {
			logExeceptionSource(e);
			die(e);
		}
	}

	/**
	 * Prepara os servidores (micro servidores) para serem utilizados pelo sistema.
	 * Deve considerar os arquivos de configura��es definidos e configur�-los de tal modo.
	 */

	private static void prepareServers()
	{
		ConfigSystem system = ConfigSystem.getInstance();
		Configurations configs = system.get(SYSTEM_CONFIGS);

		String folder = configs.getString(SYSTEM_SERVER_FOLDER);
		String files[] = configs.getString(SYSTEM_SERVER_FILES).split(",");

		for (String file : files)
		{
			String filepath = format("config/%s/%s", folder, file);
			prepareServer(configs, filepath);
		}
	}

	/**
	 * Prepara um novo servidor de acordo com as configura��es de sistema.
	 * Tamb�m � necess�rio definir o local das configura��es desse servidor.
	 * @param systemConfigs conjunto das configura��es referentes ao sistema.
	 * @param filepath local onde se encontra as configura��es do servidor.
	 */

	private static void prepareServer(Configurations systemConfigs, String filepath)
	{
		Configurations serverConfigs = newServerConfigs();
		serverConfigs.add(systemConfigs);

		ConfigReader read = new ConfigReader();
		read.setConfigurations(serverConfigs);
		read.setFilePath(filepath);

		try {
			logNotice("lido %d configura��es de '%s'.\n", read.read(), filepath);
		} catch (RagnarokException e) {
			logError("falha ao ler configura��es de '%s'.\n", filepath);
			logException(e);
		}

		prepareMicroServers(serverConfigs);
	}

	/**
	 * Prepara a cria��o dos servidores de acesso, personagem e mapa se necess�rio.
	 * Atrav�s das configura��es identifica o c�digo de cada servidor e verifica se
	 * os mesmos j� foram criados, caso n�o tenham sido ir� criar e configurar.
	 * @param configs conjunto de configura��es referente ao servidor.
	 */

	private static void prepareMicroServers(Configurations configs)
	{
		int loginID = configs.getInt(SERVER_LOGINID);
		int charID = configs.getInt(SERVER_LOGINID);
		int mapID = configs.getInt(SERVER_LOGINID);

		if (SERVERS.getLoginServer(loginID) == null)
			prepareLoginServer(loginID, configs);

		if (SERVERS.getCharServer(charID) == null)
			prepareCharServer(charID, configs);

		if (SERVERS.getMapServer(mapID) == null)
			prepareMapServer(mapID, configs);
	}

	/**
	 * Prepara a cria��o e configura��o de um novo servidor de acesso conforme:
	 * @param id c�digo de identifica��o �nico entre os servidores de acesso.
	 * @param configs conjunto de configura��es referentes ao micro servidor.
	 */

	private static void prepareLoginServer(int id, Configurations configs)
	{
		LoginServer server = new LoginServer();
		server.setID(id);
		server.setConfigurations(configs);

		String files = configs.getString(SYSTEM_SERVER_DEFAULT_FILES)+ "," +configs.getString(SYSTEM_SERVER_DEFAULT_LOGIN_FILES);
		configs.add(SYSTEM_SERVER_DEFAULT_FILES, files, true);

		SERVERS.add(server);
		CONSOLE.addLoginServer(server);
	}

	/**
	 * Prepara a cria��o e configura��o de um novo servidor de personagem conforme:
	 * @param id c�digo de identifica��o �nico entre os servidores de personagem.
	 * @param configs conjunto de configura��es referentes ao micro servidor.
	 */

	private static void prepareCharServer(int id, Configurations configs)
	{
		CharServer server = new CharServer();
		server.setID(id);
		server.setConfigurations(configs);

		String files = configs.getString(SYSTEM_SERVER_DEFAULT_FILES)+ "," +configs.getString(SYSTEM_SERVER_DEFAULT_CHAR_FILES);
		configs.add(SYSTEM_SERVER_DEFAULT_FILES, files, true);

		SERVERS.add(server);
		CONSOLE.addCharServer(server);
	}

	/**
	 * Prepara a cria��o e configura��o de um novo servidor de mapa conforme:
	 * @param id c�digo de identifica��o �nico entre os servidores de mapa.
	 * @param configs conjunto de configura��es referentes ao micro servidor.
	 */

	private static void prepareMapServer(int id, Configurations configs)
	{
		MapServer server = new MapServer();
		server.setID(id);
		server.setConfigurations(configs);

		String files = configs.getString(SYSTEM_SERVER_DEFAULT_FILES)+ "," +configs.getString(SYSTEM_SERVER_DEFAULT_MAP_FILES);
		configs.add(SYSTEM_SERVER_DEFAULT_FILES, files, true);

		SERVERS.add(server);
		CONSOLE.addMapServer(server);
	}

	/**
	 * Procedimento que dever� for�ar a cria��o e inicializa��o de todos os micro servidores.
	 */

	private static void runServers()
	{
		SERVERS.createAll(true);
		SERVERS.runAll(true);
	}
}

package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newCharServerConfigs;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.character.control.AuthControl;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.character.control.ExperienceControl;
import org.diverproject.jragnarok.server.character.control.FamilyControl;
import org.diverproject.jragnarok.server.character.control.LocationControl;
import org.diverproject.jragnarok.server.character.control.LookControl;
import org.diverproject.jragnarok.server.character.control.MercenaryRankControl;
import org.diverproject.jragnarok.server.character.control.OnlineCharControl;

/**
 * <h1>Servidor de Personagem</h1>
 *
 * <p>Este servidor ser� respons�vel por realizar o controle de todas as informa��es dos personagens.
 * Essas informa��es v�o deste de identifica��es, nomes, n�veis como rela��es entre os personagens:
 * amizades, grupos, cl� e v�nculos como mascotes, hom�nculos, elementares e assistentes.</p>
 *
 * <p>O servidor de personagem tamb�m dever� possuir os servi�os adequados para realizar seus deveres.
 * Para isso h� um servi�o de comunica��o inicial com o cliente afim de receber dados e comandos.
 * Sempre que um novo servidor de personagem criado ele dever� ser autenticado no servidor de acesso.</p>
 *
 * <p>Para que ele esteja realmente funcionando dever� estar constantemente conectado com um
 * servidor de acesso especificado nas configura��es, pois algumas informa��es ser�o obtidas dele.
 * Por exemplo, alguns dados da conta do jogador necess�rios ser�o solicitados do mesmo.</p>
 *
 * @see ServiceCharClient
 * @see ServiceCharServer
 * @see ServiceCharLogin
 * @see CharacterControl
 * @see ExperienceControl
 * @see FamilyControl
 * @see LocationControl
 * @see LookControl
 * @see MercenaryRankControl
 *
 * @author Andrew
 */

public class CharServer extends Server
{
	/**
	 * Lista dos Servidores de Mapa dispon�veis.
	 */
	private MapServerList mapServers;

	/**
	 * Servi�o para comunica��o inicial com o cliente.
	 */
	private ServiceCharClient charClient;

	/**
	 * Servi�o principal do servidor de personagem.
	 */
	private ServiceCharServer charServer;

	/**
	 * Servi�o para comunica��o com o servidor de acesso.
	 */
	private ServiceCharLogin charLogin;

	/**
	 * Servi�o para autentica��o dos jogadores no servidor.
	 */
	private ServiceCharServerAuth charServerAuth;


	/**
	 * Controle para autentica��o de jogadores online.
	 */
	private AuthControl authControl;

	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineCharControl onlineCharControl;

	/**
	 * Controle para gerenciar dados dos personagens.
	 */
	private CharacterControl characterControl;

	/**
	 * Controle para gerenciar os n�veis de experi�ncia dos personagens.
	 */
	private ExperienceControl experienceControl;

	/**
	 * Controle para gerenciar a rela��o familiar dos personagens.
	 */
	private FamilyControl familyControl;

	/**
	 * Controle para gerenciar as localiza��es como pontos de retorno dos personagens.
	 */
	private LocationControl locationControl;

	/**
	 * Controle para gerenciar a apar�ncia de estilos e cores dos personagens.
	 */
	private LookControl lookControl;

	/**
	 * Controle para gerenciar a classifica��o no sistema de assistentes dos personagens.
	 */
	private MercenaryRankControl mercenaryRankControl;

	/**
	 * Cria uma nova inst�ncia de um servidor de personagem inicializa��o o seu listener.
	 */

	public CharServer()
	{
		setListener(listener);
	}

	/**
	 * @return aquisi��o da lista dos servidores de mapa dispon�veis.
	 */

	public MapServerList getMapServers()
	{
		return mapServers;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o inicial com clientes.
	 */

	public ServiceCharClient getCharClient()
	{
		return charClient;
	}

	/**
	 * @return aquisi��o do servi�o principal do servidor de personagem.
	 */

	public ServiceCharServer getCharServer()
	{
		return charServer;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o com o servidor de acesso.
	 */

	public ServiceCharLogin getCharLogin()
	{
		return charLogin;
	}

	/**
	 * @return aquisi��o do servi�o para autentica��o dos jogadores no servidor.
	 */

	public ServiceCharServerAuth getCharServerAuth()
	{
		return charServerAuth;
	}


	/**
	 * @return aquisi��o do controle para autentica��o dos jogadores online.
	 */

	public AuthControl getAuthControl()
	{
		return authControl;
	}

	/**
	 * @return aquisi��o do controle para dados de personagens online.
	 */

	public OnlineCharControl getOnlineCharControl()
	{
		return onlineCharControl;
	}

	/**
	 * @return aquisi��o do controle dos dados b�sicos dos personagens.
	 */

	public CharacterControl getCharacterControl()
	{
		return characterControl;
	}

	/**
	 * @return aquisi��o do controle para n�veis de experi�ncia dos personagens.
	 */

	public ExperienceControl getExperienceControl()
	{
		return experienceControl;
	}

	/**
	 * @return aquisi��o do controle para rela��es familiares dos personagens.
	 */

	public FamilyControl getFamilyControl()
	{
		return familyControl;
	}

	/**
	 * @return aquisi��o do controle para localiza��es dos personagens.
	 */

	public LocationControl getLocationControl()
	{
		return locationControl;
	}

	/**
	 * @return aquisi��o do controle para apar�ncia dos personagens.
	 */

	public LookControl getLookControl()
	{
		return lookControl;
	}

	/**
	 * @return aquisi��o do controle para classifica��o dos assistentes.
	 */

	public MercenaryRankControl getMercenaryRankControl()
	{
		return mercenaryRankControl;
	}

	@Override
	public String getHost()
	{
		return getConfigs().getString(CHAR_IP);
	}

	@Override
	public int getPort()
	{
		return getConfigs().getInt(CHAR_PORT);
	}

	/**
	 * Listener que ir� executar as opera��es necess�rias conforme mudan�as de estado do servidor.
	 */

	private final ServerListener listener = new ServerListener()
	{
		@Override
		public void onCreate() throws RagnarokException
		{
			setDefaultConfigs();
		}

		/**
		 * M�todo que dever� criar as configura��es necess�rias para funcionamento do servidor.
		 * Por padr�o do sistema inicializa as configura��es com seus valores padr�es.
		 * Ap�s isso dever� vincular as configura��es carregadas as configura��es do servidor.
		 * @see JRagnarokConfigs
		 */

		private void setDefaultConfigs()
		{
			Configurations server = newCharServerConfigs();
	
			Configurations configs = getConfigs();

			if (configs == null)
				setConfigurations(configs = new Configurations());

			configs.add(server);
		}

		@Override
		public void onCreated() throws RagnarokException
		{
			experienceControl = new ExperienceControl(getMySQL().getConnection());
			familyControl = new FamilyControl(getMySQL().getConnection());
			locationControl = new LocationControl(getMySQL().getConnection());
			lookControl = new LookControl(getMySQL().getConnection());
			mercenaryRankControl = new MercenaryRankControl(getMySQL().getConnection());
			characterControl = new CharacterControl(getMySQL().getConnection());
			authControl = new AuthControl();
			onlineCharControl = new OnlineCharControl(getMySQL().getConnection());

			characterControl.setExperiences(experienceControl);
			characterControl.setFamilies(familyControl);
			characterControl.setLocations(locationControl);
			characterControl.setLooks(lookControl);
			characterControl.setRanks(mercenaryRankControl);

			charClient = new ServiceCharClient(CharServer.this);
			charServer = new ServiceCharServer(CharServer.this);
			charLogin = new ServiceCharLogin(CharServer.this);
			charServerAuth = new ServiceCharServerAuth(CharServer.this);

			charLogin.init();
			charClient.init();
			charServerAuth.init();

			setDefaultParser(charClient.parse);
		};

		@Override
		public void onRunning() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStop() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStoped() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDestroy() throws RagnarokException
		{
			charClient.destroy();
			charServerAuth.destroy();
			charLogin.destroy();
		}

		@Override
		public void onDestroyed() throws RagnarokException
		{
			experienceControl = null;
			familyControl = null;
			locationControl = null;
			lookControl = null;
			mercenaryRankControl = null;
			characterControl = null;
			authControl = null;
			onlineCharControl = null;

			charClient = null;
			charServer = null;
			charLogin = null;
			charServerAuth = null;
		}
	};
}

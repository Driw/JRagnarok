package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newCharServerConfigs;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.character.control.ExperienceControl;
import org.diverproject.jragnarok.server.character.control.FamilyControl;
import org.diverproject.jragnarok.server.character.control.LocationControl;
import org.diverproject.jragnarok.server.character.control.LookControl;
import org.diverproject.jragnarok.server.character.control.MercenaryRankControl;

/**
 * <h1>Servidor de Personagem</h1>
 *
 * <p>Este servidor será responsável por realizar o controle de todas as informações dos personagens.
 * Essas informações vão deste de identificações, nomes, níveis como relações entre os personagens:
 * amizades, grupos, clã e vínculos como mascotes, homúnculos, elementares e assistentes.</p>
 *
 * <p>O servidor de personagem também deverá possuir os serviços adequados para realizar seus deveres.
 * Para isso há um serviço de comunicação inicial com o cliente afim de receber dados e comandos.
 * Sempre que um novo servidor de personagem criado ele deverá ser autenticado no servidor de acesso.</p>
 *
 * <p>Para que ele esteja realmente funcionando deverá estar constantemente conectado com um
 * servidor de acesso especificado nas configurações, pois algumas informações serão obtidas dele.
 * Por exemplo, alguns dados da conta do jogador necessários serão solicitados do mesmo.</p>
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
	 * Serviço para comunicação inicial com o cliente.
	 */
	private ServiceCharClient charClient;

	/**
	 * Serviço principal do servidor de personagem.
	 */
	private ServiceCharServer charServer;

	/**
	 * Serviço para comunicação com o servidor de acesso.
	 */
	private ServiceCharLogin charLogin;


	/**
	 * Controle para gerenciar dados dos personagens.
	 */
	private CharacterControl characterControl;

	/**
	 * Controle para gerenciar os níveis de experiência dos personagens.
	 */
	private ExperienceControl experienceControl;

	/**
	 * Controle para gerenciar a relação familiar dos personagens.
	 */
	private FamilyControl familyControl;

	/**
	 * Controle para gerenciar as localizações como pontos de retorno dos personagens.
	 */
	private LocationControl locationControl;

	/**
	 * Controle para gerenciar a aparência de estilos e cores dos personagens.
	 */
	private LookControl lookControl;

	/**
	 * Controle para gerenciar a classificação no sistema de assistentes dos personagens.
	 */
	private MercenaryRankControl ranks;

	/**
	 * Cria uma nova instância de um servidor de personagem inicialização o seu listener.
	 */

	public CharServer()
	{
		setListener(listener);
	}

	/**
	 * @return aquisição do serviço para comunicação inicial com clientes.
	 */

	public ServiceCharClient getCharClient()
	{
		return charClient;
	}

	/**
	 * @return aquisição do serviço principal do servidor de personagem.
	 */

	public ServiceCharServer getCharServer()
	{
		return charServer;
	}

	/**
	 * @return aquisição do serviço para comunicação com o servidor de acesso.
	 */

	public ServiceCharLogin getCharLogin()
	{
		return charLogin;
	}

	/**
	 * @return aquisição do controle para níveis de experiência dos personagens.
	 */

	public ExperienceControl getExperiences()
	{
		return experienceControl;
	}

	/**
	 * @return aquisição do controle para relações familiares dos personagens.
	 */

	public FamilyControl getFamilies()
	{
		return familyControl;
	}

	/**
	 * @return aquisição do controle para localizações dos personagens.
	 */

	public LocationControl getLocations()
	{
		return locationControl;
	}

	/**
	 * @return aquisição do controle para aparência dos personagens.
	 */

	public LookControl getLooks()
	{
		return lookControl;
	}

	/**
	 * @return aquisição do controle para classificação dos assistentes.
	 */

	public MercenaryRankControl getRanks()
	{
		return ranks;
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
	 * Listener que irá executar as operações necessárias conforme mudanças de estado do servidor.
	 */

	private final ServerListener listener = new ServerListener()
	{
		@Override
		public void onCreate() throws RagnarokException
		{
			setDefaultConfigs();
		}

		/**
		 * Método que deverá criar as configurações necessárias para funcionamento do servidor.
		 * Por padrão do sistema inicializa as configurações com seus valores padrões.
		 * Após isso deverá vincular as configurações carregadas as configurações do servidor.
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
			ranks = new MercenaryRankControl(getMySQL().getConnection());
			characterControl = new CharacterControl(getMySQL().getConnection());

			characterControl.setExperiences(experienceControl);
			characterControl.setFamilies(familyControl);
			characterControl.setLocations(locationControl);
			characterControl.setLooks(lookControl);
			characterControl.setRanks(ranks);

			charClient = new ServiceCharClient(CharServer.this);
			charServer = new ServiceCharServer(CharServer.this);
			charLogin = new ServiceCharLogin(CharServer.this);

			charLogin.init();

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
			charLogin.destroy();
		}

		@Override
		public void onDestroyed() throws RagnarokException
		{
			experienceControl = null;
			familyControl = null;
			locationControl = null;
			lookControl = null;
			ranks = null;
			characterControl = null;

			charClient = null;
			charServer = null;
			charLogin = null;
		}
	};
}

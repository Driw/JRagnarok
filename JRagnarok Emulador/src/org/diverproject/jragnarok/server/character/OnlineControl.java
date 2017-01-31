package org.diverproject.jragnarok.server.character;

import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.util.Util.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Controle de jogadores online</h1>
 *
 * <p>Atualiza as informações da conta dos jogadores para deixá-los como online.
 * Desta forma é possível que sistemas de fora do servidor possam contar os jogadores online.
 * Sempre que o jogador entrar fica como online e quando sair será definido como offline.</p>
 *
 * @author Andrew
 */

public class OnlineControl extends AbstractControl implements Iterable<OnlineCharData>
{
	/**
	 * Mapeamento dos jogadores que se encontram online no sistema.
	 */
	private final Map<Integer, OnlineCharData> cache;

	/**
	 * Cria um novo controle que permite controlar os jogadores online no sistema.
	 * Para isso é necessário definir um mapeador de temporizadores já que os objetos
	 * que mantém jogadores como online utilizam temporizadores para tal.
	 */

	public OnlineControl(Connection connection)
	{
		super(connection);

		cache = new IntegerLittleMap<>();

		initAccountsOffline();
		initCharsOffline();
	}

	/**
	 * Chamado internamente e sua finalidade é atualizar o banco de dados das contas.
	 * Sua atualização consiste em definir todas as contas como offline no sistema.
	 * Assim, quando o servidor de personagem criar o controle todos estarão offline.
	 */

	private void initAccountsOffline()
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("UPDATE %s SET online = 0", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.executeUpdate();

		} catch (SQLException | RagnarokException e) {
			logError("falha ao deixar contas como offline:\n");
			logException(e);
		}		
	}

	/**
	 * Chamado internamente e sua finalidade é atualizar o banco de dados dos personagens.
	 * Sua atualização consiste em definir todos os personagens como offline no sistema.
	 * Assim, quando o servidor de personagem criar o controle todos estarão offline.
	 */

	private void initCharsOffline()
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET online = 0", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.executeUpdate();

		} catch (SQLException | RagnarokException e) {
			logError("falha ao deixar jogadores como offline:\n");
			logException(e);
		}		
	}

	/**
	 * Obtém informações de um determinado jogador online pelo seu ID.
	 * @param accountID código de identificação da conta do jogador desejado.
	 * @return aquisição do objeto com as informações do jogador online.
	 */

	public OnlineCharData get(int accountID)
	{
		return cache.get(accountID);
	}

	/**
	 * Solicita ao controle para criar um novo objeto para manter um jogador/conta online.
	 * @param accountID código de identificação da conta do qual quer tornar online.
	 * @return aquisição do objeto configurado com a conta que foi passada.
	 */

	public OnlineCharData newOnlineCharData(int accountID)
	{
		OnlineCharData online = new OnlineCharData();
		online.setAccountID(accountID);
		cache.add(accountID, online);

		return online;
	}

	/**
	 * Através das informações passadas irá atualizar o banco de dados podendo:
	 * definir uma conta e/ou personagem como online se houver uma identificação.
	 * Caso não haja uma identificação não será efetuada mudanças no banco de dados.
	 * @param online objeto com as informações do jogador online.
	 */

	public void makeOnline(OnlineCharData online)
	{
		if (online != null)
		{
			if (online.getAccountID() > 0)
				setAccountState(online, true);

			if (online.getCharID() > 0)
				setCharState(online, true);
		}
	}

	/**
	 * Através das informações passadas irá atualizar o banco de dados podendo:
	 * definir uma conta e/ou personagem como offline se houver uma identificação.
	 * Caso não haja uma identificação não será efetuada mudanças no banco de dados.
	 * @param online objeto com as informações do jogador online.
	 */

	public void remove(OnlineCharData online)
	{
		if (cache.removeKey(online.getAccountID()))
		{
			if (online.getAccountID() > 0)
				setAccountState(online, false);

			if (online.getCharID() > 0)
				setCharState(online, false);

			cache.removeKey(online.getAccountID());

			logDebug("account#%d removido do cache.\n", online.getAccountID());
		}
	}

	/**
	 * Procedimento interno que irá fazer a alteração direta no banco de dados (online/offline).
	 * Este método irá alterar o estado de uma conta especificada a seguir:
	 * @param onlineData dados do identificador online para alterar o estado da conta.
	 * @param online true para deixar online ou false caso seja para deixar offline.
	 * @return true se conseguir alterar o estado da conta ou false caso contrário.
	 */

	public boolean setAccountState(OnlineCharData onlineData, boolean online)
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("UPDATE %s SET online = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setBoolean(1, online);
			ps.setInt(2, onlineData.getAccountID());

			boolean result = ps.executeUpdate() == 1;

			if (result)
			{
				if (online)
					logDebug("account#%d está online.\n", onlineData.getAccountID());
				else
					logDebug("account#%d está offline.\n", onlineData.getAccountID());
			}

			return result;

		} catch (SQLException | RagnarokException e) {
			logError("falha ao mudar o estado da conta (aid: %d, online: %s)", onlineData.getAccountID(), online);
			logException(e);
		}

		return false;
	}

	/**
	 * Procedimento interno que irá fazer a alteração direta no banco de dados (online/offline).
	 * Este método irá alterar o estado de um personagem específico a seguir:
	 * @param onlineData dados do identificador online para alterar o estado do personagem.
	 * @param online true para deixar online ou false caso seja para deixar offline.
	 * @return true se conseguir alterar o estado da conta ou false caso contrário.
	 */

	public boolean setCharState(OnlineCharData onlineData, boolean online)
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET online = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setBoolean(1, online);
			ps.setInt(2, onlineData.getCharID());

			boolean result = ps.executeUpdate() == 1;

			if (result)
			{
				if (online)
					logDebug("char#%d está online.\n", onlineData.getCharID());
				else
					logDebug("char#%d está offline.\n", onlineData.getCharID());
			}

			return result;

		} catch (SQLException | RagnarokException e) {
			logError("falha ao mudar o estado do personagem (cid: %d, online: %s)", onlineData.getCharID(), online);
			logException(e);
		}

		return false;
	}

	/**
	 * Atualiza a informação de todos os jogadores e conta online para offline.
	 * Além disso remove todos os objetos contendo os dados de online do controle.
	 */

	public void clear()
	{
		for (OnlineCharData online : cache)
		{
			setAccountState(online, false);
			setCharState(online, false);
		}

		cache.clear();
	}

	@Override
	public Iterator<OnlineCharData> iterator()
	{
		return cache.iterator();
	}

	@Override
	public void toString(ObjectDescription description)
	{
		description.append("online", cache.size());
	}
}

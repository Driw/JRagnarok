package org.diverproject.jragnarok.server.character.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.character.structures.OnlineCharData;
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

public class OnlineCharControl extends AbstractControl
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

	public OnlineCharControl(Connection connection)
	{
		super(connection);

		this.cache = new IntegerLittleMap<>();

		String table = Tables.getInstance().getAccounts();
		String sql = format("UPDATE %s SET online = 0", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.executeUpdate();

		} catch (SQLException | RagnarokException e) {
			logError("falha ao deixar jogadores como offline:");
			logExeception(e);
		}
	}

	/**
	 * Obtém informações de um determinado jogador online pelo seu ID.
	 * @param id código de identificação da conta do jogador desejado.
	 * @return aquisição do objeto com as informações do jogador online.
	 */

	public OnlineCharData get(int id)
	{
		return cache.get(id);
	}

	/**
	 * Adiciona um novo jogador como online através das informações abaixo:
	 * @param online informações referentes ao jogador online.
	 */

	public void add(OnlineCharData online)
	{
		cache.add(online.getAccountID(), online);

		try{

			setAccountOnline(online.getAccountID(), true);
			setCharOnline(online.getCharID(), true);

		} catch (RagnarokException e) {
			logExeception(e);
		}

		logDebug("account#%d está online.\n", online.getAccountID());
	}

	/**
	 * Atualiza as informações de uma conta para tornar uma conta online ou offline.
	 * @param online informações referentes ao jogador online a ser removido.
	 */

	public void remove(OnlineCharData online)
	{
		try {

			if (online.getCharID() == 0)
				setAccountOnline(online.getAccountID(), false);
			else
				setCharOnline(online.getCharID(), false);

		} catch (RagnarokException e) {
			logExeception(e);
		}

		cache.removeKey(online.getAccountID());

		logDebug("account#%d não está mais online.\n", online.getAccountID());
	}

	/**
	 * Procedimento interno que irá fazer a alteração direta no banco de dados (online/offline).
	 * Este método irá alterar o estado de uma conta especificada a seguir:
	 * @param accountID código da conta que terá o estado alterado em online ou offline.
	 * @param online true para deixar online ou false caso seja para deixar offline.
	 * @return true se conseguir alterar o estado da conta ou false caso contrário.
	 * @throws RagnarokException apenas se houver falha na conexão.
	 */

	private boolean setAccountOnline(int accountID, boolean online) throws RagnarokException
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("UPDATE %s SET online = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setBoolean(1, online);
			ps.setInt(2, accountID);

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Procedimento interno que irá fazer a alteração direta no banco de dados (online/offline).
	 * Este método irá alterar o estado de um personagem específico a seguir:
	 * @param charID código da conta que terá o estado alterado em online ou offline.
	 * @param online true para deixar online ou false caso seja para deixar offline.
	 * @return true se conseguir alterar o estado da conta ou false caso contrário.
	 * @throws RagnarokException apenas se houver falha na conexão.
	 */

	private boolean setCharOnline(int charID, boolean online) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET online = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setBoolean(1, online);
			ps.setInt(2, charID);

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}		
	}

	/**
	 * Atualiza a informação de todos os jogadores e conta online para offline.
	 * Além disso remove todos os objetos contendo os dados de online do controle.
	 */

	public void clear()
	{
		for (OnlineCharData online : cache)
			try {
				setCharOnline(online.getCharID(), false);
			} catch (RagnarokException e) {
				logExeception(e);
			}

		cache.clear();
	}

	@Override
	public void toString(ObjectDescription description)
	{
		description.append("online", cache.size());
	}
}

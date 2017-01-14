package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;

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
 * <p>Atualiza as informa��es da conta dos jogadores para deix�-los como online.
 * Desta forma � poss�vel que sistemas de fora do servidor possam contar os jogadores online.
 * Sempre que o jogador entrar fica como online e quando sair ser� definido como offline.</p>
 *
 * @author Andrew
 */

public class OnlineMap extends AbstractControl implements Iterable<OnlineCharData>
{
	/**
	 * Mapeamento dos jogadores que se encontram online no sistema.
	 */
	private final Map<Integer, OnlineCharData> cache;

	/**
	 * Cria um novo controle que permite controlar os jogadores online no sistema.
	 * Para isso � necess�rio definir um mapeador de temporizadores j� que os objetos
	 * que mant�m jogadores como online utilizam temporizadores para tal.
	 */

	public OnlineMap(Connection connection)
	{
		super(connection);

		this.cache = new IntegerLittleMap<>();

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
	 * Obt�m informa��es de um determinado jogador online pelo seu ID.
	 * @param id c�digo de identifica��o da conta do jogador desejado.
	 * @return aquisi��o do objeto com as informa��es do jogador online.
	 */

	public OnlineCharData get(int id)
	{
		return cache.get(id);
	}

	/**
	 * Adiciona um novo jogador como online atrav�s das informa��es abaixo:
	 * @param online informa��es referentes ao jogador online.
	 */

	public void add(OnlineCharData online)
	{
		cache.add(online.getAccountID(), online);

		logDebug("account#%d adicionado ao cache.\n", online.getAccountID());
	}

	/**
	 * Torna um determinado jogador como online mesmo que n�o esteja em cache.
	 * Caso n�o esteja em cache tamb�m ser� adicionado conforme informa��es abaixo:
	 * @param online informa��es referentes ao jogador online.
	 */

	public void makeOnline(OnlineCharData online)
	{
		if (!cache.containsKey(online.getAccountID()))
			add(online);

		try{

			setAccountOnline(online.getAccountID(), true);
			setCharOnline(online.getCharID(), true);

		} catch (RagnarokException e) {
			logException(e);
		}
	}

	/**
	 * Atualiza as informa��es de uma conta para tornar uma conta online ou offline.
	 * @param online informa��es referentes ao jogador online a ser removido.
	 */

	public void remove(OnlineCharData online)
	{
		try {

			if (online.getCharID() == 0)
				setAccountOnline(online.getAccountID(), false);
			else
				setCharOnline(online.getCharID(), false);

		} catch (RagnarokException e) {
			logException(e);
		}

		cache.removeKey(online.getAccountID());

		logDebug("account#%d removido do cache.\n", online.getAccountID());
	}

	/**
	 * Procedimento interno que ir� fazer a altera��o direta no banco de dados (online/offline).
	 * Este m�todo ir� alterar o estado de uma conta especificada a seguir:
	 * @param accountID c�digo da conta que ter� o estado alterado em online ou offline.
	 * @param online true para deixar online ou false caso seja para deixar offline.
	 * @return true se conseguir alterar o estado da conta ou false caso contr�rio.
	 * @throws RagnarokException apenas se houver falha na conex�o.
	 */

	private boolean setAccountOnline(int accountID, boolean online) throws RagnarokException
	{
		String table = Tables.getInstance().getAccounts();
		String sql = format("UPDATE %s SET online = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setBoolean(1, online);
			ps.setInt(2, accountID);

			boolean result = ps.executeUpdate() == 1;

			if (result)
			{
				if (online)
					logDebug("account#%d est� online.\n", accountID);
				else
					logDebug("account#%d est� offline.\n", accountID);
			}

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Procedimento interno que ir� fazer a altera��o direta no banco de dados (online/offline).
	 * Este m�todo ir� alterar o estado de um personagem espec�fico a seguir:
	 * @param charID c�digo da conta que ter� o estado alterado em online ou offline.
	 * @param online true para deixar online ou false caso seja para deixar offline.
	 * @return true se conseguir alterar o estado da conta ou false caso contr�rio.
	 * @throws RagnarokException apenas se houver falha na conex�o.
	 */

	private boolean setCharOnline(int charID, boolean online) throws RagnarokException
	{
		String table = Tables.getInstance().getCharacters();
		String sql = format("UPDATE %s SET online = ? WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setBoolean(1, online);
			ps.setInt(2, charID);

			boolean result = ps.executeUpdate() == 1;

			if (result)
			{
				if (online)
					logDebug("char#%d est� online.\n", charID);
				else
					logDebug("char#%d est� offline.\n", charID);
			}

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}		
	}

	/**
	 * Efetua uma limpeza deixando jogadores offline em servidores desconhecidos,
	 * como tamb�m remover informa��es dos jogadores que n�o estejam em um servidor.
	 */

	public void cleanup()
	{
		for (OnlineCharData online : cache)
		{
			if (online.getFileDescriptor() == null)
			{
				cache.remove(online);
				continue;
			}

			if (!online.getFileDescriptor().isConnected() || online.getServer() == OnlineCharData.UNKNOW_SERVER)
				try {
					setCharOnline(online.getCharID(), false);
				} catch (RagnarokException e) {
					logException(e);
				}

			if (online.getServer() < 0)
				cache.remove(online);
		}

		logDebug("%d jogadores online.\n", cache.size());
	}

	/**
	 * Atualiza a informa��o de todos os jogadores e conta online para offline.
	 * Al�m disso remove todos os objetos contendo os dados de online do controle.
	 */

	public void clear()
	{
		for (OnlineCharData online : cache)
			try {
				setCharOnline(online.getCharID(), false);
			} catch (RagnarokException e) {
				logException(e);
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

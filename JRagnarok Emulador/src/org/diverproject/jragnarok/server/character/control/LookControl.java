package org.diverproject.jragnarok.server.character.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.log.LogSystem.logDebug;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.character.structures.Look;

/**
 * <h1>Controle para Apar�ncia de Personagens</h1>
 *
 * <p>Para a tabela de apar�ncia de personagens � armazenados dados do visual do jogador (cores/apar�ncia).
 * As informa��es consistem determinar o tipo de visual como estilo do cabelo, corpo e equipamentos
 * (arma, escudo e equipamentos para cabe�a: topo, meio, baixo), cor do cabelo e cores da roupa.</p>
 *
 * @see AbstractControl
 * @see Connection
 * @see Look
 *
 * @author Andrew
 */

public class LookControl extends AbstractControl
{
	/**
	 * Cria uma nova inst�ncia de um controle para gerenciamento da apar�ncia dos personagens.
	 * @param connection refer�ncia do objeto contendo a conex�o com o banco de dados.
	 */

	public LookControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Procedimento interno que garante a validade do objeto controlado por este controle.
	 * @param rank refer�ncia do objeto que ser� validado pelo controle.
	 * @throws RagnarokException pode ocorrer apenas se o objeto passado for nulo
	 * ou ent�o se o c�digo de identifica��o for inv�lido (menor que um | <1).
	 */

	private void validate(Look rank) throws RagnarokException
	{
		if (rank == null)
			throw new RagnarokException("classifica��o nula");

		if (rank.getID() < 1)
			throw new RagnarokException("classificaia��o inv�lida");
	}

	/**
	 * Permite obter as informa��es da apar�ncia de um determinado personagem.
	 * @param id c�digo de identifica��o do personagem que deseja a apar�ncia.
	 * @return aquisi��o do objeto contendo as informa��es de apar�ncia
	 * ou no caso do c�digo de identifica��o do personagem ser inv�lido: null.
	 * @throws RagnarokException falha de conex�o com o banco de dados.
	 */

	public Look get(int id) throws RagnarokException
	{
		Look look = null;

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT hair, hair_color, clothes_color, body, weapon, shield, "
						+	"head_top, head_mid, head_bottom, robe FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				look = new Look();
				look.setID(id);
				look.setHair(rs.getShort("hair"));
				look.setHairColor(rs.getShort("hair_color"));
				look.setClothesColor(rs.getShort("clothes_color"));
				look.setBody(rs.getShort("body"));
				look.setWeapon(rs.getShort("weapon"));
				look.setShield(rs.getShort("shield"));
				look.setHeadTop(rs.getShort("head_top"));
				look.setHeadMid(rs.getShort("head_mid"));
				look.setHeadBottom(rs.getShort("head_bottom"));
				look.setRobe(rs.getShort("robe"));

				logDebug("Look#%d carregado.\n", look.getID());
			}

			return look;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona informa��es referentes a apar�ncia de um personagem.
	 * @param look objeto contendo informa��es do personagem � adicionar.
	 * @return true se conseguir adicionar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou j� existe.
	 */

	public boolean add(Look look) throws RagnarokException
	{
		validate(look);

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("INSERT INTO %s (hair, hair_color, clothes_color, body, weapon, shield, "
						+	"head_top, head_mid, head_bottom, robe) "
						+	"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setShort(1, look.getHair());
			ps.setShort(2, look.getHairColor());
			ps.setShort(3, look.getClothesColor());
			ps.setShort(4, look.getBody());
			ps.setShort(5, look.getWeapon());
			ps.setShort(6, look.getShield());
			ps.setShort(7, look.getHeadTop());
			ps.setShort(8, look.getHeadMid());
			ps.setShort(9, look.getHeadBottom());
			ps.setShort(10, look.getRobe());
			ps.setInt(11, look.getID());

			boolean result = ps.executeUpdate() == 1;

			if (result)
				logDebug("Look#%d adicionado.\n", look.getID());

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as informa��es referentes a apar�ncia de um personagem.
	 * @param look objeto contendo informa��es do personagem � atualizar.
	 * @return true se conseguir atualizar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
	 */

	public boolean set(Look look) throws RagnarokException
	{
		validate(look);

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("UPDATE %s SET hair = ?, hair_color = ?, clothes_color = ?, body = ?, weapon = ?, "
						+	"shield = ?, head_top = ?, head_mid = ?, head_bottom = ?, robe = ? "
						+	"WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setShort(1, look.getHair());
			ps.setShort(2, look.getHairColor());
			ps.setShort(3, look.getClothesColor());
			ps.setShort(4, look.getBody());
			ps.setShort(5, look.getWeapon());
			ps.setShort(6, look.getShield());
			ps.setShort(7, look.getHeadTop());
			ps.setShort(8, look.getHeadMid());
			ps.setShort(9, look.getHeadBottom());
			ps.setShort(10, look.getRobe());
			ps.setInt(11, look.getID());

			boolean result = ps.executeUpdate() == 1;

			if (result)
				logDebug("Look#%d atualizado.\n", look.getID());

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Remove as informa��es referentes a apar�ncia de um personagem.
	 * @param look objeto contendo informa��es do personagem � remover.
	 * @return true se conseguir remover ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
	 */

	public boolean remove(Look look) throws RagnarokException
	{
		validate(look);

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("DELETE FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, look.getID());

			boolean result = ps.executeUpdate() == 1;

			if (result)
				logDebug("Look#%d removido.\n", look.getID());

			return result;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Recarrega as informa��es de uma determinada apar�ncia conforme o banco de dados.
	 * Esse procedimento pode ser utilizado quando o banco de dados for alterador por fora,
	 * ou seja, quando houve mudan�a nos dados por�m n�o vieram do servidor desse controle.
	 * @param look apar�ncia do personagem do qual ter� os dados recarregados.
	 * @return true se tiver recarregado ou false caso n�o encontre a classifica��o.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
	 */

	public boolean reload(Look look) throws RagnarokException
	{
		validate(look);

		String table = Tables.getInstance().getCharMercenaryRank();
		String sql = format("SELECT archer_faith, archer_calls, spear_faith, "
				+	"spear_calls, sword_faith, sword_calls FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, look.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				look.setHair(rs.getShort("hair"));
				look.setHairColor(rs.getShort("hair_color"));
				look.setClothesColor(rs.getShort("clothes_color"));
				look.setBody(rs.getShort("body"));
				look.setWeapon(rs.getShort("weapon"));
				look.setShield(rs.getShort("shield"));
				look.setHeadTop(rs.getShort("head_top"));
				look.setHeadMid(rs.getShort("head_mid"));
				look.setHeadBottom(rs.getShort("head_bottom"));
				look.setRobe(rs.getShort("robe"));

				logDebug("Look#%d recarregado.\n", look.getID());

				return true;
			}

			return false;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

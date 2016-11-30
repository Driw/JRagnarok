package org.diverproject.jragnarok.server.character.control;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MEMOPOINTS;
import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.character.structures.Locations;

/**
 * <h1>Controle para Localiza��o dos Personagens</h1>
 *
 * <p>Para a tabela de localiza��es � armazenado valores referentes ao pontos espec�ficos em mapas.
 * Cada ponto indica uma localiza��o salve, cada personagem tem ao menos duas localiza��es.</p>
 *
 * <p>A primeira localiza��o � a do pr�prio personagem e a segunda para o ponto de retorno.
 * O ponto de retorno � usado em caso de morte ou uso de itens que retorna ao ponto principal.
 * Os outros pontos dependem de <code>MAX_MEMOPOINTS</code> e s�o memoriz�veis para habilidades.</p>
 *
 * @see AbstractControl
 * @see Connection
 * @see Locations
 *
 * @author Andrew
 */

public class LocationControl extends AbstractControl
{
	/**
	 * Cria uma nova inst�ncia de um controle para gerenciamento das localiza��es.
	 * @param connection refer�ncia do objeto contendo a conex�o com o banco de dados.
	 */

	public LocationControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Procedimento interno que garante a validade do objeto controlado por este controle.
	 * @param locations refer�ncia do objeto que ser� validado pelo controle.
	 * @throws RagnarokException pode ocorrer apenas se o objeto passado for nulo
	 * ou ent�o se o c�digo de identifica��o for inv�lido (menor que um | <1).
	 */

	private void validate(Locations locations) throws RagnarokException
	{
		if (locations == null)
			throw new RagnarokException("localiza��o nula");

		if (locations.getID() == 0 ||
			locations.getLastPoint().isNull() ||
			locations.getSavePoint().isNull())
			throw new RagnarokException("localiza��o inv�lida");
	}

	/**
	 * Permite obter as informa��es de localiza��es obtidas de um determinado personagem.
	 * @param id c�digo de identifica��o do personagem que deseja as localiza��es.
	 * @return aquisi��o do objeto contendo as informa��es de localiza��es do personagem
	 * ou no caso do c�digo de identifica��o do personagem ser inv�lido: null.
	 * @throws RagnarokException falha de conex�o com o banco de dados.
	 */

	public Locations get(int id) throws RagnarokException
	{
		String table = Tables.getInstance().getCharLocations();
		String sql = format("SELECT mapname, coord_x, coord_y FROM %s WHERE charid = ? ORDER BY num", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				return null;

			Locations locations = new Locations();
			locations.getLastPoint().setMap(rs.getString("mapname"));
			locations.getLastPoint().setX(rs.getInt("coord_x"));
			locations.getLastPoint().setY(rs.getInt("coord_y"));

			if (rs.next())
			{
				locations.getSavePoint().setMap(rs.getString("mapname"));
				locations.getLastPoint().setX(rs.getInt("coord_x"));
				locations.getLastPoint().setY(rs.getInt("coord_y"));

				for (int i = 0; rs.next() && i < MAX_MEMOPOINTS; i++)
				{
					locations.getMemoPoint(i).setMap(rs.getString("mapname"));
					locations.getMemoPoint(i).setX(rs.getInt("coord_x"));
					locations.getMemoPoint(i).setY(rs.getInt("coord_y"));
				}
			}

			return locations;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Adiciona informa��es referentes das localiza��es salvas de um personagem.
	 * @param locations objeto contendo informa��es do personagem � adicionar.
	 * @return true se conseguir adicionar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou j� existe.
	 */

	public boolean add(Locations locations) throws RagnarokException
	{
		validate(locations);

		String table = Tables.getInstance().getCharLocations();
		String sql = format("INSERT INTO %s (charid, num, mapname, coord_x, coor_y) VALUES", table);

		sql += " (?, ?, ?, ?, ?)"; // last point
		sql += " (?, ?, ?, ?, ?)"; // save point

		for (int i = 0; i < MAX_MEMOPOINTS + 2; i++)
			sql += " (?, ?, ?, ?, ?)";

		try {

			PreparedStatement ps = prepare(sql);

			ps.setInt(1, locations.getID());
			ps.setInt(2, 1);
			ps.setString(3, locations.getLastPoint().getMap());
			ps.setInt(4, locations.getLastPoint().getX());
			ps.setInt(5, locations.getLastPoint().getY());

			ps.setInt(6, locations.getID());
			ps.setInt(7, 2);
			ps.setString(8, locations.getSavePoint().getMap());
			ps.setInt(9, locations.getSavePoint().getX());
			ps.setInt(10, locations.getSavePoint().getX());

			for (int i = 0; i < MAX_MEMOPOINTS; i++)
			{
				ps.setInt(11 + (i * 5), locations.getID());
				ps.setInt(12 + (i * 5), 3 + i);
				ps.setString(13 + (i * 5), locations.getMemoPoint(i).getMap());
				ps.setInt(14 + (i * 5), locations.getMemoPoint(i).getX());
				ps.setInt(15 + (i * 5), locations.getMemoPoint(i).getY());
			}

			return ps.executeUpdate() == 2 + MAX_MEMOPOINTS;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Atualiza as informa��es referentes as localiza��es salvas de um personagem.
	 * @param locations objeto contendo informa��es do personagem � atualizar.
	 * @return true se conseguir atualizar ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
	 */

	public boolean set(Locations locations) throws RagnarokException
	{
		validate(locations);

		String table = Tables.getInstance().getCharLocations();
		String sql = format("UPDATE %s SET mapname = ?, coord_x = ?, coor_y = ? WHERE charid = ? AND num + ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, locations.getLastPoint().getMap());
			ps.setInt(2, locations.getLastPoint().getX());
			ps.setInt(3, locations.getLastPoint().getY());
			ps.setInt(4, locations.getID());
			ps.setInt(5, 1);

			if (ps.executeUpdate() == 0)
				throw new RagnarokException("localiza��o %d:1 n�o encontrada", locations.getID());

			ps = prepare(sql);
			ps.setString(1, locations.getSavePoint().getMap());
			ps.setInt(2, locations.getSavePoint().getX());
			ps.setInt(3, locations.getSavePoint().getY());
			ps.setInt(4, locations.getID());
			ps.setInt(5, 1);

			if (ps.executeUpdate() == 0)
				throw new RagnarokException("localiza��o %d:2 n�o encontrada", locations.getID());

			for (int i = 0; i < MAX_MEMOPOINTS; i++)
			{
				ps = prepare(sql);
				ps.setString(1, locations.getMemoPoint(i).getMap());
				ps.setInt(2, locations.getMemoPoint(i).getX());
				ps.setInt(3, locations.getMemoPoint(i).getY());
				ps.setInt(4, locations.getID());
				ps.setInt(5, 3+i);

				if (ps.executeUpdate() == 0)
					throw new RagnarokException("localiza��o %d:%d n�o encontrada", locations.getID(), 3+i);
			}

			return true;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Remove as informa��es referentes as localiza��es salvas de um personagem.
	 * @param locations objeto contendo informa��es do personagem � remover.
	 * @return true se conseguir remover ou false caso contr�rio.
	 * @throws RagnarokException falha de conex�o com o banco de dados,
	 * objeto passado sem c�digo de identifica��o v�lido ou n�o existe.
	 */

	public boolean remove(Locations locations) throws RagnarokException
	{
		validate(locations);

		String table = Tables.getInstance().getCharLocations();
		String sql = format("DELETE FROM %s WHERE charid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, locations.getID());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

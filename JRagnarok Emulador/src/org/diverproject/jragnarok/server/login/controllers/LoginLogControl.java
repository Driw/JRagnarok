package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractDAO;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.LoginLog;
import org.diverproject.util.SocketUtil;

/**
 * <h1>Controlador para Registro de Acesso</h1>
 *
 * <p>Esse controlador permite trabalhar com os objetos para Registro de Acessos.
 * Por ser registros no lado do servidor será interessante a quantidade e inserção.
 * Para que esse seja criado é necessário definir uma conexão através do construtor.</p>
 *
 * @see AbstractDAO
 * @see LoginLog
 *
 * @author Andrew
 */

public class LoginLogControl extends AbstractDAO
{
	/**
	 * Cria um novo controlador para trabalhar com os registros de acesso no servidor.
	 * Necessário a existência de uma conexão com o banco de dados válida e ativa.
	 * @param connection referência do objeto contendo a conexão com o banco de dados.
	 */

	public LoginLogControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Método usado para saber quantas tentativas de acesso sem sucesso foram realizadas.
	 * Irá considerar um endereço de IP para filtrar os acessos e o tempo de intervalo.
	 * @param ip string com o endereço de IP da onde deseja obter a quantidade de falhas.
	 * @param minutes tempo limite em minutos para considerar a contagem das filhas.
	 * @return aquisição da quantidade de tentativas de acessos falhos.
	 * @throws RagnarokException parâmetros inválidos, falha de conexão ou sem resultados.
	 */

	public int getFailedAttempts(String ip, int minutes) throws RagnarokException
	{
		if (ip == null)
			throw new RagnarokException("endereço de IP nulo");

		if (!SocketUtil.isIP(ip))
			throw new RagnarokException("endereço de IP inválido");

		if (minutes < 1)
			throw new RagnarokException("minutos inválido");

		String table = Tables.getInstance().getLoginLog();
		String sql = format("SELECT COUNT(*) AS count"
						+ " FROM %s"
						+ " WHERE ip = ? AND (rcode = 0 OR rcode = 1) AND time > (NOW() - INTERVAL ? MINUTE)",
						table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, ip);
			ps.setInt(2, minutes);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return rs.getInt("count");

			throw new RagnarokException("resultado não encontrado");

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Insere um novo registro de acesso em uma tabela no banco de dados.
	 * @param log referência do objeto de registro de acesso a inserir.
	 * @return true se tiver conseguido inserir ou false caso contrário.
	 * @throws RagnarokException parâmetro inválido ou falha de conexão.
	 * @see Tables
	 */

	public boolean insert(LoginLog log) throws RagnarokException
	{
		if (log == null)
			throw new RagnarokException("registro nulo");

		if (log.getLogin() == null)
			throw new RagnarokException("registro sem acesso");

		String table = Tables.getInstance().getLoginLog();
		String sql = format("INSERT INTO %s (time, ip, account, rcode, message) VALUES (?, ?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setTimestamp(1, log.getTime().toTimestamp());
			ps.setInt(2, log.getIP().get());
			ps.setInt(3, log.getLogin().getID());
			ps.setInt(4, log.getRCode());
			ps.setString(5, log.getMessage());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

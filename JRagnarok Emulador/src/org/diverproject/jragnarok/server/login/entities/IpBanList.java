package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;
import org.diverproject.util.lang.Bits;
import org.diverproject.util.lang.IntUtil;

/**
 * <h1>Lista para Ban de Endere�os IP</h1>
 *
 * <p>Objeto que cont�m as informa��es para banimento de determinados endere�os de IP.
 * Consiste em definir um endere�o ou conjunto de endere�os IP que ser�o banidos.
 * Como tamb�m definir quando e at� quando ficaram banidos tal como a raz�o do mesmo.</p>
 *
 * @author Andrew
 */

public class IpBanList
{
	/**
	 * C�digo de identifica��o da lista de endere�os de IP banidos.
	 */
	private int id;

	/**
	 * Lista contendo o endere�o de IP ou conjunto de IPs banidos.
	 */
	private String addressList;

	/**
	 * Quando o banimento foi realizado.
	 */
	private Time banTime;

	/**
	 * Quando o banimento ser� desfeito.
	 */
	private Time resumeTime;

	/**
	 * Raz�o do qual o banimento foi realizado.
	 */
	private String reason;

	/**
	 * @return aquisi��o do c�digo de identifica��o da lista de endere�os de IP banidos.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id c�digo de identifica��o da lista de endere�os de IP banidos.
	 */

	public void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisi��o do endere�os de IP a ser banidos.
	 */

	public String getAdressList()
	{
		return addressList;
	}

	/**
	 * @param addressList endere�os de IP a ser banidos.
	 */

	public void setAddressList(String addressList)
	{
		this.addressList = addressList;
	}

	/**
	 * @return aquisi��o do hor�rio em que o banimento foi feito.
	 */

	public Time getBanTime()
	{
		return banTime;
	}

	/**
	 * @return aquisi��o do hor�rio em que o banimento ser� desfeito.
	 */

	public Time getResumeTime()
	{
		return resumeTime;
	}

	/**
	 * @return aquisi��o da raz�o pelo qual o banimento foi feito.
	 */

	public String getReason()
	{
		return reason;
	}

	/**
	 * @param reason raz�o pelo qual o banimento foi feito.
	 */

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	/**
	 * Verifica se a lista de endere�os IP definido inclui um determinado endere�o IP.
	 * @param ip endere�o IP do qual deseja verificar se est� definido como banido.
	 * @return true se estiver contido nessa lista de endere�os ou false caso contr�rio.
	 */

	public boolean contains(int ip)
	{
		int a = IntUtil.parseByte(Bits.byteOf(ip, 4));
		int b = IntUtil.parseByte(Bits.byteOf(ip, 3));
		int c = IntUtil.parseByte(Bits.byteOf(ip, 2));
		int d = IntUtil.parseByte(Bits.byteOf(ip, 1));

		return	addressList.equals(format("%d.*.*.*", a)) ||
				addressList.equals(format("%d.%d.*.*", a, b)) ||
				addressList.equals(format("%d.%d.%d.*", a, b, c)) ||
				addressList.equals(format("%d.%d.%d.%d", a, b, c, d));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof IpBanList)
		{
			IpBanList list = (IpBanList) obj;

			return list.addressList.equals(addressList);
		}

		return false;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("list", addressList);
		description.append("banTime", banTime);
		description.append("resumeTime", resumeTime);
		description.append("reason", reason);

		return description.toString();
	}
}

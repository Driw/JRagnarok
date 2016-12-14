package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;
import org.diverproject.util.lang.Bits;
import org.diverproject.util.lang.IntUtil;

/**
 * <h1>Lista para Ban de Endereços IP</h1>
 *
 * <p>Objeto que contém as informações para banimento de determinados endereços de IP.
 * Consiste em definir um endereço ou conjunto de endereços IP que serão banidos.
 * Como também definir quando e até quando ficaram banidos tal como a razão do mesmo.</p>
 *
 * @author Andrew
 */

public class IpBanList
{
	/**
	 * Código de identificação da lista de endereços de IP banidos.
	 */
	private int id;

	/**
	 * Lista contendo o endereço de IP ou conjunto de IPs banidos.
	 */
	private String addressList;

	/**
	 * Quando o banimento foi realizado.
	 */
	private Time banTime;

	/**
	 * Quando o banimento será desfeito.
	 */
	private Time resumeTime;

	/**
	 * Razão do qual o banimento foi realizado.
	 */
	private String reason;

	/**
	 * @return aquisição do código de identificação da lista de endereços de IP banidos.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id código de identificação da lista de endereços de IP banidos.
	 */

	public void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisição do endereços de IP a ser banidos.
	 */

	public String getAdressList()
	{
		return addressList;
	}

	/**
	 * @param addressList endereços de IP a ser banidos.
	 */

	public void setAddressList(String addressList)
	{
		this.addressList = addressList;
	}

	/**
	 * @return aquisição do horário em que o banimento foi feito.
	 */

	public Time getBanTime()
	{
		return banTime;
	}

	/**
	 * @return aquisição do horário em que o banimento será desfeito.
	 */

	public Time getResumeTime()
	{
		return resumeTime;
	}

	/**
	 * @return aquisição da razão pelo qual o banimento foi feito.
	 */

	public String getReason()
	{
		return reason;
	}

	/**
	 * @param reason razão pelo qual o banimento foi feito.
	 */

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	/**
	 * Verifica se a lista de endereços IP definido inclui um determinado endereço IP.
	 * @param ip endereço IP do qual deseja verificar se está definido como banido.
	 * @return true se estiver contido nessa lista de endereços ou false caso contrário.
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

package org.diverproject.jragnarok;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.log.LogSystem.setUpSource;
import static org.diverproject.util.Util.random;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.lang.IntUtil.interval;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnaork.messages.Messages;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.ServerThreaed;
import org.diverproject.jragnarok.server.character.CharServer;
import org.diverproject.jragnarok.server.map.MapServer;
import org.diverproject.util.SizeUtil;
import org.diverproject.util.SystemUtil;
import org.diverproject.util.lang.StringUtil;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.StreamRuntimeException;

/**
 * <h1>Utilit�rios JRagnarok</h1>
 *
 * <p>Classe usada para definir m�todos utilit�rios em todo o projeto o Emulador.
 * S�o conjuntos de c�digos que s�o usados diversas vezes, por isso define um m�todo.
 * Essa classe deve ser importada como favorita para realizar os imports est�ticos.</p>
 *
 * @author Andrew
 */

public class JRagnarokUtil
{
	/**
	 * Vetor contendo a data de todas as vers�es de clientes (pacotes) dispon�veis.
	 */
	private static final int PACKETS_VER[] = new int[]
	{
		       0,        0,        0,        0, 20040906,        0,        0,        0,        0, 20040920, // 10
		20041005, 20041025, 20041129, 20050110, 20050509, 20050628, 20050718, 20050719, 20060327, 20070108, // 20
		20070212, 20080910, 20080827, 20080910, 20101124, 20111005, 20111102, 20120307, 20120410, 20120418, // 30
		20120618, 20120702, 20130320, 20130515, 20130522, 20130529, 20130605, 20130612, 20130618, 20130626, // 40
		20130703, 20130710, 20130717, 20130807, 20131223, 20140212, 20140613, 20141016,        0, 20141022, // 50
		20150513, 20150916, 20151001, 20151104, 20151104 //55
	};

	/**
	 * Construtor privado pois n�o ser� necess�rio outras inst�ncias dessa classe.
	 */

	private JRagnarokUtil()
	{
		
	}

	/**
	 * Chamado para realizar uma pausa (dormir) na Thread em que a chamar.
	 * O tempo da pausa ser� definido atrav�s do valor passado por par�metro.
	 * Esse m�todo n�o necessita a realiza��o do try catch como de costume.
	 * @param mileseconds tempo da dura��o da pausa em milissegundos.
	 */

	public static void sleep(long mileseconds)
	{
		if (mileseconds < 1)
			return;

		try {
			Thread.sleep(mileseconds);
		} catch (InterruptedException e) {
			setUpSource(1);
			logException(e);
		}
	}

	/**
	 * Realiza um chamado for�ado do Garbage Collector do java para libera��o de mem�ria.
	 * Al�m disso ir� registrar no console o valor aproximado do espa�o que foi liberado.
	 */

	public static void free()
	{
		long totalFreeMemory = SystemUtil.getTotalFreeMemory();

		System.gc();

		long newTotalFreeMemory = SystemUtil.getFreeMemory();
		long freeMemory = newTotalFreeMemory - totalFreeMemory;

		setUpSource(1);

		log("%s liberado pelo GC.\n", SizeUtil.toString(freeMemory));
	}

	/**
	 * Permite encriptar o valor de uma determinada string no formato MD5.
	 * @param string refer�ncia da string contendo o valor a ser encriptado.
	 * @return aquisi��o de uma string com o valor hash do MD5.
	 */

	public static String md5Encrypt(String string)
	{
		try {

			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte encrypted[] = messageDigest.digest(string.getBytes());
			String md5String =  new String(encrypted);

			return md5String;

		} catch (NoSuchAlgorithmException e) {

			setUpSource(1);
			logException(e);

			throw new RagnarokRuntimeException(e.getMessage());
		}
	}

	/**
	 * Cria um novo valor MD5 aleat�rio com um valor de caracteres especificados.
	 * @param length quantidade de caracteres que esse valor MD5 deve possuir.
	 * @return aquisi��o de uma string com o valor hash do MD5 gerado.
	 */

	public static String md5Salt(int length)
	{
		byte bytes[] = new byte[length];

		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) (1 + random() % 255);

		return new String(bytes);
	}

	/**
	 * Converte o valor bin�rio de uma string para hexadecimal.
	 * @param string string que ter� ao seu valor convertido.
	 * @param count quantidade de caracteres a considerar.
	 * @return aquisi��o de uma string com o valor hexadecimal.
	 */

	public static String binToHex(String string, int count)
	{
		String output = "";
		String toHex = "0123456789ABCDEF";

		for (int i = 0; i < count && i < string.length(); i++)
		{
			output += toHex.charAt(string.charAt(i) & 0xF0 >> 4);
			output += toHex.charAt(string.charAt(i) & 0x0F >> 0);
		}

		return output;
	}

	/**
	 * Verifica se um determinado endere�o de e-mail passado possui um formato v�lido.
	 * @param email endere�o de e-mail do qual deseja verificar a validez do formato.
	 * @return true se estiver com um formato v�lido ou false caso contr�rio.
	 */

	public static boolean emailCheck(String email)
	{
		if (email == null || !interval(email.length(), 3, EMAIL_LENGTH))
			return false;

		if (StringUtil.countOf(email, "@") != 1 || email.endsWith("@") || email.startsWith("@"))
			return false;

		if (!email.contains(".") || email.endsWith(".") || email.startsWith("."))
			return false;

		return !email.contains("@.");
	}

	/**
	 * Permite pular uma determinada quantidade de bytes de um FileDecriptor.
	 * @param fd refer�ncia do FileDecriptor que ter� bytes pulados na stream.
	 * @param input true para pular da entrada de dados ou false para a sa�da.
	 * @param bytes quantos bytes dever�o ser pulados na stream.
	 */

	public static void skip(FileDescriptor fd, boolean input, int bytes)
	{
		try {

			if (input)
				fd.getPacketBuilder().newInputPacket("SkipPacket").skipe(bytes);
			else
				fd.getPacketBuilder().newInputPacket("SkipPacket").skipe(bytes);

		} catch (StreamException e) {

			setUpSource(1);
			logError("falha ao pular %d bytes (ip: %s)", bytes, fd.getAddressString());

			throw new StreamRuntimeException(e.getMessage());
		}
	}

	/**
	 * Permite obter uma determinada mensagem carregada dos arquivos de mensagens.
	 * Essa mensagem � referente a uma listada nas mensagens do servidor de acesso.
	 * @param number c�digo de identifica��o da mensagem desejada.
	 * @return aquisi��o da mensagem no servidor de acesso.
	 */

	public static String loginMessage(int number)
	{
		String message = Messages.getInstance().getLoginMessages().get(number);

		if (message == null)
		{
			setUpSource(1);
			logException(new RagnarokException("mensagem '%d' n�o existe no LoginServer", number));
		}

		return message == null ? "null" : message;
	}

	/**
	 * Permite obter uma determinada mensagem carregada dos arquivos de mensagens.
	 * Essa mensagem � referente a uma listada nas mensagens do servidor de personagens.
	 * @param number c�digo de identifica��o da mensagem desejada.
	 * @return aquisi��o da mensagem no servidor de personagens.
	 */

	public static String charMessage(int number)
	{
		String message = Messages.getInstance().getCharMessages().get(number);

		if (message == null)
			logWarning("mensagem '%d' n�o existe no CharServer\n", number);

		return message == null ? "null" : message;
	}

	/**
	 * Permite obter uma determinada mensagem carregada dos arquivos de mensagens.
	 * Essa mensagem � referente a uma listada nas mensagens do servidor de mapas.
	 * @param number c�digo de identifica��o da mensagem desejada.
	 * @return aquisi��o da mensagem no servidor de mapas.
	 */

	public static String mapMessage(int number)
	{
		String message = Messages.getInstance().getMapMessages().get(number);

		if (message == null)
			logWarning("mensagem '%d' n�o existe no MapServer\n", number);

		return message == null ? "null" : message;
	}

	/**
	 * Permite obter o n�mero da vers�o do cliente atrav�s de uma datada especificada.
	 * @param date valor num�rico da data no formado YYYYMMDD usado em PACKETVER.
	 * @return aquisi��o do n�mero da vers�o referente a data especificada.
	 */

	public static int dateToVersion(int date)
	{
		for (int i = 0; i < PACKETS_VER.length; i++)
			if (PACKETS_VER[i] == 0)
				continue;
			else if (date < PACKETS_VER[i])
				return i;

		return PACKETS_VER.length;
	}

	/**
	 * Este m�todo s� ir� funcionar caso esteja sendo utilizado dentro do servidor de mapas.
	 * @param mapname nome do mapa do qual deseja obter o c�digo de identifica��o.
	 * @return aquisi��o do c�digo de identifica��o do nome do mapa passado.
	 */

	public static short mapname2mapid(String mapname)
	{
		if (Thread.currentThread() instanceof ServerThreaed)
		{
			ServerThreaed thread = (ServerThreaed) Thread.currentThread();

			if (thread.getServer() instanceof CharServer)
			{
				CharServer server = (CharServer) thread.getServer();

				return s(server.getFacade().getMapIndexes().getMapID(mapname));
			}

			if (thread.getServer() instanceof MapServer)
			{
				MapServer server = (MapServer) thread.getServer();

				return s(server.getFacade().getMapIndexes().getMapID(mapname));
			}
		}

		return 0;
	}

	/**
	 * Este m�todo s� ir� funcionar caso esteja sendo utilizado dentro do servidor de mapas.
	 * @param mapid c�digo de identifica��o do mapa do qual deseja obter o nome.
	 * @return aquisi��o do nome do mapa referente ao c�digo de identifica��o passado.
	 */

	public static String mapid2mapname(short mapid)
	{
		if (Thread.currentThread() instanceof ServerThreaed)
		{
			ServerThreaed thread = (ServerThreaed) Thread.currentThread();

			if (thread.getServer() instanceof CharServer)
			{
				CharServer server = (CharServer) thread.getServer();

				return server.getFacade().getMapIndexes().getMapName(mapid);
			}

			if (thread.getServer() instanceof MapServer)
			{
				MapServer server = (MapServer) thread.getServer();

				return server.getFacade().getMapIndexes().getMapName(mapid);
			}
		}

		return null;
	}
}

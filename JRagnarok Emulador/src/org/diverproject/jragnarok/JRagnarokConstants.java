package org.diverproject.jragnarok;

public class JRagnarokConstants
{
	/**
	 * Versão do cliente em que o sistema irá operar.
	 */
	public static final int PACKETVER = 20151104;

	/**
	 * Endereço de IP para conexões locais.
	 */
	public static final String LOCALHOST = "127.0.0.1";

	/**
	 * Quantidade limite de conexões por servidor.
	 */
	public static final int FD_SIZE = 4096;

	/**
	 * Limite de servidores criados por tipo de servidor.
	 */
	public static final int MAX_SERVERS = 30;


	/**
	 * Formato padrão de datas com horário.
	 */
	public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

	/**
	 * Formato padrão de datas.
	 */
	public static final String DATETIME_FORMAT = "dd/MM/yyyy";


	/**
	 * Limite de caracteres permitido por nome.
	 */
	public static final int NAME_LENGTH = 24;

	/**
	 * Limite de caracteres permitido por nome de usuário.
	 */
	public static final int USERNAME_LENGTH = 24;

	/**
	 * Limite de caracteres permitido por senhas de usuários.
	 */
	public static final int PASSWORD_LENGTH = 32;

	/**
	 * Limite de caracteres permitido no endereço de e-mail.
	 */
	public static final int EMAIL_LENGTH = 40;

	/**
	 * Limite de dígitos permitido em um código pin.
	 */
	public static final int PINCODE_LENGTH = 4;


	/**
	 * Limite de personagens por contas comuns (0: ilimitado).
	 */
	public static final int MAX_CHARS = 9;

	/**
	 * Limite de personagens por contas VIP (0: ilimitado).
	 */
	public static final int MAX_CHAR_VIP = 0;

	/**
	 * TODO what is that?
	 */
	public static final int MAX_CHAR_BILLING = 0;

	private JRagnarokConstants()
	{
		
	}
}

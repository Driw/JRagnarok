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
	 * Limite de dígitos permitido em um código pin.
	 */
	public static final int MAX_MAP_PER_SERVER = 1500;


	/**
	 * Formato padrão de datas com horário.
	 */
	public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

	/**
	 * Formato padrão de datas.
	 */
	public static final String DATETIME_FORMAT = "dd/MM/yyyy";


	/**
	 * Quantidade de caracteres que compõe um nome.
	 */
	public static final int NAME_LENGTH = 24;

	/**
	 * Limite mínimo de caracteres permitido por nome.
	 */
	public static final int MIN_NAME_LENGTH = 4;

	/**
	 * Limite máximo de caracteres permitido por nome.
	 */
	public static final int MAX_NAME_LENGTH = NAME_LENGTH;

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
	 * Limite mínimo de personagens por contas comuns.
	 */
	public static final byte MIN_CHARS = 3;

	/**
	 * Limite máximo de personagens por contas comuns (0: ilimitado).
	 */
	public static final byte MAX_CHARS = 9;

	/**
	 * Limite máximo de personagens por contas VIP (0: ilimitado).
	 */
	public static final byte MAX_CHAR_VIP = 0;

	/**
	 * TODO what is that?
	 */
	public static final byte MAX_CHAR_BILLING = 0;

	/**
	 * Limite de pontos salvos por personagem.
	 */
	public static final int MAX_MEMOPOINTS = 3;

	/**
	 * Limite máximo na quantidade de HP.
	 */
	public static final int MAX_HP = 10000000;

	/**
	 * Limite máximo na quantidade de SP.
	 */
	public static final int MAX_SP = 32768;

	/**
	 * Velocidade padrão de movimento dos personagens.
	 */
	public static final int DEFAULT_WALK_SPEED = 150;


	/**
	 * Quantidade de caracteres permitido para definir a abreviatura do nome do mapa.
	 */
	public static final int MAP_NAME_LENGTH = 12;

	/**
	 * Quantidade de caracteres permitido para definir o nome do mapa.
	 */
	public static final int MAP_NAME_LENGTH_EXT = MAP_NAME_LENGTH + 4;

	private JRagnarokConstants()
	{
		
	}
}

package org.diverproject.jragnarok;

/**
 * <h1>Constantes JRagnarok</h1>
 *
 * <p>Classe utilit�ria apenas para definir valores constantes utilizados em todo o sistema (API ou SDK).</p>
 *
 * @author Andrew
 */

public class JRagnarokConstants
{
	/**
	 * Vers�o do cliente em que o sistema ir� operar.
	 */
	public static final int PACKETVER = 20151104;

	/**
	 * Endere�o de IP para conex�es locais.
	 */
	public static final String LOCALHOST = "127.0.0.1";

	/**
	 * Limite de servidores criados por tipo de servidor.
	 */
	public static final int MAX_SERVERS = 30;


	/**
	 * Formato padr�o de datas com hor�rio.
	 */
	public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

	/**
	 * Formato padr�o de datas.
	 */
	public static final String DATETIME_FORMAT = "dd/MM/yyyy";

	/**
	 * Email padr�o.
	 */
	public static final String DEFAULT_EMAIL = "a@a.com";


	/**
	 * Quantidade de caracteres que comp�e um nome.
	 */
	public static final int NAME_LENGTH = 24;

	/**
	 * Limite m�nimo de caracteres permitido por nome.
	 */
	public static final int MIN_NAME_LENGTH = 4;

	/**
	 * Limite m�ximo de caracteres permitido por nome.
	 */
	public static final int MAX_NAME_LENGTH = NAME_LENGTH;

	/**
	 * Limite de caracteres permitido por nome de usu�rio.
	 */
	public static final int USERNAME_LENGTH = 24;

	/**
	 * Limite de caracteres permitido por senhas de usu�rios.
	 */
	public static final int PASSWORD_LENGTH = 24;

	/**
	 * Limite de caracteres permitido no endere�o de e-mail.
	 */
	public static final int EMAIL_LENGTH = 40;

	/**
	 * Limite de d�gitos permitido em um c�digo pin.
	 */
	public static final int PINCODE_LENGTH = 4;


	/**
	 * Quantidade de slots m�nimo para personagens por contas comuns (0: ilimitado).
	 */
	public static final byte MAX_CHARS = 9;

	/**
	 * Quantidade de slots m�nimo para personagens por contas comuns.
	 */
	public static final byte MIN_CHARS = 9;

	/**
	 * Limite m�ximo de personagens por contas VIP (0: ilimitado).
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
	 * Limite m�ximo na quantidade de HP.
	 */
	public static final int MAX_HP = 10000000;

	/**
	 * Limite m�ximo na quantidade de SP.
	 */
	public static final int MAX_SP = 32768;

	/**
	 * Velocidade padr�o de movimento dos personagens.
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



	/**
	 * Limite de d�gitos permitido em um c�digo pin.
	 */
	public static final int MAX_MAP_PER_SERVER = 1500;

	/**
	 * Limite na quantidade de mapas com �ndice definido no sistema.
	 */
	public static final int MAX_MAP_INDEX = 2000;

	/**
	 * Limite de c�lulas dispon�veis no tamanho do mapa (largura e altura).
	 */
	public static final int MAX_MAP_SIZE = 512;

	/**
	 * Limite de c�lulas dispon�veis no tamanho do mapa (largura * altura).
	 */
	public static final int MAX_MAP_SIZE2 = MAX_MAP_SIZE * MAX_MAP_SIZE;

	/**
	 * Limite m�nimo de caracteres permitidos no nome do mapa.
	 */
	public static final int MIN_MAP_NAME_LENGTH = 3;

	/**
	 * Limite m�ximo de caracteres permitidos no nome do mapa.
	 */
	public static final int MAX_MAP_NAME_LENGTH = 16;

	/**
	 * Limite de NPCs alocados por mapa.
	 */
	public static final int MAX_NPC_PER_MAP = 512;

	/**
	 * TODO
	 */
	public static final int MAX_DROP_PER_MAP = 48;

	/**
	 * LImite m�ximo de spawns distintos por mapa.
	 */
	public static final int MAX_MOB_LIST_PER_MAP = 128;

	/**
	 * Limite m�ximo de caracteres para definir o nome de um evento.
	 */
	public static final int EVENT_NAME_LENGTH = (NAME_LENGTH * 2) + 3;

	/**
	 * Dimens�o em c�lulas de largura e comprimento de uma chunk do mapa.
	 */
	public static final int MAP_CHUNK_SIZE = 8;

	/**
	 * Nome de identifica��o do mapa da cidade de Prontera.
	 */
	public static final String MAP_PRONTERA = "prontera";

	/**
	 * Nome de identifica��o do mapa da cidade de Geffen.
	 */
	public static final String MAP_GEFFEN = "geffen";

	/**
	 * Nome de identifica��o do mapa da cidade de Morroc.
	 */
	public static final String MAP_MORROC = "morocc";

	/**
	 * Nome de identifica��o do mapa da cidade de Alberta.
	 */
	public static final String MAP_ALBERTA = "alberta";

	/**
	 * Nome de identifica��o do mapa da cidade de Payou.
	 */
	public static final String MAP_PAYON = "payon";

	/**
	 * Nome de identifica��o do mapa da cidade de Izlude.
	 */
	public static final String MAP_IZLUDE = "izlude";

	private JRagnarokConstants()
	{
		
	}
}

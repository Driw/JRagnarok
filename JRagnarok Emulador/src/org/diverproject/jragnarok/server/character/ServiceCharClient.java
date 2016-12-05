package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CHAR_SERVER_SELECTED;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.receive.AcknowledgePacket;
import org.diverproject.jragnarok.packets.response.NotifyAuth;
import org.diverproject.jragnarok.packets.response.PincodeSendState;
import org.diverproject.jragnarok.packets.response.PincodeSendState.PincodeState;
import org.diverproject.jragnarok.packets.response.RefuseEnter;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.character.control.OnlineCharControl;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.jragnarok.server.character.structures.OnlineCharData;
import org.diverproject.jragnarok.server.common.NotifyAuthResult;
import org.diverproject.util.lang.HexUtil;

/**
 * <h1>Servi�o para Acesso dos Clientes</h1>
 *
 * <p>Esse servi�o � a primeira fronteira do servidor para se comunicar com o cliente.
 * Nele ser� recebido uma nova conex�o solicitada para com o servidor de personagem.
 * Ap�s receber a conex�o dever� redirecionar o cliente conforme o comando recebido.</p>
 *
 * <p>De acordo com o comando recebido, dever� redirecionar a outros servi�os se necess�rio.
 * Fica sendo de sua responsabilidade garantir tamb�m o envio de pacotes para os clientes.
 * Podendo ainda ser necess�rio comunicar-se com outro servi�o para auxili�-lo.</p>
 *
 * @see AbstractCharService
 * @see ServiceCharServerAuth
 * @see ServiceCharLogin
 * @see OnlineCharControl
 *
 * @author Andrew
 */

public class ServiceCharClient extends AbstractCharService
{
	/**
	 * Instancia um novo servi�o para recebimento de novos clientes no servidor.
	 * Para este servi�o � necess�rio realizar chamados para iniciar e destruir.
	 * @param server servidor de personagem respons�vel por este servi�o.
	 */

	public ServiceCharClient(CharServer server)
	{
		super(server);
	}

	/**
	 * Listener usado para receber novas conex�es solicitadas com o servidor de personagem.
	 */

	public final FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			logDebug("parsing fd#%d.\n", fd.getID());

			CFileDescriptor cfd = (CFileDescriptor) fd;

			if (!fd.isConnected())
				return false;

			// J� foi autenticado, n�o deveria estar aqui
			if (cfd.getSessionData().getCache() == null)
				return parseAlreadyAuth(cfd);

			return acknowledgePacket(cfd);
		}
	};

	/**
	 * Procedimento de chamado ap�s a primeira an�lise do cliente em rela��o a sua sess�o.
	 * Aqui o cliente ser� redirecionado corretamente conforme o tipo de comando solicitado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se deve manter a conex�o ou false caso contr�rio.
	 */

	private boolean acknowledgePacket(CFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd, false);

		short command = packetReceivePacketID.getPacketID();

		switch (command)
		{
			case PACKET_CHAR_SERVER_SELECTED:
				return auth.parse(fd);

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logNotice("fim de conex�o inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	/**
	 * Isso ocorre durante a an�lise de novos pacotes recebidos do cliente.
	 * Dever� remover a autentica��o de um jogador que j� foi possui uma sess�o.
	 * Clientes s� devem possuir sess�o caso tenham sido autenticados.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return sempre false, pois deve fechar a sess�o do jogador com o servidor.
	 */

	private boolean parseAlreadyAuth(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		OnlineCharData online = onlines.get(sd.getID());

		if (online != null && online.getFileDescriptor().getID() == fd.getID())
			online.setFileDescriptor(null);

		// Se n�o est� em nenhum servidor deixar offline.
		if (online == null || online.getServer() == -1)
			onlines.remove(online);

		return false;
	}

	/**
	 * Notifica o cliente que houve algum problema ap�s a autentica��o do acesso.
	 * O acesso foi autenticado por�m houve algum problema em liberar o acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da libera��o do acesso para o cliente.
	 */

	public void sendNotifyResult(CFileDescriptor fd, NotifyAuthResult result)
	{
		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);

		logDebug("notify result sent fd#%d.\n", fd.getID());
	}

	/**
	 * Recusa a entrada de uma determinada sess�o no servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado que ser� mostrado ao cliente.
	 */

	public void refuseEnter(CFileDescriptor fd, byte result)
	{
		RefuseEnter packet = new RefuseEnter();
		packet.setResult(result);
		packet.send(fd);

		logDebug("refuse enter sent fd#%d.\n", fd.getID());
	}

	/**
	 * Informa ao cliente que o sistema de c�digo PIN est� entrando no estado de:
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param sd dados da sess�o do cliente com o servidor de personagem.
	 * @param state estado do qual o c�digo PIN deve assumir.
	 */

	public void pincodeSendState(CFileDescriptor fd, CharSessionData sd, PincodeState state)
	{
		sd.getPincode().genSeed();

		PincodeSendState packet = new PincodeSendState();
		packet.setPincodeSeed(sd.getPincode().getSeed());
		packet.setAccountID(sd.getID());
		packet.setState(state.CODE);
		packet.send(fd);

		logDebug("send pincode state fd#%d.\n", fd.getID());
	}
}

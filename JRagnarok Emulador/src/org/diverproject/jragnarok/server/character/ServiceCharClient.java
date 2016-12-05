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
 * <h1>Serviço para Acesso dos Clientes</h1>
 *
 * <p>Esse serviço é a primeira fronteira do servidor para se comunicar com o cliente.
 * Nele será recebido uma nova conexão solicitada para com o servidor de personagem.
 * Após receber a conexão deverá redirecionar o cliente conforme o comando recebido.</p>
 *
 * <p>De acordo com o comando recebido, deverá redirecionar a outros serviços se necessário.
 * Fica sendo de sua responsabilidade garantir também o envio de pacotes para os clientes.
 * Podendo ainda ser necessário comunicar-se com outro serviço para auxiliá-lo.</p>
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
	 * Instancia um novo serviço para recebimento de novos clientes no servidor.
	 * Para este serviço é necessário realizar chamados para iniciar e destruir.
	 * @param server servidor de personagem responsável por este serviço.
	 */

	public ServiceCharClient(CharServer server)
	{
		super(server);
	}

	/**
	 * Listener usado para receber novas conexões solicitadas com o servidor de personagem.
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

			// Já foi autenticado, não deveria estar aqui
			if (cfd.getSessionData().getCache() == null)
				return parseAlreadyAuth(cfd);

			return acknowledgePacket(cfd);
		}
	};

	/**
	 * Procedimento de chamado após a primeira análise do cliente em relação a sua sessão.
	 * Aqui o cliente será redirecionado corretamente conforme o tipo de comando solicitado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se deve manter a conexão ou false caso contrário.
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
				logNotice("fim de conexão inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	/**
	 * Isso ocorre durante a análise de novos pacotes recebidos do cliente.
	 * Deverá remover a autenticação de um jogador que já foi possui uma sessão.
	 * Clientes só devem possuir sessão caso tenham sido autenticados.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return sempre false, pois deve fechar a sessão do jogador com o servidor.
	 */

	private boolean parseAlreadyAuth(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		OnlineCharData online = onlines.get(sd.getID());

		if (online != null && online.getFileDescriptor().getID() == fd.getID())
			online.setFileDescriptor(null);

		// Se não está em nenhum servidor deixar offline.
		if (online == null || online.getServer() == -1)
			onlines.remove(online);

		return false;
	}

	/**
	 * Notifica o cliente que houve algum problema após a autenticação do acesso.
	 * O acesso foi autenticado porém houve algum problema em liberar o acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da liberação do acesso para o cliente.
	 */

	public void sendNotifyResult(CFileDescriptor fd, NotifyAuthResult result)
	{
		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);

		logDebug("notify result sent fd#%d.\n", fd.getID());
	}

	/**
	 * Recusa a entrada de uma determinada sessão no servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado que será mostrado ao cliente.
	 */

	public void refuseEnter(CFileDescriptor fd, byte result)
	{
		RefuseEnter packet = new RefuseEnter();
		packet.setResult(result);
		packet.send(fd);

		logDebug("refuse enter sent fd#%d.\n", fd.getID());
	}

	/**
	 * Informa ao cliente que o sistema de código PIN está entrando no estado de:
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param sd dados da sessão do cliente com o servidor de personagem.
	 * @param state estado do qual o código PIN deve assumir.
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

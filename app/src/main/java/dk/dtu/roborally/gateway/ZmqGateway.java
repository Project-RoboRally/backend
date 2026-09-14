package dk.dtu.roborally.gateway;

import dk.dtu.roborally.gateway.sockets.BroadcastSocket;
import dk.dtu.roborally.gateway.sockets.PlayerSocket;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * Gateway for ZMQ (websocket connections)
 *
 * @author Elias
 */
public class ZmqGateway implements AutoCloseable {

	private final ZContext context;
	private PlayerSocket playerSocket;
	private BroadcastSocket broadcastSocket;

	public ZmqGateway() {
		this.context = new ZContext();
	}

	public void init() {
		// Setup socket to receive messages from players
		ZMQ.Socket socket1 = context.createSocket(SocketType.ROUTER);
		socket1.setLinger(0);
		socket1.bind("tcp://*:5555");
		this.playerSocket = new PlayerSocket(socket1);

		// Setup socket to broadcast messages to the players
		ZMQ.Socket socket2 = context.createSocket(SocketType.PUB);
		socket2.setLinger(0);
		socket2.bind("tcp://*:5556");
		this.broadcastSocket = new BroadcastSocket(socket2);

	}

	/*
	 * Once the server stops, close the sockets.
	 */
	@Override
	public void close() throws Exception {
		if (playerSocket != null) {
			playerSocket.close();
		}

		if (broadcastSocket != null) {
			broadcastSocket.close();
		}

		context.close();
	}
}

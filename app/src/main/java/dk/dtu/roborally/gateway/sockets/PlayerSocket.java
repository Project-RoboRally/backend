package dk.dtu.roborally.gateway.sockets;

import org.zeromq.ZMQ;

/**
 * Socket for the player to send messages from the player to the server
 *
 * @author Elias
 */
public class PlayerSocket extends AbstractSocket {
	public PlayerSocket(ZMQ.Socket socket) {
		super(socket);
	}

	// todo: Setup zmq and setup a reciever where when a message is recieved,
	// it is matched with a specific player and the message is send to the
	// logic.
}

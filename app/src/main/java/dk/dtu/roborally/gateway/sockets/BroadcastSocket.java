package dk.dtu.roborally.gateway.sockets;

import org.zeromq.ZMQ;

/**
 * Socket for the server to broadcast messages from the server to the client
 *
 * @author Elias
 */
public class BroadcastSocket extends AbstractSocket {
    public BroadcastSocket(ZMQ.Socket socket) {
        super(socket);
    }


    // todo: Setup zmq and make function to broadcast messages to users in specific games.
}

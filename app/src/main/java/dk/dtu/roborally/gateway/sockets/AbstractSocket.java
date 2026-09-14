package dk.dtu.roborally.gateway.sockets;

import lombok.Getter;
import org.zeromq.ZMQ;

/**
 * General class structure for out different socket types in the proejct
 *
 * @author Elias
 */
abstract class AbstractSocket {

	@Getter
	private ZMQ.Socket socket;

	public AbstractSocket(ZMQ.Socket socket) {
		this.socket = socket;
	}

	public void close() {
		if (socket != null) {
			socket.close();
		}
	}
}

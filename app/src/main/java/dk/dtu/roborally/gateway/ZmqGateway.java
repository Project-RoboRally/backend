package dk.dtu.roborally.gateway;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;


/**
 * Gateway for ZMQ (websocket connections)
 *
 * @author Elias
 */
public class ZmqGateway {

    public void init() {

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(ZMQ.PUB);
            socket.bind("tcp://*:5555");

            // Give subscribers a moment to connect.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            socket.send("test".getBytes(ZMQ.CHARSET), 0);

            // Keep socket/context alive if you intend to publish more messages.
        }
    }
}

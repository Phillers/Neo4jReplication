package neo4jreplication;


import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Communication implements Runnable, AutoCloseable {
    private ZContext context;
    private int id;
    private ZMQ.Socket reqSocket;
    private String[] addresses;
    Neo4jReplication base;

    public Communication(String[] addresses, int id, Neo4jReplication base) {
        this.base = base;
        context = new ZContext();
        this.addresses = addresses.clone();
        reqSocket = context.createSocket(SocketType.REQ);
        this.id = id;
    }


    public String send(int id, String message) {
        reqSocket.connect(addresses[id]);
        reqSocket.send(message);
        String reply = reqSocket.recvStr();
        reqSocket.disconnect(addresses[id]);
        return reply;
    }

    @Override
    public void run() {
        ZMQ.Socket repSocket = context.createSocket(SocketType.REP);
        repSocket.bind(addresses[id]);
        while (!Thread.currentThread().isInterrupted()) {
            // Block until a message is received
            String message = repSocket.recvStr(0);
            String reply = base.processMessage(message);
            repSocket.send(reply);
        }
    }

    @Override
    public void close() {
        context.close();
    }
}
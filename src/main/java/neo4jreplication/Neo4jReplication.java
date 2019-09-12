package neo4jreplication;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Neo4jReplication implements Neo4jReplicationStub {
    private final String SEPARATOR = ";_;";
    private final char GET_WRITES = 'G';
    private DatabaseDriver driver;
    private Communication communication;
    private int id;
    private int[] writes;
    private List<String> operations = new ArrayList<>();
    private Thread listener;

    public Neo4jReplication(String uri, String user, String password, String[] addresses, int id) {
        driver = new DatabaseDriver(uri, user, password);
        communication = new Communication(addresses, id, this);
        listener = new Thread(communication);
        listener.start();
        writes = new int[addresses.length];
        this.id = id;
    }

    public static void main(String[] args) {
        try {
            //if (System.getSecurityManager() == null) {
            //    System.setSecurityManager(new SecurityManager());
           // }
            String name = "Neo4jReplication";
            String[] addresses = {"tcp://localhost:1234", "tcp://localhost:4321"};
            Neo4jReplication engine = new Neo4jReplication("bolt://localhost:7687", "neo4j", "qwazerty2", addresses, 0);
            Neo4jReplicationStub stub =
                    (Neo4jReplicationStub) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry;
            try {registry = LocateRegistry.createRegistry(12345);}
            catch (RemoteException e){
                registry = LocateRegistry.getRegistry();
            }
            registry.rebind(name, stub);
            System.out.println("ComputeEngine bound");
        } catch (Exception e) {
            System.err.println("ComputeEngine exception:");
            e.printStackTrace();
        }
    }

    public RequestResult read(RequestParameter param) {
        int[] check = {};
        int level = param.level & (param.RYW|param.MR);
        if (level == param.MR){
            check = param.read;
        }
        if (level == param.RYW){
            check = param.write;
        }
        if (level ==  (param.RYW|param.MR)){
            check = param.write.clone();
            for(int i=0;i<check.length;i++){
                if (param.read[i]>check[i])
                    check[i] = param.read[i];
            }
        }
        getWrites(check);
        check = writes.clone();
        for(int i=0;i<check.length;i++){
            if (param.read[i]>check[i])
                check[i] = param.read[i];
        }
        return new RequestResult(driver.read(param.query), param.write, writes);
    }

    public RequestResult write(RequestParameter param) {
        int[] check = {};
        int level = param.level & (param.WFR|param.MW);
        if (level == param.WFR){
            check = param.read;
        }
        if (level == param.MW){
            check = param.write;
        }
        if (level ==  (param.WFR|param.MW)){
            check = param.write.clone();
            for(int i=0;i<check.length;i++){
                if (param.read[i]>check[i])
                    check[i] = param.read[i];
            }
        }
        getWrites(check);
        writes[id]++;
        operations.add(param.query);
        check = writes.clone();
        for(int i=0;i<check.length;i++){
            if (param.write[i]>check[i])
                check[i] = param.write[i];
        }
        return new RequestResult(driver.write(param.query), check, param.read);
    }


    private void getWrites(int[] check) {
        for (int i = 0; i < check.length; i++) {
            if (writes[i] < check[i]) {
                String remoteOps = communication.send(i, "" + GET_WRITES + writes[i]);
                processRemoteOps(i, remoteOps);
            }
        }
        ;
    }

    private void processRemoteOps(int id, String remoteOps) {
        String[] list = remoteOps.split(SEPARATOR);
        for (String q : list) {
            driver.write(q);
            writes[id]++;
        }
    }

    public void close() {
        driver.close();
    }

    public String processMessage(String message) {
        switch (message.charAt(0)) {
            case GET_WRITES:
                int index = Integer.parseInt(message.substring(1));
                StringBuilder sb = new StringBuilder();
                for (int i = index; i < writes[id]; i++) {
                    sb.append(operations.get(i));
                    if (i < writes[id])
                        sb.append(SEPARATOR);
                }
                return sb.toString();
        }
        return "";
    }
}

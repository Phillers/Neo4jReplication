package neo4jreplication;

import java.util.ArrayList;
import java.util.List;

public class Neo4jReplication {
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

    public RequestResult read(RequestParameter param) {
        //TODO

        checkWrites(param, false);
        return new RequestResult(driver.read(param.query), param.write, writes);
    }

    public RequestResult write(RequestParameter param) {
        //TODO

        checkWrites(param, true);
        writes[id]++;
        operations.add(param.query);
        return new RequestResult(driver.write(param.query), writes, param.read);
    }

    private void checkWrites(RequestParameter param, boolean isWrite) {

        int[] check = {};
        if ((param.level == RequestParameter.Level.MonotonicReads && !isWrite) ||
                (param.level == RequestParameter.Level.WritesFollowReads && isWrite)) {
            check = param.read;
        }
        if ((param.level == RequestParameter.Level.ReadYourWrites && !isWrite) ||
                (param.level == RequestParameter.Level.MonotonicWrites && isWrite) ||
                param.level == RequestParameter.Level.PRAM) {
            check = param.write;
        }

        if (param.level == RequestParameter.Level.Sequential) {
            check = param.write;
            for (int i = 0; i < check.length; i++) {
                if (param.read[i] > check[i])
                    check[i] = param.read[i];
            }
        }
        getWrites(check);
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

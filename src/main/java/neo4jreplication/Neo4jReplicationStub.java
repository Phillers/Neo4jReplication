package neo4jreplication;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Neo4jReplicationStub extends Remote {
    public RequestResult read(RequestParameter param) throws RemoteException;

    public RequestResult write(RequestParameter param) throws RemoteException;
}

package neo4jreplication;

import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestResult implements Serializable {
    public int[] read;
    public int[] write;
    public List<Map<String, Object>> records = new ArrayList<>();

    public RequestResult(StatementResult res, int[] write, int[] read) {
        setRecords(res);
        this.write = write.clone();
        this.read = read.clone();
    }


    private void setRecords(StatementResult res) {
        while (res.hasNext()) {
            Map<String, Object> m = new HashMap<>(res.next().asMap());
            for (Map.Entry<String, Object> e : m.entrySet()) {
                Object o = e.getValue();
                if (o instanceof Node) {
                    e.setValue(new MyNodeRelation((Node) o));
                }
                if (o instanceof Relationship) {
                    e.setValue(new MyNodeRelation((Relationship) o));
                }
            }
            records.add(m);
        }
    }
}

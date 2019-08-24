package neo4jreplication;

import org.neo4j.driver.v1.Record;

import java.util.List;

public class RequestResult {
    public int[] read;
    public int[] write;
    public List<Record> records;

    public RequestResult(List<Record> result, int[] write, int[] read) {
        records = result;
        this.write = write.clone();
        this.read = read.clone();
    }
}

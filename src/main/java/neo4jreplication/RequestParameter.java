package neo4jreplication;

import java.io.Serializable;

public class RequestParameter implements Serializable {
    public static final int RYW = 1;
    public static final int MW = 2;
    public static final int MR = 4;
    public static final int WFR = 8;
    public int[] write = {};
    public int[] read = {};
    public int level = 0;
    public String query;

    public RequestParameter(String s) {
        query = s;

    }

    public RequestParameter(String s, int level, RequestResult res) {
        query = s;
        if (res.write == null)
            write = new int[res.read.length];
        else
            write = res.write.clone();
        if (res.read == null)
            read = new int[res.write.length];
        else
            read = res.read.clone();
        this.level = level;
    }

    public enum Level {
        Default, ReadYourWrites, MonotonicWrites, MonotonicReads, WritesFollowReads, PRAM, Sequential
    }

}

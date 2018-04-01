package com.commons;

import com.aerospike.client.*;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;

/**
 * @author Administrator
 * @date 2018-04-01 22:32
 **/
public class Test1 {
    private static Host[] hosts = new Host[] {
            new Host("192.168.0.105", 3000),
            new Host("192.168.0.107", 3000)
    };
    private static AerospikeClient client = new AerospikeClient(new ClientPolicy(), hosts);

    public static  void main(String[] args) {
        /*byte[] arr = "abc".getBytes();
        for(int i = 0;i < arr.length; i++){
            System.out.print(arr[i] + ",");
        }*/

        write();

        query();

        removeBin();

        get();

        ops();


    }

    public static void write(){
        WritePolicy policy = new WritePolicy();
        policy.setTimeout(50);

        Key key = new Key("test", "myset", "k1");
        Bin bin1 = new Bin("name", "John");
        Bin bin2 = new Bin("age", 25);
        Bin bin3 = new Bin("desc", "hello".getBytes());
        client.put(policy, key, bin1, bin2, bin3);
        System.out.println("write success");
    }

    public static void removeBin(){
        WritePolicy policy = new WritePolicy();
        policy.setTimeout(50);

        Key key = new Key("test", "myset", "k1");

        client.put(policy, key, Bin.asNull("desc"));
        System.out.println("removeBin success");
    }

    public static void get(){
        Key key = new Key("test", "myset", "k1");
        Record record = client.get(null, key);
        System.out.println(record.bins);
    }

    public static void delete(){
        WritePolicy policy = new WritePolicy();
        policy.setTimeout(50);
        Key key = new Key("test", "myset", "k1");
        client.delete(policy, key);
        System.out.println("delete success");
    }

    public static void ops() {
        WritePolicy policy = new WritePolicy();
        policy.setTimeout(50);

        Key key = new Key("test", "set2", "opkey");
        Bin bin1 = new Bin("optintbin", 7);
        Bin bin2 = new Bin("optstringbin", "string value");
        client.put(policy, key, bin1, bin2);

        Bin bin3 = new Bin(bin1.name, 10);
        Bin bin4 = new Bin(bin2.name, "new string");

        //相当于事务
        Record record = client.operate(policy, key, Operation.add(bin3), Operation.put(bin4), Operation.get());
        System.out.println(record.bins);
    }

    public static void query() {
        Statement stmt = new Statement();
        stmt.setNamespace("test");
        stmt.setSetName("myset");
        //stmt.setFilter(Filter.range("age", 20, 30)); // 使用bin baz上的数字索引来查找,所以需要先给age设置索引才能使用这个过滤条件
        RecordSet rs = client.query(null, stmt);
        try {
            while (rs.next()) {
                Key key = rs.getKey();
                Record record = rs.getRecord();
                System.out.println("key:" + key + "  record:" + record.bins);
            }
        }
        finally {
            rs.close();
        }
    }
}

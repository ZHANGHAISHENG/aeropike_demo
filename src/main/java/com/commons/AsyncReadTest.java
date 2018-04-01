package com.commons;

import com.aerospike.client.*;
import com.aerospike.client.async.*;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.Policy;

import java.util.concurrent.atomic.AtomicInteger;

public final class AsyncReadTest {
    private AerospikeClient client;
    private EventLoops eventLoops;
    private final Monitor monitor = new Monitor();
    private final int writeTimeout = 5000;
    private final int eventLoopSize;

    public static void main(String[] args) {
        try {
            AsyncReadTest test = new AsyncReadTest();
            test.runTest();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AsyncReadTest() {
        // Allocate an event loop for each cpu core.获取cpu核心数
        eventLoopSize = Runtime.getRuntime().availableProcessors();
    }
    public void runTest() throws AerospikeException {
        EventPolicy eventPolicy = new EventPolicy();
        eventPolicy.minTimeout = writeTimeout;
        eventLoops = new NioEventLoops(eventPolicy, eventLoopSize);
        try {
            ClientPolicy clientPolicy = new ClientPolicy();
            clientPolicy.eventLoops = eventLoops;
            clientPolicy.writePolicyDefault.setTimeout(writeTimeout);
            client = new AerospikeClient(clientPolicy, new Host("192.168.0.105", 3000), new Host("192.168.0.107", 3000));
            try {
                Policy policy = new Policy();
                Key key = new Key("test", "myset", 1);
                client.get(eventLoops.next(), new ReadHandler(), policy, key);
                monitor.waitTillComplete();
                System.out.println("reader complete.");
            }
            finally {
                client.close();
            }
        }
        finally {
            eventLoops.close();
        }
    }

    private class ReadHandler implements RecordListener {
        public void onSuccess(Key key, Record record) {
            Object received = (record == null)? null : record.bins;
            System.out.println(String.format("Received: " + received));
            monitor.notifyComplete();
        }
        public void onFailure(AerospikeException e) {
            e.printStackTrace();
        }
    }

}

package com.commons;

import com.aerospike.client.*;
import com.aerospike.client.async.EventLoops;
import com.aerospike.client.async.EventPolicy;
import com.aerospike.client.async.Monitor;
import com.aerospike.client.async.NioEventLoops;
import com.aerospike.client.listener.DeleteListener;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;

public final class AsyncDeleteTest {
    private AerospikeClient client;
    private EventLoops eventLoops;
    private final Monitor monitor = new Monitor();
    private final int writeTimeout = 5000;
    private final int eventLoopSize;

    public static void main(String[] args) {
        try {
            AsyncDeleteTest test = new AsyncDeleteTest();
            test.runTest();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AsyncDeleteTest() {
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
                Key key = new Key("test", "myset", 1);
                client.delete(eventLoops.next(), new DeleteHandler(), clientPolicy.writePolicyDefault, key);
                monitor.waitTillComplete();
                System.out.println("delete complete.");
            }
            finally {
                client.close();
            }
        }
        finally {
            eventLoops.close();
        }
    }

    private class DeleteHandler implements DeleteListener {
        public void onSuccess(Key key, boolean b) {
            System.out.println(String.format("is success: " + b));
            monitor.notifyComplete();
        }
        public void onFailure(AerospikeException e) {
            e.printStackTrace();
        }
    }

}

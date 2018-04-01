package com.commons;

import com.aerospike.client.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.aerospike.client.async.*;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.ClientPolicy;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;

public final class AsyncWriteTest {
    private AerospikeClient client;
    private EventLoops eventLoops;
    private final Monitor monitor = new Monitor();
    private final AtomicInteger recordCount = new AtomicInteger();
    private final int recordMax = 100000;
    private final int writeTimeout = 5000;
    private final int eventLoopSize;
    private final int concurrentMax;

    public static void main(String[] args) {
        try {
            AsyncWriteTest test = new AsyncWriteTest();
            test.runTest();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AsyncWriteTest() {
        // Allocate an event loop for each cpu core.获取cpu核心数
        eventLoopSize = Runtime.getRuntime().availableProcessors();
        // Allow 40 concurrent commands per event loop.
        concurrentMax = eventLoopSize * 40;
        //eventLoopSize: 4 concurrentMax: 160
        System.out.println("eventLoopSize: " + eventLoopSize + " concurrentMax: " + concurrentMax);
    }
    public void runTest() throws AerospikeException {
        EventPolicy eventPolicy = new EventPolicy();
        eventPolicy.minTimeout = writeTimeout;
        eventLoops = new NioEventLoops(eventPolicy, eventLoopSize);
        // EventLoopGroup group = new EpollEventLoopGroup(4);
        // EventLoops eventLoops = new NettyEventLoops(eventPolicy, group);

        try {
            ClientPolicy clientPolicy = new ClientPolicy();
            clientPolicy.eventLoops = eventLoops;
            clientPolicy.maxConnsPerNode = concurrentMax;
            clientPolicy.writePolicyDefault.setTimeout(writeTimeout);
            client = new AerospikeClient(clientPolicy, new Host("192.168.0.105", 3000), new Host("192.168.0.107", 3000));
            try {
                writeRecords();
                monitor.waitTillComplete();
                System.out.println("Records written: " + recordCount.get());
            }
            finally {
                client.close();
            }
        }
        finally {
            eventLoops.close();
        }
    }

    private void writeRecords() {
        // Write exactly concurrentMax commands to seed event loops.
        // Distribute seed commands across event loops.
        // A new command will be initiated after each command completion in WriteListener.
        for (int i = 1; i <= concurrentMax; i++) {
            EventLoop eventLoop = eventLoops.next();
            writeRecord(eventLoop, new AWriteListener(eventLoop), i);
        }
    }

    private void writeRecord(EventLoop eventLoop, WriteListener listener, int keyIndex) {
        Key key = new Key("test", "myset", keyIndex);
        Bin bin1 = new Bin("name", "John");
        Bin bin2 = new Bin("age", 25);
        Bin bin3 = new Bin("desc", "hello".getBytes());
        client.put(eventLoop, listener, null, key, bin1, bin2, bin3);
    }

    private class AWriteListener implements WriteListener {
        private final EventLoop eventLoop;

        public AWriteListener(EventLoop eventLoop) {
            this.eventLoop = eventLoop;
        }
        public void onSuccess(Key key) {
            try {
                int count = recordCount.incrementAndGet();
                // Stop if all records have been written.
                if (count >= recordMax) {
                    monitor.notifyComplete();
                    return;
                }
                if (count % 10000 == 0) {
                    System.out.println("Records written: " + count);
                }
                // Issue one new command if necessary.
                int keyIndex = concurrentMax + count;
                if (keyIndex <= recordMax) {
                    // Write next record on same event loop.
                    writeRecord(eventLoop, this, keyIndex);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                monitor.notifyComplete();
            }
        }
        public void onFailure(AerospikeException e) {
            e.printStackTrace();
            monitor.notifyComplete();
        }
    }
}

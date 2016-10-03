package edu.wallet.server.net;

import edu.wallet.server.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 *
 */
public class NetServer {

    private final IProcessor processor;

    private final Configuration config;

    private final ILogger logger;

    private ExecutorService serverExecutor;
    private ServerChannelRunnable serverRunnable;

    private ExecutorService channelExecutor;
    private ChannelRunnable[] channelRunnables;


    public NetServer(IProcessor proc, ILogger logger, Configuration cfg) {
        assert proc != null;
        assert logger != null;
        assert cfg != null;

        this.processor = proc;
        this.logger = logger;
        this.config = cfg;
    }

    public void start() throws Exception {
        int port = config.getServerPort();

        // Once bind, we will not change the port in future.
        InetSocketAddress locAddr = new InetSocketAddress(/*addr, */port);

        // This method will throw exception if address already in use.
        Selector acceptSelector = createAcceptSelector(locAddr);

        serverRunnable = new ServerChannelRunnable(logger, acceptSelector, this);

        serverExecutor = Executors.newSingleThreadExecutor();

        serverExecutor.submit(serverRunnable);

        int numServerThreads = config.getNumThreads();

        channelExecutor = Executors.newFixedThreadPool(numServerThreads);

        channelRunnables = new ChannelRunnable[numServerThreads];

        for (int i=0; i<numServerThreads; i++) {
            Selector selector = SelectorProvider.provider().openSelector();

            ChannelRunnable channelRunnable = new ChannelRunnable(processor, i, logger, selector);

            channelRunnables[i] = channelRunnable;

            channelExecutor.submit(channelRunnable);
        }

        logger.info("Server started at port " + port, null);
    }

    private Selector createAcceptSelector(SocketAddress addr) throws Exception {
         // Create a new selector
        Selector selector = SelectorProvider.provider().openSelector();

        if (addr != null) {
            // Create a new non-blocking server socket channel
            ServerSocketChannel srvrCh = ServerSocketChannel.open();

            srvrCh.configureBlocking(false);

//                if (sockRcvBuf > 0)
//                    srvrCh.socket().setReceiveBufferSize(sockRcvBuf);

                // Bind the server socket to the specified address and port
            srvrCh.socket().bind(addr);

                // Register the server socket channel, indicating an interest in
                // accepting new connections
            srvrCh.register(selector, SelectionKey.OP_ACCEPT);
        }

        return selector;
    }

    private final AtomicInteger roundRobinIndex = new AtomicInteger();

    private int incModAndGet(int module) {
        while (true) {
            int idx = roundRobinIndex.get();

            int newVal = (idx + 1) % module;

            if (roundRobinIndex.compareAndSet(idx, newVal)) {
                return newVal;
            }
        }
    }

    void offerBalanced(SocketChannel sockCh) {
        // round-robin to any of the channel runnables:
        int idx = incModAndGet(config.getNumThreads());

        channelRunnables[idx].offer(sockCh);
    }

    public void stop() throws Exception {
        serverRunnable.stop();

        for (int i=0; i<channelRunnables.length; i++) {
            channelRunnables[i].stop();
        }

        Thread.sleep(1000);

        serverExecutor.shutdownNow();
        channelExecutor.shutdownNow();

        boolean t1 = serverExecutor.awaitTermination(1, TimeUnit.SECONDS);
        boolean t2 = channelExecutor.awaitTermination(1, TimeUnit.SECONDS);

        if (!t1 || !t2) {
            throw new Exception("Failed to stop the server.");
        }
    }
}

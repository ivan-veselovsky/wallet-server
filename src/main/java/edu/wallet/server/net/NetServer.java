package edu.wallet.server.net;

import edu.wallet.config.Cfg;
import edu.wallet.config.IConfiguration;
import edu.wallet.log.ILogger;
import edu.wallet.server.IProcessor;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class NetServer implements Closeable {
    private final AtomicInteger roundRobinIndex = new AtomicInteger();

    private final IProcessor processor;
    private final IConfiguration config;
    private final ILogger logger;
    private final Cfg cfg;

    private ExecutorService serverExecutor;
    private ServerChannelRunnable serverRunnable;

    private ExecutorService channelExecutor;
    private ChannelRunnable[] channelRunnables;

    public NetServer(Cfg cfg) {
        this.cfg = Objects.requireNonNull(cfg);
        this.config = Objects.requireNonNull(cfg.getConfiguration());
        this.logger = Objects.requireNonNull(cfg.getLogger());
        this.processor = Objects.requireNonNull(cfg.getProcessor());
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

        for (int i = 0; i < numServerThreads; i++) {
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

            // Bind the server socket to the specified address and port
            srvrCh.socket().bind(addr);

            // Register the server socket channel, indicating an interest in
            // accepting new connections
            srvrCh.register(selector, SelectionKey.OP_ACCEPT);
        }

        return selector;
    }

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

    public void close() throws IOException {
        serverRunnable.stop();

        for (int i = 0; i < channelRunnables.length; i++) {
            channelRunnables[i].stop();
        }

        try {
            Thread.sleep(1000); // rough grace gap to allow all runnables to stop themselves.

            serverExecutor.shutdownNow();
            channelExecutor.shutdownNow();

            boolean t1 = serverExecutor.awaitTermination(1, TimeUnit.SECONDS);
            boolean t2 = channelExecutor.awaitTermination(1, TimeUnit.SECONDS);

            processor.close(); // NB: Db Saver will be closed there.

            cfg.getPersistentStorage().close();

            if (!t1 || !t2) {
                throw new Exception("Failed to stop the server.");
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}

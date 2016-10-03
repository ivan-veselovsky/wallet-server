package edu.wallet.server.net;

import edu.wallet.server.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;

/**
 *
 */
public class ServerChannelRunnable implements Runnable {
    /** Selector for this thread. */
    private final Selector selector;
    private final ILogger logger;
    private final NetServer srv;
    private volatile boolean closed;

    ServerChannelRunnable(ILogger logger, Selector selector, NetServer srv) {
        assert logger != null;
        assert selector != null;
        assert srv != null;

        this.logger = logger;
        this.selector = selector;
        this.srv = srv;
    }

    @Override public void run() {
        try {
            while (!closed && !Thread.currentThread().isInterrupted()) {
                try {
                    accept();
                }
                catch (Exception e) {
                    logger.error("While accepting on server socket: ", e);

                    break;
                }
            }
        } catch (Throwable t) {
            logger.error("In accept thread: ", t);
        }
        finally {
            closeSelector(); // Safety.
        }
    }

    private void accept() throws Exception {
        try {
            while (!closed && selector.isOpen() && !Thread.currentThread().isInterrupted()) {
                // Wake up every 2 seconds to check if closed.
                if (selector.select(2000) > 0)
                    // Walk through the ready keys collection and process date requests.
                    processSelectedKeys(selector.selectedKeys());
            }
        }
        finally {
            closeSelector();
        }
    }

    private void closeSelector() {
        try {
            if (selector.isOpen()) {
                // Close all channels registered with selector.
                for (SelectionKey key : selector.keys())
                    key.channel().close();

                selector.close();
            }
        } catch (Exception e) {
            logger.info("On close selector: ", e);
        }
    }

    private void processSelectedKeys(Set<SelectionKey> keys) throws IOException {
        for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext();) {
            SelectionKey key = iter.next();

            iter.remove();

            // Was key closed?
            if (!key.isValid())
                continue;

            if (key.isAcceptable()) {
                // The key indexes into the selector so we
                // can retrieve the socket that's ready for I/O
                ServerSocketChannel srvrCh = (ServerSocketChannel)key.channel();

                SocketChannel sockCh = srvrCh.accept();

                sockCh.configureBlocking(false);
                sockCh.socket().setKeepAlive(true);

                addRegistrationReq(sockCh);
            }
        }
    }

    private void addRegistrationReq(SocketChannel sockCh) {
        srv.offerBalanced(sockCh);
    }

    public void stop() {
        closed = true;
    }
}

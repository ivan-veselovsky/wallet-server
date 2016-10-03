package edu.wallet.server.net;

import edu.wallet.server.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 */
public class ChannelRunnable implements Runnable {
    private final IProcessor processor;

    private final ILogger logger;

    private final int idx;

    private final Selector selector;

    private final ConcurrentLinkedQueue<SocketChannel> changeReqs = new ConcurrentLinkedQueue<>();

    private volatile boolean closed;

        protected ChannelRunnable(IProcessor proc, int idx, ILogger log, Selector selector) {
            assert log != null;
            assert selector != null;
            assert proc != null;

            this.processor = proc;
            this.idx = idx;
            this.logger = log;
            this.selector = selector;
        }

    @Override public void run() {
        try {
            while (!closed) {
                runImpl();
            }
        }
        catch (Throwable t) {
            logger.error("On channels processing: ", t);
        }
    }

        public void offer(SocketChannel req) {
            changeReqs.offer(req);

            selector.wakeup();
        }

        private void runImpl() throws Exception {
            try {
                SocketChannel req;

                while (!closed && selector.isOpen()) {
                    while ((req = changeReqs.poll()) != null) {
                        register(req);
                    }

                    if (selector.select(2000) > 0) {
                        processSelectedKeys(selector.selectedKeys());
                    }
                }
            }
            finally {
                if (selector.isOpen()) {
                    selector.close();
                }
            }
        }

        private void processSelectedKeys(Set<SelectionKey> keys) throws IOException {
            if (keys.isEmpty())
                return;

            for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext(); ) {
                SelectionKey key = iter.next();

                iter.remove();

                // Was key closed?
                if (!key.isValid())
                    continue;

                if (key.isReadable())
                    processRead(key);

                if (key.isValid() && key.isWritable()) {
                    //System.out.println("### process write " + key);
                    processWrite(key);
                }
            }
        }

        private void register(SocketChannel sockCh) throws Exception {
            assert sockCh != null;

            // TODO: add OP_WRITE there?
            SelectionKey key = sockCh.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                new ChannelAttachmant());
        }

    // Need there?
        protected void close(SelectionKey key) {
            // Shutdown input and output so that remote client will see correct socket close.
            Socket sock = ((SocketChannel)key.channel()).socket();

                try {
                    sock.shutdownInput();
                }
                catch (IOException ignored) {
                    // No-op.
                }

                try {
                    sock.shutdownOutput();
                }
                catch (IOException ignored) {
                    // No-op.
                }
        }

    // Request:
        protected void processRead(SelectionKey key) throws IOException {
            ChannelAttachmant attach = (ChannelAttachmant)key.attachment();

            ReadableByteChannel readableCh = (ReadableByteChannel)key.channel();

            attach.readBuffer.clear();

            int read = readableCh.read(attach.readBuffer);

            if (read > 0) {
                attach.readBuffer.flip();

                assert attach.readBuffer.remaining() == read;

                byte[] data = new byte[read];

                attach.readBuffer.get(data);

                //boolean put = attach.incomingRequets.offer(data);
                //assert put;

                //System.out.println("SRV: rq : " + data.length + " bytes");

                // NB: notice that catula request processing is synchronous with the request read:
                byte[] rsp = processor.process(data);

                assert rsp != null;
                assert rsp[rsp.length - 1] == 10;

                //System.out.println("SRV: rsp: " + rsp.length + " bytes");

                attach.outgoingResponses.offer(rsp);
            }
        }

    // Response:
        protected void processWrite(SelectionKey key) throws IOException {
            ChannelAttachmant attach = (ChannelAttachmant)key.attachment();

            WritableByteChannel sockCh = (WritableByteChannel)key.channel();

            byte[] outgoingMessage = attach.outgoingResponses.poll();

            if (outgoingMessage != null && outgoingMessage.length > 0) {
                attach.writeBuffer.clear();

                attach.writeBuffer.put(outgoingMessage);

                attach.writeBuffer.flip();

                int cnt = sockCh.write(attach.writeBuffer);

                assert cnt == outgoingMessage.length;
            }
        }

    static class ChannelAttachmant {
        final ByteBuffer readBuffer = ByteBuffer.allocateDirect(32 * 1024);
        final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(32 * 1024);

        //final Queue<byte[]> incomingRequets = new ConcurrentLinkedQueue<>();
        final Queue<byte[]> outgoingResponses = new ConcurrentLinkedQueue<>();
    }


        public void stop() {
            closed = true;
        }
    }
package edu.wallet.client;

import edu.wallet.config.Cfg;
import edu.wallet.config.IConfiguration;
import edu.wallet.server.Const;

import java.io.*;
import java.net.Socket;

/**
 * The Client of Wallet server.
 */
public class Client implements Closeable {

    private final Socket clientSocket;
    private final InputStream is;
    private final OutputStream os;

    public static void main(String[] args) {
        // TODO: make a telenet-like client that would read stdIn as requetsts and write the responses to stdOut.
    }

    public Client() throws IOException {
        IConfiguration c = Cfg.getEntryBean().getConfiguration();
        int port = c.getServerPort();

        // TODO: address should also be configurable.
        clientSocket = new Socket("localhost", port);

        is = clientSocket.getInputStream();
        os = clientSocket.getOutputStream();
    }

    public String send(String msg) throws Exception {
        String m2 = msg;

        if (!m2.endsWith(Const.EOM_MARKER)) {
            m2 = m2.concat(Const.EOM_MARKER);
        }

        byte[] bb = m2.getBytes(Const.UTF8);

        return send(bb);
    }

    public String send(byte[] bb) throws IOException {
        assert bb[bb.length - 1] == 10;

        os.write(bb);

        // TODO: buffer size may be paramatrized.
        ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);

        int b;
        while (true) {
            b = is.read();
            if (b < 0 || b == 10) {
                break; // end of message
            } else {
                baos.write(b);
            }
        }

        byte[] rsp = baos.toByteArray();

        return new String(rsp, Const.UTF8);
    }

    public void close() throws IOException {
        // Not necessary to close the streams, socket closing will close them both.
        clientSocket.close();
    }
}

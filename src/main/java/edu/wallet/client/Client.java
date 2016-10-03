package edu.wallet.client;

import edu.wallet.server.*;
import java.io.*;
import java.net.*;

public class Client implements Closeable {

    private final Socket clientSocket;
    private final InputStream is;
    private final OutputStream os;

    public static void main(String[] args) {}

    public Client() throws IOException {
        clientSocket = new Socket("localhost", 8888);

        is = clientSocket.getInputStream();
        os = clientSocket.getOutputStream();
    }

    public String send(String msg) throws Exception {
        String m2 = msg;

        if (!m2.endsWith(Const.EOM_MARKER)) {
            m2 = m2.concat(Const.EOM_MARKER);
        }

        System.out.println("Written request: [" + msg + "]");

        byte[] bb = m2.getBytes(Const.UTF8);

        return send(bb);
    }

    public String send(byte[] bb) throws IOException {
        assert bb[bb.length - 1] == 10;

        os.write(bb);

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
        clientSocket.close();
    }
}

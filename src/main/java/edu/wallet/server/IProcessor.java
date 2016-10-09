package edu.wallet.server;

import java.io.Closeable;

/**
 * Interface that represents logical processor of the request.
 * Introduced to decouple logical business logic from transport between client and server.
 */
public interface IProcessor extends Closeable {
    /**
     * This method should not throw nay exceptions.
     * TODO: for better efficiency can be refactored into ByteBuffer form.
     *
     * @param request The serialized request.
     * @return The serialized response.
     */
    byte[] process(byte[] request);
}

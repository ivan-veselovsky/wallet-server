package edu.wallet.server;

/**
 *
 */
public interface IProcessor {
    /**
     * This method should not throw nay exceptions.
     * @param request
     * @return
     */
    byte[] process(byte[] request);
}

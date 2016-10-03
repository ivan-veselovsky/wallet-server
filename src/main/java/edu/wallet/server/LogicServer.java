package edu.wallet.server;

import edu.wallet.server.model.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Main procesing business logic resides there.
 */
public class LogicServer implements IProcessor {
    private final Configuration configuration;

    private ILogger logger;

    private final LazyConcurrentMap<String, AtomicReference<ValueObject>> valueObjectLazyMap;

    private final LazyConcurrentMap<Request, Response> processingHistoryLazyMap;

    private static final byte[] badRequestResponse
        = new Response(-1, Const.ErrorCode.badRequest.ordinal(), 0, 0, 0).serialize();

    private static final byte[] generalServerErrorResponse
        = new Response(-1, Const.ErrorCode.InternalServerError.ordinal(), 0, 0, 0).serialize();

    public LogicServer(Configuration c, ILogger logger) {
        assert c != null;
        assert logger != null;

        this.configuration = c;
        this.logger = logger;

        LazyConcurrentMap.ValueFactory<String, AtomicReference<ValueObject>> fac
            = new LazyConcurrentMap.ValueFactory<String, AtomicReference<ValueObject>>() {
            @Override public AtomicReference<ValueObject> createValue(String key) throws IOException {
                // This may be heavy operation since it accesses DB:
                ValueObject vo = getFromDB(key);

                if (vo == null) {
                    vo = new ValueObject(key/*user name*/, 0/*initial account balance*/, 0L/*version*/);
                }
                // Take the value from DB by user name
                return new AtomicReference<>(vo);
            }
        };

        valueObjectLazyMap = new LazyConcurrentMap<>(fac, new ConcurrentHashMap<String, Object>());

        LazyConcurrentMap.ValueFactory<Request, Response> fac2 = new LazyConcurrentMap.ValueFactory<Request, Response>() {
            @Override public Response createValue(Request rq) throws IOException {
                assert rq != null;
                assert rq.userName != null;
                assert rq.userName.length() > 0;

                return process00(rq);
            }
        };

        final int hardLimit = configuration.getMaxHistory();
        final int numThreads = configuration.getNumThreads();

        ConcurrentHashMap<Request, EvictableValue<Request>> map0 = new ConcurrentHashMap<>();
        LimitedConcurrentMap<Request, EvictableValue<Request>> limited = new LimitedConcurrentMap<>(map0, hardLimit,
            numThreads);
        processingHistoryLazyMap = new LazyConcurrentMap<>(fac2, limited);
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    ValueObject getFromDB(String userName) {
        // TODO: gets from DB, null if no such user exists.
        //return null;

        return null;
    }

    @Override public byte[] process(byte[] request) {
        try {
            Request rq = new Request().deserialize(request);

            Response rsp = process(rq);

            final byte[] bb;

            if (rsp == null)
                bb = badRequestResponse;
            else
                bb = rsp.serialize();

            return bb;
        } catch (Exception e) {
            logger.error("Error processing request: ", e);

            // Normally this should not happen,
            // Send "Internal server Error":
            return generalServerErrorResponse;
        }
    }

    /**
     * The main entry point.
     *
     * @param rq
     * @return
     */
    public Response process(Request rq) {
        Response rsp = null;
        Exception e = null;

        try {
            rsp = process0(rq);
        } catch (Exception e0) {
            e = e0;
        }

        logger.info("Rq: " + rq + ", -> Rsp: " + rsp, e);

        return rsp;
    }

    Response process0(Request rq) {
        // 0. Basic validation. Spam requests just ignored.
        if (rq.userName == null || rq.userName.isEmpty()) {
            // Cannot identify user if there is no name.
            // Just ignore such requests.
            return null;
        }

        // 1. Validate the request without getting/saving to model at all:
        Const.ErrorCode errorCode = validate(rq);

        if (errorCode != Const.ErrorCode.Okay) {
            // NB: user cannot know current balance and its version
            // by sending an invalid requests.
            return new Response(rq.transactionId,
                errorCode.ordinal(),
                -1L, // unknown balance version
                0,
                -1); // unknown current balance
        }

        // 2. Now take the actual response either from history, or by direct processing:
        return processingHistoryLazyMap.getOrCreate(rq);
    }

    /**
     * Actual processing related to ValueObject model goes there:
     * @param rq The request.
     * @return The response.
     */
    Response process00(final Request rq) {
        final AtomicReference<ValueObject> voRef = valueObjectLazyMap.getOrCreate(rq.userName);

        assert voRef != null;

        // Once we have the value object, we can actually process the Request.
        // The below code will change the value object for this user:
        while (true) {
            final ValueObject vo = voRef.get();

            assert vo != null; // must be taken from DB or created. Also it is never substituted with null.
            assert rq.userName.equals(vo.userName);

            final int newBalance = vo.currentBalance + rq.balanceChange;

            if (newBalance < 0) {
                // Negative balance case.
                // Notice that ValueObject is not changed in this case.
                return new Response(rq.transactionId, Const.ErrorCode.NegativeBalance.ordinal(), vo.balanceVersion,
                    0/*change*/, vo.currentBalance);
            }

            // inc the balance version:
            final long newBalanceVersion = vo.balanceVersion + 1;

            ValueObject newVo = new ValueObject(rq.userName, newBalance, newBalanceVersion);

            Response rsp = new Response(rq.transactionId, Const.ErrorCode.Okay.ordinal(), newBalanceVersion,
                rq.balanceChange, newBalance);

            if (voRef.compareAndSet(vo, newVo)) {
                return rsp;
            }
        }
    }

    /**
     * Perform "stateless" validation, without the currect value object state.
     * @return error code.
     */
    private Const.ErrorCode validate(Request rq) {
        // 1. check balance change limit
        if (rq.balanceChange == Integer.MIN_VALUE) {
            return Const.ErrorCode.BalanceChangeLimitExceeded;
        }

        int limit = configuration.getMaxBalanceChange();

        assert limit > 0;

        if (Math.abs(rq.balanceChange) > limit) {
            return Const.ErrorCode.BalanceChangeLimitExceeded;
        }

        // 2. check blacklist
        Set<String> blackList = configuration.getBlackList();

        if (blackList.contains(rq.userName)) {
            return Const.ErrorCode.UserBlacklisted;
        }

        return Const.ErrorCode.Okay;
    }
}

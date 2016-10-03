//package edu.wallet.server;
//
//import java.util.concurrent.*;
//
///**
// *
// */
//public class History {
//
//    private Configuration configuration;
//
//    /**
//     * Has semantic very close to {@link ConcurrentMap#putIfAbsent(Object, Object)}.
//     * Requests identity matched by transaction id.
//     * If a response is present in cache for this request, "puts up" that response and returns it.
//     * If a response is given, puts that response as a new value.
//     * Situation, when a response is given, and another response is found in the cache, is considered as an error, and
//     * an exception is throw nin such case.
//     * @param rq The request to query for.
//     * @param rsp The response to cache.
//     * @return Old value, previously mapped to that request.
//     */
//    public Response putIfAbsent(Request rq, /* Nullable */Response rsp) {
//        // TODO: impl
//        return null;
//    }
//
//    public Response get(Request rq) {
//        return null;
//    }
//}

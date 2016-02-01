package com.creatubbles.ctbmod.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * This should be used when submitting a future to a single-thread executor, so the future is guaranteed to be finished
 * prior to <code>call()</code> being executed.
 */
public class CallableFuture<T> implements Callable<T> {

    private final Future<? extends Callable<T>> future;
    
    public CallableFuture(Future<? extends Callable<T>> future) {
        this.future = future;
    }

    @Override
    public T call() throws Exception {
        return future.get().call();
    }
}

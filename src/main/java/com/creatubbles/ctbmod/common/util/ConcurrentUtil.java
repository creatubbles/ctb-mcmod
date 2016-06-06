package com.creatubbles.ctbmod.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class ConcurrentUtil {

    private static final ListeningExecutorService miscTaskExecutor = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() * 2, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));

    public static ListenableFuture<?> execute(Runnable task) {
        return miscTaskExecutor.submit(task);
    }

    public static <T> ListenableFuture<T> execute(Callable<T> task) {
        return miscTaskExecutor.submit(task);
    }
}

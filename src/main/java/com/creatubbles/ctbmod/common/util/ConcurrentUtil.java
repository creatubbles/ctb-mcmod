package com.creatubbles.ctbmod.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
    
    public static void addClientThreadListener(ListenableFuture<?> future, Runnable task) {
        future.addListener(task, new Executor() {
            
            @Override
            public void execute(Runnable command) {
                Minecraft.getMinecraft().addScheduledTask(command);
            }
        });
    }
    
    public static void addServerThreadListener(ListenableFuture<?> future, Runnable task) {
        future.addListener(task, new Executor() {
            
            @Override
            public void execute(Runnable command) {
                FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(command);
            }
        });
    }
}

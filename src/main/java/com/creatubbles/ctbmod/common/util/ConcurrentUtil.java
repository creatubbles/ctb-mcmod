package com.creatubbles.ctbmod.common.util;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraft.client.Minecraft;

public class ConcurrentUtil {

    static {
        FMLCommonHandler.instance().bus().register(new ConcurrentUtil());
    }
    
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
                Minecraft.getMinecraft().func_152344_a(command);
            }
        });
    }
    
    public static void addServerThreadListener(ListenableFuture<?> future, Runnable task) {
        future.addListener(task, new Executor() {
            
            @Override
            public void execute(Runnable command) {
                serverThreadQueue.offer(command);
            }
        });
    }
    
    private static final Queue<Runnable> serverThreadQueue = Queues.newArrayDeque();
    
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        while (!serverThreadQueue.isEmpty()) {
            serverThreadQueue.poll().run();
        }
    }
}

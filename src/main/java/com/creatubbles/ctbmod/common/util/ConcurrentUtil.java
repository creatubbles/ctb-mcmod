package com.creatubbles.ctbmod.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.request.creation.GetCreationRequest;
import com.creatubbles.api.response.creation.GetCreationResponse;
import com.creatubbles.ctbmod.common.http.CreationRelations;

public class ConcurrentUtil {
	
	private static final ExecutorService miscTaskExecutor = 
			new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() * 2, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	public static Future<?> execute(Runnable task) {
		return miscTaskExecutor.submit(task);
	}
	
	public static <T> Future<T> execute(Callable<T> task) {
		return miscTaskExecutor.submit(task);
	}
	
	public static Future<CreationRelations> completeCreation(Creation c) {
		final GetCreationRequest req = new GetCreationRequest(c.getId(), null);
		return miscTaskExecutor.submit(new Callable<CreationRelations>() {
			@Override
			public CreationRelations call() throws Exception {
				GetCreationResponse resp = req.execute().getResponse();
				return new CreationRelations(resp.getCreation(), resp.getRelationships());
			}
		});
	}
}

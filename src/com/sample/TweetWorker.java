package com.sample;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.SubscribeRequest;

public class TweetWorker {

	public static void main(String[] args) {

		ExecutorService executor = Executors.newFixedThreadPool(2);
		for (int i = 0; i < 2; i++) {
			Runnable worker = new WorkerThread();
			executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
			
			
		}
		System.out.println("Finished all threads");
	}

}

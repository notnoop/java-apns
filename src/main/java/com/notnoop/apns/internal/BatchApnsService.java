package com.notnoop.apns.internal;

import static java.util.concurrent.Executors.defaultThreadFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.exceptions.NetworkIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchApnsService extends AbstractApnsService {

    private static final Logger logger = LoggerFactory.getLogger(BatchApnsService.class);

	/**
	 * How many seconds to wait for more messages before batch is send.
	 * Each message reset the wait time
	 * 
	 * @see #maxBatchWaitTimeInSec
	 */
	private int batchWaitTimeInSec = 5;
	
	/**
	 * How many seconds can be batch delayed before execution.
	 * This time is not exact amount after which the batch will run its roughly the time
	 */
	private int maxBatchWaitTimeInSec = 10;
	
	private long firstMessageArrivedTime; 
	
	private ApnsConnection prototype;

	private Queue<ApnsNotification> batch = new ConcurrentLinkedQueue<ApnsNotification>();

	private ScheduledExecutorService scheduleService;
	private ScheduledFuture<?> taskFuture;

	private Runnable batchRunner = new SendMessagesBatch();

    public BatchApnsService(ApnsConnection prototype, ApnsFeedbackConnection feedback, int batchWaitTimeInSec, int maxBachWaitTimeInSec, ThreadFactory tf) {
        this(prototype, feedback, batchWaitTimeInSec, maxBachWaitTimeInSec,
                new ScheduledThreadPoolExecutor(1,
                        tf != null ? tf : defaultThreadFactory()));
    }

    public BatchApnsService(ApnsConnection prototype, ApnsFeedbackConnection feedback, int batchWaitTimeInSec, int maxBachWaitTimeInSec, ScheduledExecutorService executor) {
		super(feedback);
		this.prototype = prototype;
		this.batchWaitTimeInSec = batchWaitTimeInSec;
		this.maxBatchWaitTimeInSec = maxBachWaitTimeInSec;
		this.scheduleService = executor != null ? executor : new ScheduledThreadPoolExecutor(1, defaultThreadFactory());
	}

	public void start() {
		// no code
	}

	public void stop() {
		Utilities.close(prototype);
		if (taskFuture != null) {
			taskFuture.cancel(true);
		}
		scheduleService.shutdownNow();
	}

	public void testConnection() throws NetworkIOException {
		prototype.testConnection();
	}

	@Override
	public void push(ApnsNotification message) throws NetworkIOException {
		if (batch.isEmpty()) {
			firstMessageArrivedTime = System.nanoTime();
		}
		
		long sinceFirstMessageSec = (System.nanoTime() - firstMessageArrivedTime) / 1000 / 1000 / 1000;
		
		if (taskFuture != null && sinceFirstMessageSec < maxBatchWaitTimeInSec) {
			taskFuture.cancel(false);
		}
		
		batch.add(message);
		
		if (taskFuture == null || taskFuture.isDone()) {
			taskFuture = scheduleService.schedule(batchRunner, batchWaitTimeInSec, TimeUnit.SECONDS);
		}
	}

	class SendMessagesBatch implements Runnable {
		public void run() {
			ApnsConnection newConnection = prototype.copy();
			try {
				ApnsNotification msg;
				while ((msg = batch.poll()) != null) {
					try {
						newConnection.sendMessage(msg);
					} catch (NetworkIOException e) {
                        logger.warn("Network exception sending message msg "+ msg.getIdentifier(), e);
                    }
				}
			} finally {
				Utilities.close(newConnection);
			}
		}
	}
}

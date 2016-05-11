/*
 *  Copyright 2009, Mahmood Ali.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following disclaimer
 *      in the documentation and/or other materials provided with the
 *      distribution.
 *    * Neither the name of Mahmood Ali. nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns.internal;

import static java.util.concurrent.Executors.defaultThreadFactory;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.DeliveryError;
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

	@Override
	public Map<String, Set<DeliveryError>> getDeliveryErrorDevices() {
		throw new UnsupportedOperationException();
	}
}

package com.notnoop.apns.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.notnoop.apns.ApnsService;

public class ThreadedApnsService implements ApnsService {

    private ApnsService service;
    private BlockingQueue<ApnsMessage> queue;

    public ThreadedApnsService(ApnsService service) {
        this.service = service;
        this.queue = new LinkedBlockingQueue<ApnsMessage>();
    }

    @Override
    public void push(String deviceToken, String message) {
        push(new ApnsMessage(deviceToken, message));
    }

    @Override
    public void push(ApnsMessage msg) {
        queue.add(msg);
    }

    private Thread thread;
    private volatile boolean shouldContinue;

    @Override
    public void start() {
        service.start();
        if (thread != null)
            stop();
        shouldContinue = true;
        thread = new Thread() {
            public void run() {
                while (shouldContinue) {
                    try {
                        ApnsMessage msg = queue.take();
                        service.push(msg);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    public void stop() {
        shouldContinue = false;
        thread.interrupt();
        service.stop();
    }

}

package com.example.noone.inputmethod;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class InputQueue {

    private static InputQueue queue = null;

    private BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();

    private InputQueue() {

    }

    public static InputQueue getInstance() {
        if (queue == null) {
            queue = new InputQueue();
        }

        return queue;
    }

    public boolean put(String msg) {
        try {
            return blockingQueue.offer(msg, 3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String pull() {
        try {
            return blockingQueue.poll(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

}

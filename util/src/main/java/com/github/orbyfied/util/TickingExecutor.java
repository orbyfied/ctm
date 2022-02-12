package com.github.orbyfied.util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class TickingExecutor implements IExecutor {

    private volatile Queue<Runnable> toDo = new ArrayDeque<>();
    private final Object lock = new Object();
    private AtomicBoolean running = new AtomicBoolean(true);

    public void stop() {
        this.running.set(false);
    }

    @Override
    public void queue(Runnable runnable) {
        toDo.add(runnable);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void tick() {
        Runnable r;
        while ((r = toDo.poll()) != null) {
            r.run();
        }
    }

    public void loop() {
        running.set(true);
        while (running.get()) {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tick();
        }
    }

}

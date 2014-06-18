package com.notnoop.apns.utils.junit;


import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Map;

public class DumpThreadsOnErrorRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new DumpThreadsStatement(base);
    }

    private static class DumpThreadsStatement extends Statement {

        private final Statement base;

        private DumpThreadsStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } catch (Throwable t) {
                dumpAllThreads();
                throw t;
            }
        }

        private void dumpAllThreads() {
            Map liveThreads = Thread.getAllStackTraces();
            for (Object o : liveThreads.keySet()) {
                Thread key = (Thread) o;
                System.err.println("\nThread " + key.getName());
                StackTraceElement[] trace = (StackTraceElement[]) liveThreads.get(key);
                for (StackTraceElement aTrace : trace) {
                    System.err.println("\tat " + aTrace);
                }
            }
        }
    }
}
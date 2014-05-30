package com.notnoop.apns.utils.junit;


import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Iterator;
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
            for (Iterator i = liveThreads.keySet().iterator(); i.hasNext(); ) {
                Thread key = (Thread) i.next();
                System.err.println("\nThread " + key.getName());
                StackTraceElement[] trace = (StackTraceElement[]) liveThreads.get(key);
                for (int j = 0; j < trace.length; j++) {
                    System.err.println("\tat " + trace[j]);
                }
            }
        }
    }
}
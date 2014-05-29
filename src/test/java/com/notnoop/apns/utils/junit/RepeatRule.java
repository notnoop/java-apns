package com.notnoop.apns.utils.junit;


import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RepeatRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        Repeat repeat = description.getAnnotation(Repeat.class);
        if (repeat != null) {
            return new RepeatStatement(repeat.count(), base);
        }
        return base;
    }

    private static class RepeatStatement extends Statement {

        private final int count;
        private final Statement base;

        private RepeatStatement(int count, Statement base) {
            this.count = count;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            for (int i = count; i > 0; i--) {
                base.evaluate();
            }
        }
    }
}
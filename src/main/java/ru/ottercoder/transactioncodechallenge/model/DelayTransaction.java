package ru.ottercoder.transactioncodechallenge.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayTransaction implements Delayed {

    private static final int NUM_OF_SECONDS = 60;

    private double amount;
    private Instant timestamp;

    public double getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(NUM_OF_SECONDS - ChronoUnit.SECONDS.
                between(timestamp, Instant.now()), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed delayed) {
        return delayed == this ?
                0 :
                Long.compare(getDelay(TimeUnit.MILLISECONDS), delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public String toString() {
        return "DelayTransaction{" +
                "amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}

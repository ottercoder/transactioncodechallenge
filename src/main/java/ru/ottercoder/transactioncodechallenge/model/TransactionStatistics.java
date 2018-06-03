package ru.ottercoder.transactioncodechallenge.model;

import java.util.DoubleSummaryStatistics;

public class TransactionStatistics {
    private double sum;
    private double avg;
    private double max;
    private double min;
    private long count;

    public double getSum() {
        return sum;
    }

    public double getAvg() {
        return avg;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public long getCount() {
        return count;
    }

    private void setSum(double sum) {
        this.sum = sum;
    }

    private void setAvg(double avg) {
        this.avg = avg;
    }

    private void setMax(double max) {
        this.max = max;
    }

    private void setMin(double min) {
        this.min = min;
    }

    private void setCount(long count) {
        this.count = count;
    }

    public static TransactionStatistics from(DoubleSummaryStatistics statistics) {
        TransactionStatistics transactionStatistics = new TransactionStatistics();
        transactionStatistics.setCount(statistics.getCount());
        transactionStatistics.setAvg(statistics.getAverage());
        transactionStatistics.setSum(statistics.getSum());
        transactionStatistics.setMin(statistics.getMin());
        transactionStatistics.setMax(statistics.getMax());
        return transactionStatistics;
    }

}

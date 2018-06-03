package ru.ottercoder.transactioncodechallenge.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.ottercoder.transactioncodechallenge.model.DelayTransaction;

import java.util.DoubleSummaryStatistics;
import java.util.concurrent.BlockingQueue;

@Repository
public class TransactionDaoImpl implements TransactionDao {

    private final BlockingQueue<DelayTransaction> delayTransactions;
    private DoubleSummaryStatistics statistics;

    @Autowired
    public TransactionDaoImpl(BlockingQueue<DelayTransaction> delayTransactions) {
        this.delayTransactions = delayTransactions;
        this.statistics = new DoubleSummaryStatistics();
    }

    @Override
    public void addTransaction(DelayTransaction transaction) {
        this.delayTransactions.add(transaction);
    }

    @Override
    public BlockingQueue<DelayTransaction> getTransactions() {
        return this.delayTransactions;
    }

    @Override
    public DoubleSummaryStatistics getStatistics() {
        return this.statistics;
    }

    @Override
    public void updateStatistics(DoubleSummaryStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public void deleteAllTransactions() {
        delayTransactions.clear();
    }
}

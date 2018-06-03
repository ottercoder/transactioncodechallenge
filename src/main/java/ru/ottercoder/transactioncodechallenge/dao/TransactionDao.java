package ru.ottercoder.transactioncodechallenge.dao;

import ru.ottercoder.transactioncodechallenge.model.DelayTransaction;

import java.util.DoubleSummaryStatistics;
import java.util.concurrent.BlockingQueue;

public interface TransactionDao {

    void addTransaction(DelayTransaction transaction);

    BlockingQueue<DelayTransaction> getTransactions();

    DoubleSummaryStatistics getStatistics();

    void updateStatistics(DoubleSummaryStatistics statistics);

    void deleteAllTransactions();
}

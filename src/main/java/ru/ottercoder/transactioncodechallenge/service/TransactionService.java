package ru.ottercoder.transactioncodechallenge.service;

import ru.ottercoder.transactioncodechallenge.model.DelayTransaction;
import ru.ottercoder.transactioncodechallenge.model.TransactionStatistics;

import java.util.DoubleSummaryStatistics;

public interface TransactionService {

    void addTransaction(DelayTransaction transaction);

    TransactionStatistics getStatistics();

    void deleteAllTransactions();

    DoubleSummaryStatistics calculateStatistics();

}

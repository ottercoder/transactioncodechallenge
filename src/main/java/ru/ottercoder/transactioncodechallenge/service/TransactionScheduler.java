package ru.ottercoder.transactioncodechallenge.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ottercoder.transactioncodechallenge.dao.TransactionDao;
import ru.ottercoder.transactioncodechallenge.model.DelayTransaction;

import java.util.concurrent.BlockingQueue;

@Service
public class TransactionScheduler {

    private final TransactionDao transactionDao;
    private final TransactionService transactionService;

    public TransactionScheduler(TransactionDao transactionDao, TransactionService transactionService) {
        this.transactionDao = transactionDao;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedRate = 1)
    public void deleteOldTransactions() {
        BlockingQueue<DelayTransaction> transactions = transactionDao.getTransactions();
        DelayTransaction transaction = new DelayTransaction();
        while (transaction != null) {
            transaction = transactions.poll();
        }
        transactionDao.updateStatistics(transactionService.calculateStatistics());
    }
}

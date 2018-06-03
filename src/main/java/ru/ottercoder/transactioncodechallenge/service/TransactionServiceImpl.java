package ru.ottercoder.transactioncodechallenge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import ru.ottercoder.transactioncodechallenge.dao.TransactionDao;
import ru.ottercoder.transactioncodechallenge.exception.TransactionCameToEarlyException;
import ru.ottercoder.transactioncodechallenge.exception.TransactionTooOldException;
import ru.ottercoder.transactioncodechallenge.model.DelayTransaction;
import ru.ottercoder.transactioncodechallenge.model.TransactionStatistics;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private static final String TRANSACTION_LIFETIME_PARAM = "transaction.lifetime";
    private int transactionLifetime;
    private final TransactionDao transactionDao;

    @Autowired
    public TransactionServiceImpl(TransactionDao transactionDao, Environment env) {
        this.transactionDao = transactionDao;
        transactionLifetime = Integer.parseInt(env.getProperty(TRANSACTION_LIFETIME_PARAM));
    }

    @Override
    public void addTransaction(DelayTransaction transaction) {
        if (isTransactionTimeTooOld(transaction)) {
            LOGGER.warn("Transaction: {} is too old for this", transaction);
            throw new TransactionTooOldException();
        }

        if (didTransactionCameTooEarly(transaction)) {
            LOGGER.warn("Transaction: {} came too early", transaction);
            throw new TransactionCameToEarlyException();
        }

        LOGGER.info("Adding transaction: {}", transaction);
        transactionDao.addTransaction(transaction);
        DoubleSummaryStatistics statistics = this.calculateStatistics();
        LOGGER.info("Stats: {}", statistics);
        transactionDao.updateStatistics(statistics);

    }

    @Override
    public TransactionStatistics getStatistics() {
        return TransactionStatistics.from(transactionDao.getStatistics());
    }

    @Override
    public void deleteAllTransactions() {
        transactionDao.deleteAllTransactions();
    }

    @Override
    public DoubleSummaryStatistics calculateStatistics() {
        LOGGER.info("Getting transactions for last:{} seconds", transactionLifetime);
        BlockingQueue<DelayTransaction> transactions = transactionDao.getTransactions();
        if (transactions.isEmpty()) {
            LOGGER.info("There are no transactions");
            return new DoubleSummaryStatistics();
        }
        return transactions.stream().collect(Collectors.summarizingDouble(DelayTransaction::getAmount));
    }


    private boolean isTransactionTimeTooOld(DelayTransaction transaction) {
        return transaction.getTimestamp().isBefore(Instant.now().minusSeconds(transactionLifetime));
    }

    private boolean didTransactionCameTooEarly(DelayTransaction transaction) {
        return transaction.getTimestamp().isAfter(Instant.now());
    }
}

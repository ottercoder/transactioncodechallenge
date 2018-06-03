package ru.ottercoder.transactioncodechallenge;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ottercoder.transactioncodechallenge.dao.TransactionDao;
import ru.ottercoder.transactioncodechallenge.exception.TransactionTooOldException;
import ru.ottercoder.transactioncodechallenge.model.DelayTransaction;
import ru.ottercoder.transactioncodechallenge.model.TransactionStatistics;
import ru.ottercoder.transactioncodechallenge.service.TransactionService;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionCodeChallengeApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionCodeChallengeApplicationTests.class);
    private static final String TRANSACTION_LIFETIME_PARAM = "transaction.lifetime";
    private static final double DELTA = 1e-15;
    @Autowired
    private TransactionDao transactionDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private Environment env;

    @Test
    public void insertCorrectTransactionsTest() {
        Random random = new Random();
        int numOfTransactions = random.nextInt(100);
        for (int i = 0; i < numOfTransactions; i++) {
            transactionService.addTransaction(createCorrectTransactionWithRndAmount());
        }
        Assert.assertEquals(numOfTransactions, transactionDao.getTransactions().size());
        transactionService.deleteAllTransactions();
    }

    @Test
    public void insertTooOldTransactionsTest() {
        Random random = new Random();
        int numOfCorrectTransactions = random.nextInt(100);
        for (int i = 0; i < numOfCorrectTransactions; i++) {
            transactionService.addTransaction(createCorrectTransactionWithRndAmount());
        }

        int numOfIncorrectTransactions = random.nextInt(10);
        for (int i = 0; i < numOfIncorrectTransactions; i++) {
            try {
                transactionService.addTransaction(createIncorrectTransactionWithRndAmount());
            } catch (TransactionTooOldException e) {
                //do nothing
            }
        }
        Assert.assertEquals(numOfCorrectTransactions, transactionDao.getTransactions().size());
        transactionService.deleteAllTransactions();
    }

    @Test
    public void correctTransactionStatisticsTest() {
        transactionService.addTransaction(createCorrectTransaction(8.00));
        transactionService.addTransaction(createCorrectTransaction(12.50));
        transactionService.addTransaction(createCorrectTransaction(3.75));
        transactionService.addTransaction(createCorrectTransaction(5.25));
        transactionService.addTransaction(createCorrectTransaction(10.50));
        TransactionStatistics statistics = transactionService.getStatistics();

        Assert.assertEquals(5, statistics.getCount());
        Assert.assertEquals(8, statistics.getAvg(), DELTA);
        Assert.assertEquals(12.5, statistics.getMax(), DELTA);
        Assert.assertEquals(3.75, statistics.getMin(), DELTA);
        Assert.assertEquals(40, statistics.getSum(), DELTA);
        transactionService.deleteAllTransactions();
    }

    @Test
    public void expirationOfTransactionsTest() throws InterruptedException {
        transactionService.addTransaction(createCorrectTransaction(8.00));
        transactionService.addTransaction(createCorrectTransaction(12.50));
        transactionService.addTransaction(createCorrectTransaction(3.75));
        transactionService.addTransaction(createCorrectTransaction(5.25));
        transactionService.addTransaction(createCorrectTransaction(10.50));

        DelayTransaction transaction = new DelayTransaction();
        transaction.setAmount(2);
        long transactionLifetime = Long.parseLong(env.getProperty(TRANSACTION_LIFETIME_PARAM));
        transaction.setTimestamp(Instant.now().minusSeconds(transactionLifetime-2));
        transactionService.addTransaction(transaction);

        TransactionStatistics statistics = transactionService.getStatistics();
        Assert.assertEquals(6, statistics.getCount());
        Assert.assertEquals(7, statistics.getAvg(), DELTA);
        Assert.assertEquals(12.5, statistics.getMax(), DELTA);
        Assert.assertEquals(2, statistics.getMin(), DELTA);
        Assert.assertEquals(42, statistics.getSum(), DELTA);

        Semaphore semaphore = new Semaphore(0);
        if (semaphore.tryAcquire(3, TimeUnit.SECONDS)) {
            TransactionStatistics timedStatistics = transactionService.getStatistics();
            Assert.assertEquals(5, timedStatistics.getCount());
            Assert.assertEquals(8, timedStatistics.getAvg(), DELTA);
            Assert.assertEquals(12.5, timedStatistics.getMax(), DELTA);
            Assert.assertEquals(3.75, timedStatistics.getMin(), DELTA);
            Assert.assertEquals(40, timedStatistics.getSum(), DELTA);
        }
        transactionService.deleteAllTransactions();
    }

    @Test
    public void complexityForGettingStatisticsTest() {
        //heating up
        getTimeForNumOfTransactions(100);
        //doing tests
        long lowNumTime = getTimeForNumOfTransactions(100);
        long medNumTime = getTimeForNumOfTransactions(1000);
        long highNumTime = getTimeForNumOfTransactions(10000);
        LOGGER.info("Time of low transactions: {} ", lowNumTime);
        LOGGER.info("Time of medium transactions: {} ", medNumTime);
        LOGGER.info("Time of high transactions: {} ", highNumTime);

        Assert.assertNotEquals(10, Math.abs(medNumTime / lowNumTime), 5);
        Assert.assertNotEquals(100, Math.abs(highNumTime / lowNumTime), 50);
        Assert.assertNotEquals(10, Math.abs(highNumTime / medNumTime), 5);
    }

    private long getTimeForNumOfTransactions(int num) {
        for (int i = 0; i < num; i++) {
            LOGGER.info("{}/{} transactions left", num - i, num);
            transactionService.addTransaction(createCorrectTransactionWithRndAmount());
        }
        long startNum = System.nanoTime();
        TransactionStatistics statistics = transactionService.getStatistics();
        long endNum = System.nanoTime();
        LOGGER.info("Statistics for num: {}. {}", num, statistics);
        transactionService.deleteAllTransactions();
        return endNum - startNum;
    }

    private DelayTransaction createCorrectTransactionWithRndAmount() {
        Random random = new Random();
        return createCorrectTransaction(random.nextDouble());
    }

    private DelayTransaction createCorrectTransaction(double amount) {
        DelayTransaction delayTransaction = new DelayTransaction();
        delayTransaction.setAmount(amount);
        delayTransaction.setTimestamp(Instant.now());
        return delayTransaction;
    }

    private DelayTransaction createIncorrectTransactionWithRndAmount() {
        Random random = new Random();
        return createIncorrectTransaction(random.nextDouble());
    }

    private DelayTransaction createIncorrectTransaction(double amount) {
        DelayTransaction delayTransaction = new DelayTransaction();
        delayTransaction.setAmount(amount);
        delayTransaction.setTimestamp(Instant.now().minusSeconds(Long.parseLong(env.getProperty(TRANSACTION_LIFETIME_PARAM)) + 1L));
        return delayTransaction;
    }

}

package ru.ottercoder.transactioncodechallenge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.ottercoder.transactioncodechallenge.model.DelayTransaction;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

@Configuration
@EnableScheduling
public class TransactionCodeChallengeApplicationConfig {

    @Bean
    public BlockingQueue<DelayTransaction> transactionDelayQueue() {
        return new DelayQueue<>();
    }

}

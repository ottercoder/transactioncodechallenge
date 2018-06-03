package ru.ottercoder.transactioncodechallenge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.ottercoder.transactioncodechallenge.exception.TransactionCameToEarlyException;
import ru.ottercoder.transactioncodechallenge.exception.TransactionTooOldException;
import ru.ottercoder.transactioncodechallenge.model.DelayTransaction;
import ru.ottercoder.transactioncodechallenge.model.TransactionStatistics;
import ru.ottercoder.transactioncodechallenge.service.TransactionService;


@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(path = "/transactions")
    public ResponseEntity addTransaction(@RequestBody DelayTransaction transaction) {
        try {
            transactionService.addTransaction(transaction);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (TransactionTooOldException e) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (TransactionCameToEarlyException e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/statistics")
    public TransactionStatistics getStatistics() {
        return transactionService.getStatistics();
    }
}

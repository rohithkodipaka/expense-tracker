package com.rohithk.expensetracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohithk.expensetracker.entity.Expense;
import com.rohithk.expensetracker.entity.OutboxEvent;
import com.rohithk.expensetracker.repository.ExpenseRepository;
import com.rohithk.expensetracker.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    //private final MonthlyAggregateRepository aggregateRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Expense createExpense(Expense expense) throws JsonProcessingException{
        expense.setCreatedAt(Instant.now());
        Expense savedExpense = expenseRepository.save(expense);

//        int year = savedExpense.getOccurredAt().atZone(ZoneId.systemDefault()).getYear();
//        int month = savedExpense.getOccurredAt().atZone(ZoneId.systemDefault()).getMonthValue();
//        MonthlyAggregate aggregate = aggregateRepository
//                .findByUserIdAndYearAndMonthAndCategory(expense.getUserId(), year, month, expense.getCategory())
//                .orElseGet(()->{
//                    MonthlyAggregate a = new MonthlyAggregate();
//                    a.setUserId(expense.getUserId());
//                    a.setMonth(month);
//                    a.setYear(year);
//                    a.setCategory(expense.getCategory());
//                    return a;
//                });
//        BigDecimal totalAmount = aggregate.getTotalAmount().add(expense.getAmount());
//        aggregate.setTotalAmount(totalAmount);
//        aggregate.setUpdatedAt(Instant.now());
//        aggregateRepository.save(aggregate);

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Expense");
        event.setAggregateId(expense.getId());
        event.setEventType("EXPENSE_CREATED");
        event.setPayloadJson(objectMapper.writeValueAsString(savedExpense));
        event.setCreatedAt(Instant.now());
        event.setPublished(false);
        outboxRepository.save(event);

        //Publish the event asynchronously after the above commit successful.
        eventPublisher.publishEvent(event);
        return savedExpense;
    }


}

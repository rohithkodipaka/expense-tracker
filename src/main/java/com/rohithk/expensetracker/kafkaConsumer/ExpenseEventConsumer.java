package com.rohithk.expensetracker.kafkaConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohithk.expensetracker.entity.Expense;
import com.rohithk.expensetracker.entity.MonthlyAggregate;
import com.rohithk.expensetracker.entity.OutboxEvent;
import com.rohithk.expensetracker.repository.MonthlyAggregateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MonthlyAggregateRepository aggregateRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.expenses}", groupId = "expense-group")
    @Transactional
    public void consume(OutboxEvent event) throws JsonProcessingException {

        // Only process expense creation events
        if (!"EXPENSE_CREATED".equals(event.getEventType())) {
            return;
        }

        // Extract expense payload
        Expense expense = objectMapper.readValue(event.getPayloadJson(), Expense.class);

        // Compute aggregate keys
        int year = expense.getOccurredAt().atZone(ZoneId.systemDefault()).getYear();
        int month = expense.getOccurredAt().atZone(ZoneId.systemDefault()).getMonthValue();
        String category = expense.getCategory();
        UUID userId = expense.getUserId();

        // Fetch existing aggregate or create new
        MonthlyAggregate aggregate = aggregateRepository
                .findByUserIdAndYearAndMonthAndCategory(userId, year, month, category)
                .orElseGet(() -> {
                    MonthlyAggregate a = new MonthlyAggregate();
                    a.setUserId(userId);
                    a.setYear(year);
                    a.setMonth(month);
                    a.setCategory(category);
                    a.setTotalAmount(BigDecimal.ZERO);
                    return a;
                });

        // Update aggregate total
        aggregate.setTotalAmount(aggregate.getTotalAmount().add(expense.getAmount()));
        aggregate.setUpdatedAt(Instant.now());

        // Save back to DB
        MonthlyAggregate savedAggregate = aggregateRepository.save(aggregate);

        //publish to Websocket topic
        simpMessagingTemplate.convertAndSend("/topic/aggregates",savedAggregate);
        log.info("Aggregate successfully pushed to Web Socket"+savedAggregate);
    }
}

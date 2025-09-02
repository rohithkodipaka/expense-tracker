package com.rohithk.expensetracker.kafkaProducer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohithk.expensetracker.entity.OutboxEvent;
import com.rohithk.expensetracker.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {
    public final KafkaTemplate<String,OutboxEvent> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.expenses}")
    private String expensesTopic;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void publishEvent(OutboxEvent event) throws JsonProcessingException{
        kafkaTemplate.send(expensesTopic, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        // success
                        event.setPublished(true);
                        event.setPublishedAt(Instant.now());
                        outboxEventRepository.save(event);
                    } else {
                        // failure
                        System.err.println("Failed to publish event: " + event.getId() + ", " + ex.getMessage());
                    }
                });
    }

//    @Scheduled(fixedDelay = 5000)
//    public void retryFailedEvents() {
//        List<OutboxEvent> failedEvents = outboxEventRepository.findByPublishedFalse();
//        for (OutboxEvent event : failedEvents) {
//            try {
//                kafkaTemplate.send("expenses", objectMapper.writeValueAsString(event))
//                        .get(); // synchronous send ensures reliable retry
//                event.setPublished(true);
//                event.setPublishedAt(Instant.now());
//                outboxEventRepository.save(event);
//                log.info("Published outbox event: {}",event.getId());
//            } catch (Exception ex) {
//                // Event remains published=false for next scheduled retry
//                log.error("Failed to publish outbox event: {}",event.getId(), ex);
//            }
//        }
//    }
}

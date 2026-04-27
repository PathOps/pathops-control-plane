package io.pathops.controlplane.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final OutboxPublisherLockService lockService;
    private final OutboxEventQueryService queryService;
    private final RabbitOutboxPublisher publisher;
    private final OutboxEventResultService resultService;

    @Value("${pathops.events.outbox.publisher.batch-size}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${pathops.events.outbox.publisher.fixed-delay-ms}")
    public void publishPendingEvents() {
        boolean locked = lockService.tryLock();

        if (!locked) {
            return;
        }

        try {
            var events = queryService.findPendingBatch(batchSize);

            for (var event : events) {
                try {
                    publisher.publish(event);
                    resultService.markPublished(event.getId());

                    log.info(
                        "Published outbox event. eventId={}, eventType={}",
                        event.getEventId(),
                        event.getEventType()
                    );
                } catch (Exception e) {
                    resultService.markPublishFailed(event.getId(), e.getMessage());

                    log.warn(
                        "Failed to publish outbox event. eventId={}, eventType={}",
                        event.getEventId(),
                        event.getEventType(),
                        e
                    );
                }
            }
        } finally {
            lockService.unlock();
        }
    }
}
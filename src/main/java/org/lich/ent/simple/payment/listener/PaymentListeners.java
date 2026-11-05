package org.lich.ent.simple.payment.listener;

import org.lich.ent.simple.payment.config.KafkaConfig;
import org.lich.ent.simple.payment.model.PaymetNotice;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentListeners {
    private static final Logger log = LoggerFactory.getLogger(PaymentListeners.class);

    @KafkaListener(topics = "#{environment.getProperty('app.topics.payment-plain-v1')}", containerFactory = "stringListenerFactory")
    public void onString(ConsumerRecord<String, String> rec) {
        log.info("[STRING] p={} o={} key={} value='{}'", rec.partition(), rec.offset(), rec.key(), rec.value());
    }

    @KafkaListener(topics = "#{environment.getProperty('app.topics.payment-json-v1')}", containerFactory = "jsonSchemaListenerFactory")
    public void onJson(ConsumerRecord<String, PaymetNotice> rec) {
        log.info("[JSON-SCHEMA] p={} o={} key={} value={}", rec.partition(), rec.offset(), rec.key(), rec.value());
    }
}

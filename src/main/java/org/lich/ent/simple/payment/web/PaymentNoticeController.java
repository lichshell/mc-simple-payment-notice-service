package org.lich.ent.simple.payment.web;

import org.lich.ent.simple.payment.config.KafkaConfig;
import org.lich.ent.simple.payment.model.PaymetNotice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentNoticeController {

    @Value("${app.topics.payment-plain-v1}")
    private String paymentPlainTopic;

    @Value("${app.topics.payment-json-v1}")
    private String paymentJsonTopic;
    private final KafkaTemplate<String, String> stringTemplate;
    private final KafkaTemplate<String, PaymetNotice> jsonSchemaTemplate;

    public PaymentNoticeController(KafkaTemplate<String, String> stringKafkaTemplate,
                                   KafkaTemplate<String, PaymetNotice> jsonSchemaKafkaTemplate) {
        this.stringTemplate = stringKafkaTemplate;
        this.jsonSchemaTemplate = jsonSchemaKafkaTemplate;
    }

    /** Plain string flow: body is sent as-is, e.g. "Rent for November". */
    @PostMapping(value = "/string", consumes = "text/plain")
    public ResponseEntity<Map<String, Object>> sendString(@RequestBody String text,
                                                          @RequestParam(defaultValue = "acct-001") String key) {
        var result = stringTemplate.send(paymentPlainTopic, key, text).join();
        var md = result.getRecordMetadata();
        return ResponseEntity.ok(Map.of("topic", md.topic(), "partition", md.partition(), "offset", md.offset()));
    }

    /** JSON Schema flow: JSON body is bound to PaymetNotice and serialised via Schema Registry. */
    @PostMapping("/json-schema")
    public ResponseEntity<Map<String, Object>> sendJson(@RequestBody PaymetNotice note) {
        if (note.getPaymentId() == null) note.setPaymentId(UUID.randomUUID().toString());
        if (note.getCreatedAt() == null) note.setCreatedAt(Instant.now().toString());
        var result = jsonSchemaTemplate.send(paymentJsonTopic, note.getPaymentId(), note).join();
        var md = result.getRecordMetadata();
        return ResponseEntity.ok(Map.of("topic", md.topic(), "partition", md.partition(), "offset", md.offset(),
                "paymentId", note.getPaymentId()));
    }

    /** Convenience endpoint that sends a sample of each type. */
    @PostMapping("/demo")
    public Map<String, String> demo() {
        stringTemplate.send(paymentPlainTopic, "acct-001", "Demo plain string payment note");
        var note = new PaymetNotice(UUID.randomUUID().toString(), "alice", "bob",
                new BigDecimal("125.50"), "AUD", "Dinner split", Instant.now().toString());
        jsonSchemaTemplate.send(paymentJsonTopic, note.getPaymentId(), note);
        return Map.of("status", "sent", "string", "payments-string", "json", "payments-json");
    }
}

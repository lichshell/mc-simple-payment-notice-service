package org.lich.ent.simple.payment.config;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializerConfig;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.lich.ent.simple.payment.model.PaymetNotice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${app.topics.payment-plain-v1}")
    private String paymentPlainTopic;

    @Value("${app.topics.payment-json-v1}")
    private String paymentJsonTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.schema-registry-url}")
    private String schemaRegistryUrl;

    @Value("${app.consumers.payment-plain-cg-v1}")
    private String consumersPaymentPlainCg;
    @Value("${app.consumers.payment-json-cg-v1}")
    private String consumersPaymentJsonnCg;

    @Bean
    NewTopic paymentsStringTopic() {
        return TopicBuilder.name(paymentPlainTopic).partitions(3).replicas(3).build();
    }

    @Bean
    NewTopic paymentsJsonTopic() {
        return TopicBuilder.name(paymentJsonTopic).partitions(3).replicas(3).build();
    }

    // =====================================================================
    // 1) PLAIN STRING serde (no Schema Registry involved)
    // =====================================================================
    @Bean
    ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> p = new HashMap<>();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(p);
    }

    @Bean
    KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    @Bean
    ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> c = new HashMap<>();
        c.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        c.put(ConsumerConfig.GROUP_ID_CONFIG, consumersPaymentPlainCg);
        c.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        c.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        c.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(c);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> stringListenerFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, String>();
        f.setConsumerFactory(stringConsumerFactory());
        return f;
    }

    // =====================================================================
    // 2) JSON SCHEMA serde (Confluent Schema Registry)
    // =====================================================================
    @Bean
    ProducerFactory<String, PaymetNotice> jsonSchemaProducerFactory() {
        Map<String, Object> p = new HashMap<>();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class);
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        p.put("schema.registry.url", schemaRegistryUrl);
        p.put("auto.register.schemas", true);          // dev convenience; set false in prod
        p.put("use.latest.version", false);
        // Optional: reject payloads that violate the schema at produce time
        p.put("json.fail.invalid.schema", true);
        return new DefaultKafkaProducerFactory<>(p);
    }

    @Bean
    KafkaTemplate<String, PaymetNotice> jsonSchemaKafkaTemplate() {
        return new KafkaTemplate<>(jsonSchemaProducerFactory());
    }

    @Bean
    ConsumerFactory<String, PaymetNotice> jsonSchemaConsumerFactory() {
        Map<String, Object> c = new HashMap<>();
        c.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        c.put(ConsumerConfig.GROUP_ID_CONFIG, consumersPaymentJsonnCg);
        c.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        c.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        c.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class);
        c.put("schema.registry.url", schemaRegistryUrl);
        // Without this the deserializer returns a generic JsonNode instead of PaymetNotice
        c.put(KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE, PaymetNotice.class.getName());
        return new DefaultKafkaConsumerFactory<>(c);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PaymetNotice> jsonSchemaListenerFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, PaymetNotice>();
        f.setConsumerFactory(jsonSchemaConsumerFactory());
        return f;
    }
}

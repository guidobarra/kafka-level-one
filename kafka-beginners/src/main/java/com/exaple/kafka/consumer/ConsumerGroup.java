package com.exaple.kafka.consumer;

import com.exaple.kafka.UtilsConfigKafka;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerGroup {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerGroup.class);

    // execute in parallel
    // 3 consumer and 1 producer, all java
    public static void main(String[] args) {

        // create consumer properties
        Properties properties =
                UtilsConfigKafka.getPropertiesConfigurationConsumer(UtilsConfigKafka.BOOTSTRAP_SERVER, "my-three-app");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // subscribe consumer to our topics
        consumer.subscribe(Arrays.asList(UtilsConfigKafka.NAME_TOPIC));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100L));

            for (ConsumerRecord<String, String> record: records) {
                LOG.info("Key: {}, Value: {}", record.key(), record.value());
                LOG.info("Partition: {}, Offset: {}", record.partition(), record.offset());
            }
        }
    }
}

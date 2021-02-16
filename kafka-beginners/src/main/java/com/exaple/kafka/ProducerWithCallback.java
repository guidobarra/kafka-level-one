package com.exaple.kafka;

import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerWithCallback {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerWithCallback.class);

    public static void main(String[] args) {

        //create Producer properties
        Properties properties = UtilsConfig.getPropertiesConfigurationKafka();

        //create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for(int i = 0; i <10; i++) {
            sendMessage(producer, "message N°" + i);
        }

        producer.flush();
        producer.close();

    }

    private static void sendMessage(KafkaProducer<String, String> producer, String message) {
        //create a producer record
        ProducerRecord<String, String> record =
                new ProducerRecord<>("first_topic", message);

        //send data - asynchronous
        producer.send(record, (recordMetadata, exception) -> {
            //executes every time a record is successfully sent or an exception is thrown
            if (exception == null) {
                //the record was successfully sent
                String sb = "Received new." +
                        "\n" +
                        "Topic:" +
                        recordMetadata.topic() +
                        "\n" +
                        "Partition:" +
                        recordMetadata.partition() +
                        "\n" +
                        "Offset:" +
                        recordMetadata.offset() +
                        "\n" +
                        "Timestamp" +
                        recordMetadata.timestamp();

                LOG.info(sb);
            } else {
                LOG.error("Error while producing", exception);
            }
        });
    }
}

package com.exaple.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerWithKeyCallback {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerWithKeyCallback.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //create Producer properties
        Properties properties = UtilsConfig.getPropertiesConfigurationKafka();

        //create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for(int i = 0; i <10; i++) {
            String message = "hello, message N°" + i;
            String key = "id_" + i;
            LOG.info("KEY: {}", key);
            //* with the key it will always go to the same partition
            // my context, one topic, first_topic, and three partitions
            // id_0 -> Partition:1
            // id_1 -> Partition:0
            // id_2 -> Partition:2
            // id_3 -> Partition:0
            // id_4 -> Partition:2
            // id_5 -> Partition:2
            // id_6 -> Partition:0
            // id_7 -> Partition:2
            // id_8 -> Partition:1
            // id_9 -> Partition:2
            // */
            sendMessage(producer, message, key);
        }

        producer.flush();
        producer.close();

    }

    private static void sendMessage(KafkaProducer<String, String> producer, String message, String key) throws ExecutionException, InterruptedException {
        //create a producer record
        ProducerRecord<String, String> record =
                new ProducerRecord<>("first_topic", key, message);

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
        }).get(); //block the send() to make it synchronous - DO NOT DO THIS PRODUCTION!!, BAD PERFORMANCE
    }
}

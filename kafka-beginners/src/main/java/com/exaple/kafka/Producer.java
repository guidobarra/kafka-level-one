package com.exaple.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {

    public static void main(String[] args) {
        //create Producer properties
        Properties properties = UtilsConfig.getPropertiesConfigurationKafka();

        //create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        //create a producer record
        ProducerRecord<String, String> record =
                new ProducerRecord<>("first_topic", "hello world, GUBA!!");

        //send data - asynchronous
        producer.send(record);


        producer.flush();
        producer.close();

    }
}

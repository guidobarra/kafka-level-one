package com.exaple.kafka.producer;

import com.exaple.kafka.UtilsConfigKafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer {

    public static void main(String[] args) {
        //create Producer properties
        Properties properties = UtilsConfigKafka.getPropertiesConfigurationProducer();

        //create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        //create a producer record
        ProducerRecord<String, String> record =
                new ProducerRecord<>(UtilsConfigKafka.NAME_TOPIC, "hello world, GUBA!!");

        //send data - asynchronous
        producer.send(record);


        producer.flush();
        producer.close();

    }
}

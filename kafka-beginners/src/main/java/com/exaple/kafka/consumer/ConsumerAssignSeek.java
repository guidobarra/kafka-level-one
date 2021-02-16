package com.exaple.kafka.consumer;

import com.exaple.kafka.UtilsConfigKafka;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerAssignSeek {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerAssignSeek.class);

    //
    //Para permitir que los consumidores de un grupo reanuden su actividad
    //con el desplazamiento correcto, necesito configurar group.id y no auto.offset.resets


    //Usualmente usamos .assign () y .seek () para
    //capacidad de reproducción en un cierto desplazamiento

    // execute in parallel
    // 3 consumer and 1 producer, all java
    public static void main(String[] args) {

        // create consumer properties
        Properties properties =
                UtilsConfigKafka.getPropertiesConfigurationConsumer(UtilsConfigKafka.BOOTSTRAP_SERVER);

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        //assign and seek are mostly used to replay data or fetch a  specific message

        //assign, read messages to go offset 15 and partitions 0
        TopicPartition partitionToReadFrom = new TopicPartition(UtilsConfigKafka.NAME_TOPIC, 0);
        long offsetToReadFrom = 15L;
        consumer.assign(Arrays.asList(partitionToReadFrom));

        //seek
        consumer.seek(partitionToReadFrom, offsetToReadFrom);

        int countOfMessagesToRead = 5;
        boolean keepOnReading = true;
        int countOfMessagesReadSoFar = 0;

        //read messages to from partitions 0 and starting offset 15, only read 5 messages
        while (countOfMessagesReadSoFar < countOfMessagesToRead) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100L));

            for (ConsumerRecord<String, String> record: records) {
                countOfMessagesReadSoFar++;
                LOG.info("Key: {}, Value: {}", record.key(), record.value());
                LOG.info("Partition: {}, Offset: {}", record.partition(), record.offset());
                if (countOfMessagesReadSoFar >= countOfMessagesToRead) {
                    break;
                }
            }
        }
    }
}

package com.exaple.kafka.consumer;

import com.exaple.kafka.UtilsConfigKafka;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerWithThread {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerWithThread.class);

    private ConsumerWithThread() {}

    public static void main(String[] args) {
        new ConsumerWithThread().run();
    }

    private void run() {
        // create consumer properties
        Properties properties =
                UtilsConfigKafka.getPropertiesConfigurationConsumer(UtilsConfigKafka.BOOTSTRAP_SERVER, "my-four-app");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // subscribe consumer to our topics
        consumer.subscribe(Arrays.asList(UtilsConfigKafka.NAME_TOPIC));

        //latch for dealing with multiples thread
        CountDownLatch latch = new CountDownLatch(1);

        //create the consumer runnable
        LOG.info("Creating the consumer thread");
        ConsumerRunnable consumerRunnable = new ConsumerRunnable(latch, consumer);

        //start the thread
        Thread myThread = new Thread(consumerRunnable);
        myThread.start();

        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            LOG.info("Caught shutdown hook");
            consumerRunnable.shutdown();

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Application tha exited");

        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Application got interrupted", e);
        } finally {
            LOG.info("Application is closing");
        }
    }

    public class ConsumerRunnable implements Runnable {

        private final Logger LOG = LoggerFactory.getLogger(ConsumerRunnable.class);

        //for concurrent, helper to close our app correctly
        private final CountDownLatch latch;

        private final KafkaConsumer<String, String> consumer;

        public ConsumerRunnable(CountDownLatch latch, KafkaConsumer<String, String> consumer) {
            this.latch = latch;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100L));

                    for (ConsumerRecord<String, String> record: records) {
                        LOG.info("Key: {}, Value: {}", record.key(), record.value());
                        LOG.info("Partition: {}, Offset: {}", record.partition(), record.offset());
                    }
                }
            } catch (WakeupException e) {
                LOG.info("Received shutdown signal!! APAGADO");
            } finally {
                consumer.close();
                //tell our main code we are done with consumer, terminamos con el consumidor
                latch.countDown();
            }
        }

        //despertar
        public void shutdown() {
            // the wakeup() method is a special method to interrupt consumer.poll()
            // it will throw the exception WakeUpException
            consumer.wakeup();
        }
    }
}

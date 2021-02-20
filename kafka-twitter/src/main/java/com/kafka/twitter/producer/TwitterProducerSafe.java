package com.kafka.twitter.producer;

import com.google.common.collect.Lists;
import com.kafka.twitter.config.UtilsProperties;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducerSafe {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterProducerSafe.class);

    public TwitterProducerSafe() {}

    public static void main(String[] args) {
        new TwitterProducerSafe().run();
    }

    public void run() {

        LOG.info("Setup");

        /* Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(1000);

        //create a twitter client
        Client client = createTwitterClient(msgQueue);

        // Attempts to establish a connection.
        client.connect();

        //Create a kafka producer
        KafkaProducer<String, String> producerKafka = createKafkaProducer();

        // add a shutdown producer
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            LOG.info("stopping application.....");

            LOG.info("shutting down client from twitter ....");
            client.stop();

            LOG.info("closing producer...");
            producerKafka.close();
            LOG.info("done!");
        }));

        // loop to send tweets to kafka
        // on a different thread, or multiple different threads....
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }

            if (msg != null) {
                LOG.info(msg);
                //send msj to kafka
                producerKafka.send(new ProducerRecord<>("topic_twitter", null, msg), (r, e) -> {
                    if (e != null) {
                        LOG.error("something bad", e);
                    }
                });
            }
        }

        LOG.info("End of Application");
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue) {
        /* Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        List<String> terms = Lists.newArrayList("kafka", "java");
        //hosebirdEndpoint.followings(followings);
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(UtilsProperties.CONSUMER_KEY,
                                                 UtilsProperties.CONSUMER_SECRET,
                                                 UtilsProperties.TOKEN,
                                                 UtilsProperties.SECRET);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")// optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));
                //.eventMessageQueue(eventQueue);// optional: use this if you want to process client events

        return builder.build();
    }

    public KafkaProducer<String, String> createKafkaProducer() {
        //create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //create safe producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        return new KafkaProducer<>(properties);
    }
}

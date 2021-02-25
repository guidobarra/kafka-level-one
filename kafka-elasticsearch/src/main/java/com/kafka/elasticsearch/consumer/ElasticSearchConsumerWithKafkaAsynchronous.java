package com.kafka.elasticsearch.consumer;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;


public class ElasticSearchConsumerWithKafkaAsynchronous {

    private final static Logger LOG = LoggerFactory.getLogger(ElasticSearchConsumerWithKafkaAsynchronous.class);

    private static final JsonParser jsonParser = new JsonParser();

    public static void main(String[] args) throws IOException, InterruptedException {
        RestHighLevelClient client = createClient();

        KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets");

        while (true) {

            //send teen records/messages due to max.poll.records=10
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100L));

            Integer countRecords = records.count();
            LOG.info("Received " + countRecords + " records");

            //for processes for batching
            BulkRequest bulkRequest = new BulkRequest();

            for (ConsumerRecord<String, String> record: records) {
                try {
                    // two strategies
                    // kafka generic ID
                    //String id = record.topic() + "_"  + record.partition() + "_"  + record.offset();

                    // twitter feed specific id, is generate to elasticsearch
                    String id = extractIdFromTweet(record.value());

                    //where we insert data into ElasticSearch
                    IndexRequest indexRequest = new IndexRequest("twitter")
                                                    .type("tweets")
                                                    .id(id) /*this is to make our consumer idempotent*/
                                                    .source(record.value(), XContentType.JSON);
                    bulkRequest.add(indexRequest);
                } catch (NullPointerException e) {
                    LOG.warn("skipping bad data: " + record.value());
                }
            }

            if (countRecords>0) {
                BulkResponse bulkItemResponses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                LOG.info("Committing offsets....");
                consumer.commitAsync();
                LOG.info("Offsets have been committed");
                Thread.sleep(1000);
            }

        }

        //client.close();

    }

    public static RestHighLevelClient createClient() {

        final String hostName = "kafka-beginner-6975677498.eu-west-1.bonsaisearch.net";
        final String userName = "b289a83w3c";
        final String password = "f8uat70uf8";
        final int port = 443;
        final String scheme = "https";

        //do not do if you run a local ES
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(hostName, port, scheme))
                .setHttpClientConfigCallback( httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        return new RestHighLevelClient(builder);
    }

    public static KafkaConsumer<String, String> createConsumer(String topic) {

        final String bootstrapServer = "localhost:9092";
        final String groupId = "kafka-demo-elasticsearch";

        //create Producer consumer
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");//manually commit offsets and Asynchronous
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");//package teen messages send

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(topic));

        return consumer;
    }

    private static String extractIdFromTweet(String value) {
        // gson library
        return jsonParser
                .parse(value)
                .getAsJsonObject()
                .get("id_str")
                .getAsString();
    }

}
package com.kafka.streams;

import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class StreamsFilterTweets {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private static final String APPLICATION_ID = "demo-kafka-streams";

    private static final String TWITTER_TWEETS = "twitter_tweets";

    private static final String TOPIC_FILTER_TWEETS = "important_tweets";

    public static void main(String[] args) {

        //create properties
        Properties p = new Properties();
        p.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        p.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        p.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        p.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

        //create a topology
        StreamsBuilder sb = new StreamsBuilder();

        //input topic
        KStream<String, String> inputTopic = sb.stream(TWITTER_TWEETS);
        KStream<String, String> filteredStream = inputTopic.filter(
                //filter for tweets which has a user of over 10000 followers
                (k, json) -> extractUserFollowersInTweet(json).compareTo(10000) > 0
        );

        //sent tweets more 10000 followers to other topic
        filteredStream.to(TOPIC_FILTER_TWEETS);

        //build the topology
        KafkaStreams ks = new KafkaStreams(sb.build(), p);

        //start our streams application
        ks.start();
    }

    private static Integer extractUserFollowersInTweet(String tweetJson) {
        try {
            return JSON_PARSER.parse(tweetJson)
                    .getAsJsonObject()
                    .get("user")
                    .getAsJsonObject()
                    .get("followers_count")
                    .getAsInt();
        } catch (NullPointerException e) {
            return 0;
        }
    }
}

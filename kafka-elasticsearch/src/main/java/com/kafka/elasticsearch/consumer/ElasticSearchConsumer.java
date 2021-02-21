package com.kafka.elasticsearch.consumer;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
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


public class ElasticSearchConsumer {

    private final static Logger LOG = LoggerFactory.getLogger(ElasticSearchConsumer.class);

    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = createClient();

        String json = "{ \"course\": \"kafka version 2.0.0\", \n \"language\": \"java\"}";

        IndexRequest indexRequest = new IndexRequest("twitter", "tweets")
                .source(json, XContentType.JSON);

        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        LOG.info("response id: {}", indexResponse.getId());

        client.close();

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

}

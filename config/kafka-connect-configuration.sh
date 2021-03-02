#!/bin/bash

sleep 15.0s

usr/bin/kafka-topics --create --topic "$KAFKA_TOPIC_STATUS" --partitions 1 --replication-factor 1 --if-not-exists --zookeeper "$ZOOKEEPER_HOSTS"

usr/bin/kafka-topics --create --topic "$KAFKA_TOPIC_DELETE" --partitions 1 --replication-factor 1 --if-not-exists --zookeeper "$ZOOKEEPER_HOSTS"

usr/bin/connect-standalone /etc/kafka/connect-standalone-twitter.properties /etc/kafka/connect-twitter.properties
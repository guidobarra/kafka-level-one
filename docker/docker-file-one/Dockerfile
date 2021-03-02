FROM confluentinc/cp-kafka:5.0.0

COPY ./config/kafka-connect-configuration.sh /usr/bin/
COPY ./config/*.properties /etc/kafka/
COPY ./kafka-connect-twitter-0.3.33 /opt/

RUN cd /usr/share/java/kafka && \
    ln -s /opt/lib sftp

CMD ["/usr/bin/kafka-connect-configuration.sh"]

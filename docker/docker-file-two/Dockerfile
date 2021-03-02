FROM confluentinc/cp-kafka:5.0.0

COPY ./config/kafka-connect-configuration.sh /usr/bin/
COPY ./config/*.properties /etc/kafka/

ADD https://d1i4a15mxbxib1.cloudfront.net/api/plugins/jcustenborder/kafka-connect-twitter/versions/0.3.33/jcustenborder-kafka-connect-twitter-0.3.33.zip /opt/

RUN apt-get update && apt-get install unzip
RUN unzip /opt/jcustenborder-kafka-connect-twitter-0.3.33.zip -d /opt/ && \ 
    mv /opt/jcustenborder-kafka-connect-twitter-0.3.33/* /opt/ && \
    rm /opt/jcustenborder-kafka-connect-twitter-0.3.33.zip && \
    rmdir /opt/jcustenborder-kafka-connect-twitter-0.3.33

RUN cd /usr/share/java/kafka && \
    ln -s /opt/lib sftp

CMD ["/usr/bin/kafka-connect-configuration.sh"]
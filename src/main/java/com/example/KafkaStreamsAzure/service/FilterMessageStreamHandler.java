package com.example.KafkaStreamsAzure.service;

import com.example.KafkaStreamsAzure.util.FilterUtils;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class FilterMessageStreamHandler {


    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.stream.filter.app.id}")
    private String appId;


    @Value("${kafka.input.topic.name}")
    private String inputTopicName;

    public Properties kStreamsConfigs() {
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @PostConstruct
    public void handle() {

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> xmlStream = builder.stream(inputTopicName, Consumed.with(Serdes.String(), Serdes.String()));
        KStream<String, String> filteredStream = xmlStream.mapValues(FilterUtils::extractModuleFromXmlMessage);
        filteredStream.to((key, value, recordContext) -> value, Produced.with(Serdes.String(), Serdes.String()));


        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, kStreamsConfigs());
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }


}

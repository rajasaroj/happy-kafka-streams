package com.example.KafkaStreamsAzure.config;


import com.example.KafkaStreamsAzure.util.FilterUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.CleanupConfig;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
public class KafkaStreamsConfig {
    private static final Logger logger =  LoggerFactory.getLogger(KafkaStreamsConfig.class);
    private final KafkaProperties kafkaProperties;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.input.topic.name}")
    private String inputTopicName;

    @Value("${kafka.stream.filter.app.id}")
    private String appId;

    public KafkaStreamsConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String, String> kStream(StreamsBuilder kStreamBuilder) {
        KStream<String, String> xmlStream = kStreamBuilder.stream("Desk");
        KStream<String, String> filteredStream = xmlStream.mapValues(FilterUtils::extractModuleFromXmlMessage);
        filteredStream.to((key, value, recordContext) -> value, Produced.with(Serdes.String(), Serdes.String()));
        return filteredStream;
    }

    @Bean
    public CleanupConfig cleanupConfig() {
        return new CleanupConfig(true, true);
    }

}

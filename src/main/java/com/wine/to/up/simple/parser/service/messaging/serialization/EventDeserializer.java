package com.wine.to.up.simple.parser.service.messaging.serialization;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wine.to.up.demo.service.api.message.KafkaMessageSentEventOuterClass.KafkaMessageSentEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Deserializer for {@link KafkaMessageSentEvent}
 */
@Slf4j
public class EventDeserializer implements Deserializer<String> {
    /**
     * {@inheritDoc}
     */
//    @Override
//    public KafkaMessageSentEvent deserialize(String topic, byte[] bytes) {
//        try {
//            return KafkaMessageSentEvent.parseFrom(bytes);
//        } catch (InvalidProtocolBufferException e) {
//            log.error("Failed to deserialize message from topic: {}. {}", topic, e);
//            return null;
//        }
//    }
    @Override
    public String deserialize(String topic, byte[] bytes) {
        try {
            return new String(bytes);
        } catch (Exception e) {
            log.error("Failed to deserialize message from topic: {}. {}", topic, e);
            return null;
        }
    }
}

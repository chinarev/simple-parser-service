package com.wine.to.up.simple.parser.service.messaging;

import com.wine.to.up.commonlib.messaging.KafkaMessageHandler;
import com.wine.to.up.commonlib.messaging.KafkaMessageSender;
import com.wine.to.up.demo.service.api.message.KafkaMessageSentEventOuterClass.KafkaMessageSentEvent;
import com.wine.to.up.simple.parser.service.domain.entity.Message;
import com.wine.to.up.simple.parser.service.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class StringTopicKafkaMessageHandler implements KafkaMessageHandler<String> {
    //private final MessageRepository messageRepository;

    private final AtomicInteger counter = new AtomicInteger(0);

//    @Autowired
//    public StringTopicKafkaMessageHandler(MessageRepository messageRepository) {
//        this.messageRepository = messageRepository;
//    }

    @Override
    public void handle(String message) {
        counter.incrementAndGet();
        log.info("Message received message of type {}, number of messages: {}", message.getClass().getSimpleName(),
                counter.get());
        //messageRepository.save(new Message(message));
    }
}
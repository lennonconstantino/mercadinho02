package br.com.lennon.mercadinho02.service;

import br.com.lennon.mercadinho02.model.Envelope;
import br.com.lennon.mercadinho02.model.ProductEvent;
import br.com.lennon.mercadinho02.model.SnsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProductEventConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(ProductEventConsumer.class);

    private ObjectMapper objectMapper;

    @Autowired
    public ProductEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Consumidor de mensagens
    @JmsListener(destination = "${aws.sqs.queue.product.events.name}")
    public void receiveProductEvent(String message) throws JMSException, IOException {
        //SnsMessage snsMessage = objectMapper.readValue(message.getBody(SnsMessage.class).getMessage(), SnsMessage.class);
        //SnsMessage snsMessage = objectMapper.readValue(((TextMessage)message).getText(), SnsMessage.class);
        SnsMessage snsMessage = objectMapper.readValue(message, SnsMessage.class);

        Envelope envelope = objectMapper.readValue(snsMessage.getMessage(), Envelope.class);

        ProductEvent productEvent = objectMapper.readValue(envelope.getData(), ProductEvent.class);

        LOG.info("Product event received - Event: {} - ProductId: {} - MessageId: {}"
                , envelope.getEventType()
                , productEvent.getProductId()
                , snsMessage.getMessageId()
        );
    }
}

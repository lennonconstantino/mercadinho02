package br.com.lennon.mercadinho02.service;

import br.com.lennon.mercadinho02.model.Envelope;
import br.com.lennon.mercadinho02.model.ProductEvent;
import br.com.lennon.mercadinho02.model.ProductEventLog;
import br.com.lennon.mercadinho02.model.SnsMessage;
import br.com.lennon.mercadinho02.repository.ProductEventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Service
public class ProductEventConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(ProductEventConsumer.class);

    private ObjectMapper objectMapper;
    private ProductEventLogRepository productEventLogRepository;

    @Autowired
    public ProductEventConsumer(ObjectMapper objectMapper, ProductEventLogRepository productEventLogRepository) {
        this.objectMapper = objectMapper;
        this.productEventLogRepository = productEventLogRepository;
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

        ProductEventLog productEventLog = buildProductEventLog(envelope
                , productEvent
                , snsMessage.getMessageId()
        );
        productEventLogRepository.save(productEventLog);
    }

    private ProductEventLog buildProductEventLog(Envelope envelope, ProductEvent productEvent, String messageId) {
        long timestamp = Instant.now().toEpochMilli();

        ProductEventLog productEventLog = new ProductEventLog();
        productEventLog.setPk(productEvent.getCode());
        productEventLog.setSk(envelope.getEventType() + "_" + timestamp);
        productEventLog.setEventType(envelope.getEventType());
        productEventLog.setProductId(productEvent.getProductId());
        productEventLog.setUsername(productEvent.getUsername());
        productEventLog.setTtl(Instant.now().plus(Duration.ofMinutes(10)).getEpochSecond());
        productEventLog.setMessageId(messageId);

        return productEventLog;
    }
}

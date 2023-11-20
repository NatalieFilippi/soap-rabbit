package ru.service;

import org.springframework.stereotype.Service;
import ru.generated.NumberConversion;
import ru.generated.NumberConversionSoapType;
import ru.model.Message;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.logging.Logger;

@Service
public class RabbitListener {
    static Logger logger = Logger.getLogger(RabbitListener.class.getName());
    private final NumberConversionSoapType numberConversionSoap;

    public RabbitListener(NumberConversion numberConversion) {
        this.numberConversionSoap = numberConversion.getNumberConversionSoap();
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = {"q.new-queue"})
    public void onNewQueue(Message message) {
        logger.info("Get message from q.new-queue" + message);

        //имитация ошибки
        execute(message);
    }

    private void execute(Message message) {
        logger.info("Executing User Registration Event: " + message);
        throw new RuntimeException("Message Failed");
    }

    /*
    * Слушает очередь недоставленных сообщений
    * */

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = {"q.not-delivered-message"})
    public void onMessageFailure(Message message){
        logger.info("Executing fallback for failed registration " + message);
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = {"q.number-to-dollars-queue"})
    public void onNumberToDollars(BigDecimal number) {
        logger.info("Get message from q.number-to-dollars-queue: " + number);
        String response = numberConversionSoap.numberToDollars(number);
        logger.info("response = " + response);
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = {"q.number-to-word-queue"})
    public void onNumberToWord(BigInteger number) {
        logger.info("Get message from q.number-to-dollars-queue: " + number);
        String response = numberConversionSoap.numberToWords(number);
        logger.info("response = " + response);
    }
}

package ru.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;
import ru.model.Message;
import ru.service.NumberConverterService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.logging.Logger;

@RestController
public class MainController {
    static Logger logger = Logger.getLogger(MainController.class.getName());
    private final RabbitTemplate rabbitTemplate;
    private final NumberConverterService service;

    public MainController(RabbitTemplate rabbitTemplate, NumberConverterService service) {
        this.rabbitTemplate = rabbitTemplate;
        this.service = service;
    }

    @GetMapping
    public String addMessage(@RequestBody Message message) {
        logger.info("Get message " + message);
        rabbitTemplate.convertAndSend("", "q.new-queue", message);
        logger.info("Send message to Rabbit");
        return "Ok";
    }

    @PostMapping("/dollars")
    public String numberToDollars(@RequestParam BigDecimal number) {
        logger.info("Post request to /dollars with value = " + number);
        rabbitTemplate.convertAndSend("", "q.number-to-dollars-queue", number);
        logger.info("Send message to Rabbit");
        //return service.numberToDollars(number);
        return "OK!!!";
    }

    @PostMapping("/word")
    public String numberToWord(@RequestParam BigInteger number) {
        logger.info("Post request to /word with value = " + number);
        rabbitTemplate.convertAndSend("", "q.number-to-word-queue", number);
        logger.info("Send message to Rabbit");
        return "OK!!!";
    }
}

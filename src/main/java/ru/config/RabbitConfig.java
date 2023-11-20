package ru.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.Jackson2XmlMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;


@Configuration
public class RabbitConfig {

    @Bean
    public Queue createNewQueue() {
        /*
        * Чтобы очередь знала, что у неё есть очередь недоставленных сообщений
        * Если этого не требуется, то просто return new Queue("q.new-queue");
        * */
        return QueueBuilder.durable("q.new-queue")
                .withArgument("x-dead-letter-exchange","x.failure")
                .withArgument("x-dead-letter-routing-key","fall-back")
                .build();

        //return new Queue("q.new-queue");
    }

    @Bean
    public Queue createNumberToWordQueue() {
        return new Queue("q.number-to-word-queue");
    }

    @Bean
    public Queue createNumberToDollarsQueue() {
        return new Queue("q.number-to-dollars-queue");
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setMessageConverter(converterXml());
        return template;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory("localhost");
        cachingConnectionFactory.setUsername("admin");
        cachingConnectionFactory.setPassword("admin");
        return cachingConnectionFactory;
    }

    @Bean
    public Jackson2JsonMessageConverter converterJson() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Jackson2XmlMessageConverter converterXml() {
        return new Jackson2XmlMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setMessageConverter(converterXml());
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    /*конфигурирует повторную отправку в RabbitMq
    Он должен повторить попытку 2 раза только после первой попытки.
    Первый через две секунды, второй через четыре секунды.
    Когда повторная попытка исчерпана, сообщения удаляются.*/

    @Bean
    public RetryOperationsInterceptor retryInterceptor(){
        return RetryInterceptorBuilder.stateless().maxAttempts(3)
                .backOffOptions(2000, 2.0, 100000)
                .recoverer(new RejectAndDontRequeueRecoverer()) //сгенерировать это исключения после всех попыток
                .build();
    }

    /*Конфигурация очереди недоставленных сообщений*/

    @Bean
    public Declarables createDeadLetterSchema(){
        return new Declarables(
                new DirectExchange("x.failure"),
                new Queue("q.not-delivered-message"),
                new Binding("q.not-delivered-message", Binding.DestinationType.QUEUE,"x.failure", "fall-back", null)
        );
    }


}

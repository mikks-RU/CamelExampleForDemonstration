package org.satel;

import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.satel.Result;

@Component
public class CamelRoute extends RouteBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ProducerTemplate producerTemplate;

    @Override
    public void configure() throws Exception {
        /**
         * роут читает из файла файлы txt, doc, xml
         * с задержкой по умолчанию в 10с
         * если получен txt, то выводим и отправляем на почту его содержимое
         * файлы doc и xml будут прикреплены к письму, как вложения, в лог выведется его имя
         */
        from("file://c:/1/?delay=10s" +
                "&include=.*.txt|.*.doc|.*.xml" +
                "&move=C:/1/History/${file:name.noext}.${file:ext}")
                .id("RouteFile")
                .choice()
                    .when(simple("${file:ext} == 'txt'"))
                        .log(LoggingLevel.INFO, "Receive from RouteFile \n body from txt: \n ${body}")
                    .otherwise()
                        .log(LoggingLevel.INFO, "Receive from RouteFile. Read file: ${file:name}")
                    .endChoice()
                .end()
                .setProperty("fileName", simple("${file:name}"))
                .setProperty("fileExt", simple("${file:ext}"))
                .process("mailSenderProcessor");

        /**
         * роут принимает из soap сервиса, дробит сообщения на единичные c  сохранением в очереди rabbit
         * пример сообщения:
         *       <sat:data xmlns:sat="http://www.satel.org/">
         *          <mess>
         *             <delay>10</delay>
         *             <text>message 1</text>
         *          </mess>
         *          <mess>
         *             <delay>10</delay>
         *             <text>message 2</text>
         *          </mess>
         *       </sat:data>
         */
        from("cxf:bean:rabbitSenderService")
                .id("RouteCxf")
                .log(LoggingLevel.INFO, "Receive from RouteCxf \nbody: \n${body}")
                //Обработка сообщений
                .process(exchange -> {
                    Data data = exchange.getIn().getBody(Data.class);

                    int i = 0;
                    int allMessages = data.mess.size();
                    for (Data.Mess m : data.mess) {
                        try {
                            Thread.sleep(m.delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        producerTemplate.sendBody("direct:sender", m.text);
                        i++;
                        logger.info("Send message " + i + "/" + allMessages);
                    }
                })
                //Формирование ответа
                .process(exchange -> {
                    Result result = new Result();
                    result.setMessage("All OK!");
                    exchange.getIn().setBody(result);
                });
        /**
         * отправка сообщений в rabbit. используется в RouteCxf
         */
        from("direct:sender")
                .to("rabbitmq:{{rabbitmq.exchange}}?queue={{rabbitmq.targetQueue}}&routingKey={{rabbitmq.targetQueue}}&declare=true&passive=true");
        /**
         * чтение из очереди rabbit и отправка в soap сервис
         */
        from("rabbitmq:{{rabbitmq.exchange}}?queue={{rabbitmq.fromQueue}}" +
                    "&routingKey={{rabbitmq.fromQueue}}" +
                    "&declare=true" +
                    "&passive=true" +
                    "&connectionFactory=#rabbitCURRENT")
                .id("RouteRabbit")
                .log(LoggingLevel.INFO, "Receive from RouteRabbit \nbody: \n${body}")
                .removeHeaders("*")
                .to("cxf:bean:dzoWebService");
    }
}

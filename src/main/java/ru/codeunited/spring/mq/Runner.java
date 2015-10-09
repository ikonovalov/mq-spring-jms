package ru.codeunited.spring.mq;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.codeunited.spring.mq.sender.MQMessageSender;

import javax.jms.JMSException;

/**
 * Created by Igor on 2014.07.31.
 */
public class Runner {

    public static void main(String[] args) throws JMSException {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        context.registerShutdownHook();

        String destinationQueue = "JMS.SMPL.BUSN.REQ.TX";
        String replyToQueue = "JMS.SMPL.BUSN.RESP";
        MQMessageSender sender = context.getBean(MQMessageSender.class);

        int count = 5;
        while (count-- > 0) {
            sender.send(String.valueOf(System.nanoTime()), destinationQueue, replyToQueue);
        }

    }

}

package ru.codeunited.spring.mq.examples;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.JMSException;

/**
 * Created by Igor on 2014.07.31.
 */
public class Runner {

    public static void main(String[] args) throws JMSException {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        context.registerShutdownHook();

        new Thread(new Runnable() {

            private MQMessageSender sender;

            {
                sender = context.getBean(MQMessageSender.class);
            }

            @Override
            public void run() {
                int count = 100;
                while(count-->0) {
                    sender.send("Message with " + System.nanoTime());
                }
            }
        }).start();

    }

}

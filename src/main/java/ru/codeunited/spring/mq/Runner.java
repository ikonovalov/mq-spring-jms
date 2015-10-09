package ru.codeunited.spring.mq;

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
                sender.setDestinationQueue  ("JMS.SMPL.BUSN.REQ.TX");
                sender.setReplyToQueue      ("JMS.SMPL.BUSN.RESP");
            }

            @Override
            public void run() {
                int count = 2;
                while(count-->0) {
                    sender.send(String.valueOf(System.nanoTime()));
                }
            }
        }).start();

    }

}
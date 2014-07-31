package ru.codeunited.spring.mq.examples;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Date;

/**
 * Created by Igor on 2014.07.31.
 */
public class Runner {

    public static void main(String[] args) throws JMSException {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        context.registerShutdownHook();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final JmsTemplate template = context.getBean(JmsTemplate.class);
                int counter = 0;
                while (counter++ < 100) {
                    template.send("ETP.Q1", new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage tm = session.createTextMessage();
                            tm.setText("Current time is " + new Date());
                            tm.setJMSReplyTo(template.getDestinationResolver().resolveDestinationName(session, "ETP.DEFAULT.RESP", false));
                            return tm;
                        }
                    });
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();

    }

}

mq-spring-jms
=============

This is juat a sample application. That shows how to interact with WebSphere MQ via JMS API with Spring-JMS support.

Scenario is very simple:
+ MQSender send message to EPT.Q1 with ReplyToQ = ETP.DEFAULT.RESP.
+ MQListener listen ETP.Q1 and in ReplyToQ is specified reply to it.

**GPL** licence.

*... and sorry for my english*

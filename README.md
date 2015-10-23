mq-spring-jms
=============

This is juat a sample application. 

That shows how to interact with WebSphere MQ via JMS API with and without Spring-JMS support.
Samples uses really working infrastructure.

Properties                      |   Values
--------------------------------|-----------------
wmq.qmgr.host                   |   etp3.sm-soft.ru
wmq.qmgr.port                   |   2424
wmq.qmgr.hosts                  |   etp3.sm-soft.ru(2424),etp4.sm-soft(2424)
wmq.qmgr.clientReconnectTimeout |   1000
wmq.qmgr.name                   |   GU01QM
wmq.qmgr.channel                | CLNT.SAMPLE.SVRCONN
wmq.qmgr.username               | sample
wmq.qmgr.password               | sample
wmq.qmgr.ccid                   | 1208

Package structure:

- ru.codeunited.jms.service - business service package
- ru.codeunited.jms.simple  - JMS interaction under transaction(TX) and with client acknowledges(ACK)
- ru.codeunited.jms.spring  - JMS with Spring (with TX and ACK)

**LGPL** licence.

package io.cresco.agent.controller.communication;

import com.google.gson.Gson;
import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSslConnectionFactory;

import javax.jms.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;

public class ActiveAgentConsumer implements Runnable {
	private PluginBuilder plugin;
	private CLogger logger;
	private Queue RXqueue;
	private Session sess;
	private ActiveMQConnection conn;
	private ActiveMQSslConnectionFactory connf;
	private ControllerEngine controllerEngine;

	public ActiveAgentConsumer(ControllerEngine controllerEngine, String RXQueueName, String URI, String brokerUserNameAgent, String brokerPasswordAgent) throws JMSException {
		this.controllerEngine = controllerEngine;
		this.plugin = controllerEngine.getPluginBuilder();
		this.logger = plugin.getLogger(ActiveAgentConsumer.class.getName(),CLogger.Level.Info);

		logger.debug("Queue: {}", RXQueueName);
		logger.trace("RXQueue=" + RXQueueName + " URI=" + URI + " brokerUserNameAgent=" + brokerUserNameAgent + " brokerPasswordAgent=" + brokerPasswordAgent);
		int retryCount = 10;

		connf = new ActiveMQSslConnectionFactory(URI);
		connf.setKeyAndTrustManagers(controllerEngine.getCertificateManager().getKeyManagers(),controllerEngine.getCertificateManager().getTrustManagers(), new SecureRandom());
		conn = (ActiveMQConnection) connf.createConnection();
		conn.start();
		sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		RXqueue = sess.createQueue(RXQueueName);
	}

	@Override
	public void run() {
		logger.trace("Queue: {}", RXqueue);
		Gson gson = new Gson();
		try {
			controllerEngine.setConsumerThreadActive(true);
			MessageConsumer consumer = sess.createConsumer(RXqueue);
			while (controllerEngine.isConsumerThreadActive()) {
				TextMessage msg = (TextMessage) consumer.receive();
				if (msg != null) {
					logger.debug("Incoming Queue: {}", RXqueue);
					MsgEvent me = null;
					try {
						String msgPayload = new String(msg.getText());
						me = gson.fromJson(msgPayload,MsgEvent.class);
						//me = gson.fromJson(msg.getText(), MsgEvent.class);
						//me = new Gson().fromJson(msg.getText(), MsgEvent.class);
						if(me != null) {
							plugin.msgIn(me);
							logger.debug("Message: {}", me.getParams().toString());
						} else {
							logger.error("non-MsgEvent message found!");
						}
					} catch(Exception ex) {
						logger.error("MsgEvent Error  : " +  ex.getMessage());
						StringWriter errors = new StringWriter();
						ex.printStackTrace(new PrintWriter(errors));
						logger.error("MsgEvent Error Stack : " + errors.toString());

					}
				}
				else {
					logger.trace("Queue Details: {}", RXqueue);
					logger.trace("isStarted=" + conn.isStarted() + " isClosed=" + conn.isClosed() + " ClientId=" + conn.getInitializedClientID());
					logger.trace("brokerName=" + conn.getBrokerName() + " username=" + conn.getConnectionInfo().getUserName() + " password=" + conn.getConnectionInfo().getPassword());
				}
			}
			logger.debug("Cleaning up");
			sess.close();
			conn.cleanup();
			conn.close();
			logger.debug("Shutdown complete");
		}
		catch (JMSException e) {
			//TODO Fix this dirty hack
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			if(!e.getMessage().equals("java.lang.InterruptedException")) {
				controllerEngine.setConsumerThreadActive(false);
				logger.error("JMS Error : " + e.getMessage());
				logger.error("JMS Error : ", errors.toString());
			} else {
				logger.error("JMS Error java.lang.InterruptedException : ", errors.toString());
			}
		} catch (Exception ex) {
			logger.error("Run: {}", ex.getMessage());
			StringWriter errors = new StringWriter();
			ex.printStackTrace(new PrintWriter(errors));
			logger.error("Run Stack: {}", errors.toString());
			//return errors.toString();
			try {
				//self distruct
				Thread.sleep(5000);
				System.exit(0);
			} catch(Exception exx) {

			}
			controllerEngine.setConsumerThreadActive(false);
		}

	}
}
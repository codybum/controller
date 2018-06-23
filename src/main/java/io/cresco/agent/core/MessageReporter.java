package io.cresco.agent.core;

import io.cody.task.Task;
import io.cody.task.TaskService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.jms.*;

public class MessageReporter implements Runnable {

    private BundleContext context;

    public MessageReporter(BundleContext context) {

        this.context = context;
    }

    public void run(){
        //System.out.println("MyRunnable running");


            try {

                /*
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://mybroker");
                factory.setObjectMessageSerializationDefered(true);

                PooledConnectionFactory pfactory = new PooledConnectionFactory();
                pfactory.setConnectionFactory(factory);

                // Create a Connection
                Connection connection = pfactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST.FOO");

                // Create a MessageProducer from the Session to the Topic or Queue
                javax.jms.MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);


                while(true) {
                    TextMessage message = session.createTextMessage(String.valueOf(System.currentTimeMillis()));

                    Thread.sleep(1000);
                    // Tell the producer to send the message
                    //System.out.println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
                    producer.send(message);
                }
                */


                ServiceReference<?>[] servRefs = context.getServiceReferences(TaskService.class.getName(), null);

                if (servRefs == null || servRefs.length == 0)
                {
                   System.out.println("NULL FOUND NOTHING!");
                }
                else
                {
                    System.out.println("Running Service Count: " + servRefs.length);

                    for(ServiceReference sr : servRefs) {
                        boolean assign = servRefs[0].isAssignableTo(context.getBundle(), TaskService.class.getName());
                        System.out.println("Can Assign Service : " + assign);

                        TaskService ts = (TaskService)context.getService(sr);
                        for(Task t : ts.getTasks()) {
                            System.out.println(t.getTitle());
                        }
                    }

                }




            } catch(Exception ex) {
                ex.printStackTrace();
            }

            /*
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            */



    }
}
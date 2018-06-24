package io.cresco.agent.core;


import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.log.LogReaderService;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a simple bundle that utilizes the OSGi
 * framework's event mechanism to listen for service events. Upon
 * receiving a service event, it prints out the event's details.
 **/
public class Activator implements BundleActivator
{


    /**
     * Implements BundleActivator.start(). Prints
     * a message and adds itself to the bundle context as a service
     * listener.
     * @param context the framework context for the bundle.
     **/

    private List<Configuration> configurationList = new ArrayList();

    //public CrescoMeterRegistry crescoMeterRegistry;

    public void start(BundleContext context)
    {

        try {

            ServiceReference ref = context.getServiceReference(LogReaderService.class.getName());
            if (ref != null)
            {
                LogReaderService reader = (LogReaderService) context.getService(ref);
                reader.addLogListener(new LogWriter());
            }



            //crescoMeterRegistry = new CrescoMeterRegistry("cresco");
            //crescoMeterRegistry.start();

            /*
            Timer t = Timer
                    .builder("my.timer")
                    .description("a description of what this timer does") // optional
                    .tags("region", "test") // optional
                    .register(crescoMeterRegistry);
            */
            /*
            //Do Init
            ActiveBroker broker = new ActiveBroker("mybroker","name","password");

            //Timer t = crescoMeterRegistry.get("test").timer();

            JMXConsumer mc = new JMXConsumer(t);
            */


            //JMXProducer mp = new JMXProducer();
            //Post Init

            //Bundle bundle = context.installBundle("file:/Users/cody/IdeaProjects/skeleton/target/skeleton-1.0-SNAPSHOT.jar");

                //MessageReporter rep = new MessageReporter(context);
                //new Thread(new PluginManager(context)).start();


        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Implements BundleActivator.stop(). Prints
     * a message and removes itself from the bundle context as a
     * service listener.
     * @param context the framework context for the bundle.
     **/
    public void stop(BundleContext context)
    {
        System.out.println("Stopped listening for service events.");

        // Note: It is not required that we remove the listener here,
        // since the framework will do it automatically anyway.

        /*
        ServiceReference configurationAdminReference =
                context.getServiceReference(ConfigurationAdmin.class.getName());
        */
    }


}

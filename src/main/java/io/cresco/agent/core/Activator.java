package io.cresco.agent.core;


import io.cresco.agent.communications.ActiveBroker;
import io.cresco.agent.communications.JMXConsumer;
import io.cresco.agent.metrics.CrescoMeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
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

            Bundle bundle = context.installBundle("file:/Users/cody/IdeaProjects/skeleton/target/skeleton-1.0-SNAPSHOT.jar");
            bundle.start();


            ServiceReference configurationAdminReference =
                    context.getServiceReference(ConfigurationAdmin.class.getName());


            if (configurationAdminReference != null) {
                ConfigurationAdmin confAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);


                Configuration configuration = confAdmin.createFactoryConfiguration("io.cresco.configuration.factory", null);


                Dictionary properties = new Hashtable();
                //properties.put("service.pid", "io.cresco.configuration.factory");
                properties.put("pluginID", "plugin/0");

                configuration.update(properties);

                /*
                Configuration configuration2 = confAdmin.createFactoryConfiguration("io.cresco.configuration.factory", null);
                Dictionary properties2 = new Hashtable();
                //properties2.put("service.pid", "io.cresco.configuration.factory");
                properties2.put("pluginID", "plugin/1");

                configuration2.update(properties2);
                */
                configurationList.add(configuration);
                //configurationList.add(configuration2);



                for(Configuration conf : confAdmin.listConfigurations(null)) {
                    System.out.println("CONFIG:" + conf.getPid());
                }

                for(Configuration conf : configurationList) {
                    System.out.println("CONFIG2:" + conf.getPid());
                }

                //MessageReporter rep = new MessageReporter(context);
                new Thread(new MessageReporter(context)).start();

            }



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

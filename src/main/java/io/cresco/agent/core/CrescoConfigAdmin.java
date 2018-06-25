package io.cresco.agent.core;

import io.cresco.library.agent.AgentService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Dictionary;
import java.util.Hashtable;

public class CrescoConfigAdmin {


    private BundleContext context;
    private ConfigurationAdmin confAdmin;

    public CrescoConfigAdmin(BundleContext context) {

        this.context = context;

        ServiceReference configurationAdminReference = null;

            configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());

            if (configurationAdminReference != null) {

                boolean assign = configurationAdminReference.isAssignableTo(context.getBundle(), ConfigurationAdmin.class.getName());

                if (assign) {
                    confAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);
                } else {
                    System.out.println("Could not Assign Configuration Admin!");
                }

            } else {
                System.out.println("Admin Does Not Exist!");
            }

    }

    public void addBundle() {
        try {
            Bundle bundle = context.installBundle("file:/Users/vcbumg2/ResearchWorx/skeleton/target/skeleton-1.0-SNAPSHOT.jar");
            bundle.start();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void AddConfig() {

        try {

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
            //onfigurationList.add(configuration);
            //configurationList.add(configuration2);


            for (Configuration conf : confAdmin.listConfigurations(null)) {
                System.out.println("CONFIG:" + conf.getPid());
            }

            /*
            for (Configuration conf : configurationList) {
                System.out.println("CONFIG2:" + conf.getPid());
            }
            */


        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}

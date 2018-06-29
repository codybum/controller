package io.cresco.agent.core;

import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginAdmin {


    private final int PLUGINLIMIT = 900;

    private BundleContext context;
    private ConfigurationAdmin confAdmin;
    private Map<String,Configuration> configMap;
    private Map<String,PluginService> serviceMap;


    public int pluginCount() {

        synchronized (configMap) {
            return configMap.size();
        }
    }

    public PluginAdmin(BundleContext context) {

        configMap = new HashMap<>();
        serviceMap = new ConcurrentHashMap<>();
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
            Bundle bundle = context.installBundle("file:/Users/cody/IdeaProjects/skeleton/target/skeleton-1.0-SNAPSHOT.jar");
            bundle.start();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void msgOut(MsgEvent msg) {

        String pluginID = msg.getDstPlugin();

        if(serviceMap.containsKey(pluginID)) {
            serviceMap.get(pluginID).inMsg(msg);
        }
    }

    public String addConfig() {


        String pluginID = null;
        try {

            if(pluginCount() < PLUGINLIMIT) {
                boolean isEmpty = false;
                int id = 0;
                while (!isEmpty) {

                    synchronized (configMap) {
                        if (!configMap.containsKey("plugin/" + id)) {
                            pluginID = "plugin/" + id;
                            Configuration configuration = confAdmin.createFactoryConfiguration("io.cresco.skeleton.Plugin", null);
                            Dictionary properties = new Hashtable();
                            properties.put("pluginID", pluginID);
                            configuration.update(properties);
                            configMap.put(pluginID, configuration);
                            isEmpty = true;
                        }
                    }
                    id++;
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return pluginID;
    }

    public void startPlugin(String pluginID) {

        try {
            ServiceReference<?>[] servRefs = null;

            while (servRefs == null) {

                String filterString = "(pluginID=" + pluginID + ")";
                Filter filter = context.createFilter(filterString);

                servRefs = context.getServiceReferences(PluginService.class.getName(), filterString);

                if (servRefs == null || servRefs.length == 0) {
                    //System.out.println("NULL FOUND NOTHING!");
                } else {
                    //System.out.println("Running Service Count: " + servRefs.length);

                    for (ServiceReference sr : servRefs) {
                        boolean assign = servRefs[0].isAssignableTo(context.getBundle(), PluginService.class.getName());
                        //System.out.println("Can Assign Service : " + assign);

                        PluginService ps = (PluginService) context.getService(sr);

                        serviceMap.put(pluginID,(PluginService) context.getService(sr));

                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}

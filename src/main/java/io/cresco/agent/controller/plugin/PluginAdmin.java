package io.cresco.agent.controller.plugin;

import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;


import java.io.File;
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
    //private Map<String,PluginService> serviceMap;
    private Map<String,PluginNode> pluginMap;


    public int pluginCount() {

        synchronized (configMap) {
            return configMap.size();
        }
    }

    /*
    public void getRepo() {


        Repository repo = null;
        ServiceReference repoReference = null;

        repoReference = context.getServiceReference(Repository.class.getName());
        if (repoReference != null) {

            boolean assign = repoReference.isAssignableTo(context.getBundle(), Repository.class.getName());

            if (assign) {
                repo = (Repository) context.getService(repoReference);
            } else {
                System.out.println("Could not Assign Configuration Admin!");
            }

        } else {
            System.out.println("Admin Does Not Exist!");
        }


    }
    */

    public PluginAdmin(BundleContext context) {

        configMap = new HashMap<>();
        //serviceMap = new ConcurrentHashMap<>();
        pluginMap = new ConcurrentHashMap<>();
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

    public long addBundle(String fileLocation) {
        long bundleID = -1;
        try {

            Bundle bundle = null;

            File checkFile = new File(fileLocation);
            if(checkFile.isFile()) {

                bundle = context.getBundle(fileLocation);

                if(bundle == null) {
                    bundle = context.installBundle("file:" + fileLocation);
                }

                if(bundle != null) {
                    bundleID = bundle.getBundleId();
                }

            /*
            System.out.println("bundle location: " + bundle.getLocation());
            System.out.println("bundle sname: " + bundle.getSymbolicName());
            System.out.println("bundle state: " + bundle.getState());
            System.out.println("bundle id: " + bundle.getBundleId());
            System.out.println("bundle version: " + bundle.getVersion());

            bundle location: file:/Users/cody/IdeaProjects/skeleton/target/skeleton-1.0-SNAPSHOT.jar
            bundle sname: cresco.io.skeleton
            bundle state: 2
            bundle id: 17
            bundle version: 1.0.0.SNAPSHOT-2018-06-29T201634Z
            */
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return bundleID;
    }

    public boolean startBundle(long bundleID) {
        boolean isStarted = false;
        try {
            context.getBundle(bundleID).start();
            isStarted = true;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return  isStarted;
    }

    public void msgOut(MsgEvent msg) {

        String pluginID = msg.getDstPlugin();

        if(pluginMap.containsKey(pluginID)) {
            pluginMap.get(pluginID).getPluginService().inMsg(msg);
        }
    }


    public String addPlugin(String pluginName, String jarFile, Map<String,Object> map) {
        String returnPluginID = null;
        try {

            long bundleID = addBundle(jarFile);
            if(bundleID != -1) {
                if(startBundle(bundleID)) {
                    String pluginID = addConfig(pluginName, map);
                    if(pluginID != null) {
                        PluginNode pluginNode = new PluginNode(pluginID,pluginName,jarFile,map);
                        pluginMap.put(pluginID,pluginNode);
                        //create a config object here
                        if(startPlugin(pluginID)) {
                            returnPluginID = pluginID;
                        } else {
                            System.out.println("Could not start plugin " + pluginID + " pluginName " + pluginName + " no bundle " + jarFile);
                        }
                    } else {
                        System.out.println("Could not create config for " + " pluginName " + pluginName + " no bundle " + jarFile);
                    }
                } else {
                    System.out.println("Could not start bundle Id " + bundleID + " pluginName " + pluginName + " no bundle " + jarFile);
                }
            //controllerEngine.getPluginAdmin().startBundle(bundleID);
            //String pluginID = controllerEngine.getPluginAdmin().addConfig(pluginName,jarFile, map);
            //controllerEngine.getPluginAdmin().startPlugin(pluginID);
            } else {
                System.out.println("Can't add " + pluginName + " no bundle " + jarFile);
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String addConfig(String pluginName, Map<String,Object> map) {


        String pluginID = null;
        try {

            if(pluginCount() < PLUGINLIMIT) {
                boolean isEmpty = false;
                int id = 0;
                while (!isEmpty) {

                    synchronized (configMap) {
                        if (!configMap.containsKey("plugin/" + id)) {
                            pluginID = "plugin/" + id;
                            Configuration configuration = confAdmin.createFactoryConfiguration(pluginName + ".Plugin", null);
                            Dictionary properties = new Hashtable();

                            ((Hashtable) properties).putAll(map);
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

    public String addConfig2() {


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

    public boolean startPlugin(String pluginID) {
        boolean isStarted = false;
        try {
            ServiceReference<?>[] servRefs = null;

            while (servRefs == null) {

                String filterString = "(pluginID=" + pluginID + ")";
                Filter filter = context.createFilter(filterString);

                //servRefs = context.getServiceReferences(PluginService.class.getName(), filterString);
                servRefs = context.getServiceReferences(PluginService.class.getName(), null);

                if (servRefs == null || servRefs.length == 0) {
                    System.out.println("NULL FOUND NOTHING!");
                } else {
                    System.out.println("Running Service Count: " + servRefs.length);

                    for (ServiceReference sr : servRefs) {
                        boolean assign = servRefs[0].isAssignableTo(context.getBundle(), PluginService.class.getName());
                        System.out.println("Can Assign Service : " + assign);

                        PluginService ps = (PluginService) context.getService(sr);

                        pluginMap.get(pluginID).setPluginService((PluginService) context.getService(sr));
                        isStarted = true;

                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isStarted;
    }

}

package io.cresco.agent.controller.agentcontroller;

import com.google.gson.Gson;
import io.cresco.library.agent.AgentState;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import io.cresco.library.utilities.CLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;


import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PluginAdmin {

    private Gson gson;

    private int PLUGINLIMIT = 900;
    private int TRYCOUNT = 30;

    private BundleContext context;
    private ConfigurationAdmin confAdmin;
    private Map<String,Configuration> configMap;
    private Map<String,PluginNode> pluginMap;
    private CLogger logger;

    private AtomicBoolean lockConfig = new AtomicBoolean();
    private AtomicBoolean lockPlugin = new AtomicBoolean();

    private AgentState agentState;

    public int pluginCount() {

        synchronized (lockConfig) {
            return configMap.size();
        }
    }


    public PluginAdmin(PluginBuilder pluginBuilder, AgentState agentState, BundleContext context) {

        this.gson = new Gson();
        this.configMap = Collections.synchronizedMap(new HashMap<>());
        this.pluginMap = Collections.synchronizedMap(new HashMap<>());
        this.context = context;
        this.agentState = agentState;
        logger = pluginBuilder.getLogger(PluginAdmin.class.getName(), CLogger.Level.Info);


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

    public void setLogLevel(String logId, CLogger.Level level) {

        try {

            logId = logId.toLowerCase();
            /*
            if (level != CLogger.Level.Info) {
                System.out.println("LOG ID: " + logId + " LEVEL:" + level.name());
            }
            */

                Configuration logConfig = confAdmin.getConfiguration("org.ops4j.pax.logging", null);

                Dictionary<String, Object> log4jProps = logConfig.getProperties();
                log4jProps.put("log4j.logger." + logId, level.name().toUpperCase());

                logConfig.updateIfDifferent(log4jProps);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public boolean pluginTypeActive(String pluginName) {
        boolean exists = false;
        try {
            synchronized (lockConfig) {

                Iterator it = configMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();

                    String pluginID = (String) pair.getKey();
                    Configuration config = (Configuration) pair.getValue();

                    if(config.getFactoryPid().equals(pluginName + ".Plugin")) {
                        return true;
                    }
                } }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return exists;
    }

    public boolean serviceExist(String serviceName) {
        boolean exists = false;
        try {

            ServiceReference sr = context.getServiceReference(serviceName);
            if(sr != null) {
                exists = true;
                context.ungetService(sr);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return exists;
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

            }
            //check local repo
            else {
                URL bundleURL = getClass().getClassLoader().getResource(fileLocation);
                if(bundleURL != null) {

                    String bundlePath = bundleURL.getPath();
                    InputStream bundleStream = getClass().getClassLoader().getResourceAsStream(fileLocation);
                    bundle = context.installBundle(bundlePath,bundleStream);
                }
            }
            if(bundle != null) {
                bundleID = bundle.getBundleId();
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

    public boolean stopBundle(long bundleID) {
        boolean isStopped = false;
        try {
            context.getBundle(bundleID).stop();
            isStopped = true;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return  isStopped;
    }

    public boolean stopPlugin(String pluginId) {
        boolean isStopped = false;
        try {

            String jarFilePath = null;
            String pid = null;
            boolean isPluginStopped = false;

            synchronized (lockPlugin) {
                if (pluginMap.containsKey(pluginId)) {
                    jarFilePath = pluginMap.get(pluginId).getJarPath();
                    isPluginStopped = pluginMap.get(pluginId).getPluginService().isStopped();
                }
            }

            synchronized (lockConfig) {
                pid = configMap.get(pluginId).getPid();
            }

            if(isPluginStopped) {

                if ((jarFilePath != null) && (pid != null)) {

                    Configuration pluginConfig = confAdmin.getConfiguration(pid);
                    if(pluginConfig != null) {

                        pluginConfig.delete();

                            synchronized (lockPlugin) {
                                pluginMap.remove(pluginId);
                            }
                            synchronized (lockConfig) {
                                configMap.remove(pluginId);
                            }

                            isStopped = true;
                    }
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return  isStopped;
    }


    public boolean stopBundle(String pluginId) {
        boolean isStopped = false;
        try {

            String jarFilePath = null;
            String pid = null;
            boolean isPluginStopped = false;

            synchronized (lockPlugin) {
                if (pluginMap.containsKey(pluginId)) {
                    jarFilePath = pluginMap.get(pluginId).getJarPath();
                    isPluginStopped = pluginMap.get(pluginId).getPluginService().isStopped();
                }
            }

            synchronized (lockConfig) {
                pid = configMap.get(pluginId).getPid();
            }

            System.out.println("jarfilepath:" + jarFilePath + " factorypid:" + pid + " ispluginstopped:" + isPluginStopped);

            if(isPluginStopped) {

                if ((jarFilePath != null) && (pid != null)) {

                    Configuration pluginConfig = confAdmin.getConfiguration(pid);
                    if(pluginConfig != null) {

                        pluginConfig.delete();

                        long bundleID = addBundle(jarFilePath);
                        System.out.println("bundleID " + bundleID);
                        if (bundleID != -1) {
                            System.out.println("bundleID pre-stop state " + context.getBundle(bundleID).getState());
                            context.getBundle(bundleID).stop();
                            //context.getBundle(bundleID).uninstall();
                            System.out.println("bundleID pre-stop state " + context.getBundle(bundleID).getState());
                            context.getBundle(bundleID).uninstall();

                            synchronized (lockPlugin) {
                                pluginMap.remove(pluginId);
                            }
                            synchronized (lockConfig) {
                                configMap.remove(pluginId);
                            }

                            isStopped = true;
                        }
                    }
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return  isStopped;
    }

    public void msgIn(MsgEvent msg) {

        String pluginID = msg.getDstPlugin();
        synchronized (lockPlugin) {
            if (pluginMap.containsKey(pluginID)) {
                if(pluginMap.get(pluginID).getActive()) {
                    pluginMap.get(pluginID).getPluginService().inMsg(msg);
                }
            }
        }

    }

    public String addPlugin(String pluginName, String jarFile, Map<String,Object> map) {
        String returnPluginID = null;
        if(pluginCount() < PLUGINLIMIT) {
            try {

                long bundleID = addBundle(jarFile);
                if (bundleID != -1) {

                    String pluginID = addConfig(pluginName, map);

                    if (startBundle(bundleID)) {
                        if (pluginID != null) {
                            PluginNode pluginNode = new PluginNode(pluginID, pluginName, jarFile, map);
                            synchronized (lockPlugin) {
                                pluginMap.put(pluginID, pluginNode);
                            }

                            if (startPlugin(pluginID)) {
                                returnPluginID = pluginID;
                            } else {
                                System.out.println("Could not start agentcontroller " + pluginID + " pluginName " + pluginName + " no bundle " + jarFile);
                            }

                        } else {
                            System.out.println("Could not create config for " + " pluginName " + pluginName + " no bundle " + jarFile);
                        }
                    } else {
                        System.out.println("Could not start bundle Id " + bundleID + " pluginName " + pluginName + " no bundle " + jarFile);
                        System.out.println("Remove configuration! --  bundle Id " + bundleID + " pluginName " + pluginName + " no bundle " + jarFile);

                    }
                    //controllerEngine.getPluginAdmin().startBundle(bundleID);
                    //String pluginID = controllerEngine.getPluginAdmin().addConfig(pluginName,jarFile, map);
                    //controllerEngine.getPluginAdmin().startPlugin(pluginID);
                } else {
                    System.out.println("Can't add " + pluginName + " no bundle " + jarFile);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return returnPluginID;
    }

    public String addConfig(String pluginName, Map<String,Object> map) {

        String pluginID = null;
        try {


                boolean isEmpty = false;
                int id = 0;
                while (!isEmpty) {

                    synchronized (lockConfig) {
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


        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return pluginID;
    }

    public boolean startPlugin(String pluginID) {
        boolean isStarted = false;

        try {
            ServiceReference<?>[] servRefs = null;
            int count = 0;

            while ((!isStarted) && (count < TRYCOUNT)) {

                String filterString = "(pluginID=" + pluginID + ")";
                Filter filter = context.createFilter(filterString);

                //servRefs = context.getServiceReferences(PluginService.class.getName(), filterString);
                servRefs = context.getServiceReferences(PluginService.class.getName(), filterString);

                //System.out.println("REFS : " + servRefs.length);
                if (servRefs == null || servRefs.length == 0) {
                    //System.out.println("NULL FOUND NOTHING!");

                } else {
                    //System.out.println("Running Service Count: " + servRefs.length);

                    for (ServiceReference sr : servRefs) {

                        boolean assign = servRefs[0].isAssignableTo(context.getBundle(), PluginService.class.getName());

                        if(assign) {
                            PluginService ps = (PluginService) context.getService(sr);
                            int statusCode = 7;
                            String statusDesc = "Plugin instance could not be started";
                            try {
                                if(ps.isStarted()) {
                                  statusCode = 10;
                                  statusDesc = "Plugin Active";
                                }
                            } catch(Exception ex) {
                                System.out.println("Could not start!");
                                ex.printStackTrace();
                            }

                            synchronized (lockPlugin) {
                                if (pluginMap.containsKey(pluginID)) {
                                    pluginMap.get(pluginID).setPluginService((PluginService) context.getService(sr));
                                    pluginMap.get(pluginID).setStatus_code(statusCode);
                                    pluginMap.get(pluginID).setStatus_desc(statusDesc);
                                } else {
                                    System.out.println("NO PLUGIN IN PLUGIN MAP FOR THIS SERVICE : " + pluginID + " elements " + pluginMap.hashCode() + " thread:" + Thread.currentThread().getName());
                                }
                            }

                            isStarted = true;
                        }
                    }
                }
                count++;
                Thread.sleep(1000);
            }
            if(servRefs == null) {
                System.out.println("COULD NOT START PLUGIN COULD NOT GET SERVICE");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isStarted;
    }

    public Map<String,String> getPluginStatus(String pluginID) {
        Map<String,String> statusMap = null;
        try {

            synchronized (lockPlugin) {
                if(pluginMap.containsKey(pluginID)) {
                    statusMap = new HashMap<>();
                    PluginNode pluginNode = pluginMap.get(pluginID);
                    int status_code = pluginNode.getStatus_code();
                    String status_desc = pluginNode.getStatus_desc();
                    boolean isActive = pluginNode.getActive();

                    statusMap.put("status_code", String.valueOf(status_code));
                    statusMap.put("status_desc", status_desc);
                    statusMap.put("isactive", String.valueOf(isActive));
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return statusMap;
    }

    public String getPluginExport() {


        String exportString = null;
        try {

            List<Map<String,String>> configMapList = new ArrayList<>();

            synchronized (lockPlugin) {
                Iterator it = pluginMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();

                    String pluginID = (String) pair.getKey();
                    PluginNode pluginNode = (PluginNode) pair.getValue();

                    int status_code = pluginNode.getStatus_code();
                    String status_desc = pluginNode.getStatus_desc();
                    boolean isActive = pluginNode.getActive();

                    Map<String, String> configMap = new HashMap<>();


                    configMap.put("status_code", String.valueOf(status_code));
                    configMap.put("status_dest", status_desc);
                    configMap.put("isactive", String.valueOf(isActive));
                    configMap.put("pluginid", pluginID);
                    configMap.put("configparams", gson.toJson(pluginNode.exportParamMap()));
                    configMapList.add(configMap);

                    //it.remove(); // avoids a ConcurrentModificationException
                }
            }
            exportString = gson.toJson(configMapList);

        } catch(Exception ex) {
            System.out.println("PluginExport.pluginExport() Error " + ex.getMessage());
        }

        return exportString;
    }

    public Bundle installInternalBundleJars(String bundleName) {

        Bundle installedBundle = null;
        try {
            URL bundleURL = getClass().getClassLoader().getResource(bundleName);
            if(bundleURL != null) {

                String bundlePath = bundleURL.getPath();
                installedBundle = context.installBundle(bundlePath,
                        getClass().getClassLoader().getResourceAsStream(bundleName));

            } else {
                System.out.println("Bundle = null");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(installedBundle == null) {
            System.out.println("Failed to load bundle exiting!");
            System.exit(0);
        }

        return installedBundle;
    }

}

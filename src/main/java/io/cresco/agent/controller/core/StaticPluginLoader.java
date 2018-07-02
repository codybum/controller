package io.cresco.agent.controller.core;

import io.cresco.agent.core.Config;
import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

import java.io.File;
import java.util.Map;

public class StaticPluginLoader implements Runnable  {

    private ControllerEngine controllerEngine;
    private PluginBuilder plugin;
    private CLogger logger;
    private Config config;


    public StaticPluginLoader(ControllerEngine controllerEngine) {
        this.controllerEngine = controllerEngine;
        this.plugin = controllerEngine.getPluginBuilder();
        logger = plugin.getLogger(this.getClass().getName(), CLogger.Level.Info);
        try {

            File pluginConfigFile  = new File("conf/plugin.ini");

            if (pluginConfigFile.isFile()) {
                this.config = new Config(pluginConfigFile.getAbsolutePath());
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }


    public void run() {

            try {

                if(config != null) {

                    for(String tmpPluginID : config.getPluginList(1)) {


                        try {
                            Map<String, Object> map = config.getConfigMap(tmpPluginID);

                            if ((map.containsKey("pluginname") && (map.containsKey("jarfile")))) {
                                String pluginName = (String) map.get("pluginname");
                                String jarFile = (String) map.get("jarfile");
                                String pluginPath = plugin.getConfig().getStringParam("pluginpath");
                                if(pluginPath != null) {
                                    if(pluginPath.endsWith("/")) {
                                        jarFile = pluginPath + jarFile;
                                    } else {
                                        jarFile = pluginPath + "/" + jarFile;
                                    }
                                }

                                long bundleID = controllerEngine.getPluginAdmin().addBundle(jarFile);
                                if(bundleID != -1) {
                                    controllerEngine.getPluginAdmin().startBundle(bundleID);
                                    String pluginID = controllerEngine.getPluginAdmin().addConfig(pluginName, map);
                                    controllerEngine.getPluginAdmin().startPlugin(pluginID);
                                }
                            }


                        } catch(Exception exe) {
                            exe.printStackTrace();
                        }
                    }

                    //logger.info("Sent Message : " + message + " agent:" + plugin.getAgent());
                    //Thread.sleep(1000);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }

    }


}

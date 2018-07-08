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
            String pluginConfigFileName = plugin.getConfig().getStringParam("plugin_config_file","conf/plugins.ini");
            File pluginConfigFile  = new File(pluginConfigFileName);

            if (pluginConfigFile.isFile()) {
                this.config = new Config(pluginConfigFile.getAbsolutePath());
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }


    public void run() {

        boolean isStaticInit = false;

            try {

                while(!isStaticInit) {

                    if(controllerEngine.cstate.isActive()) {
                        if (config != null) {

                            for (String tmpPluginID : config.getPluginList(1)) {

                                try {
                                    Map<String, Object> map = config.getConfigMap(tmpPluginID);

                                    if ((map.containsKey("pluginname") && (map.containsKey("jarfile")))) {
                                        String pluginName = (String) map.get("pluginname");
                                        String jarFile = (String) map.get("jarfile");
                                        String pluginPath = plugin.getConfig().getStringParam("pluginpath");
                                        if (pluginPath != null) {
                                            if (pluginPath.endsWith("/")) {
                                                jarFile = pluginPath + jarFile;
                                            } else {
                                                jarFile = pluginPath + "/" + jarFile;
                                            }
                                        }
                                        //
                                        String pluginID = controllerEngine.getPluginAdmin().addPlugin(pluginName, jarFile, map);
                                        logger.info("STATIC LOADED : pluginID: " + pluginID + " pluginName: " + pluginName + " jarName: " + jarFile);

                                    }


                                } catch (Exception exe) {
                                    exe.printStackTrace();
                                }
                            }

                            //logger.info("Sent Message : " + message + " agent:" + agentcontroller.getAgent());
                            //Thread.sleep(1000);
                            isStaticInit = true;
                        }
                    }
                    Thread.sleep(1000);
                }

            } catch(Exception ex) {
                ex.printStackTrace();
            }

    }


}

package io.cresco.agent.controller.core;

import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class StaticPluginLoader implements Runnable  {

    private ControllerEngine controllerEngine;
    private PluginBuilder plugin;
    CLogger logger;

    public StaticPluginLoader(ControllerEngine controllerEngine) {
        this.controllerEngine = controllerEngine;
        this.plugin = controllerEngine.getPluginBuilder();
        logger = plugin.getLogger(this.getClass().getName(), CLogger.Level.Info);

    }


    public void run() {

            try {

                controllerEngine.getPluginAdmin().addBundle();
                String pluginID = controllerEngine.getPluginAdmin().addConfig();
                controllerEngine.getPluginAdmin().startPlugin(pluginID);

                //logger.info("Sent Message : " + message + " agent:" + plugin.getAgent());
                Thread.sleep(1000);
            } catch(Exception ex) {
                ex.printStackTrace();
            }

    }


}

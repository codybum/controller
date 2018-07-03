package io.cresco.agent.controller.core;

import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class ExecutorImpl implements Executor {

    private ControllerEngine controllerEngine;
    private PluginBuilder plugin;
    CLogger logger;

    public ExecutorImpl(ControllerEngine controllerEngine) {
        this.controllerEngine = controllerEngine;
        this.plugin = controllerEngine.getPluginBuilder();
        logger = plugin.getLogger(ExecutorImpl.class.getName(),CLogger.Level.Info);
    }

    @Override
    public MsgEvent executeCONFIG(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeDISCOVER(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeERROR(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeINFO(MsgEvent incoming) {
        //System.out.println("INCOMING INFO MESSAGE FOR AGENT FROM " + incoming.getSrcPlugin() + " setting new desc");
        /*
        String pluginId = controllerEngine.getPluginAdmin().addConfig();
            if(pluginId != null) {
                controllerEngine.getPluginAdmin().startPlugin(pluginId);
            }
*/
        //if(rm.getParam("desc").startsWith("to-plugin")) {
        incoming.setParam("desc","to-plugin-agent-rpc");
        return incoming;
    }
    @Override
    public MsgEvent executeEXEC(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeWATCHDOG(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeKPI(MsgEvent incoming) {
        return null;
    }


}
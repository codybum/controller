package io.cresco.agent.controller.agentcontroller;

import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

import java.util.HashMap;
import java.util.Map;

public class AgentExecutor implements Executor {

    private ControllerEngine controllerEngine;
    private PluginBuilder plugin;
    CLogger logger;

    public AgentExecutor(ControllerEngine controllerEngine) {
        this.controllerEngine = controllerEngine;
        this.plugin = controllerEngine.getPluginBuilder();
        logger = plugin.getLogger(AgentExecutor.class.getName(),CLogger.Level.Info);
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
        System.out.println("INCOMING INFO MESSAGE FOR AGENT FROM " + incoming.getSrcPlugin() + " setting new desc");



        /*
            String pluginName = "io.cresco.skeleton";
            String jarFile = "/Users/cody/IdeaProjects/skeleton/target/skeleton-1.0-SNAPSHOT.jar";
            Map<String, Object> map = new HashMap<>();

            map.put("pluginname", pluginName);
            map.put("jarfile", jarFile);

            controllerEngine.getPluginAdmin().addPlugin(pluginName, jarFile, map);
        */
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
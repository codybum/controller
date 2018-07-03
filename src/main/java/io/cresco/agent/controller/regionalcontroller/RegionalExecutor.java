package io.cresco.agent.controller.regionalcontroller;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.agent.controller.globalcontroller.GlobalCommandExec;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class RegionalExecutor implements Executor {

    private ControllerEngine controllerEngine;
    private PluginBuilder plugin;
    private CLogger logger;
    private GlobalCommandExec gce;

    public RegionalExecutor(ControllerEngine controllerEngine) {
        this.controllerEngine = controllerEngine;
        this.plugin = controllerEngine.getPluginBuilder();
        logger = plugin.getLogger(RegionalExecutor.class.getName(),CLogger.Level.Info);
        gce = new GlobalCommandExec(controllerEngine);
    }

    @Override
    public MsgEvent executeCONFIG(MsgEvent incoming) {
        if(incoming.getParam("action") != null) {
            switch (incoming.getParam("action")) {
                case "agent_disable":
                    logger.debug("CONFIG : AGENTDISCOVER REMOVE: " + incoming.printHeader());
                    if (controllerEngine.getGDB().removeNode(incoming)) {
                        incoming.setParam("success",Boolean.TRUE.toString());
                    } else {
                        incoming.setParam("success",Boolean.FALSE.toString());
                    }
                    return incoming;

                case "agent_enable":
                    logger.debug("CONFIG : AGENT ADD: " + incoming.printHeader());
                    if(controllerEngine.getGDB().addNode(incoming)) {
                        incoming.setParam("success",Boolean.TRUE.toString());
                    } else {
                        incoming.setParam("success",Boolean.FALSE.toString());
                    }
                    return incoming;
                default:
                    logger.debug("RegionalCommandExec Unknown configtype found: {}", incoming.getParam("action"));
                    return null;
            }

        }
        else {
            logger.error("CONFIG : UNKNOWN ACTION: " + incoming.printHeader());
            //return gce.cmdExec(le);
        }
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
      return null;
    }
    @Override
    public MsgEvent executeEXEC(MsgEvent incoming) {
        if(incoming.getParam("action") != null) {
            switch (incoming.getParam("action")) {

                case "ping":
                    return pingReply(incoming);

                default:
                    logger.error("RegionalCommandExec Unknown configtype found {} for {}:", incoming.getParam("action"), incoming.getMsgType().toString());
                    return null;
            }
        } else {
            logger.error("EXEC : UNKNOWN ACTION: Region:" + incoming.printHeader());
        }
        return null;
    }
    @Override
    public MsgEvent executeWATCHDOG(MsgEvent incoming) {

        if(!controllerEngine.getGDB().watchDogUpdate(incoming)) {
            logger.error("Unable to update Regional WatchDog " + incoming.printHeader());
        }
        return null;
    }
    @Override
    public MsgEvent executeKPI(MsgEvent incoming) {
        logger.debug("KPI: " + incoming.printHeader());

        if(controllerEngine.cstate.isGlobalController()) {
            return gce.execute(incoming);
        }
        else {
            if(plugin.getConfig().getBooleanParam("forward_global_kpi",true)){
                globalSend(incoming);
            }
            return null;
        }
    }

    private void globalSend(MsgEvent ge) {
        try {
            if(!controllerEngine.cstate.isGlobalController()) {
                ge.setParam("dst_region",controllerEngine.cstate.getGlobalRegion());
                ge.setParam("dst_agent",controllerEngine.cstate.getGlobalAgent());
                //ge.setParam("dst_plugin",controllerEngine.cstate.getControllerId());
                ge.setParam("globalcmd", Boolean.TRUE.toString());
                controllerEngine.sendAPMessage(ge);
            }
        }
        catch (Exception ex) {
            logger.error("globalSend : " + ex.getMessage());
        }
    }

    private MsgEvent pingReply(MsgEvent msg) {
        logger.debug("ping message type found");
        msg.setParam("action","pong");
        msg.setParam("remote_ts", String.valueOf(System.currentTimeMillis()));
        msg.setParam("type", "agent_controller");
        logger.debug("Returning communication details to Cresco agent");
        return msg;
    }


}
package io.cresco.agent.controller.regionalcontroller;


import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.agent.controller.globalcontroller.GlobalCommandExec;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class RegionalCommandExec {

    private ControllerEngine controllerEngine;
	private PluginBuilder plugin;
	private CLogger logger;
	//private AgentDiscovery regionalDiscovery;
	public GlobalCommandExec gce;

	public RegionalCommandExec(ControllerEngine controllerEngine)
	{
        this.controllerEngine = controllerEngine;
        this.plugin = controllerEngine.getPluginBuilder();
        this.logger = plugin.getLogger(RegionalCommandExec.class.getName(),CLogger.Level.Info);

        //this.logger = new CLogger(RegionalCommandExec.class, plugin.getMsgOutQueue(), plugin.getRegion(), plugin.getAgent(), plugin.getPluginID(), CLogger.Level.Info);
		//this.plugin = plugin;
		//regionalDiscovery = new AgentDiscovery(plugin);
		gce = new GlobalCommandExec(controllerEngine);
	}

	public MsgEvent execute(MsgEvent le) {

        if(le.getParam("is_global") != null) {
                //this is a global command
                if(controllerEngine.cstate.isGlobalController()) {
                    return gce.execute(le);
                }
                else {
                    globalSend(le);
                    return null;
                }
            }

        if(le.getMsgType() == MsgEvent.Type.EXEC) {
            if(le.getParam("action") != null) {
                switch (le.getParam("action")) {

                        case "ping":
                            return pingReply(le);

                        default:
                            logger.error("RegionalCommandExec Unknown configtype found {} for {}:", le.getParam("action"), le.getMsgType().toString());
                            return null;
                }
            }
            } else if(le.getMsgType() == MsgEvent.Type.CONFIG) {

            if(le.getParam("action") != null) {
                    switch (le.getParam("action")) {
                        case "agent_disable":
                            logger.debug("CONFIG : AGENTDISCOVER REMOVE: Region:" + le.getParam("src_region") + " Agent:" + le.getParam("src_agent"));
                            logger.trace("Message Body [" + le.getMsgBody() + "] [" + le.getParams().toString() + "]");
                            controllerEngine.getGDB().removeNode(le);
                            break;
                        case "agent_enable":
                            logger.debug("CONFIG : AGENT ADD: Region:" + le.getParam("src_region") + " Agent:" + le.getParam("src_agent"));
                            logger.trace("Message Body [" + le.getMsgBody() + "] [" + le.getParams().toString() + "]");
                            return enableAgent(le);
                        default:
                            logger.debug("RegionalCommandExec Unknown configtype found: {}", le.getParam("action"));
                            return null;
                    }

                }
                else {
                    logger.error("CONFIG : UNKNOWN ACTION: Region:" + le.getParam("src_region") + " Agent:" + le.getParam("src_agent") + " " +  le.getParams());
                    //return gce.cmdExec(le);
                }
			}
			else if(le.getMsgType() == MsgEvent.Type.WATCHDOG) {
				//regionalDiscovery.discover(le);
				watchDogAgent(le);
        }
            else if (le.getMsgType() == MsgEvent.Type.INFO) {
                //logger.debug("INFO: Region:" + le.getParam("src_region") + " Agent:" + le.getParam("src_agent"));
                //logger.trace("Message Body [" + le.getMsgBody() + "] [" + le.getParams().toString() + "]");
            }
			else if (le.getMsgType() == MsgEvent.Type.KPI) {
				logger.debug("KPI: Region:" + le.getParam("src_region") + " Agent:" + le.getParam("src_agent"));
				//logger.info("Send GLOBAL KPI: MsgType=" + le.getMsgType() + " Params=" + le.getParams());
                if(controllerEngine.cstate.isGlobalController()) {
                    return gce.execute(le);
                }
                else {
                    if(plugin.getConfig().getBooleanParam("forward_global_kpi",true)){
                        globalSend(le);
                    }
                    return null;
                }
			}

			else {
				logger.error("RegionalCommandExec UNKNOWN MESSAGE! : MsgType=" + le.getMsgType() + " " +  le.getParams());
			}

        return null;
	}

    private void watchDogAgent(MsgEvent le) {
        try {

                logger.debug("WATCHDOG : Region:" + le.getParam("src_region") + " Agent:" + le.getParam("src_agent"));
                logger.trace("Message Body [" + le.getMsgBody() + "] [" + le.getParams().toString() + "]");

            controllerEngine.getGDB().watchDogUpdate(le);


        } catch (Exception ex) {
            ex.printStackTrace();
            logger.debug("watchDogAgent : " + ex.toString());
        }
    }

	private MsgEvent enableAgent(MsgEvent le) {

        logger.debug("CONFIG : AGENTDISCOVER ADD: Region:" + le.getParam("src_region") + " Agent:" + le.getParam("src_agent"));
        logger.trace("Message Body [" + le.getMsgBody() + "] [" + le.getParams().toString() + "]");


        controllerEngine.getGDB().addNode(le);

            if(!controllerEngine.cstate.isGlobalController()) {
            //TODO Fix Global Send
                //globalSend(le);
        }

        //gdb.addNode(region, agent,plugin);
        //gdb.setNodeParams(region,agent,plugin, de.getParams());

        //process agent configs

        //whut
        //le.setParam("globalcmd", Boolean.TRUE.toString());
        //globalSend(le);

        return le;
    }

    private MsgEvent pingReply(MsgEvent msg) {
        logger.debug("ping message type found");
        msg.setParam("action","pong");
        msg.setParam("remote_ts", String.valueOf(System.currentTimeMillis()));
        msg.setParam("type", "agent_controller");
        logger.debug("Returning communication details to Cresco agent");
        return msg;
    }

    private void regionSend(MsgEvent rs) {
        controllerEngine.sendAPMessage(rs);
    }

    private void globalSend(MsgEvent ge) {
        try {
            if(!controllerEngine.cstate.isGlobalController()) {
                    ge.setParam("dst_region",controllerEngine.cstate.getGlobalRegion());
                    ge.setParam("dst_agent",controllerEngine.cstate.getGlobalAgent());
                    ge.setParam("dst_plugin",controllerEngine.cstate.getControllerId());
                    ge.setParam("globalcmd", Boolean.TRUE.toString());
                    controllerEngine.sendAPMessage(ge);
            }
        }
        catch (Exception ex) {
            logger.error("globalSend : " + ex.getMessage());
        }
    }

}

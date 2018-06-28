package io.cresco.agent.controller.communication;


import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class MsgRoute implements Runnable {

    private ControllerEngine controllerEngine;
    private PluginBuilder plugin;
    private MsgEvent rm;
    private CLogger logger;
    private long messageTimeStamp;

    public MsgRoute(ControllerEngine controllerEngine, MsgEvent rm) {
        this.controllerEngine = controllerEngine;
        this.plugin = controllerEngine.getPluginBuilder();
        this.logger = plugin.getLogger(MsgRoute.class.getName(),CLogger.Level.Info);
        this.rm = rm;
        this.messageTimeStamp = System.nanoTime();

    }

    private MsgEvent getExec() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-agent")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("BAD MESSAGE : " + rm.getParams());
        }
        return null;
    }

    private MsgEvent getPlugin() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-plugin")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("BAD MESSAGE : " + rm.getParams());
        }
        return null;
    }

    private MsgEvent getRegional() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-region")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("BAD MESSAGE : " + rm.getParams());
        }
        return null;
    }

    private MsgEvent getGlobal() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-global")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("BAD MESSAGE : " + rm.getParams());
        }
        return null;
    }

    public void run() {
        try {
            if (!getTTL()) { //check ttl
                return;
            }

            int routePath = getRoutePath();
            //todo change back
            //rm.setParam("routepath",String.valueOf(routePath));
            rm.setParam("routepath-" + plugin.getAgent(),String.valueOf(routePath));

            MsgEvent re = null;
            switch (routePath) {

                /*
                case 0:  //System.out.println("CONTROLLER ROUTE CASE 0");
                    if (rm.getParam("configtype") != null) {
                        if (rm.getParam("configtype").equals("comminit")) {
                            logger.debug("CONTROLLER SENDING REGIONAL MESSAGE 0");
                            logger.trace(rm.getParams().toString());
                            //PluginEngine.msgInQueue.add(rm);
                            plugin.sendMsgEvent(rm);
                        }
                    }
                    break;
                */
                case 655:
                    logger.debug("Local agent sending message to external agent 655");
                    logger.trace(rm.getParams().toString());
                    re = getGlobal();
                    break;

                case 671:
                    logger.debug("Local agent sending message to external plugin 671");
                    logger.trace(rm.getParams().toString());
                    re = getGlobal();
                    break;

                case 687:
                    logger.debug("Local plugin sending message to external agent 687");
                    logger.trace(rm.getParams().toString());
                    re = getGlobal();
                    break;

                case 703:
                    logger.debug("Local plugin sending message to external plugin 703");
                    logger.trace(rm.getParams().toString());
                    re = getGlobal();
                    break;

                case 719:
                    logger.debug("Local agent sending message to regional agent 719");
                    logger.trace(rm.getParams().toString());
                    re = getRegional();
                    break;

                case 735:
                    logger.debug("Local agent sending message to regional plugin 735");
                    logger.trace(rm.getParams().toString());
                    re = getRegional();
                    break;

                case 751:
                    logger.debug("Local plugin sending message to regional agent 751");
                    logger.trace(rm.getParams().toString());
                    re = getRegional();
                    break;

                case 767:
                    logger.debug("Local plugin sending message to regional plugin 767");
                    logger.trace(rm.getParams().toString());
                    re = getRegional();
                    break;

                case 975:
                    logger.debug("Local controller sending message to self 975");
                    logger.trace(rm.getParams().toString());
                    re = getExec();
                    break;

                case 991:
                    logger.debug("Local agent sending message to local plugin 991");
                    logger.trace(rm.getParams().toString());
                    re = getPlugin();
                    break;
                case 1007:
                    logger.debug("Local plugin sending message to local agent 1007");
                    logger.trace(rm.getParams().toString());
                    re = getExec();
                    break;

                case 1023:
                    logger.debug("Local plugin sending message to local plugin 1023");
                    logger.trace(rm.getParams().toString());
                    re = getPlugin();
                    break;

                default:
                    //System.out.println("CONTROLLER ROUTE CASE " + routePath + " " + rm.getParams());
                    logger.error("DEFAULT ROUTE CASE " + routePath + " " + rm.getParams());
                    re = null;
                    break;
            }


            if (re != null) {

                re.setReturn(); //reverse to-from for return
                plugin.sendMsgEvent(re);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Controller : MsgRoute : Route Failed " + ex.toString() + " " + rm.getParams().toString());

        }
        finally
        {
            /*
            if(plugin.cstate.isActive()) {
                plugin.getMeasurementEngine().updateTimer("message.transaction.time", messageTimeStamp);
            }
            */
        }

    }

    private MsgEvent getNull() {
        return null;
    }

    private MsgEvent getRegionalCommandExecOutGoing() {
        //TODO route should send directly there, local in commandexec will figure it out.
        try {
            boolean incoming = false;

            String callId = "callId-" + /*PluginEngine.region*/plugin.getRegion() + "-" + /*PluginEngine.agent*/plugin.getAgent() + "-" + /*PluginEngine.plugin*/plugin.getPluginID(); //calculate callID
            if (rm.getParam(callId) != null) { //send message to RPC hash
            logger.error("WTF: WHY IS THERE A CALLID!");
            }



                //return PluginEngine.commandExec.cmdExec(rm);
                if(controllerEngine.getRegionHealthWatcher() != null) {
                    if(controllerEngine.getRegionHealthWatcher().rce != null) {
                        return controllerEngine.getRegionHealthWatcher().rce.execute(rm);
                    } else {
                        logger.error("getRegionHealthWatcher().rce = null");
                    }

                } else {
                    logger.error("getRegionHealthWatcher() = null");
                }
        } catch (Exception ex) {
            logger.error("getRegionalCommandExec - " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    private MsgEvent getRegionalCommandExec() {

        if((rm.getParam("dst_region").equals(plugin.getRegion())) && (rm.getParam("dst_agent").equals(plugin.getAgent())) && (rm.getParam("dst_plugin").equals(plugin.getPluginID()))) {
            return getCommandExec();
        } else {
            externalSend();
            return null;
        }
    }

    private MsgEvent getRegionalCommandExec2() {
        //TODO route should send directly there, local in commandexec will figure it out.
        try {
            String callId = "callId-" + /*PluginEngine.region*/plugin.getRegion() + "-" + /*PluginEngine.agent*/plugin.getAgent() + "-" + /*PluginEngine.plugin*/plugin.getPluginID(); //calculate callID
            if (rm.getParam(callId) != null) { //send message to RPC hash
                //PluginEngine.rpcMap.put(rm.getParam(callId), rm);
                //plugin.receiveRPC(rm.getParam(callId), rm);
                String RPCkey = rm.getParam(callId);
                rm.removeParam(callId);
                rm.removeParam("is_rpc");
                //todo FIX RPC
                //plugin.receiveRPC(RPCkey, rm);
            } else {
                //return PluginEngine.commandExec.cmdExec(rm);
                if(controllerEngine.getRegionHealthWatcher() != null) {
                    if(controllerEngine.getRegionHealthWatcher().rce != null) {
                        return controllerEngine.getRegionHealthWatcher().rce.execute(rm);
                    } else {
                        logger.error("getRegionHealthWatcher().rce = null");
                    }

                } else {
                    logger.error("getRegionHealthWatcher() = null");
                }
            }
        } catch (Exception ex) {
            logger.error("getRegionalCommandExec - " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    private MsgEvent getCommandExec() {
        try {
            String callId = "callId-" + /*PluginEngine.region*/plugin.getRegion() + "-" + /*PluginEngine.agent*/plugin.getAgent() + "-" + /*PluginEngine.plugin*/plugin.getPluginID(); //calculate callID
            if (rm.getParam(callId) != null) { //send message to RPC hash
                //PluginEngine.rpcMap.put(rm.getParam(callId), rm);
                String RPCkey = rm.getParam(callId);
                rm.removeParam(callId);
                rm.removeParam("is_rpc");
                //todo FIX RPC
                //plugin.receiveRPC(RPCkey, rm);
            }
            //check header for propper exec location
            else {
                if((rm.getParam("is_regional") != null) &&(rm.getParam("is_global") != null)) {
                    //this is a global command
                    if(controllerEngine.cstate.isGlobalController()) {
                        return controllerEngine.getRegionHealthWatcher().rce.gce.execute(rm);
                    } else {
                        //todo Make sure this is correct
                        //set regional controller as dest
                        //logger.error("Global command sent, but controller is not global");
                        //return null;
                        //send to exec to determine where it should go
                        //todo fix exec
                        //return plugin.execute(rm);
                    }
                } else if((rm.getParam("is_regional") != null) &&(rm.getParam("is_global") == null)) {
                    //this is a regional command
                    if(controllerEngine.cstate.isRegionalController()) {
                        return controllerEngine.getRegionHealthWatcher().rce.execute(rm);
                    } else {
                        logger.error("Regional command sent, but controller is not regional");
                        return null;
                    }
                } else {
                    //todo
                    //fix exec
                    //return plugin.execute(rm);
                }
            }

        } catch (Exception ex) {
            logger.error("getCommandExec - " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }


    private void externalSend() {
        String targetAgent = null;
        try {
            if ((rm.getParam("dst_region") != null) && (rm.getParam("dst_agent") != null)) {
                //agent message
                targetAgent = rm.getParam("dst_region") + "_" + rm.getParam("dst_agent");

            } else if ((rm.getParam("dst_region") != null) && (rm.getParam("dst_agent") == null)) {
                //regional message
                targetAgent = rm.getParam("dst_region");
            }
            logger.trace("Send Target : " + targetAgent);
            if (/*PluginEngine.isReachableAgent(targetAgent)*/controllerEngine.isReachableAgent(targetAgent)) {
                //PluginEngine.ap.sendMessage(rm);
                controllerEngine.sendAPMessage(rm);
                logger.trace("Send Target : " + targetAgent + " params : " + rm.getParams().toString());
                //System.out.println("SENT NOT CONTROLLER MESSAGE / REMOTE=: " + targetAgent + " " + " region=" + ce.getParam("dst_region") + " agent=" + ce.getParam("dst_agent") + " "  + ce.getParams());
            } else {
                logger.error("Unreachable External Agent : " + targetAgent);
                logger.error(rm.getParams().toString());
            }
        } catch (Exception ex) {
            logger.error("External Send Error : " + targetAgent);
            ex.printStackTrace();
        }
    }

    private int getRoutePath() {
        int routePath;
        try {
            //determine if local or controller
            String RXre = "0";
            String RXr = "0";
            String RXae = "0";
            String RXa = "0";
            String RXp = "0";
            String RXpe = "0";


            String TXr = "0";
            String TXre = "0";
            String TXa = "0";
            String TXae = "0";
            String TXp = "0";
            String TXpe = "0";

            if (rm.getParam("dst_region") != null) {
                RXre = "1";
                if (rm.getParam("dst_region").equals(/*PluginEngine.region*/plugin.getRegion())) {
                    RXr = "1";
                }
            }
            if (rm.getParam("dst_agent") != null) {
                RXae = "1";
                if (rm.getParam("dst_agent").equals(/*PluginEngine.agent*/plugin.getAgent())) {
                    RXa = "1";
                }
            }
            if (rm.getParam("dst_plugin") != null) {
                RXpe = "1";
                if (rm.getParam("dst_plugin").equals(/*PluginEngine.plugin*/plugin.getPluginID())) {
                    RXp = "1";
                }
            }


            if (rm.getParam("src_region") != null) {
                TXre = "1";
                if (rm.getParam("src_region").equals(/*PluginEngine.region*/plugin.getRegion())) {
                    TXr = "1";
                }
            }
            if (rm.getParam("src_agent") != null) {
                TXae = "1";
                if (rm.getParam("src_agent").equals(/*PluginEngine.agent*/plugin.getAgent())) {
                    TXa = "1";
                }
            }
            if (rm.getParam("src_plugin") != null) {
                TXpe = "1";
                if (rm.getParam("src_plugin").equals(/*PluginEngine.plugin*/plugin.getPluginID())) {
                    TXp = "1";
                }
            }

            // 001011 10 11 11
            String routeString = TXp + RXp + TXa + RXa + TXr + RXr + TXpe + RXpe + TXae + RXae + TXre + RXre;
            routePath = Integer.parseInt(routeString, 2);
            //System.out.println("desc:" + rm.getParam("desc") + "\nroutePath:" + routePath + " RouteString:\n" + routeString + "\n" + rm.getParams());
        } catch (Exception ex) {
            if(rm != null) {
                logger.error("Controller : MsgRoute : getRoutePath Error: " + ex.getMessage() + " " + rm.getParams().toString());
            } else {
                logger.error("Controller : MsgRoute : getRoutePath Error: " + ex.getMessage() + " RM=NULL");
            }
            ex.printStackTrace();
            routePath = -1;
        }
        //System.out.println("REGIONAL CONTROLLER ROUTEPATH=" + routePath + " MsgType=" + rm.getMsgType() + " Params=" + rm.getParams());

        return routePath;
    }

    private boolean getTTL() {

        boolean isValid = true;
        try {
            if (rm.getParam("ttl") != null) {
                int ttlCount = Integer.valueOf(rm.getParam("ttl"));

                if (ttlCount > 10) {
                    System.out.println("**Controller : MsgRoute : High Loop Count**");
                    System.out.println("MsgType=" + rm.getMsgType().toString());
                    System.out.println("Region=" + rm.getMsgRegion() + " Agent=" + rm.getMsgAgent() + " plugin=" + rm.getMsgPlugin());
                    System.out.println("params=" + rm.getParams());
                    isValid = false;
                }

                ttlCount++;
                rm.setParam("ttl", String.valueOf(ttlCount));
            } else {
                rm.setParam("ttl", "0");
            }
        } catch (Exception ex) {
            isValid = false;
        }
        return isValid;
    }
}

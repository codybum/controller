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

    public void exit() {

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
            String RC = "0";
            String RXr = "0";
            String RXa = "0";
            String RXp = "0";
            String TXr = "0";
            String TXa = "0";
            String TXp = "0";


            if(controllerEngine.cstate.isRegionalController()) {
                RC = "1";
            }

            if (rm.getParam("dst_region") != null) {
                if (rm.getParam("dst_region").equals(/*PluginEngine.region*/plugin.getRegion())) {
                    RXr = "1";
                    if (rm.getParam("dst_agent") != null) {
                        if (rm.getParam("dst_agent").equals(/*PluginEngine.agent*/plugin.getAgent())) {
                            RXa = "1";
                            if (rm.getParam("dst_plugin") != null) {
                                if (rm.getParam("dst_plugin").equals(/*PluginEngine.plugin*/plugin.getPluginID())) {
                                    RXp = "1";
                                }
                            }
                        }
                    }
                }

            }
            if (rm.getParam("src_region") != null) {
                if (rm.getParam("src_region").equals(/*PluginEngine.region*/plugin.getRegion())) {
                    TXr = "1";
                    if (rm.getParam("src_agent") != null) {
                        if (rm.getParam("src_agent").equals(/*PluginEngine.agent*/plugin.getAgent())) {
                            TXa = "1";
                            if (rm.getParam("src_plugin") != null) {
                                if (rm.getParam("src_plugin").equals(/*PluginEngine.plugin*/plugin.getPluginID())) {
                                    TXp = "1";
                                }
                            }
                        }
                    }
                }

            }
            String routeString = RC + RXr + TXr + RXa + TXa + RXp + TXp;
            routePath = Integer.parseInt(routeString, 2);
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

    private int getRoutePath2() {
        /*
         * rE aE pE  rM aM pM   Logic                                             Values   Action
         * -- -- --  -- -- --   ------------------------------------------------  -------  ---------------------------------------
         *  0  X  X   X  X  X   Global Broadcast Message                          0 - 31   Broadcast to Global
         *  1  0  0   0  X  X   Regional Broadcast Message (External Region)      32 - 35  Forward to Global
         *  1  0  0   1  X  X   Regional Broadcast Message (Current Region)       36 - 39  Broadcast to Region
         *  1  0  1   0  X  X   Regional Broadcast Message (External Region)      40 - 43  Forward to Global
         *  1  0  1   1  X  0   Regional Broadcast Message (Current Region)       44 - 47  Broadcast to Region
         *  1  0  1   1  X  1   Regional Broadcast Message (Current Region)       44 - 47  Broadcast to Region & Execute
         *  1  1  0   0  X  X   Agent Message (External Agent / External Region)  48 - 51  Forward to Global
         *  1  1  0   1  0  X   Agent Message (External Agent / Current Region)   52 - 53  Forward to Regional Controller or Agent
         *  1  1  0   1  1  X   Agent Message                                     54 - 55  Forward to Agent
         *  1  1  1   0  X  X   Plugin Message (External Region)                  56 - 59  Forward to Global
         *  1  1  1   1  0  X   Plugin Message (External Agent / Current Region)  60 - 61  Forward to Regional Controller or Agent
         *  1  1  1   1  1  0   Plugin Message (External Plugin / Current Agent)  62       Forward to Agent
         *  1  1  1   1  1  1   Plugin Message (Current Plugin / Current Agent)   63       Execute
         *
         *  Results:
         *  --------
         *  GlobalForward & Broadcast       0-31
         *  GlobalForward                   32-35, 40-43, 48-51, 56-59
         *  RegionalForward & Broadcast     36-39
         *  RegionalForward & Execute       44-47
         *  RegionalForward                 52-53, 60-61
         *  AgentForward                    54-55, 62
         *  Execute                         63
         */
        try {
            String rExists = "0";
            String aExists = "0";
            String pExists = "0";
            String rMatches = "0";
            String aMatches = "0";
            String pMatches = "0";

            if (rm.getParam("dst_region") != null) {
                rExists = "1";
                if (rm.getParam("dst_region").equals(plugin.getRegion()))
                    rMatches = "1";
            }
            if (rm.getParam("dst_agent") != null) {
                aExists = "1";
                if (rm.getParam("dst_agent").equals(plugin.getAgent()))
                    aMatches = "1";
            }
            if (rm.getParam("dst_plugin") != null) {
                pExists = "1";
                if (rm.getParam("dst_plugin").equals(plugin.getPluginID()))
                    pMatches = "1";
            }

            return Integer.parseInt(rExists + aExists + pExists + rMatches + aMatches + pMatches);
        } catch (Exception e) {
            logger.error("getRoutePath2 Error : {}", e.getMessage());
            return -1;
        }
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

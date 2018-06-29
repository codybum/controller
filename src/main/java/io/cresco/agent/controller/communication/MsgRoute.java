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

    private MsgEvent forwardToLocalAgent() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-agent")) {
                try {
                    controllerEngine.getPluginBuilder().msgIn(rm);
                    isOk = true;
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if(!isOk) {
            System.out.println("forwardToLocalAgent() BAD MESSAGE : " + rm.getParams() + " RouteCase :" + getRoutePath());
        }
        return null;
    }

    private MsgEvent forwardToLocalPlugin() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-plugin")) {
                try {
                    controllerEngine.getPluginAdmin().msgOut(rm);
                    isOk = true;
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if(!isOk) {
            System.out.println("forwardToLocalPlugin() BAD MESSAGE : " + rm.getParams() + " RouteCase :" + getRoutePath());
        }
        return null;
    }

    private MsgEvent forwardToLocalRegionalController() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-rc")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("forwardToLocalRegionalController() BAD MESSAGE : " + rm.getParams() + " RouteCase :" + getRoutePath());
        }
        return null;
    }

    private MsgEvent forwardToRemoteRegionalController() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-rc")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("forwardToRemoteRegionalController() BAD MESSAGE : " + rm.getParams() + " RouteCase :" + getRoutePath());
        }
        return null;
    }

    private MsgEvent forwardToLocalRegion() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-region")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("forwardToLocalRegion() BAD MESSAGE : " + rm.getParams() + " RouteCase :" + getRoutePath());
        }
        return null;
    }

    private MsgEvent forwardToRemoteRegion() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-region")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("forwardToRemoteRegion() BAD MESSAGE : " + rm.getParams() + " RouteCase :" + getRoutePath());
        }
        return null;
    }

    private MsgEvent forwardToLocalGlobal() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-global")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("forwardToLocalGlobal() BAD MESSAGE : " + rm.getParams() + " RouteCase :" + getRoutePath());
        }
        return null;
    }

    private MsgEvent forwardToRemoteGlobal() {
        boolean isOk = false;
        if(rm.getParam("desc") != null) {
            if(rm.getParam("desc").startsWith("to-global")) {
                isOk = true;
            }
        }

        if(!isOk) {
            System.out.println("forwardToRemoteGlobal() BAD MESSAGE : " + rm.getParams() + " RouteCase :" + getRoutePath());
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

                case 655:
                    logger.debug("Local agent sending message to remote global agent 655");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteGlobal();
                    break;

                case 671:
                    logger.debug("Local agent sending message to remote global plugin 671");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteGlobal();
                    break;

                case 687:
                    logger.debug("Local plugin sending message to remote global agent 687");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteGlobal();
                    break;

                case 703:
                    logger.debug("Local plugin sending message to remote global plugin 703");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteGlobal();
                    break;

                case 719:
                    logger.debug("Local agent sending message to remote regional agent 719");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteRegion();
                    break;

                case 735:
                    logger.debug("Local agent sending message to remote regional plugin 735");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteRegion();
                    break;

                case 751:
                    logger.debug("Local plugin sending message to remote regional agent 751");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteRegion();
                    break;

                case 767:
                    logger.debug("Local plugin sending message to remote regional plugin 767");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteRegion();
                    break;

                case 991:
                    logger.debug("Local agent sending message to local plugin 991");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalPlugin();
                    break;

                case 975:
                    logger.debug("Local agent sending message to self 1007");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalAgent();
                    break;

                case 1007:
                    logger.debug("Local plugin sending message to local agent 1007");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalAgent();
                    break;

                case 1023:
                    logger.debug("Local plugin sending message to local plugin 1023");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalPlugin();
                    break;

                case 4751:
                    logger.debug("Local plugin sending message to remote global agent 4751");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteGlobal();
                    break;

                case 4767:
                    logger.debug("Local plugin sending message to remote global agent 4767");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteGlobal();
                    break;

                case 4783:
                    logger.debug("Local plugin sending message to remote global agent 4783");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteGlobal();
                    break;

                case 4799:
                    logger.debug("Local plugin sending message to remote global plugin 4799");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteGlobal();
                    break;

                case 4815:
                    logger.debug("Local agent sending message to local regional agent 4815");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegion();
                    break;

                case 4831:
                    logger.debug("Local agent sending message to local regional plugin 4831");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegion();
                    break;


                case 4847:
                    logger.debug("Local plugin sending message to local regional agent 4847");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegion();
                    break;

                case 4863:
                    logger.debug("Local plugin sending message to local regional plugin 4863");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegion();
                    break;

                case 5071:
                    logger.debug("Local agent sending message to self 5071");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalAgent();
                    break;

                case 5087:
                    logger.debug("Local agent sending message to local plugin 5087");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalPlugin();
                    break;

                case 5103:
                    logger.debug("Local plugin sending message to local agent 5103");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalAgent();
                    break;

                case 5119:
                    logger.debug("Local plugin sending message to local plugin 5119");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalPlugin();
                    break;

                case 12943:
                    logger.debug("Local agent sending message to local global agent 12943");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalGlobal();
                    break;

                case 12959:
                    logger.debug("Local agent sending message to local regional plugin 12959");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalGlobal();
                    break;

                case 12975:
                    logger.debug("Local plugin sending message to local global agent 12975");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalGlobal();
                    break;

                case 12991:
                    logger.debug("Local plugin sending message to local global plugin 12991");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalGlobal();
                    break;

                case 13007:
                    logger.debug("Local agent sending message to local regional agent 13007");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegion();
                    break;

                case 13023:
                    logger.debug("Local agent sending message to local regional plugin 13023");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegion();
                    break;

                case 13039:
                    logger.debug("Local plugin sending message to local regional agent 13039");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegion();
                    break;

                case 13055:
                    logger.debug("Local plugin sending message to local regional plugin 13055");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegion();
                    break;

                case 13279:
                    logger.debug("Local agent sending message to local plugin 13279");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalPlugin();
                    break;

                case 13311:
                    logger.debug("Local plugin sending message to local plugin 13311");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalPlugin();
                    break;

                case 13263:
                    logger.debug("Local agent sending message to self 13263");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalAgent();
                    break;

                case 13295:
                    logger.debug("Local plugin sending message to local agent 13295");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalAgent();
                    break;

                case 17359:
                    logger.debug("Local plugin sending message to remote regional or global controller 17359");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteRegionalController();
                    break;

                case 17391:
                    logger.debug("Local plugin sending message to remote regional or global controller 17391");
                    logger.trace(rm.getParams().toString());
                    re = forwardToRemoteRegionalController();
                    break;

                case 21455:
                    logger.debug("Local agent sending message to local regional or global controller 21455");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegionalController();
                    break;

                case 21487:
                    logger.debug("Local plugin sending message to local regional or remote global controller 21487");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegionalController();
                    break;

                case 29647 :
                    logger.debug("Local agent sending message to local regional or local global controller 29647");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegionalController();
                    break;

                case 29679:
                    logger.debug("Local plugin sending message to local regional or global controller 29679");
                    logger.trace(rm.getParams().toString());
                    re = forwardToLocalRegionalController();
                    break;


                default:
                    //System.out.println("CONTROLLER ROUTE CASE " + routePath + " " + rm.getParams());
                    logger.error("DEFAULT ROUTE CASE " + routePath + " " + rm.getParam("desc"));
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
            String RC = "0";
            if(controllerEngine.cstate.isRegionalController()) {
                RC = "1";
            }

            String GC = "0";
            if(controllerEngine.cstate.isGlobalController()) {
                GC = "1";
            }

            String RM = "0";
            if(rm.isRegional()) {
                RM = "1";
            }
            String GM = "0";
            if(rm.isGlobal()) {
                RM = "1";
            }

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
            String routeString = GM + RM + GC + RC + TXp + RXp + TXa + RXa + TXr + RXr + TXpe + RXpe + TXae + RXae + TXre + RXre;
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

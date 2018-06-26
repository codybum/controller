package io.cresco.agent.controller.core;

import io.cresco.agent.controller.app.gPayload;
import io.cresco.agent.controller.communication.*;
import io.cresco.agent.controller.db.DBInterface;
import io.cresco.agent.core.AgentStateEngine;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.apache.activemq.command.ActiveMQDestination;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet6Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ControllerEngine {


    private PluginBuilder pluginBuilder;
    //private AgentStateEngine agentStateEngine;
    private CLogger logger;

    //manager for all certificates
    private CertificateManager certificateManager;
    private ConcurrentHashMap<String, BrokeredAgent> brokeredAgents;
    private BlockingQueue<MsgEvent> incomingCanidateBrokers;
    private BlockingQueue<MsgEvent> outgoingMessages;
    private BlockingQueue<MsgEvent> resourceScheduleQueue;
    private BlockingQueue<gPayload> appScheduleQueue;
    private Map<String, Long> discoveryMap;


    public AtomicInteger responds = new AtomicInteger(0);

    private boolean ConsumerThreadActive = false;
    private boolean ActiveBrokerManagerActive = false;
    private boolean clientDiscoveryActiveIPv4 = false;
    private boolean clientDiscoveryActiveIPv6 = false;
    private boolean DiscoveryActive = false;
    private boolean UDPDiscoveryActive = false;
    private boolean TCPDiscoveryActive = false;
    private boolean DBManagerActive = false;
    private boolean GlobalControllerManagerActive = false;
    private boolean restartOnShutdown = false;




    private String region = "init";

    private String brokerAddressAgent;
    public String brokerUserNameAgent;
    public String brokerPasswordAgent;


    public ControllerState cstate;
    private ActiveBroker broker;
    private DBInterface gdb;
    private KPIProducer kpip;
    private ActiveProducer ap;

    private Thread consumerAgentThread;
    private Thread activeBrokerManagerThread;



    //public ControllerEngine(PluginBuilder pluginBuilder, AgentStateEngine agentStateEngine){
    public ControllerEngine(PluginBuilder pluginBuilder){

        this.pluginBuilder = pluginBuilder;
        //this.agentStateEngine = agentStateEngine;
        this.logger = pluginBuilder.getLogger(ControllerEngine.class.getName(), CLogger.Level.Info);
        this.cstate = new ControllerState(this);
        logger.info("Controller Init");
    }











    //helper functions
    public CertificateManager getCertificateManager() {
        return certificateManager;
    }

    public PluginBuilder getPluginBuilder() {return  pluginBuilder; }
    public void setConsumerThreadActive(boolean consumerThreadActive) {
        ConsumerThreadActive = consumerThreadActive;
    }
    public boolean isConsumerThreadActive() {
        return ConsumerThreadActive;
    }

    public ConcurrentHashMap<String, BrokeredAgent> getBrokeredAgents() {
        return brokeredAgents;
    }
    public void setBrokeredAgents(ConcurrentHashMap<String, BrokeredAgent> brokeredAgents) {
        this.brokeredAgents = brokeredAgents;
    }
    public boolean isActiveBrokerManagerActive() {
        return ActiveBrokerManagerActive;
    }
    public void setActiveBrokerManagerActive(boolean activeBrokerManagerActive) {
        ActiveBrokerManagerActive = activeBrokerManagerActive;
    }
    public BlockingQueue<MsgEvent> getIncomingCanidateBrokers() {
        return incomingCanidateBrokers;
    }
    public void setIncomingCanidateBrokers(BlockingQueue<MsgEvent> incomingCanidateBrokers) {
        this.incomingCanidateBrokers = incomingCanidateBrokers;
    }

    public ActiveBroker getBroker() {
        return broker;
    }
    public void setBroker(ActiveBroker broker) {
        this.broker = broker;
    }


    public boolean isLocal(String checkAddress) {
        boolean isLocal = false;
        if (checkAddress.contains("%")) {
            String[] checkScope = checkAddress.split("%");
            checkAddress = checkScope[0];
        }
        List<String> localAddressList = localAddresses();
        for (String localAddress : localAddressList) {
            if (localAddress.contains(checkAddress)) {
                isLocal = true;
            }
        }
        return isLocal;
    }
    public List<String> localAddresses() {
        List<String> localAddressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces();
            while (inter.hasMoreElements()) {
                NetworkInterface networkInter = inter.nextElement();
                for (InterfaceAddress interfaceAddress : networkInter.getInterfaceAddresses()) {
                    String localAddress = interfaceAddress.getAddress().getHostAddress();
                    if (localAddress.contains("%")) {
                        String[] localScope = localAddress.split("%");
                        localAddress = localScope[0];
                    }
                    if (!localAddressList.contains(localAddress)) {
                        localAddressList.add(localAddress);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("localAddresses Error: {}", ex.getMessage());
        }
        return localAddressList;
    }

    public boolean isIPv6() {
        boolean isIPv6 = false;
        try {


            if (pluginBuilder.getConfig().getStringParam("isIPv6") != null) {
                isIPv6 = pluginBuilder.getConfig().getBooleanParam("isIPv6", false);
            }
            else {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.getDisplayName().startsWith("veth") || networkInterface.isLoopback() || !networkInterface.isUp() || !networkInterface.supportsMulticast() || networkInterface.isPointToPoint() || networkInterface.isVirtual()) {
                        continue; // Don't want to broadcast to the loopback interface
                    }
                    if (networkInterface.supportsMulticast()) {
                        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                            if ((interfaceAddress.getAddress() instanceof Inet6Address)) {
                                isIPv6 = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("isIPv6 Error: {}", ex.getMessage());
        }
        return isIPv6;
    }

    public boolean isReachableAgent(String remoteAgentPath) {
        boolean isReachableAgent = false;
        if (this.cstate.isRegionalController()) {
            try {
                ActiveMQDestination[] er = this.broker.broker.getBroker().getDestinations();
                for (ActiveMQDestination des : er) {
                    //for(String despaths : des.getDestinationPaths()) {
                    //    logger.info("isReachable destPaths: " + despaths);
                    //}
                    if (des.isQueue()) {
                        String testPath = des.getPhysicalName();

                        logger.trace("isReachable isQueue: physical = " + testPath + " qualified = " + des.getQualifiedName());
                        if (testPath.equals(remoteAgentPath)) {
                            isReachableAgent = true;
                        }
                    }
                }

                er = this.broker.broker.getRegionBroker().getDestinations();
                for (ActiveMQDestination des : er) {
                    //for(String despaths : des.getDestinationPaths()) {
                    //    logger.info("isReachable destPaths: " + despaths);
                    //}

                    if (des.isQueue()) {
                        String testPath = des.getPhysicalName();
                        logger.trace("Regional isReachable isQueue: physical = " + testPath + " qualified = " + des.getQualifiedName());
                        if (testPath.equals(remoteAgentPath)) {
                            isReachableAgent = true;
                        }
                    }
                }
                /*
                Map<String,BrokeredAgent> brokerAgentMap = this.getBrokeredAgents();
                for (Map.Entry<String, BrokeredAgent> entry : brokerAgentMap.entrySet()) {
                    String agentPath = entry.getKey();
                    BrokeredAgent bAgent = entry.getValue();

                    logger.info("isReachable : agentName: " + agentPath + " agentPath:" + bAgent.agentPath + " " + bAgent.activeAddress + " " + bAgent.brokerStatus.toString());
                    if((remoteAgentPath.equals(agentPath)) && (bAgent.brokerStatus == BrokerStatusType.ACTIVE))
                    {
                        isReachableAgent = true;
                    }
                }
                */
            } catch (Exception ex) {
                logger.error("isReachableAgent Error: {}", ex.getMessage());
            }
        } else {
            isReachableAgent = true; //send all messages to regional controller if not broker
        }
        return isReachableAgent;
    }

    public boolean isClientDiscoveryActiveIPv4() {
        return clientDiscoveryActiveIPv4;
    }
    public void setClientDiscoveryActiveIPv4(boolean clientDiscoveryActiveIPv4) {
        this.clientDiscoveryActiveIPv4 = clientDiscoveryActiveIPv4;
    }

    public boolean isClientDiscoveryActiveIPv6() {
        return clientDiscoveryActiveIPv6;
    }
    public void setClientDiscoveryActiveIPv6(boolean clientDiscoveryActiveIPv6) {
        this.clientDiscoveryActiveIPv6 = clientDiscoveryActiveIPv6;
    }

    public List<String> reachableAgents() {
        List<String> rAgents = null;
        try {
            rAgents = new ArrayList<>();
            if (this.cstate.isRegionalController()) {
                ActiveMQDestination[] er = this.broker.broker.getBroker().getDestinations();
                for (ActiveMQDestination des : er) {
                    if (des.isQueue()) {
                        rAgents.add(des.getPhysicalName());
                    }
                }
            } else {
                rAgents.add(this.region); //just return regional controller
            }
        } catch (Exception ex) {
            logger.error("isReachableAgent Error: {}", ex.getMessage());
        }
        return rAgents;
    }
    public boolean isUDPDiscoveryActive() {
        return UDPDiscoveryActive;
    }
    public void setTCPDiscoveryActive(boolean discoveryActive) {
        TCPDiscoveryActive = discoveryActive;
    }

    public boolean isTCPDiscoveryActive() {
        return TCPDiscoveryActive;
    }

    public void setUDPDiscoveryActive(boolean discoveryActive) {
        UDPDiscoveryActive = discoveryActive;
    }

    public String getStringFromError(Exception ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public BlockingQueue<gPayload> getAppScheduleQueue() {
        return appScheduleQueue;
    }
    public void setAppScheduleQueue(BlockingQueue<gPayload> appScheduleQueue) {
        this.appScheduleQueue = appScheduleQueue;
    }

    public DBInterface getGDB() {
        return gdb;
    }
    public void setGDB(DBInterface gdb) {
        this.gdb = gdb;
    }

    public KPIProducer getKPIProducer() { return this.kpip; }

    public boolean isDBManagerActive() {
        return DBManagerActive;
    }
    public void setDBManagerActive(boolean DBManagerActive) {
        this.DBManagerActive = DBManagerActive;
    }

    public BlockingQueue<MsgEvent> getResourceScheduleQueue() {
        return resourceScheduleQueue;
    }
    public void setResourceScheduleQueue(BlockingQueue<MsgEvent> appScheduleQueue) {
        this.resourceScheduleQueue = appScheduleQueue;
    }

    public boolean hasActiveProducter() {
        boolean hasAP = false;
        try {
            if(ap != null) {
                hasAP = true;
            }
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
        }
        return hasAP;
    }

    public boolean isGlobalControllerManagerActive() {
        return GlobalControllerManagerActive;
    }
    public void setGlobalControllerManagerActive(boolean activeBrokerManagerActive) {
        GlobalControllerManagerActive = activeBrokerManagerActive;
    }

    public void sendAPMessage(MsgEvent msg) {
        if ((this.ap == null) && (!region.equals("init"))) {
            logger.error("AP is null");
            logger.error("Message: " + msg.getParams());
            return;
        }
        else if(this.ap == null) {
            logger.trace("AP is null");
            return;
        }
        this.ap.sendMessage(msg);
    }

    public Map<String, Long> getDiscoveryMap() {
        return discoveryMap;
    }

    public Thread getConsumerAgentThread() {
        return consumerAgentThread;
    }

    public boolean isDiscoveryActive() {
        return DiscoveryActive;
    }

    public Thread getActiveBrokerManagerThread() {
        return activeBrokerManagerThread;
    }
    public void setActiveBrokerManagerThread(Thread activeBrokerManagerThread) {
        this.activeBrokerManagerThread = activeBrokerManagerThread;
    }

    public void removeGDBNode(String region, String agent, String pluginID) {
        if (this.gdb != null)
            this.gdb.removeNode(region, agent, pluginID);
    }

    public void setRestartOnShutdown(boolean restartOnShutdown) {
        this.restartOnShutdown = restartOnShutdown;
    }


    public void closeCommunications() {


    }


}

package io.cresco.agent.core;

import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class MessageSender implements Runnable  {

    private PluginBuilder plugin;
    CLogger logger;

    public MessageSender(PluginBuilder plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger(this.getClass().getName(), CLogger.Level.Info);

    }


    public void run() {

        while(true) {
            try {
                //String timeStamp = String.valueOf(System.nanoTime());
                //msg.setParam("ts",timeStamp);

                //TO AGENT
                MsgEvent msg = plugin.getAgentMsgEvent(MsgEvent.Type.INFO);
                msg.setParam("desc","to-agent-exec");
                plugin.msgIn(msg);


                msg = plugin.getRegionalControllerMsgEvent(MsgEvent.Type.INFO);
                msg.setParam("desc","to-agent-regional");
                plugin.msgIn(msg);

                msg = plugin.getGlobalControllerMsgEvent(MsgEvent.Type.INFO);
                msg.setParam("desc","to-agent-global");
                plugin.msgIn(msg);


                msg = plugin.getPluginMsgEvent(MsgEvent.Type.INFO, "plugin/0");
                msg.setParam("desc","to-plugin-agent");
                plugin.msgIn(msg);



                //TO REGION
                msg = plugin.getRegionalAgentMsgEvent(MsgEvent.Type.INFO, "unknownagent");
                msg.setParam("desc","to-region-agent");
                plugin.msgIn(msg);


                msg = plugin.getRegionalPluginMsgEvent(MsgEvent.Type.INFO,"unknownagent","plugin/0");
                msg.setParam("desc","to-region-plugin");
                plugin.msgIn(msg);


                //TO GLOBAL
                msg = plugin.getGlobalAgentMsgEvent(MsgEvent.Type.INFO,"unknownregion","unknownagent");
                msg.setParam("desc","to-global-agent");
                plugin.msgIn(msg);


                msg = plugin.getGlobalPluginMsgEvent(MsgEvent.Type.INFO,"unknownregion","unknownagent", "plugin/0");
                msg.setParam("desc","to-global-plugin");
                plugin.msgIn(msg);


                //logger.info("Sent Message : " + message + " agent:" + plugin.getAgent());
                Thread.sleep(1000);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}

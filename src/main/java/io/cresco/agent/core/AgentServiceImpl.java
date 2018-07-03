package io.cresco.agent.core;


import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.agent.controller.plugin.PluginAdmin;
import io.cresco.library.agent.AgentService;
import io.cresco.library.agent.AgentState;
import io.cresco.library.agent.ControllerState;
import io.cresco.library.agent.LoaderService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.*;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;



@Component(
        service = { AgentService.class} ,
        immediate = true,
        //reference=@Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class)
        reference={ @Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class) , @Reference(name="LogService", service=LogService.class) }
)


/*
@Component(
        //name = "cody",
        service = { AgentService.class },
        //scope=ServiceScope.PROTOTYPE,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        servicefactory = true,
        reference=@Reference(name="io.cresco.library.agent.LoaderService", service=LoaderService.class)
)
*/

public class AgentServiceImpl implements AgentService {

    private ControllerEngine controllerEngine;
    private ControllerState controllerState;
    private AgentState agentState;
    private PluginBuilder plugin;
    private PluginAdmin pluginAdmin;

    public AgentServiceImpl() {

    }

    @Activate
    //void activate(BundleContext context, Map<String,Object> map) {
    void activate(BundleContext context) {


            this.controllerState = new ControllerState();
            this.agentState = new AgentState(controllerState);
            this.pluginAdmin = new PluginAdmin(agentState,context);


        try {

            ServiceReference ref = context.getServiceReference(LogReaderService.class.getName());
            if (ref != null)
            {
                LogReaderService reader = (LogReaderService) context.getService(ref);
                reader.addLogListener(new LogWriter());
            }

            /*
            if((controllerState != null) && (pluginAdmin != null) && (agentState != null)) {
                plugin = new PluginBuilder(this, this.getClass().getName(), context, map);
                controllerEngine = new ControllerEngine(controllerState, plugin, pluginAdmin);
            }
            */


            String agentConfig = System.getProperty("agentConfig");

            if(agentConfig == null) {
                agentConfig = "conf/agent.ini";
            }

            Map<String,Object> map = null;

            File configFile  = new File(agentConfig);
            if(configFile.isFile()) {

                //Agent Config
                Config config = config = new Config(configFile.getAbsolutePath());

                map = config.getConfigMap();

            } else {
                map = new HashMap<>();
                System.out.println("NO CONFIG FILE " + agentConfig  + " FOUND! ");
            }


            plugin = new PluginBuilder(this, this.getClass().getName(), context, map);

            controllerEngine = new ControllerEngine(controllerState, plugin, pluginAdmin);





            //MessageSender messageSender = new MessageSender(plugin);
            //new Thread(messageSender).start();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Modified
    void modified(BundleContext context, Map<String,Object> map) {
        System.out.println("Modified Config Map PluginID:" + (String) map.get("pluginID"));
    }

    @Override
    public AgentState getAgentState() {
        return agentState;
    }


    @Override
    public void msgOut(String id, MsgEvent msg) {
        controllerEngine.msgIn(msg);
    }

}
package io.cresco.agent.core;


import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.agent.controller.agentcontroller.PluginAdmin;
import io.cresco.library.agent.AgentService;
import io.cresco.library.agent.AgentState;
import io.cresco.library.agent.ControllerState;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.*;

import java.io.File;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;



@Component(
        service = { AgentService.class} ,
        immediate = true,
        reference=@Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class)
        //reference=@Reference(name="LogService", service=LogService.class)

/*
        reference={ @Reference(name="LogService", service=LogService.class),
                    @Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class)
        }
*/
        /*
        reference={ @Reference(name="LogReaderService", service=LogReaderService.class),
                @Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class)
        }
        */
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



            /*
            ServiceReference ref = context.getServiceReference(LogReaderService.class.getName());
            if (ref != null)
            {
                LogReaderService reader = (LogReaderService) context.getService(ref);
                reader.addLogListener(new LogWriter());
            }
            */

            /*
            if((controllerState != null) && (pluginAdmin != null) && (agentState != null)) {
                agentcontroller = new PluginBuilder(this, this.getClass().getName(), context, map);
                controllerEngine = new ControllerEngine(controllerState, agentcontroller, pluginAdmin);
            }
            */
            //set HTTP in case of Dashboard
            //setHttpConfig(context);


            String agentConfig = System.getProperty("agentConfig");

            if(agentConfig == null) {
                agentConfig = "conf/agent.ini";
            }

            Map<String,Object> map = null;

            File configFile  = new File(agentConfig);
            Config config = null;
            if(configFile.isFile()) {

                //Agent Config
                config = new Config(configFile.getAbsolutePath());
                map = config.getConfigMap();

            }

            if(config == null) {
                map = new HashMap<>();
                System.out.println("NO CONFIG FILE " + agentConfig  + " FOUND! ");
            }

            plugin = new PluginBuilder(this, this.getClass().getName(), context, map);

            controllerEngine = new ControllerEngine(controllerState, plugin, pluginAdmin);





            //MessageSender messageSender = new MessageSender(agentcontroller);
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


    private void setHttpConfig(BundleContext context) {
        try {

            ConfigurationAdmin configurationAdmin;

            ServiceReference configurationAdminReference = null;

            configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());

            if (configurationAdminReference != null) {

                boolean assign = configurationAdminReference.isAssignableTo(context.getBundle(), ConfigurationAdmin.class.getName());

                if (assign) {
                    configurationAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);

                    Configuration configuration = configurationAdmin.getConfiguration("com.eclipsesource.jaxrs.connector", null);
                    Dictionary props = configuration.getProperties();
                    if (props == null) {
                        props = new Hashtable();
                    }
                    props.put("root", "/");
                    configuration.update(props);


                } else {
                    System.out.println("Could not Assign Configuration Admin!");
                }

            } else {
                System.out.println("Admin Does Not Exist!");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


}
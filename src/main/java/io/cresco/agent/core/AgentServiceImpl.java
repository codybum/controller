package io.cresco.agent.core;


import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.library.agent.AgentService;
import io.cresco.library.agent.AgentState;
import io.cresco.library.plugin.PluginBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component(
        service = { AgentService.class} ,
        immediate = true,
        //reference=@Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class)
        reference={ @Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class) , @Reference(name="LogService", service=LogService.class) }



)
public class AgentServiceImpl implements AgentService {

    private AgentState agentState;

    private ExecutorService msgInProcessQueue;
    public AgentServiceImpl() {

        //this.msgInProcessQueue = Executors.newFixedThreadPool(4);
        this.msgInProcessQueue = Executors.newCachedThreadPool();
        //this.msgInProcessQueue = Executors.newSingleThreadExecutor();
    }

    @Activate
    void activate(BundleContext context, Map<String,Object> map) {

        agentState = new AgentState();
        agentState.setId("0");
        //context.registerService(TaskService.class,this,null);

        try {
            ServiceReference ref = context.getServiceReference(LogReaderService.class.getName());
            if (ref != null)
            {
                LogReaderService reader = (LogReaderService) context.getService(ref);
                reader.addLogListener(new LogWriter());
            }

            PluginBuilder plugin = new PluginBuilder(this, this.getClass().getName(),context, map);

            ControllerEngine controllerEngine = new ControllerEngine(plugin);

            /*
            new Thread(new PluginManager(context)).start();
            */



        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public AgentState getAgentState() {
        //return taskMap.get(id);
        return agentState;
    }


    @Override
    public void msgIn(String id, String msg) {
        long startTime = Long.parseLong(msg);
        //t.record(System.nanoTime() - startTime,TimeUnit.NANOSECONDS);
        //System.out.println("SEND MESSAGE!!!");
        //System.out.println("SEND MESSAGE!!!");
    }

}
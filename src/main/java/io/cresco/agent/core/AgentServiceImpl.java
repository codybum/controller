package io.cresco.agent.core;


import io.cresco.agent.metrics.CrescoMeterRegistry;
import io.cresco.library.agent.AgentService;
import io.cresco.library.agent.AgentState;
import io.micrometer.core.instrument.Timer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component(
        service = { AgentService.class} ,
        immediate = true,
        reference=@Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class)

)
public class AgentServiceImpl implements AgentService {

    private AgentState agentState;

    private ExecutorService msgInProcessQueue;
    private CrescoMeterRegistry crescoMeterRegistry;
    private Timer t;
    public AgentServiceImpl() {

        //this.msgInProcessQueue = Executors.newFixedThreadPool(4);
        this.msgInProcessQueue = Executors.newCachedThreadPool();
        //this.msgInProcessQueue = Executors.newSingleThreadExecutor();
        crescoMeterRegistry = new CrescoMeterRegistry("cresco");
        t = Timer
                .builder("my.timer")
                .description("a description of what this timer does") // optional
                .tags("region", "test") // optional
                .register(crescoMeterRegistry);
    }

    @Activate
    void activate(BundleContext context, Map<String,Object> map) {

        agentState = new AgentState();
        agentState.setId("0");
        //context.registerService(TaskService.class,this,null);
        new Thread(new PluginManager(context)).start();

    }

    @Override
    public AgentState getAgentState() {
        //return taskMap.get(id);
        return agentState;
    }

    @Override
    public void msgIn(String msg) {
        long startTime = Long.parseLong(msg);
        t.record(System.nanoTime() - startTime,TimeUnit.NANOSECONDS);
        System.out.println("SEND MESSAGE!!!");
        System.out.println("SEND MESSAGE!!!");
    }

}
package io.cresco.agent.core;


import io.cresco.agent.metrics.CrescoMeterRegistry;
import io.cresco.library.agent.Agent;
import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.micrometer.core.instrument.Timer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component(service = { AgentService.class })
public class AgentServiceImpl implements AgentService {

    private Agent agent;

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

        agent = new Agent();
        agent.setId("0");
        //context.registerService(TaskService.class,this,null);

    }



    @Override
    public Agent getAgent() {
        //return taskMap.get(id);
        return agent;
    }

    @Override
    public void msgIn(String msg) {
        long startTime = Long.parseLong(msg);
        t.record(System.nanoTime() - startTime,TimeUnit.NANOSECONDS);

    }

}
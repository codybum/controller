package io.cresco.agent.core;

import io.cody.task.Task;
import io.cody.task.TaskService;
import io.cresco.agent.metrics.CrescoConfig;
import io.cresco.library.agent.AgentService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class PluginManager implements Runnable {

    private BundleContext context;
    private CrescoConfigAdmin crescoConfigAdmin;


    public PluginManager(BundleContext context) {

        this.context = context;
        crescoConfigAdmin = new CrescoConfigAdmin(context);

    }

    public void run(){
        //System.out.println("MyRunnable running");


            try {

                /*
                ServiceReference sr = context.getServiceReference(AgentService.class.getName());
                while(sr == null) {
                    System.out.println("SR = null !!!");
                }
                if(sr != null) {

                    boolean assign = false;
                    while (!assign) {
                        assign = sr.isAssignableTo(context.getBundle(), AgentService.class.getName());
                    }

                    System.out.println("Waiting on assignment");
                } else {
                    System.out.println("SR = null");
                }
                */

                crescoConfigAdmin.addBundle();
                crescoConfigAdmin.AddConfig();


                ServiceReference<?>[] servRefs = null;

                while(servRefs == null) {

                    //ServiceReference<?>[] servRefs = context.getServiceReferences(TaskService.class.getName(), null);

                    servRefs = context.getServiceReferences(TaskService.class.getName(), null);

                    if (servRefs == null || servRefs.length == 0) {
                        System.out.println("NULL FOUND NOTHING!");
                    } else {
                        System.out.println("Running Service Count: " + servRefs.length);

                        for (ServiceReference sr : servRefs) {
                            boolean assign = servRefs[0].isAssignableTo(context.getBundle(), TaskService.class.getName());
                            System.out.println("Can Assign Service : " + assign);

                            TaskService ts = (TaskService) context.getService(sr);

                        for(Task t : ts.getTasks()) {
                            System.out.println(t.getTitle());
                        }

                        }
                    }
                    Thread.sleep(1000);
                }




            } catch(Exception ex) {
                ex.printStackTrace();
            }

            /*
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            */



    }
}
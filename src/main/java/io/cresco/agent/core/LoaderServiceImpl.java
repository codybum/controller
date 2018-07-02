package io.cresco.agent.core;


import io.cresco.library.agent.LoaderService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/*
@Component(
        service = { LoaderService.class} ,
        immediate = true,
        reference={ @Reference(name="ConfigurationAdmin", service=ConfigurationAdmin.class) , @Reference(name="LogService", service=LogService.class) }

)
*/
public class LoaderServiceImpl implements LoaderService {

    public LoaderServiceImpl() {

    }

//    @Activate
    void activate(BundleContext context) {

        try {

            ServiceReference ref = context.getServiceReference(LogReaderService.class.getName());
            if (ref != null)
            {
                LogReaderService reader = (LogReaderService) context.getService(ref);
                reader.addLogListener(new LogWriter());
            }

            StaticAgentLoader staticAgentLoader = new StaticAgentLoader(context);
            new Thread(staticAgentLoader).start();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
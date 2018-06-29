package io.cresco.agent.controller.plugin;

import io.cresco.library.messaging.MsgEvent;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.BlockingQueue;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class PluginNode {
    private static final Logger logger = LoggerFactory.getLogger("Plugins");
    private String pluginID;
    private String jarPath;
    private String name;
    private String version;
    private boolean active = false;
    private int status_code = 3;
    private String status_desc = "Plugin Configuration Created";
    private long watchdog_ts = 0;
    private long watchdogtimer = 0;
    private long runtime = 0;
    //private String inode_id;
    //private String resource_id;

    /*
    status_code = 3; //plugin init
    status_code = 8; //plugin disabled
    status_code = 10; //started and working
    status_code = 40; //WATCHDOG check failed with agent
    status_code = 80; //failed to start
    status_code = 90; //Exception on timeout shutdown
    status_code = 91; //Exception on timeout verification to confirm down
    status_code = 92; //timeout on disable verification
     */

    public PluginNode(String pluginID, String jarPath) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.pluginID = pluginID;
        this.jarPath = jarPath;
        Manifest manifest = new JarInputStream(new FileInputStream(new File(this.jarPath))).getManifest();
        Attributes mainAttributess = manifest.getMainAttributes();
        name = mainAttributess.getValue("artifactId");
        version = mainAttributess.getValue("Implementation-Version");
        URL url = new File(jarPath).toURI().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[] {new File(jarPath).toURI().toURL()}, this.getClass().getClassLoader());
        ResourceFinder finder = new ResourceFinder("META-INF/services", loader, url);
    }

    public String getJarPath() {
        return jarPath;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public long getWatchdogTimer() {
        return watchdogtimer;
    }

    public void setWatchDogTS(long watchdog_ts) {
        this.watchdog_ts = watchdog_ts;
    }

    public long getWatchdogTS() {
        return watchdog_ts;
    }

    public void setWatchDogTimer(long watchdogtimer) {
        this.watchdogtimer = watchdogtimer;
    }

    public long getRuntime() {
        return runtime;
    }

    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    public int getStatus_code() {return status_code;}

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public void setStatus_desc(String status_desc) {
        this.status_desc = status_desc;
    }

    public String getStatus_desc() {return status_desc;}


    /*
    public String getInodeId() {return inode_id;}

    public void setInodeId(String inode_id) {
        this.inode_id = inode_id;
    }

    public String getResourceId() {return resource_id;}

    public void setResourceId(String resource_id) {
        this.resource_id = resource_id;
    }
    */

    public boolean getActive() {
        return active;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"id\":\"");
        sb.append(pluginID);
        sb.append("\",");

        sb.append("\"jar\":\"");
        sb.append(jarPath);
        sb.append("\",");

        sb.append("\"name\":\"");
        sb.append(name);
        sb.append("\",");

        sb.append("\"version\":\"");
        sb.append(version);
        sb.append("\",");

        sb.append("\"active\":");
        sb.append(active);

        sb.append("}");
        return sb.toString();
    }
}

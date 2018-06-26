package io.cresco.agent.core;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Config {

    private HierarchicalINIConfiguration iniConfObj;

    public Config(String configFile) throws ConfigurationException {
        iniConfObj = new HierarchicalINIConfiguration(configFile);
        iniConfObj.setDelimiterParsingDisabled(true);
        iniConfObj.setAutoSave(true);

    }

    public String getPluginConfigString() {
        SubnodeConfiguration sObj = iniConfObj.getSection("general");
        //final Map<String,String> result=new TreeMap<String,String>();
        StringBuilder sb = new StringBuilder();
        final Iterator it = sObj.getKeys();
        while (it.hasNext()) {
            final Object key = it.next();
            final String value = sObj.getString(key.toString());
            //result.put(key.toString(),value);
            sb.append(key.toString() + "=" + value + ",");

        }
        return sb.toString().substring(0, sb.length() - 1);
        //return result;
    }

    public Map<String,Object> getConfigMap() {

        Map<String,Object> configMap = new HashMap<>();

        SubnodeConfiguration sObj = iniConfObj.getSection("general");
        //final Map<String,String> result=new TreeMap<String,String>();
        //StringBuilder sb = new StringBuilder();
        final Iterator it = sObj.getKeys();
        while (it.hasNext()) {
            final Object key = it.next();
            final Object value = sObj.getString(key.toString());
            configMap.put(key.toString(),value);
            //result.put(key.toString(),value);
            //sb.append(key.toString() + "=" + value + ",");

        }
        //return sb.toString().substring(0, sb.length() - 1);
        //return result;
        return  configMap;
    }


    public int getIntParams(String section, String param) {
        int return_param = -1;
        try {
            SubnodeConfiguration sObj = iniConfObj.getSection(section);
            return_param = Integer.parseInt(sObj.getString(param));
        } catch (Exception ex) {
            System.out.println("AgentEngine : Config : Error : " + ex.toString());
        }
        return return_param;
    }

    public String getStringParams(String section, String param) {
        String return_param = null;
        try {
            SubnodeConfiguration sObj = iniConfObj.getSection(section);
            return_param = sObj.getString(param);
        } catch (Exception ex) {
            System.out.println("AgentEngine : Config : Error : " + ex.toString());
        }
        return return_param;
    }

    public String getPluginPath() {
        SubnodeConfiguration sObj = iniConfObj.getSection("general");
        String pluginPath = sObj.getString("pluginpath");
        if (!pluginPath.endsWith("/")) {
            pluginPath = pluginPath + "/";
        }
        return pluginPath;
    }

    public String getLogPath() {
        SubnodeConfiguration sObj = iniConfObj.getSection("general");
        String logPath = sObj.getString("logpath");
        if (logPath == null)
            logPath = "./logs";
        if (logPath.endsWith("/") || logPath.endsWith("\\\\"))
            logPath = logPath.substring(0, logPath.length() - 1);
        return new File(logPath).getAbsolutePath();
    }

    public String getPluginConfigFile() {
        SubnodeConfiguration sObj = iniConfObj.getSection("general");
        return sObj.getString("plugin_config_file");
    }


}
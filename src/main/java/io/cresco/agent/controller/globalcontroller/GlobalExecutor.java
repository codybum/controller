package io.cresco.agent.controller.globalcontroller;

import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import com.google.gson.Gson;
import io.cresco.agent.controller.app.gPayload;
import io.cresco.agent.controller.core.ControllerEngine;
import io.cresco.agent.controller.globalscheduler.PollRemovePipeline;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.*;

public class GlobalExecutor implements Executor {

    private ControllerEngine controllerEngine;
    private PluginBuilder plugin;
    private CLogger logger;
    private ExecutorService removePipelineExecutor;


    public GlobalExecutor(ControllerEngine controllerEngine) {

        this.controllerEngine = controllerEngine;
        this.plugin = controllerEngine.getPluginBuilder();
        this.logger = plugin.getLogger(GlobalExecutor.class.getName(),CLogger.Level.Info);
        removePipelineExecutor = Executors.newFixedThreadPool(100);
    }

    @Override
    public MsgEvent executeCONFIG(MsgEvent ce) {

        if(ce.getParam("action") != null) {

            switch (ce.getParam("action")) {
                case "region_disable":
                    return globalDisable(ce);

                case "region_enable":
                    return globalEnable(ce);

                case "regionalimport":
                    return regionalImport(ce);

                case "addplugin":
                    return addPlugin(ce);

                case "removeplugin":
                    return removePlugin(ce);

                case "gpipelinesubmit":
                    return gPipelineSubmit(ce);

                case "gpipelineremove":
                    return gPipelineRemove(ce);

                case "plugindownload":
                    return pluginDownload(ce);

                case "setinodestatus":
                    return setINodeStatus(ce);

                default:
                    logger.error("Unknown configtype found: {} {}", ce.getParam("action"), ce.getMsgType());
                    return null;
            }
        }
        return null;
    }
    @Override
    public MsgEvent executeDISCOVER(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeERROR(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeINFO(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeEXEC(MsgEvent ce) {

        switch (ce.getParam("action")) {
            case "listregions":
                return listRegions(ce);

            case "listagents":
                return listAgents(ce);

            case "listplugins":
                return listPlugins(ce);

            case "listpluginsbytype":
                return listPluginsByType(ce);

            case "listpluginsrepo":
                return listPluginsRepo(ce);

            case "listrepoinstances":
                return listRepoInstances(ce);

            case "plugininfo":
                return pluginInfo(ce);

            case "pluginkpi":
                return pluginKPI(ce);

            case "resourceinfo":
                return resourceInfo(ce);

            case "netresourceinfo":
                return netResourceInfo(ce);

            case "getenvstatus":
                return getEnvStatus(ce);

            case "getinodestatus":
                return getINodeStatus(ce);

            case "resourceinventory":
                return resourceInventory(ce);

            case "plugininventory":
                return pluginInventory(ce);

            case "getgpipeline":
                return getGPipeline(ce);

            case "getgpipelineexport":
                return getGPipelineExport(ce);

            case "getgpipelinestatus":
                return getGPipelineStatus(ce);

            case "getisassignmentinfo":
                return getIsAssignment(ce);

            default:
                logger.error("Unknown configtype found: {} {}", ce.getParam("action"), ce.getMsgType());
                return null;
        }
    }
    @Override
    public MsgEvent executeWATCHDOG(MsgEvent incoming) {
        return null;
    }
    @Override
    public MsgEvent executeKPI(MsgEvent incoming) {
        globalKPI(incoming);
        return null;
    }

    //EXEC

    /**
     * Query to list all regions
     * @param ce MsgEvent.Type.EXEC, action=listregions
     * @return creates "regionslist", in compressed json format
     * @see GlobalExecutor#executeEXEC(MsgEvent)
     */
    private MsgEvent listRegions(MsgEvent ce) {
        try {
            //ce.setParam("regionslist", controllerEngine.getGDB().getRegionList());
            ce.setCompressedParam("regionslist", controllerEngine.getGDB().getRegionList());
            logger.trace("list regions return : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }
    /**
     * Query to list all agents (action_region=null) or agents in a specific region (action_region=[region]
     * @param ce MsgEvent.Type.EXEC, action=listagents, action_region=[optional region]
     *           if action_region=null all agents are listed
     * @return creates "agentslist", in compressed json format
     * @see GlobalExecutor#executeEXEC(MsgEvent)
     */
    private MsgEvent listAgents(MsgEvent ce) {

        try {
            String actionRegionAgents = null;

            if(ce.getParam("action_region") != null) {
                actionRegionAgents = ce.getParam("action_region");
            }
            //ce.setParam("agentslist",controllerEngine.getGDB().getAgentList(actionRegionAgents));
            ce.setCompressedParam("agentslist",controllerEngine.getGDB().getAgentList(actionRegionAgents));
            logger.trace("list agents return : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    /**
     * Query to list plugins on an agent:<br>
     * (action_region=[region] action_agent=[agent])
     * in a region: (action_region=[region] action_agent=null, or all know plugins: (action_region=null action_agent=null)
     * @param ce MsgEvent.Type.EXEC, action=listplugins, action_region=[optional region] action_agent=[optional agent]
     *           if action_region=null all agents are listed
     * @return creates "agentcontroller list", in compressed json format
     *
     * <ul>
     * <li>Coffee</li>
     * <li>Tea</li>
     * <li>Milk</li>
     * </ul>
     *
     * @see GlobalExecutor#executeEXEC(MsgEvent)
     */

    private MsgEvent listPlugins(MsgEvent ce) {
        try {
            String actionRegionPlugins = null;
            String actionAgentPlugins = null;

            if((ce.getParam("action_region") != null) && (ce.getParam("action_agent") != null)) {
                actionRegionPlugins = ce.getParam("action_region");
                actionAgentPlugins = ce.getParam("action_agent");
            } else if((ce.getParam("action_region") != null) && (ce.getParam("action_agent") == null)) {
                actionRegionPlugins = ce.getParam("action_region");
            }
            //ce.setParam("pluginslist",controllerEngine.getGDB().getPluginList(actionRegionPlugins, actionAgentPlugins));
            ce.setCompressedParam("pluginslist",controllerEngine.getGDB().getPluginList(actionRegionPlugins, actionAgentPlugins));
            logger.trace("list plugins return : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent listPluginsByType(MsgEvent ce) {
        try {
            String actionPluginTypeId = null;
            String actionpluginTypeValue = null;

            if((ce.getParam("action_plugintype_id") != null) && (ce.getParam("action_plugintype_value") != null)) {
                actionPluginTypeId = ce.getParam("action_plugintype_id");
                actionpluginTypeValue = ce.getParam("action_plugintype_value");
            }

            ce.setCompressedParam("pluginsbytypelist",controllerEngine.getGDB().getPluginListByType(actionPluginTypeId,actionpluginTypeValue));
            logger.trace("list plugins by type return : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent listRepoInstances(MsgEvent ce) {
        try {

            ce.setCompressedParam("listrepoinstances",controllerEngine.getGDB().getPluginListByType("pluginname","io.cresco.repo"));
            logger.trace("list repos : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }
        return ce;
    }

    private MsgEvent listPluginsRepo(MsgEvent ce) {
        try {

            ce.setCompressedParam("listpluginsrepo",controllerEngine.getGDB().getPluginListRepo());
            logger.trace("list plugins in repos : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        //String repoPluginsJSON = getPluginListByType("pluginname","io.cresco.repo");
        //
        return ce;
    }

    /**
     * Query to list a specific agentcontroller configuration
     * @param ce regionlist action, MsgEvent.Type.EXEC, action=plugininfo, action_region=[region] action_agent=[agent] action_plugin=[plugin_id]
     * @return creates "agentcontroller info", in compressed json format
     * @see GlobalExecutor#executeEXEC(MsgEvent)
     */

    private MsgEvent pluginInfo(MsgEvent ce) {
        try {
            ce.setCompressedParam("plugininfo", controllerEngine.getGDB().getPluginInfo(ce.getParam("action_region"), ce.getParam("action_agent"), ce.getParam("action_plugin")));
            logger.trace("plugins info return : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent pluginKPI(MsgEvent ce) {
        try {
            ce.setCompressedParam("pluginkpi", controllerEngine.getGDB().getIsAttachedMetrics(ce.getParam("action_region"), ce.getParam("action_agent"), ce.getParam("action_plugin")));
            logger.trace("plugins KPI return : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent netResourceInfo(MsgEvent ce) {
        try {
            ce.setParam("netresourceinfo",controllerEngine.getGDB().getNetResourceInfo());

            /*
            String actionRegionResourceInfo = null;
            String actionAgentResourceInfo = null;

            if((ce.getParam("action_region") != null) && (ce.getParam("action_agent") != null)) {
                actionRegionResourceInfo = ce.getParam("action_region");
                actionAgentResourceInfo = ce.getParam("action_agent");
            } else if((ce.getParam("action_region") != null) && (ce.getParam("action_agent") == null)) {
                actionRegionResourceInfo = ce.getParam("action_region");
            }
            ce.setParam("netresourceinfo",controllerEngine.getGDB().getResourceInfo(actionRegionResourceInfo, actionAgentResourceInfo));
            logger.trace("list plugins return : " + ce.getParams().toString());
            */
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent resourceInfo(MsgEvent ce) {
        try {
            String actionRegionResourceInfo = null;
            String actionAgentResourceInfo = null;

            if((ce.getParam("action_region") != null) && (ce.getParam("action_agent") != null)) {
                actionRegionResourceInfo = ce.getParam("action_region");
                actionAgentResourceInfo = ce.getParam("action_agent");
            } else if((ce.getParam("action_region") != null) && (ce.getParam("action_agent") == null)) {
                actionRegionResourceInfo = ce.getParam("action_region");
            }
            ce.setParam("resourceinfo",controllerEngine.getGDB().getResourceInfo(actionRegionResourceInfo, actionAgentResourceInfo));
            logger.trace("list plugins return : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent getGPipelineStatus(MsgEvent ce) {

        String actionPipeline = null;

        try {
            if(ce.getParam("action_pipeline") != null) {
                actionPipeline = ce.getParam("action_pipeline");
            }

            ce.setParam("pipelineinfo",controllerEngine.getGDB().getPipelineInfo(actionPipeline));
            logger.trace("list pipeline return : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent getIsAssignment(MsgEvent ce) {

        String actionInodeId = null;
        String actionResourceId = null;

        try {
            if((ce.getParam("action_inodeid") != null) && (ce.getParam("action_resourceid") != null)) {
                actionInodeId = ce.getParam("action_inodeid");
                actionResourceId = ce.getParam("action_resourceid");
            }

            ce.setParam("isassignmentinfo",controllerEngine.getGDB().getIsAssignedInfo(actionResourceId,actionInodeId,false));
            ce.setParam("isassignmentresourceinfo",controllerEngine.getGDB().getIsAssignedInfo(actionResourceId,actionInodeId,true));

            logger.trace("get isassigned params : " + ce.getParams().toString());
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent getGPipeline(MsgEvent ce) {
        try
        {
            if(ce.getParam("action_pipelineid") != null) {
                String actionPipelineId = ce.getParam("action_pipelineid");
                String returnGetGpipeline = controllerEngine.getGDB().getGPipeline(actionPipelineId);
                if(returnGetGpipeline != null) {
                    ce.setCompressedParam("gpipeline", returnGetGpipeline);
                    ce.setParam("success", Boolean.TRUE.toString());

                } else {
                    ce.setParam("error", "action_pipelineid does not exist.");
                }
            } else {
                ce.setParam("error", "no action_pipelineid provided.");
            }

        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent getGPipelineExport(MsgEvent ce) {
        try
        {
            if(ce.getParam("action_pipelineid") != null) {
                String actionPipelineId = ce.getParam("action_pipelineid");
                String returnGetGpipeline = controllerEngine.getGDB().getGPipelineExport(actionPipelineId);
                if (returnGetGpipeline != null) {
                    ce.setParam("gpipeline", returnGetGpipeline);
                    ce.setParam("success", Boolean.TRUE.toString());

                } else {
                    ce.setParam("error", "action_pipelineid does not exist.");
                }
            } else {
                ce.setParam("error", "no action_pipelineid provided.");
            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent getINodeStatus(MsgEvent ce) {
        try
        {
            if((ce.getParam("inode_id") != null) && (ce.getParam("resource_id") != null))
            {
                String status_code = controllerEngine.getGDB().dba.getINodeParam(ce.getParam("inode_id"),"status_code");
                String status_desc = controllerEngine.getGDB().dba.getINodeParam(ce.getParam("inode_id"),"status_desc");
                if((status_code != null) && (status_desc != null))
                {
                    ce.setParam("status_code",status_code);
                    ce.setParam("status_desc",status_desc);

                    Map<String,String> nodeMap = controllerEngine.getGDB().dba.getpNodeINode(ce.getParam("inode_id"));
                    Gson gson = new Gson();
                    String pNodeMapString = gson.toJson(nodeMap);
                    ce.setCompressedParam("pnode",pNodeMapString);

                }
                else
                {
                    ce.setParam("status_code","1");
                    ce.setParam("status_desc","Could not read iNode params");
                }
            }
            else
            {
                ce.setParam("status_code","1");
                ce.setParam("status_desc","No iNode_id found in payload!");
            }

        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent pluginInventory(MsgEvent ce) {
        try
        {
            List<String> pluginFiles = getPluginFiles();

            if(pluginFiles != null)
            {
                String pluginList = null;
                for (String pluginPath : pluginFiles)
                {
                    if(pluginList == null)
                    {
                        pluginList = getPluginName(pluginPath) + "=" + getPluginVersion(pluginPath) + ",";
                    }
                    else
                    {
                        pluginList = pluginList + getPluginName(pluginPath) + "=" + getPluginVersion(pluginPath) + ",";
                    }
                }
                pluginList = pluginList.substring(0, pluginList.length() - 1);
                ce.setParam("pluginlist", pluginList);
                ce.setMsgBody("There were " + pluginFiles.size() + " plugins found.");
            }
            else
            {
                ce.setMsgBody("No agentcontroller directory exist to inventory");
            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }
        return ce;
    }

    private MsgEvent resourceInventory(MsgEvent ce) {
        try
        {
            Map<String,String> resourceTotal = controllerEngine.getGDB().getResourceTotal();


            if(resourceTotal != null)
            {
                logger.trace(resourceTotal.toString());
                ce.setParam("resourceinventory", resourceTotal.toString());
                ce.setMsgBody("Inventory found.");
            }
            else
            {
                ce.setMsgBody("No agentcontroller directory exist to inventory");
            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent getEnvStatus(MsgEvent ce) {
        try
        {
            if((ce.getParam("environment_id") != null) && (ce.getParam("environment_value") != null))
            {
                String indexName = ce.getParam("environment_id");
                String indexValue = ce.getParam("environment_value");

                List<String> envNodeList = controllerEngine.getGDB().gdb.getANodeFromIndex(indexName, indexValue);
                ce.setParam("count",String.valueOf(envNodeList.size()));
            }
            else
            {
                ce.setParam("count","unknown");
            }

        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent pluginInfo2(MsgEvent ce) {
        try
        {
            if(ce.getParam("plugin_id") != null)
            {
                String plugin_id = ce.getParam("plugin_id");
                List<String> pluginFiles = getPluginFiles();

                if(pluginFiles != null)
                {
                    for (String pluginPath : pluginFiles)
                    {
                        String found_plugin_id = getPluginName(pluginPath) + "=" + getPluginVersion(pluginPath);
                        if(plugin_id.equals(found_plugin_id))
                        {
                            String params = getPluginParams(pluginPath);
                            if(params != null)
                            {
                                System.out.println("Found Plugin: " + plugin_id);
                                ce.setParam("node_name",getPluginName(pluginPath));
                                ce.setParam("node_id",plugin_id);
                                ce.setParam("params",params);
                            }

                        }
                    }
                }
                else
                {
                    ce.setMsgBody("Plugin does not exist");
                }
            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }



    //CONFIG

    private MsgEvent setINodeStatus(MsgEvent ce) {
        try
        {
            if((ce.getParam("action_inodeid") != null) && (ce.getParam("action_statuscode") != null) && (ce.getParam("action_statusdesc") != null))
            {
                controllerEngine.getGDB().dba.setINodeParam(ce.getParam("action_inodeid"),"status_code",ce.getParam("action_statuscode"));
                controllerEngine.getGDB().dba.setINodeParam(ce.getParam("action_inodeid"),"status_desc",ce.getParam("action_statusdesc"));
                ce.setParam("success", Boolean.TRUE.toString());
            } else {
                ce.setParam("error", "Missing Information action_inodeid=" + ce.getParam("action_inodeid") + " action_statuscode=" + ce.getParam("action_statuscode") + " action_statusdesc=" + ce.getParam("action_statusdesc"));
            }

        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent regionalImport(MsgEvent ce) {
        try {
            logger.debug("CONFIG : regionalimport message type found");
            logger.debug(ce.getParam("exportdata"));
            if(ce.getParam("exportdata") != null) {
                controllerEngine.getGDB().submitDBImport(ce.getParam("exportdata"));
                /*
                if (controllerEngine.getGDB().gdb.setDBImport(ce.getParam("exportdata"))) {
                    logger.debug("Database Imported.");
                } else {
                    logger.debug("Database Import Failed!");
                }
                */
            } else {
                logger.error("regionalImport Failed : exportdata == null");
            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return null;
    }

    private MsgEvent globalEnable(MsgEvent ce) {
        try {
            logger.debug("CONFIG : AGENTDISCOVER ADD: Region:" + ce.getParam("src_region") + " Agent:" + ce.getParam("src_agent"));
            logger.trace("Message Body [" + ce.getMsgBody() + "] [" + ce.getParams().toString() + "]");
            controllerEngine.getGDB().addNode(ce);

            //TODO Hack to create region health edge
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("enable_pending", Boolean.TRUE.toString());
            paramMap.put("region", ce.getParam("src_region"));


            String edgeId = controllerEngine.getGDB().gdb.addEdge(ce.getSrcRegion(),null,null, ce.getDstRegion(), null,null,"isRegionHealth",paramMap);

            ce.setParam("is_registered","true");
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
        //return null;
    }


    private MsgEvent globalDisable(MsgEvent ce) {
        try {
            logger.debug("CONFIG : AGENTDISCOVER REMOVE: Region:" + ce.getParam("src_region") + " Agent:" + ce.getParam("src_agent"));
            logger.trace("Message Body [" + ce.getMsgBody() + "] [" + ce.getParams().toString() + "]");
            controllerEngine.getGDB().removeNode(ce);
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return null;
        //return ce;
    }


    private MsgEvent removePlugin(MsgEvent ce) {
        try {

            if((ce.getParam("inode_id") != null) && (ce.getParam("resource_id") != null)) {
                if(controllerEngine.getGDB().dba.getpNodeINode(ce.getParam("inode_id")) != null)
                {
                    if((controllerEngine.getGDB().dba.setINodeParam(ce.getParam("inode_id"),"status_code","10")) &&
                            (controllerEngine.getGDB().dba.setINodeParam(ce.getParam("inode_id"),"status_desc","iNode scheduled for removal.")))
                    {
                        ce.setParam("status_code","10");
                        ce.setParam("status_desc","iNode scheduled for removal.");
                        //ControllerEngine.resourceScheduleQueue.add(ce);
                    }
                    else
                    {
                        ce.setParam("status_code","1");
                        ce.setParam("status_desc","Could not set iNode params");
                    }
                }
                else
                {
                    ce.setParam("status_code","1");
                    ce.setParam("status_desc","iNode_id does not exist in DB!");
                }
            }
            else
            {
                ce.setParam("status_code","1");
                ce.setParam("status_desc","No resource_id or iNode_id found in payload!");
            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent pluginDownload(MsgEvent ce) {
        try
        {
            String baseUrl = ce.getParam("pluginurl");
            if(!baseUrl.endsWith("/"))
            {
                baseUrl = baseUrl + "/";
            }

            URL website = new URL(baseUrl + ce.getParam("agentcontroller"));
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());

            File jarLocation = new File(ControllerEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            String parentDirName = jarLocation.getParent(); // to get the parent dir name
            String pluginDir = parentDirName + "/plugins";
            //check if directory exist, if not create it
            File pluginDirfile = new File(pluginDir);
            if (!pluginDirfile.exists()) {
                if (pluginDirfile.mkdir()) {
                    System.out.println("Directory " + pluginDir + " didn't exist and was created.");
                } else {
                    System.out.println("Directory " + pluginDir + " didn't exist and we failed to create it!");
                }
            }
            String pluginFile = parentDirName + "/plugins/" + ce.getParam("agentcontroller");
            boolean forceDownload = false;
            if(ce.getParam("forceplugindownload") != null)
            {
                forceDownload = true;
                System.out.println("Forcing Plugin Download");
            }

            File pluginFileObject = new File(pluginFile);
            if (!pluginFileObject.exists() || forceDownload)
            {
                FileOutputStream fos = new FileOutputStream(parentDirName + "/plugins/" + ce.getParam("agentcontroller"));

                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                if(pluginFileObject.exists())
                {
                    ce.setParam("hasplugin", ce.getParam("agentcontroller"));
                    ce.setMsgBody("Downloaded Plugin:" + ce.getParam("agentcontroller"));
                    System.out.println("Downloaded Plugin:" + ce.getParam("agentcontroller"));
                }
                else
                {
                    ce.setMsgBody("Problem Downloading Plugin:" + ce.getParam("agentcontroller"));
                    System.out.println("Problem Downloading Plugin:" + ce.getParam("agentcontroller"));
                }
            }
            else
            {
                ce.setMsgBody("Plugin already exists:" + ce.getParam("agentcontroller"));
                ce.setParam("hasplugin", ce.getParam("agentcontroller"));
                System.out.println("Plugin already exists:" + ce.getParam("agentcontroller"));
            }

        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;

    }

    private MsgEvent gPipelineRemove(MsgEvent ce) {
        try
        {
            if(ce.getParam("action_pipelineid") != null) {
                String pipelineId = ce.getParam("action_pipelineid");
                if(pipelineId != null) {
                    String pipelinString = controllerEngine.getGDB().dba.getPipeline(pipelineId);
                    if(pipelinString != null) {
                        logger.trace("removePipelineExecutor.execute(new PollRemovePipeline(agentcontroller, pipelineId));");
                        removePipelineExecutor.execute(new PollRemovePipeline(controllerEngine, pipelineId));
                                /*
                                List<String> iNodeList = controllerEngine.getGDB().dba.getresourceNodeList(pipelineId,null);

                                for(String iNodeId : iNodeList) {

                                    logger.info("removing iNode " + iNodeId);
                                    MsgEvent me = new MsgEvent(MsgEvent.Type.CONFIG, null, null, null, "add application node");

                                    me.setParam("globalcmd", "removeplugin");
                                    me.setParam("inode_id", iNodeId);
                                    me.setParam("resource_id", pipelineId);
                                    //ghw.resourceScheduleQueue.add(me);
                                    agentcontroller.getResourceScheduleQueue().add(me);

                                }
                                */

                        ce.setParam("success", Boolean.TRUE.toString());
                    } else {
                        ce.setParam("error", "action_pipelineid does not exist");
                    }
                } else {
                    ce.setParam("error", "missing action_pipelineid.");
                }

            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent gPipelineSubmit(MsgEvent ce) {
        try
        {
            if((ce.getParam("action_gpipeline") != null) && (ce.getParam("action_tenantid") != null)) {
                String pipelineJSON = ce.getCompressedParam("action_gpipeline");
                String tenantID = ce.getParam("action_tenantid");

                //pipelineJSON = controllerEngine.getGDB().gdb.stringUncompress(pipelineJSON);

                /*
                if(ce.getParam("gpipeline_compressed") != null) {
                    boolean isCompressed = Boolean.parseBoolean(ce.getParam("gpipeline_compressed"));
                    if(isCompressed) {
                        pipelineJSON = controllerEngine.getGDB().gdb.stringUncompress(pipelineJSON);
                    }
                    logger.debug("Pipeline Compressed " + isCompressed + " " + ce.getParam("gpipeline"));
                    logger.debug("*" + pipelineJSON + "*");

                }
                */

                gPayload gpay = controllerEngine.getGDB().dba.createPipelineRecord(tenantID, pipelineJSON);
                //String returnGpipeline = controllerEngine.getGDB().dba.JsonFromgPayLoad(gpay);
                //remove for the sake of network traffic
                ce.removeParam("action_gpipeline");
                ce.setParam("gpipeline_id",gpay.pipeline_id);
            } else {
                ce.setParam("error","missing data in submission");
            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    private MsgEvent addPlugin(MsgEvent ce) {
        try {
            logger.error("g: add agentcontroller!");

            if((ce.getParam("inode_id") != null) && (ce.getParam("resource_id") != null) && (ce.getParam("configparams") != null)) {

                if(controllerEngine.getGDB().dba.getpNodeINode(ce.getParam("inode_id")) == null)
                {
                    if(controllerEngine.getGDB().dba.addINode(ce.getParam("resource_id"),ce.getParam("inode_id")) != null)
                    {
                        if((controllerEngine.getGDB().dba.setINodeParam(ce.getParam("inode_id"),"status_code","0")) &&
                                (controllerEngine.getGDB().dba.setINodeParam(ce.getParam("inode_id"),"status_desc","iNode Scheduled.")) &&
                                (controllerEngine.getGDB().dba.setINodeParam(ce.getParam("inode_id"),"configparams",ce.getParam("configparams"))))
                        {
                            ce.setParam("status_code","0");
                            ce.setParam("status_desc","iNode Scheduled");
                            //ControllerEngine.resourceScheduleQueue.add(ce);
                        }
                        else
                        {
                            ce.setParam("status_code","1");
                            ce.setParam("status_desc","Could not set iNode params");
                        }
                    }
                    else
                    {
                        ce.setParam("status_code","1");
                        ce.setParam("status_desc","Could not create iNode_id!");
                    }
                }
                else
                {
                    ce.setParam("status_code","1");
                    ce.setParam("status_desc","iNode_id already exist!");
                }
            }
            else
            {
                ce.setParam("status_code","1");
                ce.setParam("status_desc","No iNode_id found in payload!");
            }
        }
        catch(Exception ex) {
            ce.setParam("error", ex.getMessage());
        }

        return ce;
    }

    //WATCHDOG
    private void globalWatchdog(MsgEvent ce) {
        try {

/*
        String region = null;
        String agent = null;
        String pluginid = null;
        String resource_id = null;
        String inode_id = null;

        region = ce.getParam("src_region");
        agent = ce.getParam("src_agent");
        pluginid = ce.getParam("src_plugin");
        resource_id = ce.getParam("resource_id");
        inode_id = ce.getParam("inode_id");

        Map<String,String> params = ce.getParams();
        controllerEngine.getGDB().dba.updateKPI(region, agent, pluginid, resource_id, inode_id, params);
 */
            controllerEngine.getGDB().watchDogUpdate(ce);


        }
        catch(Exception ex) {
            logger.error("globalWatchdog " + ex.getMessage());
        }

    }

    //KPI
    private void globalKPI(MsgEvent ce) {
        try {


            String region = null;
            String agent = null;
            String pluginid = null;
            String resource_id = null;
            String inode_id = null;

            region = ce.getParam("src_region");
            agent = ce.getParam("src_agent");
            pluginid = ce.getParam("src_plugin");
            resource_id = ce.getParam("resource_id");
            inode_id = ce.getParam("inode_id");


            ce.removeParam("ttl");
            ce.removeParam("msg");
            ce.removeParam("routepath");

            ce.removeParam("src_agent");
            ce.removeParam("src_region");
            ce.removeParam("src_plugin");
            ce.removeParam("dst_agent");
            ce.removeParam("dst_region");
            ce.removeParam("dst_plugin");

            Map<String,String> params = ce.getParams();

            //clean params for edge
				/*
				ce.removeParam("loop");
				ce.removeParam("isGlobal");
				ce.removeParam("src_agent");
				ce.removeParam("src_region");
				ce.removeParam("src_plugin");
				ce.removeParam("dst_agent");
				ce.removeParam("dst_region");
				ce.removeParam("dst_plugin");
				*/
            controllerEngine.getGDB().dba.updateKPI(region, agent, pluginid, resource_id, inode_id, params);

        }
        catch(Exception ex) {
            logger.error("globalKPI " + ex.getMessage());
        }

    }


    public String getPluginName(String jarFile) {
        String version;
        try{
            //String jarFile = AgentEngine.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            //System.out.println("JARFILE:" + jarFile);
            //File file = new File(jarFile.substring(5, (jarFile.length() )));
            File file = new File(jarFile);
            FileInputStream fis = new FileInputStream(file);
            @SuppressWarnings("resource")
            JarInputStream jarStream = new JarInputStream(fis);
            Manifest mf = jarStream.getManifest();

            Attributes mainAttribs = mf.getMainAttributes();
            version = mainAttribs.getValue("artifactId");
        }
        catch(Exception ex)
        {
            String msg = "Unable to determine Plugin Version " + ex.toString();
            System.err.println(msg);
            version = "Unable to determine Version";
        }
        return version;
    }

    public Map<String,String> getPluginParamMap(String jarFileName) {
        Map<String,String> phm = null;
        try
        {
            phm = new HashMap<String,String>();
            JarFile jarFile = new JarFile(jarFileName);
            JarEntry je = jarFile.getJarEntry("agentcontroller.conf");
            InputStream in = jarFile.getInputStream(je);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.replaceAll("\\s+","");
                String[] sline = line.split("=");
                if((sline[0] != null) && (sline[1] != null))
                {
                    phm.put(sline[0], sline[1]);
                }
            }
            reader.close();
            in.close();
            jarFile.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return phm;
    }

    public  String getPluginParams(String jarFileName) {
        String params = "";
        try
        {
            JarFile jarFile = new JarFile(jarFileName);
            JarEntry je = jarFile.getJarEntry("agentcontroller.conf");
            InputStream in = jarFile.getInputStream(je);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.replaceAll("\\s+","");
                if(line.contains("="))
                {
                    String[] sline = line.split("=");
                    if((sline[0] != null) && (sline[1] != null))
                    {
                        //phm.put(sline[0], sline[1]);
                        if((sline[1].equals("required")) || sline[1].equals("optional"))
                        {
                            params = params + sline[0] + ":" + sline[1] + ",";
                        }
                    }
                }
            }
            reader.close();
            in.close();
            jarFile.close();
            if(params.length() == 0)
            {
                params = null;
            }
            else
            {
                params = params.substring(0,params.length() -1);
            }
        }
        catch (IOException e)
        {
            params = null;
            e.printStackTrace();
        }
        return params;
    }

    public  String getPluginVersion(String jarFile) {
        String version;
        try{
            //String jarFile = AgentEngine.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            //System.out.println("JARFILE:" + jarFile);
            //File file = new File(jarFile.substring(5, (jarFile.length() )));
            File file = new File(jarFile);
            FileInputStream fis = new FileInputStream(file);
            @SuppressWarnings("resource")
            JarInputStream jarStream = new JarInputStream(fis);
            Manifest mf = jarStream.getManifest();

            Attributes mainAttribs = mf.getMainAttributes();
            version = mainAttribs.getValue("Implementation-Version");
        }
        catch(Exception ex)
        {
            String msg = "Unable to determine Plugin Version " + ex.toString();
            System.err.println(msg);
            version = "Unable to determine Version";
        }
        return version;
    }

    public List<String> getPluginFiles() {
        List<String> pluginFiles = null;
        try
        {
            String pluginDirectory = null;
            if(plugin.getConfig().getStringParam("localpluginrepo") != null) {
                pluginDirectory = plugin.getConfig().getStringParam("localpluginrepo");
            }
            else {
                //if not listed use the controller directory
                File jarLocation = new File(ControllerEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                pluginDirectory = jarLocation.getParent(); // to get the parent dir name
            }

            File folder = new File(pluginDirectory);
            if(folder.exists())
            {
                pluginFiles = new ArrayList<String>();
                File[] listOfFiles = folder.listFiles();

                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        pluginFiles.add(listOfFile.getAbsolutePath());
                    }

                }
                if(pluginFiles.isEmpty())
                {
                    pluginFiles = null;
                }
            }
            else {
                logger.error("getPluginFiles Directory ");
            }

        }
        catch(Exception ex)
        {
            logger.error("getPluginFiles() " + ex.getMessage());
            pluginFiles = null;
        }
        return pluginFiles;
    }

    private String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

}
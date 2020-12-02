package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class WorkflowNodeDefInfoDto {
    private String nodeId;
    private String nodeName;
    private String nodeType;
    
    private String nodeDefId;

    private String serviceId;
    private String serviceName;
    
    private List<RegisteredEntityDefDto> boundEntities = new ArrayList<>();

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<RegisteredEntityDefDto> getBoundEntities() {
        return boundEntities;
    }

    public void setBoundEntities(List<RegisteredEntityDefDto> boundEntities) {
        this.boundEntities = boundEntities;
    }
    
    
}

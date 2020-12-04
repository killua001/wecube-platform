package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.dto.workflow.DynamicWorkflowInstCreationInfoDto;
import com.webank.wecube.platform.core.dto.workflow.DynamicWorkflowInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.RegisteredEntityAttrDefDto;
import com.webank.wecube.platform.core.dto.workflow.RegisteredEntityDefDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowNodeDefInfoDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributeQueryEntity;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntityQueryEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefAuthInfoQueryEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcRoleBindingRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;

@Service
public class WorkflowPublicAccessService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowPublicAccessService.class);

    @Autowired
    private ProcDefInfoRepository procDefInfoRepository;

    @Autowired
    private ProcRoleBindingRepository procRoleBindingRepository;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepository;

    @Autowired
    private EntityManager entityManager;

    public List<WorkflowDefInfoDto> fetchLatestReleasedWorkflowDefs() {
        List<WorkflowDefInfoDto> procDefInfoDtos = new ArrayList<>();
        Set<String> currUserRoleNames = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoleNames == null || currUserRoleNames.isEmpty()) {
            return procDefInfoDtos;
        }

        List<ProcDefAuthInfoQueryEntity> procDefInfos = retrieveAllAuthorizedProcDefs(currUserRoleNames);
        if (procDefInfos == null || procDefInfos.isEmpty()) {
            log.debug("There is no authorized process found for {}", currUserRoleNames);
            return procDefInfoDtos;
        }

        Map<String, ProcDefAuthInfoQueryEntity> latestProcDefInfos = new HashMap<>();
        for (ProcDefAuthInfoQueryEntity e : procDefInfos) {
            ProcDefAuthInfoQueryEntity last = latestProcDefInfos.get(e.getProcDefKey());
            if (last == null) {
                latestProcDefInfos.put(e.getProcDefKey(), e);
                continue;
            }

            if (e.getProcDefVersion() > last.getProcDefVersion()) {
                latestProcDefInfos.put(e.getProcDefKey(), e);
            }
        }

        for (ProcDefAuthInfoQueryEntity e : latestProcDefInfos.values()) {
            WorkflowDefInfoDto dto = new WorkflowDefInfoDto();
            dto.setCreatedTime("");
            dto.setProcDefId(e.getId());
            dto.setProcDefKey(e.getProcDefKey());
            dto.setProcDefName(e.getProcDefName());
            dto.setRootEntity(buildRegisteredEntityDefDto(e.getRootEntity()));
            dto.setStatus(e.getStatus());

            procDefInfoDtos.add(dto);
        }

        return procDefInfoDtos;
    }

    private RegisteredEntityDefDto buildRegisteredEntityDefDto(String rootEntity) {
        if (StringUtils.isBlank(rootEntity)) {
            return null;
        }

        String[] rootEntityParts = rootEntity.split(":");
        if (rootEntityParts.length != 2) {
            log.info("Abnormal root entity string : {}", rootEntity);
            return null;
        }

        String packageName = rootEntityParts[0];
        String entityName = rootEntityParts[1];
        //
        PluginPackageEntityQueryEntity entity = findLatestPluginPackageEntity(packageName, entityName);
        if(entity == null) {
            log.info("Cannot find entity with package name: {} and entity name: {}", packageName, entityName);
            return null;
        }
        RegisteredEntityDefDto entityDefDto = new RegisteredEntityDefDto();
        entityDefDto.setDescription(entity.getDescription());
        entityDefDto.setDisplayName(entity.getDisplayName());
        entityDefDto.setId(entity.getId());
        entityDefDto.setName(entity.getName());
        entityDefDto.setPackageName(entity.getPackageName());
        
        List<PluginPackageAttributeQueryEntity> attrs = findPluginPackageAttributesByEntityId(entity.getId());
        if(attrs == null || attrs.isEmpty()) {
            return entityDefDto;
        }
        
        for(PluginPackageAttributeQueryEntity attr : attrs ) {
            RegisteredEntityAttrDefDto dto = buildRegisteredEntityAttrDefDto(attr);
            entityDefDto.getAttributes().add(dto);
        }
        
        return entityDefDto;
    }
    
    private RegisteredEntityAttrDefDto buildRegisteredEntityAttrDefDto(PluginPackageAttributeQueryEntity attr) {
        RegisteredEntityAttrDefDto attrDto = new RegisteredEntityAttrDefDto();
        attrDto.setDataType(attr.getDataType());
        attrDto.setDescription(attr.getDescription());
        attrDto.setId(attr.getId());
        attrDto.setMandatory(attr.isMandatory());
        attrDto.setName(attr.getName());
        
        String referenceId = attr.getReferenceId();
        if(StringUtils.isBlank(referenceId)) {
            return attrDto;
        }
        
        PluginPackageAttributeQueryEntity refAttr = findPluginPackageAttributeById(referenceId);
        if(refAttr == null) {
            log.info("Cannot find plugin package attribute by reference id:{}", referenceId);
            return attrDto;
        }
        
        PluginPackageEntityQueryEntity refEntity = findPluginPackageEntityById(refAttr.getEntityId());
        if(refEntity == null) {
            log.info("Cannot find plugin package entity by reference entity id:{}", refAttr.getEntityId());
            return attrDto;
        }
        
        attrDto.setRefAttrName(refAttr.getName());
        attrDto.setRefEntityName(refEntity.getName());
        attrDto.setRefPackageName(refEntity.getPackageName());
        
        return attrDto;
    }

    

    public List<WorkflowNodeDefInfoDto> fetchWorkflowTasknodeInfos(String procDefId) {
        List<WorkflowNodeDefInfoDto> nodeDefInfoDtos = new ArrayList<>();

        if (StringUtils.isBlank(procDefId)) {
            return nodeDefInfoDtos;
        }

        Optional<ProcDefInfoEntity> procDefInfoOpt = procDefInfoRepository.findById(procDefId);
        if (!procDefInfoOpt.isPresent()) {
            log.info("Invalid process id {}", procDefId);
            return nodeDefInfoDtos;
        }

        ProcDefInfoEntity procDefInfo = procDefInfoOpt.get();

        Set<String> currUserRoleNames = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoleNames == null || currUserRoleNames.isEmpty()) {
            log.info("There is not any user role names found to fetch workflow task node infos.");
            return nodeDefInfoDtos;
        }

        ProcRoleBindingEntity procRoleBinding = null;
        for (String roleName : currUserRoleNames) {
            Optional<ProcRoleBindingEntity> procRoleBindingOpt = procRoleBindingRepository
                    .findByProcIdAndRoleNameAndPermission(procDefInfo.getId(), roleName,
                            ProcRoleBindingEntity.permissionEnum.USE);
            if (procRoleBindingOpt.isPresent()) {
                procRoleBinding = procRoleBindingOpt.get();
                break;
            }
        }

        if (procRoleBinding == null) {
            log.info("There is not any authorized process found for {}.", currUserRoleNames);
            return nodeDefInfoDtos;
        }

        List<TaskNodeDefInfoEntity> taskNodeDefInfos = taskNodeDefInfoRepository.findAllByProcDefId(procDefId);

        if (taskNodeDefInfos == null || taskNodeDefInfos.isEmpty()) {
            return nodeDefInfoDtos;
        }

        for (TaskNodeDefInfoEntity nodeDefInfo : taskNodeDefInfos) {
            WorkflowNodeDefInfoDto nodeDto = new WorkflowNodeDefInfoDto();
            nodeDto.setNodeDefId(nodeDefInfo.getId());
            nodeDto.setNodeId(nodeDefInfo.getNodeId());
            nodeDto.setNodeName(nodeDefInfo.getNodeName());
            nodeDto.setNodeType(nodeDefInfo.getNodeType());
            nodeDto.setServiceId(nodeDefInfo.getServiceId());
            nodeDto.setServiceName(nodeDefInfo.getServiceName());

            nodeDefInfoDtos.add(nodeDto);
        }

        return nodeDefInfoDtos;
    }

    public DynamicWorkflowInstInfoDto createNewWorkflowInstance(DynamicWorkflowInstCreationInfoDto creationInfoDto) {
        return new DynamicWorkflowInstInfoDto();
    }
    
    private PluginPackageEntityQueryEntity findLatestPluginPackageEntity(String packageName, String entityName) {
        String sql = "SELECT " + "    t1.id AS id," + "    t1.data_model_version AS dataModelVersion,"
                + "    t1.package_name AS packageName," + "    t1.name AS name," + "    t1.display_name AS displayName,"
                + "    t1.description AS description " + "FROM " + "    plugin_package_entities t1 "
                + "WHERE " + "    t1.package_name = :packageName " + "        AND t1.name = :entityName "
                + "        AND t1.data_model_version = (SELECT  " + "            MAX(t2.data_model_version) "
                + "        FROM " + "            plugin_package_entities t2 " + "        WHERE "
                + "            t2.package_name = :packageName " + "                AND t2.name = :entityName "
                + "        GROUP BY t2.package_name , t2.name) ";
        
        
        Query query = entityManager.createNativeQuery(sql, PluginPackageEntityQueryEntity.class).setParameter("packageName",
                packageName).setParameter("entityName", entityName);
        PluginPackageEntityQueryEntity entity = (PluginPackageEntityQueryEntity)query.getSingleResult();
        return entity;
    }
    
    private PluginPackageEntityQueryEntity findPluginPackageEntityById(String entityId) {
        String sql = "SELECT " + "    t1.id AS id," + "    t1.data_model_version AS dataModelVersion,"
                + "    t1.package_name AS packageName," + "    t1.name AS name," + "    t1.display_name AS displayName,"
                + "    t1.description AS description " + "FROM " + "    plugin_package_entities t1 "
                + "WHERE " + "    t1.id = :entityId ";
        
        Query query = entityManager.createNativeQuery(sql, PluginPackageEntityQueryEntity.class).setParameter("entityId",
                entityId);
        PluginPackageEntityQueryEntity entity = (PluginPackageEntityQueryEntity)query.getSingleResult();
        
        return entity;
    }

    @SuppressWarnings("unchecked")
    private List<ProcDefAuthInfoQueryEntity> retrieveAllAuthorizedProcDefs(Set<String> roleNames) {

        String sql = "SELECT t1.id AS id," + "t1.proc_def_key AS procDefKey," + "t1.proc_def_name AS procDefName,"
                + "t1.root_entity AS rootEntity," + "t1.status AS status," + "t1.proc_def_ver AS procDefVersion "
                + "FROM core_re_proc_def_info t1 JOIN core_ru_proc_role_binding t2 "
                + "ON t2.proc_id = t1.id AND t2.permission = 'USE' AND t2.role_name IN (:roleNames) "
                + "AND t1.status = 'deployed'";

        Query query = entityManager.createNativeQuery(sql, ProcDefAuthInfoQueryEntity.class).setParameter("roleNames",
                roleNames);
        List<ProcDefAuthInfoQueryEntity> procDefInfos = query.getResultList();
        if (procDefInfos == null) {
            return Collections.emptyList();
        }

        return procDefInfos;
    }
    
    private PluginPackageAttributeQueryEntity findPluginPackageAttributeById(String attrId) {
        String sql = "SELECT  " + 
                "    t1.id AS id, " + 
                "    t1.entity_id AS entityId, "+
                "    t1.name AS name, " + 
                "    t1.data_type AS dataType, " + 
                "    t1.description AS description, " + 
                "    t1.reference_id AS referenceId " + 
                "FROM " + 
                "    plugin_package_attributes t1 " + 
                "WHERE " + 
                "    t1.id = :attrId ";
        
        Query query = entityManager.createNativeQuery(sql, PluginPackageAttributeQueryEntity.class).setParameter("attrId",
                attrId);
        PluginPackageAttributeQueryEntity entity = (PluginPackageAttributeQueryEntity)query.getSingleResult();
        return entity;
    }
    
    @SuppressWarnings("unchecked")
    private List<PluginPackageAttributeQueryEntity> findPluginPackageAttributesByEntityId(String entityId){
        String sql = "SELECT " + 
                "    t1.id AS id," + 
                "    t1.name AS name," + 
                "    t1.data_type AS dataType," + 
                "    t1.description AS description," + 
                "    t1.reference_id AS referenceId " + 
                "FROM " + 
                "    plugin_package_attributes t1 " + 
                "WHERE " + 
                "    t1.entity_id = :entityId";
        
        Query query = entityManager.createNativeQuery(sql, PluginPackageAttributeQueryEntity.class).setParameter("entityId",
                entityId);
        
        List<PluginPackageAttributeQueryEntity> attributes = query.getResultList();
        return attributes;
    }

}

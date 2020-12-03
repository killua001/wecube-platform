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
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowNodeDefInfoDto;
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
            dto.setRootEntity(e.getRootEntity());
            dto.setStatus(e.getStatus());

            procDefInfoDtos.add(dto);
        }

        return procDefInfoDtos;
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
            return nodeDefInfoDtos;
        }

        ProcRoleBindingEntity procRoleBinding = null;
        for (String roleName : currUserRoleNames) {
            Optional<ProcRoleBindingEntity> procRoleBindingOpt = procRoleBindingRepository
                    .findByProcIdAndRoleNameAndPermission(procDefInfo.getId(), roleName,
                            ProcRoleBindingEntity.permissionEnum.USE);
            if(procRoleBindingOpt.isPresent()) {
                procRoleBinding = procRoleBindingOpt.get();
                break;
            }
        }
        
        if(procRoleBinding == null) {
            log.info("There is not any authorized process found for {}.", currUserRoleNames);
            return nodeDefInfoDtos;
        }
        
        List<TaskNodeDefInfoEntity> taskNodeDefInfos = taskNodeDefInfoRepository.findAllByProcDefId(procDefId);
        
        if(taskNodeDefInfos == null || taskNodeDefInfos.isEmpty()) {
            return nodeDefInfoDtos;
        }
        
        for(TaskNodeDefInfoEntity nodeDefInfo : taskNodeDefInfos) {
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

}

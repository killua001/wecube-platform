package com.webank.wecube.platform.core.controller.workflow;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.DynamicWorkflowInstCreationInfoDto;
import com.webank.wecube.platform.core.dto.workflow.DynamicWorkflowInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowNodeDefInfoDto;

@RestController
@RequestMapping("/v1")
public class WorkflowPublicAccessController {
    
    @GetMapping("/release/process/definitions")
    public CommonResponseDto fetchLatestReleasedWorkflowDefs() {
        List<WorkflowDefInfoDto> procDefInfos = new ArrayList<>();
        //TODO
        return CommonResponseDto.okayWithData(procDefInfos);
    }
    
    @GetMapping("/release/process/definitions/{proc-def-id}/tasknodes")
    public CommonResponseDto fetchWorkflowTasknodeInfos(@PathVariable("proc-def-id")String procDefId) {
        List<WorkflowNodeDefInfoDto> nodeDefInfos = new ArrayList<>();
        //TODO
        return CommonResponseDto.okayWithData(nodeDefInfos);
    }

    
    @PostMapping("/release/process/instances")
    public CommonResponseDto createNewWorkflowInstance(@RequestBody DynamicWorkflowInstCreationInfoDto creationInfoDto) {
        DynamicWorkflowInstInfoDto procInstInfo = new DynamicWorkflowInstInfoDto();
        return CommonResponseDto.okayWithData(procInstInfo);
    }
}

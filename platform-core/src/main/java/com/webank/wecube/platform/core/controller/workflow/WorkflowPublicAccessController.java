package com.webank.wecube.platform.core.controller.workflow;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowNodeDefInfoDto;

@RestController
@RequestMapping("/v1")
public class WorkflowPublicAccessController {
    
    @GetMapping("/release/process/definitions")
    public CommonResponseDto fetchLatestReleasedWorkflowDefs() {
        List<WorkflowDefInfoDto> defInfos = new ArrayList<>();
        //TODO
        return CommonResponseDto.okayWithData(defInfos);
    }
    
    @GetMapping("/release/process/definitions/{proc-def-id}/tasknodes")
    public CommonResponseDto fetchWorkflowTasknodeInfos(@PathVariable("proc-def-id")String procDefId) {
        List<WorkflowNodeDefInfoDto> nodeInfos = new ArrayList<>();
        //TODO
        return CommonResponseDto.okayWithData(nodeInfos);
    }

}

package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.service.plugin.BatchExecutionService;

@RestController
@RequestMapping("/v1")
public class BatchExecutionController {

    @Autowired
    private BatchExecutionService batchExecutionService;

    /**
     * 
     * @param batchExecutionRequest
     * @return
     */
    @PostMapping("/batch-execution/run")
    public CommonResponseDto runBatchExecution(@RequestBody BatchExecutionRequestDto batchExecutionRequest){
        return okayWithData(batchExecutionService.handleBatchExecutionJob(batchExecutionRequest));
    }

}
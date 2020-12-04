package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowNodeDefInfoDto;

//@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowPublicAccessServiceTest {
    
    @Autowired
    private WorkflowPublicAccessService workflowPublicAccessService;
    
    @Before
    public void setUp() {
        List<String> authorities = new ArrayList<>();
        authorities.add("SUPER_ADMIN");
        AuthenticatedUser user = new AuthenticatedUser("umadmin", null, authorities);
        AuthenticationContextHolder.setAuthenticatedUser(user);
    }
    
    @Test
    public void testFetchLatestReleasedWorkflowDefs() {
        List<WorkflowDefInfoDto> defInfos = workflowPublicAccessService.fetchLatestReleasedWorkflowDefs();
        
        System.out.println("size:"+defInfos.size());
        for(WorkflowDefInfoDto dto : defInfos) {
            System.out.println(dto.getRootEntity());
        }
    }

    @Test
    public void testFetchWorkflowTasknodeInfos() {
        List<WorkflowNodeDefInfoDto> dtos = workflowPublicAccessService.fetchWorkflowTasknodeInfos("seNN7xCA2Cp");
        dtos.forEach(d -> {
            System.out.println(d.getNodeName());
        });
    }
    

}

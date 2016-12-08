package de.holisticon.bpm.cughh;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.spring.boot.starter.test.helper.AbstractProcessEngineRuleTest;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

@Deployment(resources = "bpm/SampleProcess.bpmn")
public class SampleProcessTest extends AbstractProcessEngineRuleTest {

    @Test
    public void create_new_task() throws Exception {
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("SampleProcess");
        assertThat(processInstance).isWaitingAt("task-do-important-stuff");

        assertThat(taskService().createTaskQuery().processInstanceId(processInstance.getId()).list()).hasSize(1);

        runtimeService().setVariable(processInstance.getId(), "count", 1);
        assertThat(taskService().createTaskQuery().processInstanceId(processInstance.getId()).list()).hasSize(1);

        runtimeService().setVariable(processInstance.getId(), "count", 2);
        assertThat(taskService().createTaskQuery().processInstanceId(processInstance.getId()).list()).hasSize(2);
    }
}

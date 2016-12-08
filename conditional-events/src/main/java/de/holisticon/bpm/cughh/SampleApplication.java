package de.holisticon.bpm.cughh;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.extension.reactor.bus.CamundaEventBus;
import org.camunda.bpm.extension.reactor.bus.SelectorBuilder;
import org.camunda.bpm.extension.reactor.spring.CamundaReactorConfiguration;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.function.Function;

import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_CREATE;
import static org.camunda.bpm.extension.reactor.bus.SelectorBuilder.selector;

@SpringBootApplication
@EnableProcessApplication
@EnableScheduling
@Slf4j
@Import(CamundaReactorConfiguration.class)
public class SampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private CamundaEventBus eventBus;

    private boolean increase;


    @PostConstruct
    public void postConstruct() {
        eventBus.register(selector().bpmn().task()
                        .event(EVENTNAME_CREATE),
                (TaskListener) task -> {
                    task.setName(String.format("%s (%s)",
                            task.getName(),
                            Optional.ofNullable(task.getVariable("count"))
                                    .orElse(0)));
                });
    }

    @EventListener
    public void startIncreasing(ProcessApplicationStartedEvent unused) {
        increase = true;
    }

    @Scheduled(fixedDelay = 5000L)
    public void increaseCount() {
        if (!increase) {
            return;
        }
        runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("SampleProcess")
                .list()
                .forEach(instance -> {
                    Integer value = (Integer) runtimeService.getVariable(instance.getId(), "count");

                    int next = value != null ? value.intValue() + 1 : 1;

                    log.info("increasing {} {}", instance.getId(), next);

                    runtimeService.setVariable(instance.getId(), "count", Integer.valueOf(next));

                });
    }


}

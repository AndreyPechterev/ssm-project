package com.pechterev.statemachine;

import com.pechterev.statemachine.configuration.IdentInquiryIntermediateStateMachineConfiguration;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

@SpringBootTest(classes = IdentInquiryIntermediateStateMachineConfiguration.class)
@DisplayName("Стейтмашина промежуточных состояний заявки на идентификацию должна ")
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractStateMachineSpringBootTest {

    static final String MSG = "msg";

    @Autowired
    StateMachineFactory<IdentInquiryState, IdentInquiryStateEvent> factory;

    StateMachine<IdentInquiryState, IdentInquiryStateEvent> stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = factory.getStateMachine();
    }
}

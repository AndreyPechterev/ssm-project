package com.pechterev.statemachine.configuration;



import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Класс для получения стейтмашины в актуальном состоянии заявки
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InquiryStateMachineBuilder {

    private final StateMachineFactory<IdentInquiryState, IdentInquiryStateEvent> stateFactory;

    public StateMachine<IdentInquiryState, IdentInquiryStateEvent> build(InquiryEntity inquiry) {
        var stateMachine = stateFactory.getStateMachine(inquiry.getId());
        stateMachine.stop();

        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        IdentInquiryState currentState = IdentInquiryState.valueOfState(inquiry.getState());
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(currentState,
                        null, null, extendedState, null, inquiry.getId().toString())));
        log.debug("Восстановлена StateMachine для заявки {} в состоянии {}", inquiry.getId(), inquiry.getState());
        stateMachine.start();
        return stateMachine;
    }
}


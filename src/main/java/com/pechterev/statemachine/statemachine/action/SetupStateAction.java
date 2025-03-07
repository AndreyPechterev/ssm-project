package com.pechterev.statemachine.statemachine.action;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

/**
 * Установка стейта в заявку
 */
@Slf4j
public class SetupStateAction extends AbstractInquiryAction {

    @Override
    public void execute(StateContext<IdentInquiryState, IdentInquiryStateEvent> context) {
        InquiryEntity inquiry = getInquiry(context);
        Integer state = context.getStateMachine().getState().getId().getState();
        inquiry.setState(state);
        log.debug("Обновлено состояние заявки на {} ({})", state, IdentInquiryState.valueOfState(state));
    }
}

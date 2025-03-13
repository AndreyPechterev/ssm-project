package com.pechterev.statemachine.action;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

/**
 * Абстрактный класс связанный действием с заявкой
 */
public abstract class AbstractInquiryAction implements Action<IdentInquiryState, IdentInquiryStateEvent> {

    protected InquiryEntity getInquiry(StateContext<IdentInquiryState, IdentInquiryStateEvent> context) {
        InquiryEntity inquiryEntity = context.getExtendedState()
                .get(IdentInquiryStateMachineUtils.INQUIRY_VAR, InquiryEntity.class);
        if (inquiryEntity == null) {
            throw new IllegalStateException(IdentInquiryStateMachineUtils.ILLEGAL_STATE_MGG);
        }
        return inquiryEntity;
    }
}


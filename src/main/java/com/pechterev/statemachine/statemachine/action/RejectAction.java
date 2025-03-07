package com.pechterev.statemachine.statemachine.action;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryStage;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

/**
 * Класс для установки стадии заявки Rejected
 */
@Slf4j
public class RejectAction extends AbstractInquiryAction {

    @Override
    public void execute(StateContext<IdentInquiryState, IdentInquiryStateEvent> context) {
        InquiryEntity inquiry = getInquiry(context);
        inquiry.setStage(IdentInquiryStage.REJECT.name());
        inquiry.setState(IdentInquiryState.REJECTED.getState());
        log.info("Для заявки {} установлена стадия {}", inquiry.getId(), IdentInquiryStage.REJECT);
    }
}

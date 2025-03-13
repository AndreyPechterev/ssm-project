package com.pechterev.statemachine.action;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryStage;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

/**
 * Класс для установки стадии заявки Completed
 */
@Slf4j
public class CompletedAction extends AbstractInquiryAction {

    @Override
    public void execute(StateContext<IdentInquiryState, IdentInquiryStateEvent> context) {
        InquiryEntity inquiry = getInquiry(context);
        inquiry.setStage(IdentInquiryStage.COMPLETED.name());
        log.info("Для заявки {} установлена стадия {}", inquiry.getId(), IdentInquiryStage.COMPLETED);
    }
}


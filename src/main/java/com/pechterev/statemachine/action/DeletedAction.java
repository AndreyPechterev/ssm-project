package com.pechterev.statemachine.action;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

/**
 * Класс для установки стадии заявки Deleted
 */
@Slf4j
public class DeletedAction extends AbstractInquiryAction {

    @Override
    public void execute(StateContext<IdentInquiryState, IdentInquiryStateEvent> context) {
        InquiryEntity inquiry = getInquiry(context);
        inquiry.setStage(IdentInquiryState.DELETED.name());
        log.info("Для заявки {} установлена стадия {}", inquiry.getId(), IdentInquiryState.DELETED);
    }

}


package com.pechterev.statemachine.guard;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

/**
 * Guard для проверки оставшихся попыток для перевода заявки в соответствующую стадию
 */
@Slf4j
public class AttemptsGuard implements Guard<IdentInquiryState, IdentInquiryStateEvent> {

    @Override
    public boolean evaluate(StateContext<IdentInquiryState, IdentInquiryStateEvent> stateContext) {
        InquiryEntity inquiryEntity = stateContext.getExtendedState()
                .get(IdentInquiryStateMachineUtils.INQUIRY_VAR, InquiryEntity.class);
        if (inquiryEntity == null) {
            throw new IllegalStateException(IdentInquiryStateMachineUtils.ILLEGAL_STATE_MGG);
        }
        Integer attemptCount = inquiryEntity.getAttemptCount();
        log.debug("Проверка количества оставшихся попыток идентификации по заявке {}, осталось попыток: {}",
                inquiryEntity.getId(), attemptCount);
        return attemptCount >= 1;
    }
}


package com.pechterev.statemachine.action;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

/**
 * Action для уменьшения количества попыток идентификации по заявке
 */
@Slf4j
public class AttemptDecreaseAction extends AbstractInquiryAction {

    /**
     * Метод получает из контекста переменную inquiry, в которую мы должны положить объект сущности
     * для изменения параметра attemptCount
     *
     * @param context контекст стейтмашины
     * @throws IllegalStateException если в extendedState не поместили объект сущность для изменения
     */
    @Override
    public void execute(StateContext<IdentInquiryState, IdentInquiryStateEvent> context) {
        InquiryEntity inquiryEntity = getInquiry(context);
        Integer currentAttemptCount = inquiryEntity.getAttemptCount();
        int attemptsLeft = 0;
        if (currentAttemptCount != null) {
            attemptsLeft = currentAttemptCount - 1;
        }
        inquiryEntity.setAttemptCount(attemptsLeft);
        log.info("Количество попыток уменьшено до {} для заявки {}", attemptsLeft, context.getStateMachine().getId());
    }
}


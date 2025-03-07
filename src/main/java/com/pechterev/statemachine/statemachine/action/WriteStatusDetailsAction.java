package com.pechterev.statemachine.statemachine.action;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateContext;

/**
 * Action для записи statusDetails в inquiry
 */
@Slf4j
public class WriteStatusDetailsAction extends AbstractInquiryAction {

    public static final String NO_FAIL_MSG_HEADER = "Не передан заголовок %s".formatted(IdentInquiryStateMachineUtils.MSG_HEADER);

    /**
     * Метод получает из контекста переменную inquiry, для записи сообщения в statusDetails
     *
     * @param context контекст стейтмашины
     * @throws IllegalStateException если в extendedState не поместили объект сущность для изменения
     */
    @Override
    public void execute(StateContext<IdentInquiryState, IdentInquiryStateEvent> context) {
        InquiryEntity inquiryEntity = getInquiry(context);
        if (context.getMessageHeaders() == null ||
                context.getMessageHeaders().get(IdentInquiryStateMachineUtils.MSG_HEADER, String.class) == null) {
            throw new IllegalStateException(NO_FAIL_MSG_HEADER);
        }
        MessageHeaders headers = context.getMessageHeaders();
        String msg = headers.get(IdentInquiryStateMachineUtils.MSG_HEADER, String.class);
        inquiryEntity.setStatusDetails(msg);
        log.info("Установлено в statusDetails: {}", msg);
    }
}

package com.pechterev.statemachine.util;

import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import lombok.experimental.UtilityClass;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@UtilityClass
public class TestData {

    public static final String NOT_CONFIRMED_MSG = "Учетная запись СФЛ не подтверждена";

    public static final Message<IdentInquiryStateEvent> NOT_CONFIRMED_EVENT = MessageBuilder
            .withPayload(IdentInquiryStateEvent.NOT_CONFIRMED_ESIA_PROFILE)
            .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER,
                    NOT_CONFIRMED_MSG).build();
}


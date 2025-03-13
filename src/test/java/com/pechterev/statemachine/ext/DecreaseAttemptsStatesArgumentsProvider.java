package com.pechterev.statemachine.ext;

import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.messaging.support.MessageBuilder;

import java.util.stream.Stream;

public class DecreaseAttemptsStatesArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        var notConfirmedEsia = MessageBuilder.withPayload(IdentInquiryStateEvent.NOT_CONFIRMED_ESIA_PROFILE)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, "test").build();
        var mismatchedScope = MessageBuilder.withPayload(IdentInquiryStateEvent.SCOPES_DONT_MATCH)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, "test").build();
        return Stream.of(
                Arguments.of(notConfirmedEsia, mismatchedScope, notConfirmedEsia),
                Arguments.of(notConfirmedEsia, notConfirmedEsia, notConfirmedEsia),
                Arguments.of(mismatchedScope, mismatchedScope, mismatchedScope),
                Arguments.of(notConfirmedEsia, notConfirmedEsia, mismatchedScope),
                Arguments.of(mismatchedScope, notConfirmedEsia, notConfirmedEsia)
        );
    }
}

package com.pechterev.statemachine.action;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateContext;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@DisplayName("WriteStatusDetailsAction должен ")
@SuppressWarnings({"rawtypes", "unchecked"})
@FieldDefaults(level = AccessLevel.PRIVATE)
class WriteStatusDetailsActionTest {

    final WriteStatusDetailsAction action = new WriteStatusDetailsAction();

    DefaultStateContext mockCtx;

    StateMachine mockSm;

    @BeforeEach
    void setUp() {
        mockCtx = Mockito.mock(DefaultStateContext.class);
        mockSm = Mockito.mock(StateMachine.class);
        Mockito.when(mockCtx.getStateMachine()).thenReturn(mockSm);
    }

    @DisplayName("записать в поле statusDetails данные об ошибке")
    @Test
    void shouldWriteStatusDetails() {
        UUID inquiryId = UUID.randomUUID();
        Mockito.when(mockSm.getId()).thenReturn(inquiryId.toString());
        MessageHeaders messageHeaders = new MessageHeaders(Map.of(IdentInquiryStateMachineUtils.MSG_HEADER, "сообщение"));
        Mockito.when(mockCtx.getMessageHeaders()).thenReturn(messageHeaders);
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        Mockito.when(mockCtx.getExtendedState()).thenReturn(extendedState);

        action.execute(mockCtx);

        Assertions.assertThat(inquiry.getStatusDetails()).isEqualTo("сообщение");
    }

    @DisplayName("бросить исключение если не переданы заголовки")
    @Test
    void shouldThrowExceptionIfNoHeaders() {
        UUID inquiryId = UUID.randomUUID();
        Mockito.when(mockSm.getId()).thenReturn(inquiryId.toString());
        Mockito.when(mockCtx.getMessageHeaders()).thenReturn(null);
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        Mockito.when(mockCtx.getExtendedState()).thenReturn(extendedState);

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> action.execute(mockCtx))
                .withMessage(WriteStatusDetailsAction.NO_FAIL_MSG_HEADER
                        .formatted(IdentInquiryStateMachineUtils.MSG_HEADER));
    }

    @DisplayName("бросить исключение если не передан заголовок с сообщением")
    @Test
    void shouldThrowExceptionIfNoFailMsgHeader() {
        UUID inquiryId = UUID.randomUUID();
        Mockito.when(mockSm.getId()).thenReturn(inquiryId.toString());
        MessageHeaders messageHeaders = new MessageHeaders(Collections.emptyMap());
        Mockito.when(mockCtx.getMessageHeaders()).thenReturn(messageHeaders);
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        Mockito.when(mockCtx.getExtendedState()).thenReturn(extendedState);

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> action.execute(mockCtx))
                .withMessage(WriteStatusDetailsAction.NO_FAIL_MSG_HEADER
                        .formatted(IdentInquiryStateMachineUtils.MSG_HEADER));
    }

    @DisplayName("бросить исключение если не был передан объект сущности заявки")
    @Test
    void shouldThrowExceptionIfNoInquiryPassed() {
        DefaultExtendedState extendedState = new DefaultExtendedState();
        Mockito.when(mockCtx.getExtendedState()).thenReturn(extendedState);

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> action.execute(mockCtx))
                .withMessage(IdentInquiryStateMachineUtils.ILLEGAL_STATE_MGG);
    }

}

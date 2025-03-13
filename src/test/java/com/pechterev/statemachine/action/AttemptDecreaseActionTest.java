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
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateContext;

import java.util.Map;
import java.util.UUID;

@DisplayName("AttemptDecreaseAction должен ")
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings({"rawtypes", "unchecked"})
class AttemptDecreaseActionTest {

    final AttemptDecreaseAction action = new AttemptDecreaseAction();

    DefaultStateContext mockCtx;

    StateMachine mockSm;

    @BeforeEach
    void setUp() {
        mockCtx = Mockito.mock(DefaultStateContext.class);
        mockSm = Mockito.mock(StateMachine.class);
        Mockito.when(mockCtx.getStateMachine()).thenReturn(mockSm);
    }

    @DisplayName("уменьшить на 1 количество попыток в заявке")
    @Test
    void shouldDecreaseEntityAttemptAction() {
        UUID inquiryId = UUID.randomUUID();
        Mockito.when(mockSm.getId()).thenReturn(inquiryId.toString());
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(100).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        Mockito.when(mockCtx.getExtendedState()).thenReturn(extendedState);

        action.execute(mockCtx);

        Assertions.assertThat(inquiry).hasFieldOrPropertyWithValue("attemptCount", 99)
                .hasFieldOrPropertyWithValue("id", inquiryId);
    }

    @DisplayName("установить количество оставшихся попыток на 0, если по какой то причине у заявки " +
            "количество попыток не установлено")
    @Test
    void shouldSetAttemptCountToZeroIfAttemptCountIsNull() {
        UUID inquiryId = UUID.randomUUID();
        Mockito.when(mockSm.getId()).thenReturn(inquiryId.toString());
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        Mockito.when(mockCtx.getExtendedState()).thenReturn(extendedState);

        action.execute(mockCtx);

        Assertions.assertThat(inquiry).hasFieldOrPropertyWithValue("attemptCount", 0)
                .hasFieldOrPropertyWithValue("id", inquiryId);
    }

    @DisplayName("бросить исключение если не был передан объект сущности заявки")
    @Test
    void shouldThrowException() {
        DefaultExtendedState extendedState = new DefaultExtendedState();
        Mockito.when(mockCtx.getExtendedState()).thenReturn(extendedState);

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> action.execute(mockCtx))
                .withMessage(IdentInquiryStateMachineUtils.ILLEGAL_STATE_MGG);
    }
}


package com.pechterev.statemachine.it;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryStage;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

import java.util.Map;
import java.util.UUID;

import static com.pechterev.statemachine.events.IdentInquiryState.*;

class PreIdentCheckStateMachineTest extends AbstractStateMachineSpringBootTest {

    @DisplayName("пройти по состояниям по предпроверке, записать в заявку  in_progress")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesForPreValidation() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(ACCEPTED,
                        null, null, extendedState, null, inquiryId.toString())));

        var msgIntCheckPassed = MessageBuilder.withPayload(IdentInquiryStateEvent.INTERNAL_CHECK_PASSED)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, MSG)
                .build();
        var msgExtCheckPassed = MessageBuilder.withPayload(IdentInquiryStateEvent.PRE_VALIDATION_CHECKS_PASSED)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, MSG)
                .build();
        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_CHECK_STARTED)
                .expectState(IdentInquiryState.INTERNAL_CHECK_IN_PROGRESS)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(msgIntCheckPassed)
                .expectState(EXTERNAL_CHECK_IN_PROGRESS)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(msgExtCheckPassed)
                .expectState(WAITING_FOR_IDENT)
                .expectStateChanged(1)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(inquiry)
                .extracting(InquiryEntity::getState, InquiryEntity::getStage, InquiryEntity::getStatusDetails)
                .contains(WAITING_FOR_IDENT.getState(), IdentInquiryStage.IN_PROGRESS.name(), MSG);
    }

    @DisplayName("пройти по состояниям по предпроверке с пропуском дубликатов, записать в заявку  in_progress")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesForPreValidationWithForceCreate() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(ACCEPTED,
                        null, null, extendedState, null, inquiryId.toString())));

        var msgExtCheckPassed = MessageBuilder.withPayload(IdentInquiryStateEvent.PRE_VALIDATION_CHECKS_PASSED)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, MSG)
                .build();
        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_CHECK_STARTED)
                .expectState(IdentInquiryState.INTERNAL_CHECK_IN_PROGRESS)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(msgExtCheckPassed)
                .expectState(WAITING_FOR_IDENT)
                .expectStateChanged(1)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(inquiry)
                .extracting(InquiryEntity::getState, InquiryEntity::getStage, InquiryEntity::getStatusDetails)
                .contains(WAITING_FOR_IDENT.getState(), IdentInquiryStage.IN_PROGRESS.name(), MSG);
    }

    @DisplayName("перевести заявку из accepted в in progress, записать стейт")
    @Test
    @SneakyThrows
    void shouldTransitFromAcceptedToIntCheckInProgress() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId)
                .stage(IdentInquiryStage.IN_PROGRESS.name())
                .attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(ACCEPTED,
                        null, null, extendedState, null, inquiryId.toString())));
        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_CHECK_STARTED)
                .expectState(IdentInquiryState.INTERNAL_CHECK_IN_PROGRESS)
                .expectStateChanged(1)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(inquiry)
                .extracting(InquiryEntity::getState, InquiryEntity::getStage)
                .contains(INTERNAL_CHECK_IN_PROGRESS.getState(), IdentInquiryStage.IN_PROGRESS.name());
    }

    @DisplayName("завершиться при неуспешной внутренней проверке, записать в заявку статусы")
    @Test
    @SneakyThrows
    void shouldTransitForToInternalCheckFailed() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(ACCEPTED,
                        null, null, extendedState, null, inquiryId.toString())));
        var messageEvent = MessageBuilder.withPayload(IdentInquiryStateEvent.CHECK_FAILED)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, "Заявка является дубликатом").build();

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_CHECK_STARTED)
                .expectState(IdentInquiryState.INTERNAL_CHECK_IN_PROGRESS)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(messageEvent)
                .expectState(IdentInquiryState.REJECTED)
                .expectStateChanged(1)
                .expectVariable(IdentInquiryStateMachineUtils.INQUIRY_VAR)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(stateMachine.isComplete()).isTrue();
        Assertions.assertThat(inquiry)
                .extracting(InquiryEntity::getState, InquiryEntity::getStage, InquiryEntity::getStatusDetails)
                .contains(IdentInquiryState.REJECTED.getState(), IdentInquiryStage.REJECT.name(),
                        "Заявка является дубликатом");
    }

    @DisplayName("завершиться при неуспешной внешней проверке, записать в statusDetails и статусы")
    @Test
    @SneakyThrows
    void shouldTransitForToExternalCheckFailed() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(EXTERNAL_CHECK_IN_PROGRESS,
                        null, null, extendedState, null, inquiryId.toString())));
        var messageEvent = MessageBuilder.withPayload(IdentInquiryStateEvent.CHECK_FAILED)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, "При получении запроса из ЕПК всё сломалось").build();

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(messageEvent)
                .expectState(IdentInquiryState.REJECTED)
                .expectStateChanged(1)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(stateMachine.isComplete()).isTrue();
        Assertions.assertThat(inquiry)
                .extracting(InquiryEntity::getState, InquiryEntity::getStage, InquiryEntity::getStatusDetails)
                .contains(IdentInquiryState.REJECTED.getState(), IdentInquiryStage.REJECT.name(),
                        "При получении запроса из ЕПК всё сломалось");
    }
}

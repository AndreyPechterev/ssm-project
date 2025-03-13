package com.pechterev.statemachine.it;

import com.pechterev.statemachine.InquiryEntity;
import com.pechterev.statemachine.events.IdentInquiryStage;
import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.ext.DecreaseAttemptsStatesArgumentsProvider;
import com.pechterev.statemachine.util.TestData;
import com.pechterev.statemachine.utils.IdentInquiryStateMachineUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

import java.util.Map;
import java.util.UUID;

import static com.pechterev.statemachine.events.IdentInquiryState.*;

class ProcessCheckStateMachineTest extends AbstractStateMachineSpringBootTest {

    @DisplayName("пройти по состояниям по процедуре идентификации, скип внешнюю проверку")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesForIdentificationProcedureSkipExternal() {
        //TODO после включения внешней проверки по стоп листам добавить переходы
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(WAITING_FOR_IDENT,
                        null, null, extendedState, null, inquiryId.toString())));

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.IDENTIFICATION_STARTED)
                .expectState(AT_LEAST_ONE_PROCEDURE)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.PROFILE_DATA_RECEIVED)
                .expectState(IdentInquiryState.AT_LEAST_ONE_DOC_SET)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_STARTED)
                .expectState(INTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_CHECK_SUCCESS)
                .expectState(COMPLETED)
                .expectStateChanged(1)
                .and()
                .build();
        plan.test();
    }

    @DisplayName("пройти по состояниям по процедуре идентификации, скип внешнюю проверку")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesForIdentificationProcedureIfFirstAttemptWasUnSuccessful() {
        //TODO после включения внешней проверки по стоп листам добавить переходы
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        var esiaNotConfirmed = MessageBuilder.withPayload(IdentInquiryStateEvent.NOT_CONFIRMED_ESIA_PROFILE)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, "test").build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(WAITING_FOR_IDENT,
                        null, null, extendedState, null, inquiryId.toString())));

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.IDENTIFICATION_STARTED)
                .expectState(AT_LEAST_ONE_PROCEDURE)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(esiaNotConfirmed)
                .expectState(AT_LEAST_ONE_PROCEDURE)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.IDENTIFICATION_STARTED)
                .expectState(AT_LEAST_ONE_PROCEDURE)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.PROFILE_DATA_RECEIVED)
                .expectState(AT_LEAST_ONE_DOC_SET)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_STARTED)
                .expectState(INTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_CHECK_SUCCESS)
                .expectState(COMPLETED)
                .expectStateChanged(1)
                .and()
                .build();
        plan.test();
    }

    @DisplayName("пройти по состояниям по процедуре идентификации")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesForIdentificationProcedure() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(WAITING_FOR_IDENT,
                        null, null, extendedState, null, inquiryId.toString())));

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.IDENTIFICATION_STARTED)
                .expectState(AT_LEAST_ONE_PROCEDURE)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.PROFILE_DATA_RECEIVED)
                .expectState(IdentInquiryState.AT_LEAST_ONE_DOC_SET)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_STARTED)
                .expectState(INTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_FINISHED)
                .expectState(EXTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .expectStateChanged(1)
                .and()
                .step()
                .sendEvent(IdentInquiryStateEvent.EXTERNAL_PROFILE_CHECKS_FINISHED)
                .expectState(PROFILE_CHECKS_FINISHED)
                .expectStateChanged(1)
                .and()
                .build();
        plan.test();
    }

    @Disabled
    @DisplayName("перейти из WAITING FOR IDENT в EXPIRED, если заявка просрочена")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesExpiredFromWaitingForIdent() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3)
                .build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(WAITING_FOR_IDENT,
                        null, null, extendedState, null, inquiryId.toString())));

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.EXPIRED)
                .expectState(IdentInquiryState.EXPIRED)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(stateMachine.isComplete()).isTrue();
    }

    @DisplayName("перейти из AT_LEAST_ONE_PROCEDURE, в ERROR если больше нет попыток, записать состояние в details")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesNoAttemptsFromVerifyingResponseToRejected() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(1)
                .build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));

        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(AT_LEAST_ONE_PROCEDURE,
                        null, null, extendedState, null, inquiryId.toString())));

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(TestData.NOT_CONFIRMED_EVENT)
                .expectState(IdentInquiryState.ERROR)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(stateMachine.isComplete()).isTrue();
        Assertions.assertThat(inquiry)
                .extracting(InquiryEntity::getState, InquiryEntity::getStage, InquiryEntity::getStatusDetails)
                .contains(ERROR.getState(), IdentInquiryStage.ERROR.name(),
                        TestData.NOT_CONFIRMED_MSG);
    }

    @DisplayName("перейти из AT_LEAST_ONE_PR, в AT_LEAST_ONE_PROCEDURE если есть еще попытки идентификации по заявке")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesHasAttemptsFromVerifyingResponseToAtLeastOneProcedure() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId)
                .stage(IdentInquiryStage.IN_PROGRESS.name())
                .attemptCount(3)
                .build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(AT_LEAST_ONE_PROCEDURE,
                        null, null, extendedState, null, inquiryId.toString())));

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(TestData.NOT_CONFIRMED_EVENT)
                .expectState(AT_LEAST_ONE_PROCEDURE)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(stateMachine.isComplete()).isFalse();
        Assertions.assertThat(inquiry)
                .extracting(InquiryEntity::getState, InquiryEntity::getStage, InquiryEntity::getStatusDetails)
                .contains(AT_LEAST_ONE_PROCEDURE.getState(), IdentInquiryStage.IN_PROGRESS.name(),
                        TestData.NOT_CONFIRMED_MSG);
    }

    @Disabled
    @DisplayName("перейти из AT_LEAST_ONE_DOC_SET в EXPIRED, если заявка просрочена")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesExpiredFromAtLeastOneDocSet() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3)
                .build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(AT_LEAST_ONE_DOC_SET,
                        null, null, extendedState, null, inquiryId.toString())));

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.EXPIRED)
                .expectState(IdentInquiryState.EXPIRED)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(stateMachine.isComplete()).isTrue();
    }

    @Disabled
    @DisplayName("перейти из AT_LEAST_ONE_PROCEDURE в EXPIRED, если заявка просрочена")
    @Test
    @SneakyThrows
    void shouldTransitForIntermediateStatesExpiredFromAtLeastOneProcedure() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3)
                .build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(AT_LEAST_ONE_PROCEDURE,
                        null, null, extendedState, null, inquiryId.toString())));

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(IdentInquiryStateEvent.EXPIRED)
                .expectState(IdentInquiryState.EXPIRED)
                .and()
                .build();
        plan.test();

        Assertions.assertThat(stateMachine.isComplete()).isTrue();
    }

    @DisplayName("при неудачной внутренней проверке уменьшить количество попыток идентификации")
    @Test
    @SneakyThrows
    void shouldCallForAttemptDecreaseAction() {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(INTERNAL_CHECK_AFTER_PROFILE_RECEIVED,
                        null, null, extendedState, null, inquiryId.toString())));
        var failMsg = MessageBuilder.withPayload(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_FAILED)
                .setHeader(IdentInquiryStateMachineUtils.MSG_HEADER, "test").build();

        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(failMsg)
                .expectState(IdentInquiryState.PROCEDURE_INTERMEDIATE_ERROR)
                .expectStateChanged(1)
                .expectVariable(IdentInquiryStateMachineUtils.INQUIRY_VAR)
                .expectVariableWith(
                        Matchers.hasValue(Matchers.allOf(
                                Matchers.hasProperty("id", Matchers.equalTo(inquiryId)),
                                Matchers.hasProperty("attemptCount", Matchers.equalTo(2)))))
                .and()
                .build();
        plan.test();
    }

    @DisplayName("при 3 неудачных попытках уменьшить количество и перевеcти заявку в ERROR")
    @ParameterizedTest
    @ArgumentsSource(DecreaseAttemptsStatesArgumentsProvider.class)
    @SneakyThrows
    void shouldDecreaseAttemptsToZeroAndTransitToError(Message<IdentInquiryStateEvent> event1,
                                                       Message<IdentInquiryStateEvent> event2,
                                                       Message<IdentInquiryStateEvent> event3) {
        UUID inquiryId = UUID.randomUUID();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).attemptCount(3).build();
        DefaultExtendedState extendedState = new DefaultExtendedState(
                Map.of(IdentInquiryStateMachineUtils.INQUIRY_VAR, inquiry));
        stateMachine.getStateMachineAccessor().doWithAllRegions(sm -> sm.resetStateMachine(
                new DefaultStateMachineContext<>(AT_LEAST_ONE_PROCEDURE,
                        null, null, extendedState, null, inquiryId.toString())));
        var plan = StateMachineTestPlanBuilder.<IdentInquiryState, IdentInquiryStateEvent>builder()
                .defaultAwaitTime(2)
                .stateMachine(stateMachine)
                .step()
                .sendEvent(event1)
                .expectState(AT_LEAST_ONE_PROCEDURE)
                .expectStateChanged(1)
                .expectVariable(IdentInquiryStateMachineUtils.INQUIRY_VAR)
                .expectVariableWith(
                        Matchers.hasValue(Matchers.allOf(
                                Matchers.hasProperty("id", Matchers.equalTo(inquiryId)),
                                Matchers.hasProperty("attemptCount", Matchers.equalTo(2)))))
                .and()
                .step()
                .sendEvent(event2)
                .expectState(AT_LEAST_ONE_PROCEDURE)
                .expectStateChanged(1)
                .expectVariable(IdentInquiryStateMachineUtils.INQUIRY_VAR)
                .expectVariableWith(
                        Matchers.hasValue(Matchers.allOf(
                                Matchers.hasProperty("id", Matchers.equalTo(inquiryId)),
                                Matchers.hasProperty("attemptCount", Matchers.equalTo(1)))))
                .and()
                .step()
                .sendEvent(event3)
                .expectState(ERROR)
                .expectStateChanged(1)
                .expectVariable(IdentInquiryStateMachineUtils.INQUIRY_VAR)
                .expectVariableWith(
                        Matchers.hasValue(Matchers.allOf(
                                Matchers.hasProperty("id", Matchers.equalTo(inquiryId)),
                                Matchers.hasProperty("attemptCount", Matchers.equalTo(0)))))
                .and()
                .build();
        plan.test();
        Assertions.assertThat(inquiry)
                .extracting(InquiryEntity::getState, InquiryEntity::getStage)
                .contains(ERROR.getState(), IdentInquiryStage.ERROR.name());
    }
}


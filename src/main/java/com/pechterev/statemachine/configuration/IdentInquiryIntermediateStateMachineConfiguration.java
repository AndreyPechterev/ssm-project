package com.pechterev.statemachine.configuration;

import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.guard.AttemptsGuard;
import com.pechterev.statemachine.listeners.StateSmListener;
import com.pechterev.statemachine.statemachine.action.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.Set;

/**
 * Конфигурация стейт машины для промежуточных состояний заявки
 */
@Slf4j
@Configuration
@EnableStateMachineFactory
public class IdentInquiryIntermediateStateMachineConfiguration
        extends StateMachineConfigurerAdapter<IdentInquiryState, IdentInquiryStateEvent> {

    @Bean
    public AttemptDecreaseAction attemptDecreaseAction() {
        return new AttemptDecreaseAction();
    }

    @Bean
    public ErrorCheckAction terminalStageSetAction() {
        return new ErrorCheckAction();
    }

    @Bean
    public CompletedAction completedAction() {
        return new CompletedAction();
    }

    @Bean
    public DeletedAction deletedAction() {
        return new DeletedAction();
    }

    @Bean
    public ExpiredAction expiredAction() {
        return new ExpiredAction();
    }

    @Bean
    public WriteStatusDetailsAction writeStatusDetailsAction() {
        return new WriteStatusDetailsAction();
    }

    @Bean
    public AttemptsGuard attemptsGuard() {
        return new AttemptsGuard();
    }

    @Bean
    public RejectAction rejectAction() {
        return new RejectAction();
    }

    @Bean
    public InProgressAction inProgressAction() {
        return new InProgressAction();
    }

    /**
     * Общая конфигурация
     *
     * @param config общие конфигурации стейтмашины
     */
    @Override
    public void configure(StateMachineConfigurationConfigurer<IdentInquiryState, IdentInquiryStateEvent> config)
            throws Exception {
        config.withConfiguration()
                .listener(new StateSmListener())
                .autoStartup(false);
    }

    /**
     * Состояния
     *
     * @param states конфигурация состояний
     */
    @Override
    public void configure(StateMachineStateConfigurer<IdentInquiryState, IdentInquiryStateEvent> states) throws Exception {
        states.withStates()
                .initial(IdentInquiryState.ACCEPTED)
                .junction(IdentInquiryState.VERIFYING_ESIA_RESPONSE)
                .junction(IdentInquiryState.VERIFYING_ATTEMPTS)
                .states(Set.of(IdentInquiryState.values()))
                .end(IdentInquiryState.REJECTED)
                .end(IdentInquiryState.COMPLETED)
                .end(IdentInquiryState.ERROR);
//                .end(IdentInquiryState.EXPIRED);
    }

    /**
     * Переходы по состояниям
     *
     * @param transitions конфигурация переходов
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<IdentInquiryState, IdentInquiryStateEvent> transitions) throws Exception {
        configureForValidation(transitions);
        configureForProcess(transitions);
        configureForExpiration(transitions);

    }

    private void configureForValidation(StateMachineTransitionConfigurer<IdentInquiryState, IdentInquiryStateEvent>
                                                transitions) throws Exception {
        transitions.withExternal()
                //смена состояния из принята на внутренние проверки в процессе
                .source(IdentInquiryState.ACCEPTED).event(IdentInquiryStateEvent.INTERNAL_CHECK_STARTED)
                .target(IdentInquiryState.INTERNAL_CHECK_IN_PROGRESS)

                .and()
                .withExternal()
                .source(IdentInquiryState.INTERNAL_CHECK_IN_PROGRESS).event(IdentInquiryStateEvent.CHECK_FAILED)
                .action(writeStatusDetailsAction())
                .action(rejectAction())
                .target(IdentInquiryState.REJECTED)

                .and()
                .withExternal()
                .source(IdentInquiryState.INTERNAL_CHECK_IN_PROGRESS).event(IdentInquiryStateEvent.INTERNAL_CHECK_PASSED)
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.EXTERNAL_CHECK_IN_PROGRESS)

                //сценарий для пропуска проверки на дубликаты
                .and()
                .withExternal()
                .source(IdentInquiryState.INTERNAL_CHECK_IN_PROGRESS)
                .event(IdentInquiryStateEvent.PRE_VALIDATION_CHECKS_PASSED)
                .action(inProgressAction())
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.WAITING_FOR_IDENT)

                .and()
                .withExternal()
                .source(IdentInquiryState.EXTERNAL_CHECK_IN_PROGRESS).event(IdentInquiryStateEvent.CHECK_FAILED)
                .action(writeStatusDetailsAction())
                .action(rejectAction())
                .target(IdentInquiryState.REJECTED)

                //завершение проверок в ЕПК
                .and()
                .withExternal()
                .source(IdentInquiryState.EXTERNAL_CHECK_IN_PROGRESS).event(IdentInquiryStateEvent.EPK_CHECK_PASSED)
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.EXTERNAL_CHECK_IN_PROGRESS)

                //завершение проверок по стоп-листам
                .and()
                .withExternal()
                .source(IdentInquiryState.EXTERNAL_CHECK_IN_PROGRESS).event(IdentInquiryStateEvent.STOPLIST_CHECK_PASSED)
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.EXTERNAL_CHECK_IN_PROGRESS)

                .and()
                .withExternal()
                .source(IdentInquiryState.EXTERNAL_CHECK_IN_PROGRESS).event(IdentInquiryStateEvent.PRE_VALIDATION_CHECKS_PASSED)
                .action(inProgressAction())
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.WAITING_FOR_IDENT);
    }

    private void configureForProcess(StateMachineTransitionConfigurer<IdentInquiryState, IdentInquiryStateEvent>
                                             transitions) throws Exception {
        transitions
                // смена состояния с ожидания на идентификацию на "процедура запущена хотя бы 1 раз"
                .withExternal()
                .source(IdentInquiryState.WAITING_FOR_IDENT)
                .event(IdentInquiryStateEvent.IDENTIFICATION_STARTED)
                .target(IdentInquiryState.AT_LEAST_ONE_PROCEDURE)
                // смена состояния с промежуточной ошибки на старт идентификации
                .and()
                .withExternal()
                .source(IdentInquiryState.PROCEDURE_INTERMEDIATE_ERROR)
                .event(IdentInquiryStateEvent.IDENTIFICATION_STARTED)
                .target(IdentInquiryState.AT_LEAST_ONE_PROCEDURE)

                // разрешение перехода при старте идентификации
                .and()
                .withExternal()
                .source(IdentInquiryState.AT_LEAST_ONE_PROCEDURE)
                .event(IdentInquiryStateEvent.IDENTIFICATION_STARTED)
                .target(IdentInquiryState.AT_LEAST_ONE_PROCEDURE)

                .and()
                .withExternal()
                .source(IdentInquiryState.AT_LEAST_ONE_PROCEDURE).event(IdentInquiryStateEvent.NOT_CONFIRMED_ESIA_PROFILE)
                .action(attemptDecreaseAction())
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.VERIFYING_ESIA_RESPONSE)

                //логика при евенте не совпадении скоупов
                .and()
                .withExternal()
                .source(IdentInquiryState.AT_LEAST_ONE_PROCEDURE).event(IdentInquiryStateEvent.SCOPES_DONT_MATCH)
                .action(attemptDecreaseAction())
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.VERIFYING_ESIA_RESPONSE)

                // промежуточная стадия проверки оставшегося количества попыток
                .and()
                .withJunction()
                .source(IdentInquiryState.VERIFYING_ESIA_RESPONSE)
                .first(IdentInquiryState.AT_LEAST_ONE_PROCEDURE, attemptsGuard())
                .last(IdentInquiryState.ERROR, terminalStageSetAction())

                // смена состояния с "процедура запущена хотя бы 1 раз" на "получен хотя бы 1 комплект документов"
                .and()
                .withExternal()
                .source(IdentInquiryState.AT_LEAST_ONE_PROCEDURE)
                .event(IdentInquiryStateEvent.PROFILE_DATA_RECEIVED)
                .target(IdentInquiryState.AT_LEAST_ONE_DOC_SET)

                // смена состояния с "получен хотя бы 1 комплект документов" на "запущена внутрення проверка после получения профиля"
                .and()
                .withExternal()
                .source(IdentInquiryState.AT_LEAST_ONE_DOC_SET)
                .event(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_STARTED)
                .target(IdentInquiryState.INTERNAL_CHECK_AFTER_PROFILE_RECEIVED)

                // смена состояния с "получен хотя бы 1 комплект документов" на "промежуточная ошибка" для кейса когда профилей нет
                .and()
                .withExternal()
                .source(IdentInquiryState.AT_LEAST_ONE_DOC_SET)
                .event(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_FAILED)
                .target(IdentInquiryState.PROCEDURE_INTERMEDIATE_ERROR)
                // смена состояния с "запущена внутренняя проверка после получения профиля" на "в процессе" после ошибки проверки
                .and()
                .withExternal()
                .source(IdentInquiryState.INTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .event(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_FAILED)
                .action(attemptDecreaseAction())
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.VERIFYING_ATTEMPTS)

                // промежуточная стадия проверки оставшегося количества попыток при ошибках в процессе сверки профилей
                .and()
                .withJunction()
                .source(IdentInquiryState.VERIFYING_ATTEMPTS)
                .first(IdentInquiryState.PROCEDURE_INTERMEDIATE_ERROR, attemptsGuard())
                .last(IdentInquiryState.ERROR, terminalStageSetAction())

                // смена состояния с "в процессе" на "запущены внешние проверки" после завершения внутренних проверки
                .and()
                .withExternal()
                .source(IdentInquiryState.INTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .event(IdentInquiryStateEvent.INTERNAL_PROFILE_CHECKS_FINISHED)
                .target(IdentInquiryState.EXTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                // смена состояния с "запущена внутрення проверка после получения профиля"  на completed
                .and()
                .withExternal()
                .source(IdentInquiryState.INTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .event(IdentInquiryStateEvent.INTERNAL_CHECK_SUCCESS)
                .action(completedAction())
                .target(IdentInquiryState.COMPLETED)
                // смена состояния с "внешние проверки" на "в процессе" после ошибки внутренних проверок
                .and()
                .withExternal()
                .source(IdentInquiryState.EXTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .event(IdentInquiryStateEvent.EXTERNAL_PROFILE_CHECKS_FAILED)
                .action(attemptDecreaseAction())
                .action(writeStatusDetailsAction())
                .target(IdentInquiryState.PROCEDURE_INTERMEDIATE_ERROR)

                // смена состояния с "внешние проверки" на "все проверки профиля завершены"
                .and()
                .withExternal()
                .source(IdentInquiryState.EXTERNAL_CHECK_AFTER_PROFILE_RECEIVED)
                .event(IdentInquiryStateEvent.EXTERNAL_PROFILE_CHECKS_FINISHED)
                .target(IdentInquiryState.PROFILE_CHECKS_FINISHED)
                // смена состояния с "внешние проверки" на expired
                .and()
                .withExternal()
                .source(IdentInquiryState.EXTERNAL_CHECK_IN_PROGRESS)
                .action(writeStatusDetailsAction())
                .action(expiredAction())
                .event(IdentInquiryStateEvent.EXPIRED)
                .target(IdentInquiryState.EXPIRED)
                // смена состояния с completed на deleted
                .and()
                .withExternal()
                .source(IdentInquiryState.COMPLETED)
                .action(deletedAction())
                .action(writeStatusDetailsAction())
                .event(IdentInquiryStateEvent.DELETED)
                .target(IdentInquiryState.DELETED)
                // смена состояния с rejected на deleted
                .and()
                .withExternal()
                .source(IdentInquiryState.REJECTED)
                .action(deletedAction())
                .action(writeStatusDetailsAction())
                .event(IdentInquiryStateEvent.DELETED)
                .target(IdentInquiryState.DELETED)
                // смена состояния с error на deleted
                .and()
                .withExternal()
                .source(IdentInquiryState.ERROR)
                .action(deletedAction())
                .action(writeStatusDetailsAction())
                .event(IdentInquiryStateEvent.DELETED)
                .target(IdentInquiryState.DELETED)
                // смена состояния с expired на deleted
                .and()
                .withExternal()
                .source(IdentInquiryState.EXPIRED)
                .action(deletedAction())
                .action(writeStatusDetailsAction())
                .event(IdentInquiryStateEvent.DELETED)
                .target(IdentInquiryState.DELETED);
    }

    private void configureForExpiration(StateMachineTransitionConfigurer<IdentInquiryState, IdentInquiryStateEvent>
                                                transitions) throws Exception {
        transitions.withExternal()
                .source(IdentInquiryState.AT_LEAST_ONE_DOC_SET).event(IdentInquiryStateEvent.EXPIRED)
                .target(IdentInquiryState.EXPIRED)
                // смена состояния при истечении срока действия заявки
                .and()
                .withExternal()
                .source(IdentInquiryState.AT_LEAST_ONE_PROCEDURE).event(IdentInquiryStateEvent.EXPIRED)
                .target(IdentInquiryState.EXPIRED)
                // смена состояния при истечении срока действия заявки
                .and()
                .withExternal()
                .source(IdentInquiryState.WAITING_FOR_IDENT).event(IdentInquiryStateEvent.EXPIRED)
                .target(IdentInquiryState.EXPIRED);
    }

}

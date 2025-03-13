package com.pechterev.statemachine.listeners;

import com.pechterev.statemachine.events.IdentInquiryState;
import com.pechterev.statemachine.events.IdentInquiryStateEvent;
import com.pechterev.statemachine.exception.WrongStateTransitionException;
import com.pechterev.statemachine.action.SetupStateAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Listener для состояний стейтмашины
 */
@Slf4j
public class StateSmListener implements StateMachineListener<IdentInquiryState, IdentInquiryStateEvent> {

    @Override
    public void stateChanged(State<IdentInquiryState, IdentInquiryStateEvent> state, State<IdentInquiryState,
            IdentInquiryStateEvent> state1) {
        //not implemented
    }

    @Override
    public void stateEntered(State<IdentInquiryState, IdentInquiryStateEvent> state) {
        //not implemented
    }

    @Override
    public void stateExited(State<IdentInquiryState, IdentInquiryStateEvent> state) {
        //not implemented
    }

    @Override
    public void eventNotAccepted(Message<IdentInquiryStateEvent> message) {
        log.warn("Переход не был совершен для события {}", message.getPayload().name());
        throw new WrongStateTransitionException("Ошибка совершения перехода при событии %s"
                .formatted(message.getPayload().name()));
    }

    @Override
    public void transition(Transition<IdentInquiryState, IdentInquiryStateEvent> transition) {
        //not implemented
    }

    @Override
    public void transitionStarted(Transition<IdentInquiryState, IdentInquiryStateEvent> transition) {
        //not implemented
    }

    @Override
    public void transitionEnded(Transition<IdentInquiryState, IdentInquiryStateEvent> transition) {
        //not implemented
    }

    @Override
    public void stateMachineStarted(StateMachine<IdentInquiryState, IdentInquiryStateEvent> stateMachine) {
        //not implemented
    }

    @Override
    public void stateMachineStopped(StateMachine<IdentInquiryState, IdentInquiryStateEvent> stateMachine) {
        //not implemented
    }

    @Override
    public void stateMachineError(StateMachine<IdentInquiryState, IdentInquiryStateEvent> stateMachine, Exception e) {
        //not implemented
    }

    @Override
    public void extendedStateChanged(Object o, Object o1) {
        log.debug("Обновлен extendedState");
    }

    @Override
    public void stateContext(StateContext<IdentInquiryState, IdentInquiryStateEvent> stateContext) {
        if (stateContext.getStage() == StateContext.Stage.TRANSITION_END) {
            SetupStateAction setupStateAction = new SetupStateAction();
            setupStateAction.execute(stateContext);
            log.debug("Изменение состояния заявки при завершении перехода");
        }
    }
}

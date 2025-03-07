package com.pechterev.statemachine.events;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Промежуточное состояние заявки
 */
@Getter
public enum IdentInquiryState {

    ACCEPTED(2000),
    INTERNAL_CHECK_IN_PROGRESS(2100),
    DUPLICATE_CHECK_PASSED(2101),
    EXTERNAL_CHECK_IN_PROGRESS(2200),
    INTERNAL_CHECK_FAILED(2003),
    EXTERNAL_CHECK_EPK_FAILED(2203),
    CHECKED(2290),
    WAITING_FOR_IDENT(4000),
    AT_LEAST_ONE_PROCEDURE(4200),
    VERIFYING_ESIA_RESPONSE(4203),
    PROCEDURE_INTERMEDIATE_ERROR(4202),
    VERIFYING_ATTEMPTS(4203),
    AT_LEAST_ONE_DOC_SET(4401),
    NOT_FULL_DATA(4501),
    INTERNAL_CHECK_AFTER_PROFILE_RECEIVED(4601),
    EXTERNAL_CHECK_AFTER_PROFILE_RECEIVED(4801),
    PROFILE_CHECKS_FINISHED(4990),
    COMPLETED(6000),
    REJECTED(7000),
    ERROR(8000),
    EXPIRED(9000),
    DELETED(9800);

    private static final Map<Integer, IdentInquiryState> BY_STATE = new HashMap<>();

    static {
        for (IdentInquiryState identState : IdentInquiryState.values()) {
            BY_STATE.put(identState.state, identState);
        }
    }

    private final Integer state;

    IdentInquiryState(Integer state) {
        this.state = state;
    }

    public static IdentInquiryState valueOfState(Integer state) {
        return BY_STATE.get(state);
    }
}


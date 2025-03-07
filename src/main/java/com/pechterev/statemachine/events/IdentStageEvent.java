package com.pechterev.statemachine.events;

/**
 * Ивенты для перехода заявки по стадиям
 */
public enum IdentStageEvent {
    INQUIRY_CHECKED,
    INQUIRY_CHECKS_FAILED,
    IDENTIFICATION_ERROR,
    EXPIRED,
    IDENTIFICATION_PERFORMED
}


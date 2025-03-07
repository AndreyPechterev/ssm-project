package com.pechterev.statemachine.events;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Статусы заявки на идентификацию
 */
public enum IdentInquiryStage {

    @JsonProperty("draft")
    DRAFT("заявка принята"),

    @JsonProperty("in_progress")
    IN_PROGRESS("заявка в работе"),

    @JsonProperty("reject")
    REJECT("заявка отклонена"),

    @JsonProperty("completed")
    COMPLETED("заявка завершена с положительным статусом"),

    @JsonProperty("error")
    ERROR("ошибка выполнения"),

    @JsonProperty("expired")
    EXPIRED("срок заявки истек"),

    @JsonProperty("deleted")
    DELETED("данные (ПДн) по заявке удалены");

    private final String status;

    IdentInquiryStage(String status) {
        this.status = status;
    }

}


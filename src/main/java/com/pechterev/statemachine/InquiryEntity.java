package com.pechterev.statemachine;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InquiryEntity {

    UUID id;
    String stage;

    Integer state;

    Integer attemptCount;

    String statusDetails;
}

package com.brysonrhodes.api.smshandler.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsMessage {

    private String to;
    private String from;
    private String message;
}

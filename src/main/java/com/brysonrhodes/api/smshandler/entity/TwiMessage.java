package com.brysonrhodes.api.smshandler.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwiMessage {
    public String ToCountry;
    public String ToState;
    public String SmsMessageSid;
    public String NumMedia;
    public String ToCity;
    public String FromZip;
    public String SmsSid;
    public String FromState;
    public String SmsStatus;
    public String FromCity;
    public String Body;
    public String FromCountry;
    public String To;
    public String MessagingServiceSid;
    public String NumSegments;
    public String ReferralNumMedia;
    public String MessageSid;
    public String AccountSid;
    public String From;
    public String ApiVersion;
}

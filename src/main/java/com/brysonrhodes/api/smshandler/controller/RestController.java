package com.brysonrhodes.api.smshandler.controller;

import com.brysonrhodes.api.smshandler.entity.SmsMessage;
import com.brysonrhodes.api.smshandler.entity.TwiMessage;
import com.brysonrhodes.api.smshandler.service.TwilioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    TwilioService twilioService;

    @Autowired
    ObjectMapper mapper;

    @RequestMapping( value = "/send")
    public com.twilio.rest.api.v2010.account.Message sendMessage(@RequestBody SmsMessage text){
        log.info("Sending Message From: " + text.getFrom().substring(text.getFrom().length() - 4));
        return twilioService.sendMessage(text.getTo(), text.getFrom(), text.getMessage());
    }

    @RequestMapping( value = "/print")
    public void print() {
        twilioService.print();
    }

    @RequestMapping( value = "/receive", consumes = "application/x-www-form-urlencoded")
    public void receiveMessage(TwiMessage twiMessage) {
        log.info("Message is: {}", twiMessage.toString());
        try {
            String from = twiMessage.From.substring(twiMessage.From.length() - 10);
            twilioService.receiveMessage(from, twiMessage.getBody());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

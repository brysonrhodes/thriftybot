package com.brysonrhodes.api.smshandler.service;

import com.brysonrhodes.api.smshandler.constants.Commands;
import com.brysonrhodes.api.smshandler.entity.User;
import com.brysonrhodes.api.smshandler.repository.UserRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TwilioService {

    @Value("${twilio.sid}")
    private String sid;

    @Value("${twilio.token}")
    private String token;

    @Value("${phone.admin}")
    private String adminPhone;

    @Value("${phone.developer}")
    private String developerPhone;

    @Value("${phone.service}")
    private String servicePhone;

    @Autowired
    private UserRepository userRepository;

    private final Commands commands = new Commands();

    public Message sendMessage(String to, String from, String message) {
        log.info("Send Message: {}", message);
        Twilio.init(sid, token);
        return Message.creator(new PhoneNumber("1" + to), new PhoneNumber("1" + from), message).create();
    }

    public void print() {
        log.info(userRepository.findAll().toString());
    }

    public void receiveMessage(String from, String message) {
        log.info("Message From: {} Body: {}", from, message);
        if(!from.equals(adminPhone)) {
            if(message.contains("@") || message.contains("#")) {
                sendMessage(from, servicePhone, "'@' and '#' are reserved commands." +
                        " You cannot reply to other users");
            } else if (isCommand(message)) {
                parseCommand(from, message);
            } else {
                sendMessage(developerPhone, servicePhone, message);
            }
        }
    }

    private boolean isCommand(String message) {
        return message.toLowerCase().contains("thrifty") || message.contains("@") || message.contains("#");
    }

    private void parseCommand(String from, String message) {
        try {
            String parsedCommand = commands.commandList.stream().filter(command -> message.toLowerCase().contains(command)).findAny().get();

            String response = null;
            switch (parsedCommand) {
                case "dinner" -> log.info("dinner");
                case "dinner set" -> log.info("dinner set");
                case "book" -> log.info("book");
                case "book set" -> log.info("book set");
                case "subscribe" -> log.info("subscribe");
                case "unsubscribe" -> log.info("unsubscribe");
                case "groups" -> response = "This feature hasn't been enabled yet."; //TODO: Parse Group list for Text Messages
                case "group create" -> log.info("group create");
                case "group remove" -> log.info("group remove");
                case "users" -> response = "This feature hasn't been enabled yet."; //TODO: Parse User list for Text Messages
                case "user add" -> response = addUser(message);
                case "user remove" -> response = removeUser(message);
                case "suggest" -> log.info("suggest");
                default -> {
                }
            }

            sendMessage(developerPhone, servicePhone, response);
        } catch (Exception e) {
            log.info("Error parsing message " + e.getMessage());
            sendMessage(from, servicePhone, "The command that you entered isn't recognized. If you need help, please reply HELP.");
        }
    }

    /** Adds a user to the database if the user doesn't already exist.
     *
     * @param message a command passed in from the user.
     * @return        a response about user status. (User added or already present)
     */
    private String addUser(String message) {
        List<String> tempList = getRegexPattern(message);

        try {
            if(tempList.size() == 6) {
                User newUser = User.builder()
                        .phone(tempList.get(3))
                        .firstName(tempList.get(4))
                        .lastName(tempList.get(5))
                        .build();

                if(userRepository.findByPhone(tempList.get(3)) != null ) return String.format("User with number %s has already been added", tempList.get(3));
                userRepository.save(newUser);
                log.info("New User: \n First Name: {} \n Last Name: {} \n Phone: {}", tempList.get(3), tempList.get(4), tempList.get(5));

                return String.format("User Added %s, %s", tempList.get(5), tempList.get(4));
            } else {
                throw new IllegalArgumentException("Too many or few parameters");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * Removes a user from the database if they exist.
     *
     * @param message a command passed in from the user.
     * @return        a response about user status. (Removed or User doesn't exist)
     */
    private String removeUser(String message) {
        List<String> tempList = getRegexPattern(message);
        log.info("Phone {}", tempList.get(3));
        try {
            if(tempList.size() == 4) {
                User user = userRepository.findByPhone(tempList.get(3));

                if(user == null) return String.format("User with %s doesn't exist", tempList.get(3));
                userRepository.delete(user);
                log.info("Removing User: {} {} with number {}", user.firstName, user.lastName, user.phone);

                return String.format("User %s has been removed", tempList.get(3));
            } else {
                throw new IllegalArgumentException("Too many or few parameters");
            }
        } catch(Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private List<String> getRegexPattern(String message) {
        Pattern patter = Pattern.compile("(\\b\\w+)");
        Matcher matcher = patter.matcher(message);

        List<String> tempList = new ArrayList<>();
        while(matcher.find()) { tempList.add(matcher.group(0)); }

        return tempList;
    }
}

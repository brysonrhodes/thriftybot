package com.brysonrhodes.api.smshandler.service;

import com.brysonrhodes.api.smshandler.constants.Commands;
import com.brysonrhodes.api.smshandler.entity.Config;
import com.brysonrhodes.api.smshandler.entity.Group;
import com.brysonrhodes.api.smshandler.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
    private FirestoreService firestoreService;

    @Autowired
    private ObjectMapper mapper;

    private final Commands commands = new Commands();

    public Message sendMessage(String to, String from, String message) {
        log.info("Send Message: {}", message);
        Twilio.init(sid, token);
        return Message.creator(new PhoneNumber("1" + to), new PhoneNumber("1" + from), message).create();
    }

    private Config getConfig() throws ExecutionException, InterruptedException {
        return (Config) firestoreService.getObject("configs", "thrifty", new Config());
    }

    private void sendMessageWithUsername(String to, String from, String message) {
        try {
            Config config = getConfig();
            config.usernames.forEach((id, phone) -> {
                if(phone.equals(from)) {
                    sendMessage(to, servicePhone, String.format("@%s %s", id, message));
                }
            });
        } catch (Exception e) {
            log.info("Unable to send message with username {}", e.getMessage());
            sendMessage(from, servicePhone, "Sorry, we were unable to send your message");
        }
    }

    public void receiveMessage(String from, String message) {
        log.info("Message From: {} Body: {}", from, message);
        if (isCommand(message)) {
            parseCommand(from, message);
        } else {
            sendMessageWithUsername(developerPhone, from, message);
        }
    }

    private boolean isCommand(String message) {
        return message.toLowerCase().contains("thrifty") || message.contains("@") || message.contains("#");
    }

    private void parseCommand(String from, String message) {
        try {
            String parsedCommand = commands.commandList.stream().filter(command -> message.toLowerCase().contains(command)).findAny().get();

            String response = null;
            if(from.equals(adminPhone) || from.equals(developerPhone)) {
                switch (parsedCommand) {
                    case "@" -> response = sendIndividualText(message);
                    case "#" -> response = sendGroupText(message);
                    case "dinner set" -> response = setDinner(message);
                    case "book set" -> response = setBook(message);
                    case "group add" -> response = addGroup(message);
                    case "group remove" -> response = removeGroup(message);
                    case "user add" -> response = addUser(message);
                    case "user remove" -> response = removeUser(message);
                    default -> {
                    }
                }
            }

            switch (parsedCommand) {
                case "dinner" -> response = getDinner();
                case "book" -> response = getBook();
                case "subscribe" -> response = subscribe(from, message);
                case "unsubscribe" -> response = unsubscribe(from, message);
                case "suggest" -> sendMessage(developerPhone, from, message);
                default -> {
                }
            }

            if(response != null) {
                sendMessage(from, servicePhone, response);
            }
        } catch (Exception e) {
            log.info("Error parsing message " + e.getMessage());
            sendMessage(from, servicePhone, "The command that you entered isn't recognized. If you need help, please reply HELP.");
        }
    }

    /**
     * Send a message to a group in the config table.
     * @param message The message the user sends.
     * @return Error if the username is not found.
     */
    private String sendGroupText(String message) {
        Pattern groupPattern = Pattern.compile("#.[a-z0-9]*\s");
        Pattern messagePattern = Pattern.compile("\s.*");
        Matcher matcher = groupPattern.matcher(message);
        try {
            String group = "";
            String textContent = "";

            if(matcher.find()) group = matcher.group(0);

            matcher = messagePattern.matcher(message);
            if(matcher.find()) textContent = matcher.group(0);

            if(!group.isEmpty()) {
                Group groupObject = (Group) firestoreService.getObject("groups", group.substring(group.length() - (group.length() - 1), group.length() -1 ), new Group());

                String finalTextContent = textContent;
                assert groupObject.users != null;
                groupObject.users.forEach(phone -> {
                    log.info("Sending message: {} to {}", finalTextContent, phone);
                    sendMessage(phone, servicePhone, message.substring(message.length() - (message.length() - 1)));
                });

                return null;
            } else {
                return "Group not found, please try again";
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return "Unable to send Individual message, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
        }
    }

    /**
     * Send a message to an individual who has a username in the config table.
     * @param message The message the user sends.
     * @return Error if the username is not found.
     */
    private String sendIndividualText(String message) {
        Pattern toPattern = Pattern.compile("@.[a-z0-9]*\s");
        Pattern messagePattern = Pattern.compile("\s.*");
        Matcher matcher = toPattern.matcher(message);
        try {
            String to = "";
            String textContent = "";

            if(matcher.find()) to = matcher.group(0);

            matcher = messagePattern.matcher(message);
            if(matcher.find()) textContent = matcher.group(0);

            if(!to.isEmpty()) {
                Config config = getConfig();
                String phone = config.usernames.get(to.substring(to.length() - (to.length() - 1), to.length() - 1)); //Remove the '@' from the beginning of the line and space from the end

                log.info("Sending message to {} at {} with value {}", to, phone, textContent);
                sendMessage(phone, servicePhone, textContent);
                return null;
            } else {
                return "Couldn't find user to send message";
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return "Unable to send Individual message, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
        }

    }

    /**
     * Gets the dinner configuration. The dinner configuration is set to where everyone will eat after the meeting.
     * @return the restaurant
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private String getDinner() throws ExecutionException, InterruptedException {
        Config config = getConfig();
        return String.format("Dinner is at %s ", config.dinner);
    }

    /**
     * Sets the dinner configuration and removes the previous config.
     * @param message the string that the user passed in
     * @return the dinner configuration
     */
    private String setDinner(String message) {
        List<String> tempList = getRegexPattern(message);
        try {
            String dinner = tempList.get(3);

            int counter = 4;
            while(counter < tempList.size()) {
                dinner = dinner.concat(" " + tempList.get(counter));
                counter++;
            }


            Config config = getConfig();
            config.setDinner(dinner);

            firestoreService.addObject("configs", config, "thrifty");
            return String.format("Dinner set to %s", dinner);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "Unable to set dinner, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
        }
    }

    /**
     * Gets the book configuration. The Book configuration is the current weeks Bible Book to go over.
     * @return the book
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private String getBook() throws ExecutionException, InterruptedException {
        Config config = getConfig();
        return String.format("We are reading %s this week", config.book);
    }

    /**
     * Sets the book configuration and removes the previous config.
     * @param message the string that the user passed in
     * @return the book configuration
     */
    private String setBook(String message) {
        List<String> tempList = getRegexPattern(message);
        try {
            String book = tempList.get(3);

            int counter = 4;
            while(counter < tempList.size()) {
                book = book.concat(" " + tempList.get(counter));
                counter++;
            }

            Config config = getConfig();
            config.setBook(book);

            firestoreService.addObject("configs", config, "thrifty");
            return String.format("Book set to %s", book);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "Unable to set book, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
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
                String phone = tempList.get(3);
                String firstName = tempList.get(4);
                String lastName = tempList.get(5);

                User newUser = User.builder()
                        .phone(phone)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();

                Config config = getConfig();

                if(!config.usernames.containsValue(phone)) {
                    log.info("Writing Username for {}", newUser);
                    String username = firstName;

                    if(config.usernames.containsKey(username)) {
                        username = firstName + phone.substring(phone.length() - 4);
                    }

                    config.usernames.put(username.toLowerCase(), phone);
                    firestoreService.addObject("configs", config, "thrifty");
                }

                if(firestoreService.getObject("users", newUser.phone, new User()) != null ) return String.format("User with number %s has already been added", newUser.phone);

                firestoreService.addObject("users", newUser, newUser.phone);
                log.info("New User: \n First Name: {} \n Last Name: {} \n Phone: {}", newUser.firstName, newUser.lastName, newUser.phone);
                return String.format("%s %s has been added", newUser.firstName, newUser.lastName);
            } else {
                throw new IllegalArgumentException("Too many or few parameters");
            }
        } catch (Exception e) {
            log.error("Error adding user {}", e.getMessage());
            return "Unable to Add User, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
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
        try {
            if(tempList.size() == 4) {
                User user = (User) firestoreService.getObject("users", tempList.get(3), new User());

                if(user == null) return String.format("User with %s doesn't exist", tempList.get(3));
                log.info("Removing User: {} {} with number {}", user.firstName, user.lastName, user.phone);
                firestoreService.removeObject("users", user.phone);
                return String.format("User %s has been removed", tempList.get(3));
            } else {
                throw new IllegalArgumentException("Too many or few parameters");
            }
        } catch(Exception e) {
            log.error("Error removing user {}", e.getMessage());
            return "Unable to Remove User, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
        }
    }

    /**
     * Adds a group to the database if it doesn't exist.
     *
     * @param message a command passed in from the user.
     * @return        a response about user status. (Removed or User doesn't exist)
     */
    private String addGroup(String message) {
        List<String> tempList = getRegexPattern(message);
        try {
            if(tempList.size() == 4) {
                Group group = Group.builder().name(tempList.get(3)).users(null).build();

                if(firestoreService.getObject("groups", group.name, new Group()) != null) return String.format("A group with the name %s already exists", group.name);

                firestoreService.addObject("groups", group, group.name);
                log.info("Adding group {} to the database", group.name);
                return String.format("Group %s has been added", group.name);
            } else {
                throw new IllegalArgumentException("Too many or too few parameters");
            }
        } catch (Exception e) {
            log.error("Error adding group {}", e.getMessage());
            return "Unable to Add group, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
        }
    }

    /**
     * Removes a group from the database if it exists.
     *
     * @param message a command passed in from the user.
     * @return        a response about user status. (Group added or already present)
     */
    private String removeGroup(String message) {
        List<String> tempList = getRegexPattern(message);
        try {
            if(tempList.size() == 4) {
                Group group = Group.builder().name(tempList.get(3)).users(null).build();

                if(firestoreService.getObject("groups", group.name, new Group()) == null) return String.format("A group with the name %s doesn't exist", group.name);

                firestoreService.removeObject("groups", group.name);
                log.info("Removing group {} to the database", group.name);
                return String.format("Group %s has been removed", group.name);
            } else {
                throw new IllegalArgumentException("Too many or too few parameters");
            }
        } catch (Exception e) {
            log.error("Error removing group {}", e.getMessage());
            return "Unable to Remove group, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
        }
    }

    /**
     * Subscribes a user to a group from the database if they both exist.
        Creates a user if the user doesn't exist and subscribes them to the group.
     * @param from the phone number of the user
     * @param message a command passed in from the user.
     * @return        a response about the status. (User added to group or error message)
     */
    private String subscribe(String from, String message) {
        List<String> tempList = getRegexPattern(message);
        try {
            if(tempList.size() == 3) {
                User user = (User) firestoreService.getObject("users", from, new User());
                Group group = (Group) firestoreService.getObject("groups", tempList.get(2), new Group());

                if(user == null) return "You are not in the system, please subscribe with your name.\n\n thrifty subscribe {group} {first name} {last name}";
                if(group == null) return String.format("Group %s doesn't exist", tempList.get(2));
                if(group.users != null && group.users.contains(user.phone)) return String.format("You are already subscribed to %s", group.name);
                if(group.users == null) group.users = new ArrayList<>();
                group.users.add(user.phone);
                firestoreService.addObject("groups", group, group.name);
                return String.format("You have been added to group %s", group.name);

            } else if(tempList.size() == 5) {
                User user = User.builder()
                        .phone(from)
                        .firstName(tempList.get(3))
                        .lastName(tempList.get(4)).build();
                Group group = (Group) firestoreService.getObject("groups", tempList.get(2), new Group());

                log.info("Adding user {} to users", user.toString());
                firestoreService.addObject("users", user, from);

                if(group == null) return String.format("Group %s doesn't exist", tempList.get(2));
                if(group.users == null) group.users = new ArrayList<>();
                group.users.add(user.phone);
                firestoreService.addObject("groups", group, group.name);
                return String.format("You have been added to group %s", group.name);
            } else {
                throw new IllegalArgumentException("Too many or too few parameters");
            }
        } catch (Exception e) {
            log.error("Error Subscribing {}", e.getMessage());
            return "Unable to Subscribe, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
        }
    }

    /**
     * Unsubscribes a user to a group from the database if they both exist.
     * @param from the phone number of the user
     * @param message a command passed in from the user.
     * @return        a response about the status. (User added to group or error message)
     */
    private String unsubscribe(String from, String message) {
        List<String> tempList = getRegexPattern(message);
        try {
            if(tempList.size() == 3) {
                User user = (User) firestoreService.getObject("users", from, new User());
                Group group = (Group) firestoreService.getObject("groups", tempList.get(2), new Group());

                if(user == null) return String.format("You have been Unsubscribed from %s", group.name);
                if(group == null) return String.format("Group %s doesn't exist", tempList.get(2));
                if(group.users != null) {
                    group.users.remove(user.phone);
                }
                firestoreService.addObject("groups", group, group.name);
                return String.format("You have been Unsubscribed from %s", group.name);

            } else {
                throw new IllegalArgumentException("Too many or too few parameters");
            }
        } catch (Exception e) {
            log.error("Error Subscribing {}", e.getMessage());
            return "Unable to Unsubscribe, please try again later or user the command: \nthrifty developer help {message} \n for assistance";
        }
    }

    private List<String> getRegexPattern(String message) {
        Pattern pattern = Pattern.compile("(\\b\\w+)");
        Matcher matcher = pattern.matcher(message);

        List<String> tempList = new ArrayList<>();
        while(matcher.find()) { tempList.add(matcher.group(0)); }

        return tempList;
    }
}

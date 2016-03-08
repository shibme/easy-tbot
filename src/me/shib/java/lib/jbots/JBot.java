package me.shib.java.lib.jbots;

import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.*;

import java.io.IOException;
import java.util.logging.Logger;

public abstract class JBot {

    private static Logger logger = Logger.getLogger(JBot.class.getName());

    protected TelegramBot bot;
    protected JBotConfig config;

    public JBot(JBotConfig config) {
        this.bot = BotProvider.getInstance(config);
        this.config = config;
    }

    public static String getAnalyticsRedirectedURL(TelegramBot bot, long user_id, String url) {
        try {
            AnalyticsBot analyticsBot = (AnalyticsBot) bot;
            String analyticsURL = analyticsBot.getAnalyticsRedirectedURL(user_id, url);
            if (analyticsURL != null) {
                return analyticsURL;
            }
        } catch (Exception e) {
            logger.throwing(JBot.class.getName(), "getAnalyticsRedirectedURL", e);
        }
        return url;
    }

    private static boolean isValidText(String text) {
        return text != null && text.matches("^[A-Za-z0-9]+$");
    }

    private static String getProperName(String firstName, String lastName, String username) {
        StringBuilder nameBuilder = new StringBuilder();
        if (isValidText(firstName)) {
            nameBuilder.append(firstName);
        }
        if (isValidText(lastName)) {
            if (!nameBuilder.toString().isEmpty()) {
                nameBuilder.append(" ");
            }
            nameBuilder.append(lastName);
        }
        if (nameBuilder.toString().isEmpty() && isValidText(username)) {
            nameBuilder.append(username);
        }
        return nameBuilder.toString();
    }

    public static String getProperName(Chat chat) {
        if (chat != null) {
            return getProperName(chat.getFirst_name(), chat.getLast_name(), chat.getUsername());
        }
        return "";
    }

    public static String getProperName(User user) {
        if (user != null) {
            return getProperName(user.getFirst_name(), user.getLast_name(), user.getUsername());
        }
        return "";
    }

    public Message forwardToAdmins(Message message) {
        try {
            long[] admins = config.getAdminIdList();
            if ((admins != null) && (admins.length > 0)) {
                for (long admin : admins) {
                    try {
                        bot.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()),
                                message.getMessage_id());
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "forwardToAdmins", e);
                    }
                }
                return bot.sendMessage(new ChatId(message.getChat().getId()),
                        "Your message has been forwarded to the *admin*. It might take quite sometime to get back to you. Please be patient.",
                        false, ParseMode.Markdown, false, message.getMessage_id());
            }
            return bot.sendMessage(new ChatId(message.getChat().getId()),
                    "The support team is unavailable. Please try later.", false, null, false, message.getMessage_id());
        } catch (IOException e) {
            logger.throwing(this.getClass().getName(), "forwardToAdmins", e);
            return null;
        }
    }

    public abstract Message onMessageFromAdmin(Message message);

    public abstract Message onCommand(Message message);

    public abstract Message onReceivingMessage(Message message);

    public abstract boolean onInlineQuery(InlineQuery query);

    public abstract boolean onChosenInlineResult(ChosenInlineResult chosenInlineResult);

    public abstract Message sendStatusMessage(long chatId);

}

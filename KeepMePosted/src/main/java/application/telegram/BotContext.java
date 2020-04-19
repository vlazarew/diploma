//package application.telegram;
//
//import application.data.model.User;
//import lombok.Data;
//
//@Data
//public class BotContext {
//
//    private TelegramBot telegramBot;
//    private User user;
//    private String input;
//
//    public BotContext(TelegramBot bot, User user, String input) {
//        this.telegramBot = bot;
//        this.user = user;
//        this.input = input;
//    }
//
//    public static BotContext of(TelegramBot bot, User user, String input) {
//        return new BotContext(bot, user, input);
//    }
//}

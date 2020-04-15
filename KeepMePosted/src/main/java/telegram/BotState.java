package telegram;

import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.Utils;

//@Data
public enum BotState {

    Start {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Приветствую!");
        }

        @Override
        public BotState nextState() {
            return EnterEmail;
        }
    }, EnterEmail {
        private BotState next;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Введите ваш e-mail");
        }

        @Override
        public void handleInput(BotContext context) {
            String email = context.getInput();

            if (Utils.isValidEmailAddress(email)) {
                context.getUser().setEmail(email);
                next = Approved;
            } else {
                sendMessage(context, "Введен некорректный адрес электронной почты");
                next = EnterEmail;
            }
        }

        @Override
        public BotState nextState() {
            return Approved;
        }
    }, Approved(false) {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Спасибо за тесты!");
        }

        @Override
        public BotState nextState() {
            return Approved;
        }
    };

    private static BotState[] states;
    private boolean inputNeeded;

    BotState() {
        this.inputNeeded = true;
    }

    BotState(boolean inputNeeded) {
        this.inputNeeded = inputNeeded;
    }

    public static BotState getInitialState() {
        return byId(0);
    }

    public static BotState byId(int id) {
        if (states == null) {
            states = BotState.values();
        }

        return states[id];
    }

    public void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage()
                .setChatId(context.getUser().getChatId())
                .setText(text);

        try {
            context.getTelegramBot().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public boolean isInputNeeded() {
        return inputNeeded;
    }

    public void handleInput(BotContext context) {

    }

    public abstract void enter(BotContext context);

    public abstract BotState nextState();

}

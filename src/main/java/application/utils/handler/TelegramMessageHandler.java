package application.utils.handler;

import application.data.model.telegram.TelegramUpdate;

public interface TelegramMessageHandler {
    void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation);
}

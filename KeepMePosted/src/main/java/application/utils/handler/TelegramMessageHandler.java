package application.utils.handler;

import application.data.model.TelegramUpdate;

public interface TelegramMessageHandler {
    void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation);
}

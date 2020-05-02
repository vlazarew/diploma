package application.utils.service;

import org.telegram.telegrambots.meta.api.objects.Chat;

public interface TelegramChatService {

    Chat save(Chat dto);

    Chat get(Long id);

}

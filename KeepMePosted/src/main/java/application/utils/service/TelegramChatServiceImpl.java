package application.utils.service;

import application.data.repository.telegram.TelegramChatRepository;
import application.utils.mapper.TelegramChatMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TelegramChatServiceImpl implements TelegramChatService {

    TelegramChatRepository telegramChatRepository;
    TelegramChatMapper telegramChatMapper;

    @Autowired
    public TelegramChatServiceImpl(TelegramChatRepository telegramChatRepository, TelegramChatMapper telegramChatMapper) {
        this.telegramChatRepository = telegramChatRepository;
        this.telegramChatMapper = telegramChatMapper;
    }

    @Override
    public Chat save(Chat dto) {
        return telegramChatMapper.toDTO(telegramChatRepository.save(telegramChatMapper.toEntity(dto)));
    }

    @Override
    public Chat get(Long id) {
        return telegramChatMapper.toDTO(telegramChatRepository.findById(id).get());
    }
}

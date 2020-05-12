package application.service;

import application.data.repository.telegram.*;
import application.utils.mapper.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TelegramUpdateServiceTest {

    @Autowired
    TelegramChatRepository telegramChatRepository;
    @Autowired
    TelegramMessageRepository telegramMessageRepository;
    @Autowired
    TelegramUpdateRepository telegramUpdateRepository;
    @Autowired
    TelegramUserRepository userRepository;
    @Autowired
    TelegramContactRepository telegramContactRepository;
    @Autowired
    TelegramLocationRepository telegramLocationRepository;

    @Autowired
    TelegramUserMapper telegramUserMapper;
    @Autowired
    TelegramChatMapper telegramChatMapper;
    @Autowired
    TelegramContactMapper telegramContactMapper;
    @Autowired
    TelegramLocationMapper telegramLocationMapper;
    @Autowired
    TelegramMessageMapper telegramMessageMapper;
    @Autowired
    TelegramUpdateMapper telegramUpdateMapper;

    @Test
    public void save() {
    }
}
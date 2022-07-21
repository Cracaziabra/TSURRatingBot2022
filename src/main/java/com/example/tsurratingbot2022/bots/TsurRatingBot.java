package com.example.tsurratingbot2022.bots;

import com.example.tsurratingbot2022.*;
import com.example.tsurratingbot2022.factories.ExcelFileCreator;
import com.example.tsurratingbot2022.factories.KeyBoardFactory;
import com.example.tsurratingbot2022.repositories.CompanyRepo;
import com.example.tsurratingbot2022.repositories.CriteriaRepo;
import com.example.tsurratingbot2022.repositories.FormRepo;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;

@Component
public class TsurRatingBot extends AbilityBot {

    private static final String botUsername = System.getenv("BOT_NAME"); //имя и токен бота, записаны в environment variables (переменные среды)
    private static final String botToken = System.getenv("BOT_TOKEN");

    private final FormRepo formRepo;
    private final CompanyRepo companyRepo;
    private final KeyBoardFactory boardFactory;
    private final CriteriaRepo critRepo;
    private final ExcelFileCreator fileCreator;
    private final Map<Long, RatingForm> forms = db.getMap("FORMS"); //chatId to RatingForm
    private final Map<Long, Long> userChatMap = db.getMap("CHAT_IDS");

    private final String starting = "Подождите когда придет запрос на оценку взаимодействия";
    private final String introduce = "Пожалуйста, оцените взаимодействие с РОИВ и ОМСУ";
    private final String chooseOMSUorROIV = "Выберите ОМСУ или РОИВ";
    private final String criterion = "Пожалуйста выберите показатель";
    private final String description = "Пожалуйта, добавьте описание к поставленной отметке";
    private final String scoreCompany = "Оцените предприятие по выбранному критерию (от -1 до 2)";
    private final String saveSuccess = "Ваш отзыв успешно сохранен!";
    private final String oneMoreForm = "Вы можете оставить ещё один, нажав на кнопку \"Ещё\"";
    private final String pleaseEnterNumber = "Оценкой считается целое число (отрицательно в том числе). Попробуйте ввести ещё раз";
    private final String pleaseFillTheForm = "Пожалуйста, нажмите на кнопку \"Ещё\"";
    private final String pleaseAnswer = "Пожалуйста, ответьте на присланое сообщение (не нажимая крестик над сообщением)";
    private final String chooseStartDate = "Выберите дату начала отчета (в формате день.месяц.год, например 26.07.22)";
    private final String chooseEndDate = "Выберите дату окончания отчета (в формате день.месяц.год, например 26.07.22)";
    private final String statisticComplete = "Сбор статистики по взаимодействию был успешно завершен!";
    private final String wrongDateFormat = "Дата введена не в том формате";
    private final String scoreBorders = "Оценка должна быть от -1 до 2 включительно";
    private final String noTime = "На данный момент оценить взаимодействие нельзя. Когда возможность вновь станет доступной, вам придет сообщение в этот чат";

    private static boolean isActive = false;
    private Predicate<Update> checkActive = update -> isActive;

    public TsurRatingBot(FormRepo formRepo, KeyBoardFactory boardFactory, CompanyRepo companyRepo, CriteriaRepo critRepo, ExcelFileCreator fileCreator) {
        super(botToken, botUsername);
        this.formRepo = formRepo;
        this.boardFactory = boardFactory;
        this.companyRepo = companyRepo;
        this.critRepo = critRepo;
        this.fileCreator = fileCreator;
    }

    @Override
    public long creatorId() {
        return 12345;
    }

    public Ability startAbility() {
        return Ability.builder()
                .name("start")
                .info("Добро пожаловать!")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    forms.put(ctx.chatId(), new RatingForm());
                    userChatMap.put(ctx.user().getId(), ctx.chatId());
                    silent.send(starting, ctx.chatId());
                    db.commit();
                })
                .build();
    }

    @PostConstruct
    public void startBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (
                TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Ability openRating() {
        return Ability
                .builder()
                .name("open")
                .info("opens rating")
                .locality(Locality.ALL)
                .privacy(Privacy.ADMIN)
                .action(ctx -> {
                    refreshAllForms();
                    isActive = true;
                    for (Long chatId : userChatMap.values()) {
                        silent.send(introduce, chatId);
                        sendKeyboardMessage(chatId, chooseOMSUorROIV, boardFactory.companyTypeKeyboard());
                    }
//                    sendKeyboardMessage(ctx.chatId(), chooseOMSUorROIV, boardFactory.companyTypeKeyboard());
                })
                .build();
    }

    public Ability endRating() {
        return Ability
                .builder()
                .name("close")
                .info("closes rating")
                .locality(Locality.ALL)
                .privacy(Privacy.ADMIN)
                .action(messageContext -> {
                    refreshAllForms();
                    isActive = false;
                    for (Long chatId : userChatMap.values()) {
                        silent.send(statisticComplete, chatId);
                    }
//                    silent.send(statisticComplete, messageContext.chatId());
                })
                .build();
    }

    public Ability getExcel() {
        return Ability
                .builder()
                .name("excel")
                .locality(Locality.ALL)
                .privacy(Privacy.ADMIN)
                .action(messageContext -> silent.forceReply(chooseStartDate, messageContext.chatId()))
                .build();
    }

    public Reply startDate() {
        return Reply.of((baseAbilityBot, update) -> {
            try {
                String message = update.getMessage().getText().strip() + " 0:0:0:0";
                Calendar start = new GregorianCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy H:m:s:S");
                start.setTime(sdf.parse(message));
                db.<Long, Calendar>getMap("START_DATES").put(AbilityUtils.getChatId(update), start);
                db.commit();
                silent.forceReply(chooseEndDate, AbilityUtils.getChatId(update));
            } catch (Exception e) {
                silent.send(wrongDateFormat, AbilityUtils.getChatId(update));
                silent.forceReply(chooseStartDate, AbilityUtils.getChatId(update));
            }
        }, Flag.REPLY, AbilityUtils.isReplyTo(chooseStartDate));
    }

    public Reply endDate() {
        return Reply.of((baseAbilityBot, update) -> {
            try {
                String message = update.getMessage().getText().strip() + " 23:59:59:9999";
                Calendar end = new GregorianCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss:SSSS");
                end.setTime(sdf.parse(message));
                Calendar start = db.<Long, Calendar>getMap("START_DATES").get(AbilityUtils.getChatId(update));
                File file = fileCreator.createExcel(start, end);
                SendDocument sendDocument = SendDocument
                        .builder()
                        .document(new InputFile().setMedia(file))
                        .chatId(AbilityUtils.getChatId(update))
                        .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build())
                        .build();
                try {
                    sender.sendDocument(sendDocument);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                silent.send(wrongDateFormat, AbilityUtils.getChatId(update));
                silent.forceReply(chooseEndDate, AbilityUtils.getChatId(update));
            }
        }, Flag.REPLY, AbilityUtils.isReplyTo(chooseEndDate));
    }

    private Predicate<Update> isCommand() {
        return update -> update.hasMessage() && update.getMessage().isCommand();
    }

    public Reply makeScore() {
        return Reply.of((baseAbilityBot, update) -> {
            long chatId = AbilityUtils.getChatId(update);
            RatingForm form = forms.get(chatId);
            if (form.getCriteria() == null) {
                sendKeyboardMessage(chatId, pleaseFillTheForm, boardFactory.continueKeyboard());
                return;
            }
            try {
                String data = update.getMessage().getText();
                Long score = Long.valueOf(data);
                if (score < -1 || score > 2) {
                    silent.send(scoreBorders, chatId);
                    silent.forceReply(scoreCompany, chatId);
                    return;
                }
                form.setScore(score);
                forms.put(chatId, form);
                silent.forceReply(description, chatId);
            } catch (NumberFormatException e) {
                silent.send(pleaseEnterNumber, chatId);
                silent.forceReply(scoreCompany, chatId);
            }
        }, Flag.REPLY, AbilityUtils.isReplyTo(scoreCompany), checkActive);
    }

    public Reply addDescription() {
        return Reply.of((baseAbilityBot, update) -> {
            long chatId = AbilityUtils.getChatId(update);
            RatingForm form = forms.get(chatId);
            if (form.getScore() == null) {
                sendKeyboardMessage(chatId, pleaseFillTheForm, boardFactory.continueKeyboard());
                return;
            }
            String description = update.getMessage().getText();
            form.setDescription(description);
            form.setScoreDate(new Date((long) update.getMessage().getDate() * 1000));
            formRepo.save(form);
            forms.put(chatId, new RatingForm());
            sendKeyboardMessage(chatId, saveSuccess, ReplyKeyboardRemove.builder().removeKeyboard(true).build());
            sendKeyboardMessage(chatId, oneMoreForm, boardFactory.continueKeyboard());
        }, Flag.REPLY, checkActive, AbilityUtils.isReplyTo(description));
    }

    public Reply refresh() {
        return Reply.of((baseAbilityBot, update) -> {
            sendKeyboardMessage(AbilityUtils.getChatId(update), chooseOMSUorROIV, boardFactory.companyTypeKeyboard());
        }, Flag.CALLBACK_QUERY, checkActive, hasCallbackWith("_REFRESH")).enableStats("refresh");
    }

    public Reply randomMessage() {
        return Reply.of((baseAbilityBot, update) -> {
            long chatId = AbilityUtils.getChatId(update);
            RatingForm form = forms.get(chatId);
            if (form.getScore() != null) {
                silent.send(pleaseAnswer, chatId);
                silent.forceReply(description, chatId);
                return;
            }
            if (form.getCriteria() != null) {
                silent.send(pleaseAnswer, chatId);
                silent.forceReply(scoreCompany, chatId);
                return;
            }
            sendKeyboardMessage(chatId, pleaseFillTheForm, boardFactory.continueKeyboard());
        }, Flag.MESSAGE, checkActive, Flag.REPLY.negate(), isCommand().negate());
    }

    public Reply notTimeYet() {
        return Reply.of((baseAbilityBot, update) ->
                silent.send(noTime,
                        AbilityUtils.getChatId(update)), checkActive.negate(),
                isCommand().negate(), hasCallbackWith("_PERIOD").negate(), isExcelMessage().negate());
    }

    private Predicate<Update> isExcelMessage() {
        return update -> update.hasMessage() && update.getMessage().isReply()
                && (update.getMessage().getReplyToMessage().getText().equals(chooseStartDate)
                || update.getMessage().getReplyToMessage().getText().equals(chooseEndDate));
    }

    public ReplyFlow mainFlow() {

        ReplyFlow chooseCriteria = ReplyFlow.builder(db)
                .action((baseAbilityBot, update) -> {
                    RatingForm form = forms.get(AbilityUtils.getChatId(update));
                    Long critId = Long.valueOf(update.getCallbackQuery().getData().split("_")[0]);
                    String critName = critRepo.findCriteriaById(critId).getName();
                    form.setCriteria(critName);
                    forms.put(AbilityUtils.getChatId(update), form);
                    silent.forceReply(scoreCompany, AbilityUtils.getChatId(update));
                })
                .enableStats("chooseCriteria")
                .onlyIf(Flag.CALLBACK_QUERY)
                .onlyIf(checkActive)
                .onlyIf(hasCallbackWith("_CRIT"))
                .build();

        ReplyFlow chooseCompany = ReplyFlow.builder(db)
                .action((baseAbilityBot, update) -> {
                    RatingForm form = forms.get(AbilityUtils.getChatId(update));
                    Long compId = Long.valueOf(update.getCallbackQuery().getData().split("_")[0]);
                    Company company = companyRepo.getCompanyById(compId);
                    form.setCompany(company.getName());
                    form.setCompanyType(company.getCompanyType());
                    forms.put(AbilityUtils.getChatId(update), form);
                    sendKeyboardMessage(AbilityUtils.getChatId(update), criterion, boardFactory.criteriaKeyboard());
                })
                .enableStats("chooseCompany")
                .onlyIf(Flag.CALLBACK_QUERY)
                .onlyIf(checkActive)
                .onlyIf(hasCallbackWith("_COMP"))
                .next(chooseCriteria)
                .build();

        return ReplyFlow.builder(db)
                .action((baseAbilityBot, update) -> {
                    forms.put(AbilityUtils.getChatId(update), new RatingForm());
                    RatingForm form = forms.get(AbilityUtils.getChatId(update));
                    form.setUserName(AbilityUtils.fullName(update.getCallbackQuery().getFrom()));
                    form.setUserId(update.getCallbackQuery().getFrom().getId());
                    forms.put(AbilityUtils.getChatId(update), form);
                    CompanyType compType = CompanyType.valueOf(update.getCallbackQuery().getData().split("_")[0]);
                    sendKeyboardMessage(AbilityUtils.getChatId(update), "Пожалуйста, выберите " + compType.getName(),
                            boardFactory.companyListKeyboard(compType, 2));
                })
                .enableStats("mainFlow")
                .onlyIf(Flag.CALLBACK_QUERY.and(hasCallbackWith("_TYPE")))
                .onlyIf(checkActive)
                .next(chooseCompany)
                .build();
    }

    private Predicate<Update> hasCallbackWith(String text) {
        return update -> update.hasCallbackQuery() && update.getCallbackQuery().getData().contains(text);
    }

    private void sendKeyboardMessage(long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboard);
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void refreshAllForms() {
        for (Long aLong : forms.keySet()) {
            forms.put(aLong, new RatingForm());
        }
        db.commit();
    }
}

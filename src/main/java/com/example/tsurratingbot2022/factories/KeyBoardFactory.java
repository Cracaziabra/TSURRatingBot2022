package com.example.tsurratingbot2022.factories;

import com.example.tsurratingbot2022.Company;
import com.example.tsurratingbot2022.CompanyType;
import com.example.tsurratingbot2022.Criteria;
import com.example.tsurratingbot2022.repositories.CompanyRepo;
import com.example.tsurratingbot2022.repositories.CriteriaRepo;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyBoardFactory {

    private final CompanyRepo companyRepo;
    private final CriteriaRepo criteriaRepo;

    public KeyBoardFactory(CompanyRepo companyRepo1, CriteriaRepo criteriaRepo1) {
        companyRepo = companyRepo1;
        criteriaRepo = criteriaRepo1;
    }

    @Data
    private class KeyboardBones {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        private void addInNColumnList(InlineKeyboardButton button, int columnCount) {
            if (keyboard.isEmpty()) keyboard.add(new ArrayList<>());
            List<InlineKeyboardButton> row = keyboard.get(keyboard.size()-1);
            if (row.size() == columnCount) {
                keyboard.add(new ArrayList<>());
                row = keyboard.get(keyboard.size()-1);
            }
            row.add(button);
        }
        private InlineKeyboardMarkup complete() {
            inlineKeyboard.setKeyboard(keyboard);
            return inlineKeyboard;
        }
    }

    public ReplyKeyboard companyTypeKeyboard() {
        KeyboardBones bones = new KeyboardBones();
        for (CompanyType companyType : CompanyType.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton(companyType.getName());
            button.setCallbackData(companyType+"_TYPE");
            bones.addInNColumnList(button, 2);
        }
        return bones.complete();
    }

    public ReplyKeyboard companyListKeyboard(CompanyType type, int columnCount) {
        KeyboardBones bones = new KeyboardBones();
        List<Company> companies = companyRepo.findByCompanyType(type);
        for (Company company : companies) {
            InlineKeyboardButton button = new InlineKeyboardButton(company.getName());
            button.setCallbackData(company.getId()+"_COMP");
            bones.addInNColumnList(button, columnCount);
        }
        return bones.complete();
    }

    public ReplyKeyboard criteriaKeyboard() {
        KeyboardBones bones = new KeyboardBones();
        List<Criteria> criteriaList = criteriaRepo.findAllByOrderById();
        for (Criteria criteria : criteriaList) {
            InlineKeyboardButton button = new InlineKeyboardButton(criteria.getName());
            button.setCallbackData(criteria.getId()+"_CRIT");
            bones.addInNColumnList(button, 1);
        }
        return bones.complete();
    }

    public ReplyKeyboard continueKeyboard() {
        KeyboardBones bones = new KeyboardBones();
        InlineKeyboardButton button = new InlineKeyboardButton("Ещё");
        button.setCallbackData("_REFRESH");
        bones.addInNColumnList(button, 1);
        return bones.complete();
    }
}

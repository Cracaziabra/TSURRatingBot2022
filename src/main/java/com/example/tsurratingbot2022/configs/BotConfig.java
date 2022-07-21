package com.example.tsurratingbot2022.configs;

import com.example.tsurratingbot2022.factories.KeyBoardFactory;
import com.example.tsurratingbot2022.repositories.CompanyRepo;
import com.example.tsurratingbot2022.repositories.CriteriaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    private final CompanyRepo companyRepo;
    private final CriteriaRepo criteriaRepo;

    @Autowired
    public BotConfig(CompanyRepo companyRepo, CriteriaRepo criteriaRepo) {
        this.companyRepo = companyRepo;
        this.criteriaRepo = criteriaRepo;
    }

    @Bean
    public KeyBoardFactory createFactory() {
        return new KeyBoardFactory(companyRepo, criteriaRepo);
    }

//    @Bean
//    public CommandLineRunner dataLoader() {
//        return args -> {
//            companyRepo.save(new Company("Министерство образования и науки", ROIV));
//            companyRepo.save(new Company("Министерство строительства и ЖКХ", ROIV));
//            companyRepo.save(new Company("Министерство здравоохранения", ROIV));
//            companyRepo.save(new Company("Министерство транспорта и дорожного хозяйства", ROIV));
//            companyRepo.save(new Company("Государственный комитет энергетики и тарифного регулирования", ROIV));
//            companyRepo.save(new Company("Министерство труда и социальной защиты", ROIV));
//            companyRepo.save(new Company("Министерство природных ресурсов и экологии", ROIV));
//            companyRepo.save(new Company("Управление по гражданской обороне, чрезвычайным ситуациям и пожарной безопасности", ROIV));
//            companyRepo.save(new Company("Министерство экономического развития", ROIV));
//            companyRepo.save(new Company("Министерство по делам юстиции и региональной безопасности", ROIV));
//            companyRepo.save(new Company("Министерство культуры", ROIV));
//            companyRepo.save(new Company("Министерство физической культуры и спорта", ROIV));
//            companyRepo.save(new Company("Министерство финансов", ROIV));
//            companyRepo.save(new Company("Министерство сельского хозяйства и продовольствия", ROIV));
//            companyRepo.save(new Company("Министерство национальной и территориальной политики", ROIV));
//            companyRepo.save(new Company("Государственный комитет цифрового развития и связи", ROIV));
//            companyRepo.save(new Company("Государственная инспекция по охране объектов культурного наследия", ROIV));
//            companyRepo.save(new Company("Министерство имущественных и земельных отношений", ROIV));
//            companyRepo.save(new Company("Государственный комитет по регулированию контрактной системы в сфере закупок", ROIV));
//            companyRepo.save(new Company("Администрация г. Абакана", OMSU));
//            companyRepo.save(new Company("Администрация г. Абазы", OMSU));
//            companyRepo.save(new Company("Администрация г. Саяногорска", OMSU));
//            companyRepo.save(new Company("Администрация г. Сорска", OMSU));
//            companyRepo.save(new Company("Администрация г. Черногорска", OMSU));
//            companyRepo.save(new Company("Администрация Алтайского района", OMSU));
//            companyRepo.save(new Company("Администрация Аскизского района", OMSU));
//            companyRepo.save(new Company("Администрация Бейского района", OMSU));
//            companyRepo.save(new Company("Администрация Боградского района", OMSU));
//            companyRepo.save(new Company("Администрация Орджоникидзевского района", OMSU));
//            companyRepo.save(new Company("Администрация Таштыпского района", OMSU));
//            companyRepo.save(new Company("Администрация Усть-Абаканского района", OMSU));
//            companyRepo.save(new Company("Администрация Ширинского района", OMSU));
//            criteriaRepo.save(new Criteria("Госпаблики (РОИВ ОМСУ и подведы)"));
//            criteriaRepo.save(new Criteria("Оперативность при подготовке ответов через ИМ"));
//            criteriaRepo.save(new Criteria("Размещение в паблике информации о решённой проблеме граждан"));
//            criteriaRepo.save(new Criteria("Содействие в сборе и подготовке информации (редакция)"));
//            criteriaRepo.save(new Criteria("Содействие в распространении (продвижение) в социальных сетях"));
//            criteriaRepo.save(new Criteria("Зеленая зона в квартальном рейтинге и  Присутствие специалиста отраслевого блока в ЦУРе"));
//            criteriaRepo.save(new Criteria("Деятельность по формированию ДК"));
//            criteriaRepo.save(new Criteria("Угрозы и риски"));
//            criteriaRepo.save(new Criteria("Таргет и онлайн-социология"));
//            criteriaRepo.save(new Criteria("Прочие направления"));
//        };
//    }
}

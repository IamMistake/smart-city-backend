package com.smartcity.springservice.seeder;

import com.smartcity.springservice.domain.core.entity.Incident;
import com.smartcity.springservice.domain.core.entity.UserProfile;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.IncidentType;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;
import com.smartcity.springservice.repository.IncidentRepository;
import com.smartcity.springservice.repository.UserProfileRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("demo")
public class IncidentSeeder {

    private final IncidentRepository incidentRepository;
    private final UserProfileRepository userProfileRepository;

    public IncidentSeeder(IncidentRepository incidentRepository,
                          UserProfileRepository userProfileRepository) {
        this.incidentRepository = incidentRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public void seed() {
        if (incidentRepository.count() > 0) {
            System.out.println(">>> [DEMO] Incidents already seeded, skipping.");
            return;
        }

        // Grab seeded users to assign as reporters
        UserProfile citizen1  = userProfileRepository.findByEmail("citizen1@smartcity.mk").orElseThrow();
        UserProfile citizen2  = userProfileRepository.findByEmail("citizen2@smartcity.mk").orElseThrow();
        UserProfile operator  = userProfileRepository.findByEmail("operator@smartcity.mk").orElseThrow();

        Instant now = Instant.now();

        List<Incident> incidents = List.of(

                // --- ACTIVE HIGH PRIORITY ---
                build("Пожар во магацин - Автокоманда",
                        "Зафатена е деловна зграда, испратени се 3 противпожарни возила.",
                        IncidentType.FIRE, PriorityLevel.HIGH, IncidentStatus.ACTIVE,
                        "41.994800", "21.430200", "ул. Климент Охридски, Автокоманда",
                        now.minus(45, ChronoUnit.MINUTES), null, citizen1),

                build("Сообраќајна несреќа - Мост Гоце Делчев",
                        "Судар на две возила, затворена е десната лента кон центар.",
                        IncidentType.ACCIDENT, PriorityLevel.HIGH, IncidentStatus.ACTIVE,
                        "41.996100", "21.431400", "Мост Гоце Делчев",
                        now.minus(20, ChronoUnit.MINUTES), null, citizen2),

                // --- ACTIVE MEDIUM PRIORITY ---
                build("Протест пред Собрание",
                        "Граѓански протест со околу 200 учесници, мирен тек.",
                        IncidentType.PROTEST, PriorityLevel.MEDIUM, IncidentStatus.ACTIVE,
                        "41.996400", "21.431300", "пл. Македонија, пред Собрание",
                        now.minus(2, ChronoUnit.HOURS), null, citizen1),

                build("Зголемено загадување - Железара",
                        "PM10 вредности над 120 μg/m³, препорачано е да не се излегува.",
                        IncidentType.POLLUTION, PriorityLevel.MEDIUM, IncidentStatus.ACTIVE,
                        "41.985600", "21.463700", "Железара, Скопје",
                        now.minus(3, ChronoUnit.HOURS), null, operator),

                build("Полициска акција - Чаир",
                        "Рација во дискотека, присутни 4 патролни возила.",
                        IncidentType.POLICE_ACTIVITY, PriorityLevel.MEDIUM, IncidentStatus.ACTIVE,
                        "41.999800", "21.434500", "бул. 1 Мај, Чаир",
                        now.minus(1, ChronoUnit.HOURS), null, citizen2),

                // --- ACTIVE LOW PRIORITY ---
                build("Паднато дрво - Градски Парк",
                        "Дрво паднато на патека поради силен ветер, нема повредени.",
                        IncidentType.OTHER, PriorityLevel.LOW, IncidentStatus.ACTIVE,
                        "41.993300", "21.409400", "Градски Парк, Скопје",
                        now.minus(5, ChronoUnit.HOURS), null, citizen1),

                // --- RESOLVED ---
                build("Сообраќаен инцидент - Буњаковец",
                        "Возило излетало од коловоз, патот исчистен.",
                        IncidentType.ACCIDENT, PriorityLevel.MEDIUM, IncidentStatus.RESOLVED,
                        "41.970200", "21.450100", "ул. Бул. Jане Сандански, Буњаковец",
                        now.minus(1, ChronoUnit.DAYS), now.minus(22, ChronoUnit.HOURS), citizen2),

                build("Пожар во стан - Аеродром",
                        "Пожар локализиран, нема жртви.",
                        IncidentType.FIRE, PriorityLevel.HIGH, IncidentStatus.RESOLVED,
                        "41.975600", "21.464300", "ул. Лазар Поп-Трајков, Аеродром",
                        now.minus(2, ChronoUnit.DAYS), now.minus(47, ChronoUnit.HOURS), citizen1),

                build("Протест - Плоштад Македонија",
                        "Демонстрации завршија без инциденти.",
                        IncidentType.PROTEST, PriorityLevel.LOW, IncidentStatus.RESOLVED,
                        "41.996300", "21.431400", "пл. Македонија",
                        now.minus(3, ChronoUnit.DAYS), now.minus(71, ChronoUnit.HOURS), operator),

                // --- UNDER_INVESTIGATION ---
                build("Сомнителна активност - Старa Чаршија",
                        "Пријавено сомнително лице, во тек е проверка.",
                        IncidentType.POLICE_ACTIVITY, PriorityLevel.MEDIUM, IncidentStatus.REPORTED,
                        "41.997200", "21.437800", "Стара Чаршија, Скопје",
                        now.minus(6, ChronoUnit.HOURS), null, citizen2)
        );

        incidentRepository.saveAll(incidents);
        System.out.println(">>> [DEMO] Seeded " + incidents.size() + " incidents.");
    }

    private Incident build(String title, String description,
                           IncidentType type, PriorityLevel priority, IncidentStatus status,
                           String lat, String lng, String address,
                           Instant occurredAt, Instant resolvedAt, UserProfile reporter) {
        Incident i = new Incident();
        i.setTitle(title);
        i.setDescription(description);
        i.setIncidentType(type);
        i.setPriority(priority);
        i.setStatus(status);
        i.setLatitude(new BigDecimal(lat));
        i.setLongitude(new BigDecimal(lng));
        i.setAddress(address);
        i.setOccurredAt(occurredAt);
        i.setResolvedAt(resolvedAt);
        i.setReportedByUser(reporter);
        return i;
    }
}
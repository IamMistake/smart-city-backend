package com.smartcity.springservice.seeder;

import com.smartcity.springservice.domain.core.entity.CityEvent;
import com.smartcity.springservice.domain.core.entity.UserProfile;
import com.smartcity.springservice.domain.core.enums.EventStatus;
import com.smartcity.springservice.domain.core.enums.EventType;
import com.smartcity.springservice.repository.CityEventRepository;
import com.smartcity.springservice.repository.UserProfileRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("demo")
public class CityEventSeeder {

    private final CityEventRepository cityEventRepository;
    private final UserProfileRepository userProfileRepository;

    public CityEventSeeder(CityEventRepository cityEventRepository,
                           UserProfileRepository userProfileRepository) {
        this.cityEventRepository = cityEventRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public void seed() {
        if (cityEventRepository.count() > 0) {
            System.out.println(">>> [DEMO] City events already seeded, skipping.");
            return;
        }

        UserProfile operator = userProfileRepository.findByEmail("operator@smartcity.mk").orElseThrow();
        UserProfile authority = userProfileRepository.findByEmail("authority@smartcity.mk").orElseThrow();

        Instant now = Instant.now();

        List<CityEvent> events = List.of(

                build("Маратон Скопје 2024",
                        "Годишен градски маратон, затворени улици во центар.",
                        EventType.PUBLIC_EVENT, EventStatus.ACTIVE,
                        "41.996300", "21.431400", "пл. Македонија",
                        now.minus(1, ChronoUnit.HOURS),
                        now.plus(5, ChronoUnit.HOURS),
                        operator),

                build("Концерт - Градски Парк",
                        "Летен концерт на отворено, очекувани 500 посетители.",
                        EventType.PUBLIC_EVENT, EventStatus.ACTIVE,
                        "41.993300", "21.409400", "Градски Парк, Скопје",
                        now.plus(2, ChronoUnit.HOURS),
                        now.plus(6, ChronoUnit.HOURS),
                        operator),

                build("Протест - Министерство за финансии",
                        "Синдикален протест, мирен тек, 100-150 учесници.",
                        EventType.PROTEST, EventStatus.ACTIVE,
                        "41.995800", "21.430100", "ул. Даме Груев, Скопје",
                        now.minus(30, ChronoUnit.MINUTES),
                        now.plus(2, ChronoUnit.HOURS),
                        authority),

                build("Одржување - бул. Партизански",
                        "Планирани градежни работи, делумно затворање на сообраќај.",
                        EventType.ROAD_CLOSURE, EventStatus.ACTIVE,
                        "41.998100", "21.425400", "бул. Партизански од Плоштад до Вардар",
                        now.minus(2, ChronoUnit.HOURS),
                        now.plus(8, ChronoUnit.HOURS),
                        authority),

                build("Фестивал Скопје Лето",
                        "Завршен настан, нема повеќе ограничувања.",
                        EventType.PUBLIC_EVENT, EventStatus.RESOLVED,
                        "41.996300", "21.431400", "пл. Македонија",
                        now.minus(2, ChronoUnit.DAYS),
                        now.minus(1, ChronoUnit.DAYS),
                        operator),

                build("Протест - Пред Влада",
                        "Протестот заврши без инциденти.",
                        EventType.PROTEST, EventStatus.RESOLVED,
                        "41.996400", "21.431300", "пред Влада на РСМ",
                        now.minus(3, ChronoUnit.DAYS),
                        now.minus(3, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS),
                        authority)
        );

        cityEventRepository.saveAll(events);
        System.out.println(">>> [DEMO] Seeded " + events.size() + " city events.");
    }

    private CityEvent build(String title, String description,
                            EventType eventType, EventStatus status,
                            String lat, String lng, String address,
                            Instant startTime, Instant endTime,
                            UserProfile createdBy) {
        CityEvent e = new CityEvent();
        e.setTitle(title);
        e.setDescription(description);
        e.setEventType(eventType);
        e.setStatus(status);
        e.setLatitude(new BigDecimal(lat));
        e.setLongitude(new BigDecimal(lng));
        e.setAddress(address);
        e.setStartTime(startTime);
        e.setEndTime(endTime);
        e.setCreatedByUser(createdBy);
        return e;
    }
}
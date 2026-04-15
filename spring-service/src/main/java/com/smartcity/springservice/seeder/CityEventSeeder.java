package com.smartcity.springservice.seeder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.smartcity.springservice.domain.core.entity.CityEvent;
import com.smartcity.springservice.domain.core.enums.EventStatus;
import com.smartcity.springservice.domain.core.enums.EventType;
import com.smartcity.springservice.domain.core.repository.CityEventRepository;

@Component
@Profile("demo")
public class CityEventSeeder {

    private final CityEventRepository cityEventRepository;

    public CityEventSeeder(CityEventRepository cityEventRepository) {
        this.cityEventRepository = cityEventRepository;
    }

    public void seed() {
        if (cityEventRepository.count() > 0) {
            System.out.println(">>> [DEMO] CityEvents already seeded, skipping.");
            return;
        }

        Instant now = Instant.now();

        cityEventRepository.save(create("City Marathon 2026", "Annual city marathon route", EventType.PUBLIC_EVENT, EventStatus.PLANNED, 41.9964, 21.4314, now.plus(7, ChronoUnit.DAYS), now.plus(7, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS)));
        cityEventRepository.save(create("Workers Rights Protest", "Organized protest at city center", EventType.PROTEST, EventStatus.ACTIVE, 41.9961, 21.4310, now.minus(1, ChronoUnit.HOURS), now.plus(3, ChronoUnit.HOURS)));
        cityEventRepository.save(create("Road Closure - Bridge Repair", "Karposh bridge maintenance closure", EventType.ROAD_CLOSURE, EventStatus.ACTIVE, 41.9933, 21.4094, now.minus(2, ChronoUnit.DAYS), now.plus(5, ChronoUnit.DAYS)));
        cityEventRepository.save(create("Police Checkpoint - Blvd. ASNOM", "Routine traffic checkpoint", EventType.POLICE_ACTIVITY, EventStatus.RESOLVED, 41.9912, 21.4378, now.minus(1, ChronoUnit.DAYS), now.minus(12, ChronoUnit.HOURS)));
        cityEventRepository.save(create("Street Fair - Bit Pazar", "Weekend street market event", EventType.PUBLIC_EVENT, EventStatus.PLANNED, 41.9981, 21.4254, now.plus(3, ChronoUnit.DAYS), now.plus(3, ChronoUnit.DAYS).plus(8, ChronoUnit.HOURS)));
        cityEventRepository.save(create("Emergency Drill - Fire Dept", "City-wide fire emergency drill", EventType.OTHER, EventStatus.RESOLVED, 41.9856, 21.4637, now.minus(3, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS)));

        System.out.println(">>> [DEMO] Seeded 6 city events.");
    }

    private CityEvent create(String title, String description, EventType eventType,
                             EventStatus status, double lat, double lon, Instant startTime, Instant endTime) {
        CityEvent event = new CityEvent();
        event.setTitle(title);
        event.setDescription(description);
        event.setEventType(eventType);
        event.setStatus(status);
        event.setLatitude(BigDecimal.valueOf(lat));
        event.setLongitude(BigDecimal.valueOf(lon));
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        return event;
    }
}
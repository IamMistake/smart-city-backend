package com.smartcity.springservice.seeder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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

        cityEventRepository.saveAll(List.of(
            create(
                "City Marathon 2026",
                "Annual city marathon route",
                EventType.PUBLIC_EVENT,
                EventStatus.PLANNED,
                41.9964,
                21.4314,
                "Macedonia Square and Macedonia Street, Skopje",
                "2026-05-03T06:00:00Z",
                "2026-05-03T12:00:00Z"
            ),
            create(
                "Workers Rights Protest",
                "Organized protest at city center",
                EventType.PROTEST,
                EventStatus.ACTIVE,
                41.9961,
                21.4310,
                "Porta Makedonija, 11 October St, Skopje",
                "2026-04-27T09:00:00Z",
                "2026-04-27T13:00:00Z"
            ),
            create(
                "Road Closure - Bridge Repair",
                "Karposh bridge maintenance closure",
                EventType.ROAD_CLOSURE,
                EventStatus.ACTIVE,
                41.9933,
                21.4094,
                "Blvd. Ilinden near the City Park footbridge, Skopje",
                "2026-04-25T06:00:00Z",
                "2026-04-30T18:00:00Z"
            ),
            create(
                "Police Checkpoint - Blvd. ASNOM",
                "Routine traffic checkpoint",
                EventType.POLICE_ACTIVITY,
                EventStatus.RESOLVED,
                41.9912,
                21.4378,
                "Blvd. ASNOM near Jane Sandanski Arena, Skopje",
                "2026-04-26T07:00:00Z",
                "2026-04-26T12:00:00Z"
            ),
            create(
                "Street Fair - Bit Pazar",
                "Weekend street market event",
                EventType.PUBLIC_EVENT,
                EventStatus.PLANNED,
                41.9981,
                21.4254,
                "Bit Pazar Market, Blvd. Krste Misirkov, Skopje",
                "2026-05-01T08:00:00Z",
                "2026-05-01T16:00:00Z"
            ),
            create(
                "Emergency Drill - Fire Dept",
                "City-wide fire emergency drill",
                EventType.OTHER,
                EventStatus.RESOLVED,
                41.9856,
                21.4637,
                "Blvd. Aleksandar Makedonski near Zelezara, Skopje",
                "2026-04-22T09:00:00Z",
                "2026-04-22T15:00:00Z"
            )
        ));

        System.out.println(">>> [DEMO] Seeded 6 city events.");
    }

    private CityEvent create(
        String title,
        String description,
        EventType eventType,
        EventStatus status,
        double lat,
        double lon,
        String address,
        String startTime,
        String endTime
    ) {
        CityEvent event = new CityEvent();
        event.setTitle(title);
        event.setDescription(description);
        event.setEventType(eventType);
        event.setStatus(status);
        event.setLatitude(BigDecimal.valueOf(lat));
        event.setLongitude(BigDecimal.valueOf(lon));
        event.setAddress(address);
        event.setStartTime(Instant.parse(startTime));
        event.setEndTime(Instant.parse(endTime));
        return event;
    }
}

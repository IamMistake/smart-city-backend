package com.smartcity.springservice.seeder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.smartcity.springservice.domain.core.entity.Incident;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.IncidentType;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;
import com.smartcity.springservice.domain.core.repository.IncidentRepository;

@Component
@Profile("demo")
public class IncidentSeeder {

    private final IncidentRepository incidentRepository;

    public IncidentSeeder(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public void seed() {
        if (incidentRepository.count() > 0) {
            System.out.println(">>> [DEMO] Incidents already seeded, skipping.");
            return;
        }

        incidentRepository.saveAll(List.of(
            create(
                "Fire at Bit Pazar",
                "Large fire near the market area",
                IncidentType.FIRE,
                PriorityLevel.CRITICAL,
                IncidentStatus.ACTIVE,
                41.9981,
                21.4254,
                "Bit Pazar Market, Blvd. Krste Misirkov, Skopje",
                "2026-04-27T06:45:00Z",
                null
            ),
            create(
                "Car accident on blvd. ASNOM",
                "Two vehicles collision",
                IncidentType.ACCIDENT,
                PriorityLevel.HIGH,
                IncidentStatus.REPORTED,
                41.9912,
                21.4378,
                "Blvd. ASNOM near Jane Sandanski Arena, Skopje",
                "2026-04-26T15:20:00Z",
                null
            ),
            create(
                "Protest at City Park",
                "Peaceful protest gathering",
                IncidentType.PROTEST,
                PriorityLevel.MEDIUM,
                IncidentStatus.ACTIVE,
                41.9933,
                21.4094,
                "City Park, Blvd. Ilinden, Skopje",
                "2026-04-27T10:00:00Z",
                null
            ),
            create(
                "Air pollution alert - Železara",
                "PM10 levels critically high",
                IncidentType.POLLUTION,
                PriorityLevel.HIGH,
                IncidentStatus.ACTIVE,
                41.9856,
                21.4637,
                "Blvd. Aleksandar Makedonski near Zelezara, Skopje",
                "2026-04-27T05:30:00Z",
                null
            ),
            create(
                "Police activity near Parliament",
                "Increased police presence",
                IncidentType.POLICE_ACTIVITY,
                PriorityLevel.LOW,
                IncidentStatus.RESOLVED,
                41.9964,
                21.4314,
                "11 October St near the Assembly of North Macedonia, Skopje",
                "2026-04-25T18:15:00Z",
                "2026-04-25T20:00:00Z"
            ),
            create(
                "Road damage on ul. Partizanska",
                "Large pothole causing hazard",
                IncidentType.OTHER,
                PriorityLevel.MEDIUM,
                IncidentStatus.REPORTED,
                41.9989,
                21.4170,
                "Blvd. Partizanski Odredi near City Park, Skopje",
                "2026-04-24T07:10:00Z",
                null
            ),
            create(
                "Gas leak in Aerodrom",
                "Suspected gas leak reported",
                IncidentType.FIRE,
                PriorityLevel.CRITICAL,
                IncidentStatus.ACTIVE,
                41.9756,
                21.4643,
                "Blvd. Serbia near Novo Lisice, Skopje",
                "2026-04-27T04:55:00Z",
                null
            ),
            create(
                "Flood in Gjorce Petrov",
                "Street flooding after heavy rain",
                IncidentType.OTHER,
                PriorityLevel.HIGH,
                IncidentStatus.REPORTED,
                41.9921,
                21.3876,
                "Near Gjorce Petrov Municipality, Skopje",
                "2026-04-23T12:40:00Z",
                null
            ),
            create(
                "Vandalism at Kale Fortress",
                "Graffiti reported on walls",
                IncidentType.OTHER,
                PriorityLevel.LOW,
                IncidentStatus.RESOLVED,
                41.9971,
                21.4326,
                "Skopje Fortress Kale, Samoilova St, Skopje",
                "2026-04-22T09:30:00Z",
                "2026-04-22T13:00:00Z"
            ),
            create(
                "Traffic accident Chair",
                "Minor collision no injuries",
                IncidentType.ACCIDENT,
                PriorityLevel.LOW,
                IncidentStatus.REJECTED,
                41.9998,
                21.4345,
                "Cvetan Dimov Blvd near Chair Municipality, Skopje",
                "2026-04-26T08:05:00Z",
                null
            )
        ));

        System.out.println(">>> [DEMO] Seeded 10 incidents.");
    }

    private Incident create(
        String title,
        String description,
        IncidentType type,
        PriorityLevel priority,
        IncidentStatus status,
        double lat,
        double lon,
        String address,
        String occurredAt,
        String resolvedAt
    ) {
        Incident incident = new Incident();
        incident.setTitle(title);
        incident.setDescription(description);
        incident.setIncidentType(type);
        incident.setPriority(priority);
        incident.setStatus(status);
        incident.setLatitude(BigDecimal.valueOf(lat));
        incident.setLongitude(BigDecimal.valueOf(lon));
        incident.setAddress(address);
        incident.setOccurredAt(Instant.parse(occurredAt));
        incident.setResolvedAt(resolvedAt == null ? null : Instant.parse(resolvedAt));
        return incident;
    }
}

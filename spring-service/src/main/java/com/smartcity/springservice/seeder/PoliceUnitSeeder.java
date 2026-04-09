package com.smartcity.springservice.seeder;

import com.smartcity.springservice.domain.core.entity.PoliceUnit;
import com.smartcity.springservice.domain.core.enums.PoliceUnitStatus;
import com.smartcity.springservice.repository.PoliceUnitRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@Profile("demo")
public class PoliceUnitSeeder {

    private final PoliceUnitRepository policeUnitRepository;

    public PoliceUnitSeeder(PoliceUnitRepository policeUnitRepository) {
        this.policeUnitRepository = policeUnitRepository;
    }

    public void seed() {
        if (policeUnitRepository.count() > 0) {
            System.out.println(">>> [DEMO] Police units already seeded, skipping.");
            return;
        }

        Instant now = Instant.now();

        List<PoliceUnit> units = List.of(
                build("UNIT-01", "Патрола Центар 1",     PoliceUnitStatus.ACTIVE,      "41.996300", "21.431400", now),
                build("UNIT-02", "Патрола Центар 2",     PoliceUnitStatus.ACTIVE,      "41.998100", "21.425400", now),
                build("UNIT-03", "Патрола Аеродром",     PoliceUnitStatus.DISPATCHED,  "41.975600", "21.464300", now),
                build("UNIT-04", "Патрола Чаир",         PoliceUnitStatus.ACTIVE,      "41.999800", "21.434500", now),
                build("UNIT-05", "Патрола Карпош",       PoliceUnitStatus.DISPATCHED,  "41.993300", "21.409400", now),
                build("UNIT-06", "Резерва Центар",       PoliceUnitStatus.AVAILABLE,   "41.994700", "21.428200", now),
                build("UNIT-07", "Патрола Ѓорче Петров", PoliceUnitStatus.AVAILABLE,   "41.992100", "21.387600", now),
                build("UNIT-08", "Смена - Одмор",        PoliceUnitStatus.OFFLINE,     null,        null,        null)
        );

        policeUnitRepository.saveAll(units);
        System.out.println(">>> [DEMO] Seeded " + units.size() + " police units.");
    }

    private PoliceUnit build(String unitCode, String displayName, PoliceUnitStatus status,
                             String lat, String lng, Instant lastReportedAt) {
        PoliceUnit u = new PoliceUnit();
        u.setUnitCode(unitCode);
        u.setDisplayName(displayName);
        u.setStatus(status);
        u.setCurrentLatitude(lat != null ? new BigDecimal(lat) : null);
        u.setCurrentLongitude(lng != null ? new BigDecimal(lng) : null);
        u.setLastReportedAt(lastReportedAt);
        return u;
    }
}
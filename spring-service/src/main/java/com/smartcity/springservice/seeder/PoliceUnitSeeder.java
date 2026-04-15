package com.smartcity.springservice.seeder;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.smartcity.springservice.domain.core.entity.PoliceUnit;
import com.smartcity.springservice.domain.core.enums.PoliceUnitStatus;
import com.smartcity.springservice.domain.core.repository.PoliceUnitRepository;

@Component
@Profile("demo")
public class PoliceUnitSeeder {

    private final PoliceUnitRepository policeUnitRepository;

    public PoliceUnitSeeder(PoliceUnitRepository policeUnitRepository) {
        this.policeUnitRepository = policeUnitRepository;
    }

    public void seed() {
        if (policeUnitRepository.count() > 0) {
            System.out.println(">>> [DEMO] PoliceUnits already seeded, skipping.");
            return;
        }

        policeUnitRepository.save(create("UNIT-ALPHA-01", "Alpha Unit 1", PoliceUnitStatus.AVAILABLE));
        policeUnitRepository.save(create("UNIT-ALPHA-02", "Alpha Unit 2", PoliceUnitStatus.DISPATCHED));
        policeUnitRepository.save(create("UNIT-BRAVO-01", "Bravo Unit 1", PoliceUnitStatus.ACTIVE));
        policeUnitRepository.save(create("UNIT-BRAVO-02", "Bravo Unit 2", PoliceUnitStatus.AVAILABLE));
        policeUnitRepository.save(create("UNIT-CHARLIE-01", "Charlie Unit 1", PoliceUnitStatus.OFFLINE));
        policeUnitRepository.save(create("UNIT-CHARLIE-02", "Charlie Unit 2", PoliceUnitStatus.AVAILABLE));
        policeUnitRepository.save(create("UNIT-DELTA-01", "Delta Unit 1", PoliceUnitStatus.DISPATCHED));
        policeUnitRepository.save(create("UNIT-DELTA-02", "Delta Unit 2", PoliceUnitStatus.ACTIVE));

        System.out.println(">>> [DEMO] Seeded 8 police units.");
    }

    private PoliceUnit create(String unitCode, String displayName, PoliceUnitStatus status) {
        PoliceUnit unit = new PoliceUnit();
        unit.setUnitCode(unitCode);
        unit.setDisplayName(displayName);
        unit.setStatus(status);
        return unit;
    }
}
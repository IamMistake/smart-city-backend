package com.smartcity.springservice.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@Order(1)
public class DemoDataSeeder implements CommandLineRunner {

    private final UserProfileSeeder userProfileSeeder;
    private final IncidentSeeder incidentSeeder;
    private final CameraSeeder cameraSeeder;
    private final PoliceUnitSeeder policeUnitSeeder;
    private final CityEventSeeder cityEventSeeder;

    public DemoDataSeeder(
            UserProfileSeeder userProfileSeeder,
            IncidentSeeder incidentSeeder,
            CameraSeeder cameraSeeder,
            PoliceUnitSeeder policeUnitSeeder,
            CityEventSeeder cityEventSeeder) {
        this.userProfileSeeder = userProfileSeeder;
        this.incidentSeeder = incidentSeeder;
        this.cameraSeeder = cameraSeeder;
        this.policeUnitSeeder = policeUnitSeeder;
        this.cityEventSeeder = cityEventSeeder;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> [DEMO] Starting demo data seeding...");
        userProfileSeeder.seed();
        incidentSeeder.seed();
        cameraSeeder.seed();
        policeUnitSeeder.seed();
        cityEventSeeder.seed();
        System.out.println(">>> [DEMO] Demo data seeding complete.");
    }
}

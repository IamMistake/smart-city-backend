package com.smartcity.springservice.seeder;

import java.math.BigDecimal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.smartcity.springservice.domain.core.entity.Camera;
import com.smartcity.springservice.domain.core.enums.CameraStatus;
import com.smartcity.springservice.domain.core.repository.CameraRepository;

@Component
@Profile("demo")
public class CameraSeeder {

    private final CameraRepository cameraRepository;

    public CameraSeeder(CameraRepository cameraRepository) {
        this.cameraRepository = cameraRepository;
    }

    public void seed() {
        if (cameraRepository.count() > 0) {
            System.out.println(">>> [DEMO] Cameras already seeded, skipping.");
            return;
        }

        cameraRepository.save(create("CAM-001", "demo", "ext-001", "Ploshtad Makedonija - North", 41.9964, 21.4314, CameraStatus.ONLINE, "rtsp://demo/cam001"));
        cameraRepository.save(create("CAM-002", "demo", "ext-002", "Ploshtad Makedonija - South", 41.9961, 21.4310, CameraStatus.ONLINE, "rtsp://demo/cam002"));
        cameraRepository.save(create("CAM-003", "demo", "ext-003", "Blvd. ASNOM Junction", 41.9912, 21.4378, CameraStatus.ONLINE, "rtsp://demo/cam003"));
        cameraRepository.save(create("CAM-004", "demo", "ext-004", "City Park Entrance", 41.9933, 21.4094, CameraStatus.OFFLINE, "rtsp://demo/cam004"));
        cameraRepository.save(create("CAM-005", "demo", "ext-005", "Kale Fortress View", 41.9971, 21.4326, CameraStatus.ONLINE, "rtsp://demo/cam005"));
        cameraRepository.save(create("CAM-006", "demo", "ext-006", "Aerodrom - Jane Sandanski", 41.9756, 21.4643, CameraStatus.MAINTENANCE, "rtsp://demo/cam006"));
        cameraRepository.save(create("CAM-007", "demo", "ext-007", "Chair - blvd. 1 Maj", 41.9998, 21.4345, CameraStatus.ONLINE, "rtsp://demo/cam007"));
        cameraRepository.save(create("CAM-008", "demo", "ext-008", "Gjorce Petrov West", 41.9921, 21.3876, CameraStatus.OFFLINE, "rtsp://demo/cam008"));

        System.out.println(">>> [DEMO] Seeded 8 cameras.");
    }

    private Camera create(String name, String provider, String externalId, String description,
                          double lat, double lon, CameraStatus status, String streamUrl) {
        Camera camera = new Camera();
        camera.setName(name);
        camera.setProvider(provider);
        camera.setExternalCameraId(externalId);
        camera.setLatitude(BigDecimal.valueOf(lat));
        camera.setLongitude(BigDecimal.valueOf(lon));
        camera.setStatus(status);
        camera.setStreamUrl(streamUrl);
        return camera;
    }
}
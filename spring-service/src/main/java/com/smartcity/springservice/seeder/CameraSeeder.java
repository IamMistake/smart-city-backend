package com.smartcity.springservice.seeder;

import com.smartcity.springservice.domain.core.entity.Camera;
import com.smartcity.springservice.domain.core.enums.CameraStatus;
import com.smartcity.springservice.repository.CameraRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

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

        List<Camera> cameras = List.of(
                build("Плоштад Македонија",        "pulse_eco", "CAM-001", "41.996300", "21.431400", CameraStatus.ONLINE,       "rtsp://demo/cam001"),
                build("Мост Гоце Делчев",          "pulse_eco", "CAM-002", "41.996100", "21.431400", CameraStatus.ONLINE,       "rtsp://demo/cam002"),
                build("бул. Партизански - Центар", "pulse_eco", "CAM-003", "41.998100", "21.425400", CameraStatus.ONLINE,       "rtsp://demo/cam003"),
                build("Автобуска Станица",         "pulse_eco", "CAM-004", "41.994700", "21.428200", CameraStatus.ONLINE,       "rtsp://demo/cam004"),
                build("Аеродром - главен влез",    "pulse_eco", "CAM-005", "41.976400", "21.461200", CameraStatus.ONLINE,       "rtsp://demo/cam005"),
                build("Чаир - бул. 1 Мај",        "pulse_eco", "CAM-006", "41.999800", "21.434500", CameraStatus.OFFLINE,      null),
                build("Ѓорче Петров - пазар",      "pulse_eco", "CAM-007", "41.992100", "21.387600", CameraStatus.ONLINE,       "rtsp://demo/cam007"),
                build("Карпош - Градски Парк",     "pulse_eco", "CAM-008", "41.993300", "21.409400", CameraStatus.MAINTENANCE,  null)
        );

        cameraRepository.saveAll(cameras);
        System.out.println(">>> [DEMO] Seeded " + cameras.size() + " cameras.");
    }

    private Camera build(String name, String provider, String externalCameraId,
                         String lat, String lng, CameraStatus status, String streamUrl) {
        Camera c = new Camera();
        c.setName(name);
        c.setProvider(provider);
        c.setExternalCameraId(externalCameraId);
        c.setLatitude(new BigDecimal(lat));
        c.setLongitude(new BigDecimal(lng));
        c.setStatus(status);
        c.setStreamUrl(streamUrl);
        return c;
    }
}
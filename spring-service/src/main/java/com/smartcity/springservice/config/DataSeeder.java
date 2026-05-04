package com.smartcity.springservice.config;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.smartcity.springservice.domain.core.entity.Incident;
import com.smartcity.springservice.domain.core.entity.UserProfile;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.IncidentType;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;
import com.smartcity.springservice.domain.core.repository.IncidentRepository;
import com.smartcity.springservice.domain.core.repository.UserProfileRepository;

@Component
public class DataSeeder implements CommandLineRunner {
	private final IncidentRepository incidentRepository;
	private final UserProfileRepository userProfileRepository;
	private final AppEnvironment appEnvironment;

	public DataSeeder(
			IncidentRepository incidentRepository,
			UserProfileRepository userProfileRepository,
			AppEnvironment appEnvironment
	) {
		this.incidentRepository = incidentRepository;
		this.userProfileRepository = userProfileRepository;
		this.appEnvironment = appEnvironment;
	}

	@Override
	public void run(String... args) {
		if (!appEnvironment.isDev()) return;
		try {
			if (incidentRepository.count() > 0) return;

			UserProfile seedUser = userProfileRepository.findAll()
					.stream()
					.findFirst()
					.orElse(null);

			if (seedUser == null) return;

			incidentRepository.saveAll(buildSeedIncidents(seedUser));
		} catch (Exception e) {

		}
	}

	private List<Incident> buildSeedIncidents(UserProfile seedUser) {
		Incident i1 = new Incident();
		i1.setTitle("Power outage in Zone A");
		i1.setPriority(PriorityLevel.CRITICAL);
		i1.setStatus(IncidentStatus.ACTIVE);
		i1.setIncidentType(IncidentType.OTHER);
		i1.setReportedByUser(seedUser);

		Incident i2 = new Incident();
		i2.setTitle("Water pipe leak on Main St");
		i2.setPriority(PriorityLevel.HIGH);
		i2.setStatus(IncidentStatus.ACTIVE);
		i2.setIncidentType(IncidentType.OTHER);
		i2.setReportedByUser(seedUser);

		Incident i3 = new Incident();
		i3.setTitle("Traffic light malfunction");
		i3.setPriority(PriorityLevel.MEDIUM);
		i3.setStatus(IncidentStatus.RESOLVED);
		i3.setIncidentType(IncidentType.OTHER);
		i3.setResolvedAt(Instant.now());
		i3.setReportedByUser(seedUser);

		Incident i4 = new Incident();
		i4.setTitle("Noise complaint downtown");
		i4.setPriority(PriorityLevel.LOW);
		i4.setStatus(IncidentStatus.RESOLVED);
		i4.setIncidentType(IncidentType.NOISE_POLLUTION);
		i4.setResolvedAt(Instant.now());
		i4.setReportedByUser(seedUser);

		return List.of(i1, i2, i3, i4);
	}
}
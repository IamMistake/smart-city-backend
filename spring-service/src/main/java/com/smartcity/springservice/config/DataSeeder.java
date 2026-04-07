package com.smartcity.springservice.config;

import com.smartcity.springservice.model.Incident;
import com.smartcity.springservice.model.IncidentPriority;
import com.smartcity.springservice.model.IncidentStatus;
import com.smartcity.springservice.repository.IncidentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

	private final IncidentRepository incidentRepository;

	public DataSeeder(IncidentRepository incidentRepository) {
		this.incidentRepository = incidentRepository;
	}

	@Override
	public void run(String... args) {
		if (incidentRepository.count() > 0) return;

		incidentRepository.saveAll(List.of(
			build("Power outage in Zone A", IncidentPriority.CRITICAL, IncidentStatus.ACTIVE, null),
			build("Water pipe leak on Main St", IncidentPriority.HIGH, IncidentStatus.ACTIVE, null),
			build("Traffic light malfunction", IncidentPriority.MEDIUM, IncidentStatus.RESOLVED, LocalDateTime.now()),
			build("Noise complaint downtown", IncidentPriority.LOW, IncidentStatus.RESOLVED, LocalDateTime.now())
		));
	}

	private Incident build(String title, IncidentPriority priority, IncidentStatus status, LocalDateTime resolvedAt) {
		Incident incident = new Incident();
		incident.setTitle(title);
		incident.setPriority(priority);
		incident.setStatus(status);
		incident.setResolvedAt(resolvedAt);
		return incident;
	}
}

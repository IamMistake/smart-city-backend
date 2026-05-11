package com.smartcity.springservice.seeder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.smartcity.springservice.domain.core.entity.Incident;
import com.smartcity.springservice.domain.core.entity.UserProfile;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.repository.IncidentRepository;
import com.smartcity.springservice.domain.core.repository.UserProfileRepository;

class IncidentSeederTest {

	@Test
	void seedCreatesDeterministicIncidentsWithAddresses() {
		AtomicReference<List<Incident>> savedIncidents = new AtomicReference<>(List.of());
		IncidentRepository incidentRepository = repositoryProxy(0L, savedIncidents);
		UserProfileRepository userProfileRepository = userProfileRepositoryProxy();

		IncidentSeeder seeder = new IncidentSeeder(
			incidentRepository,
			userProfileRepository
		);
		seeder.seed();

		List<Incident> incidents = savedIncidents.get();
		assertEquals(10, incidents.size());
		assertFalse(incidents.stream().anyMatch(incident -> incident.getAddress() == null || incident.getAddress().isBlank()));

		Incident fireAtBitPazar = incidents.get(0);
		assertEquals("Bit Pazar Market, Blvd. Krste Misirkov, Skopje", fireAtBitPazar.getAddress());
		assertEquals(Instant.parse("2026-04-27T06:45:00Z"), fireAtBitPazar.getOccurredAt());
		assertEquals("citizen1@smartcity.mk", fireAtBitPazar.getReportedByUser().getEmail());

		Incident roadDamage = incidents.get(5);
		assertEquals("Blvd. Partizanski Odredi near City Park, Skopje", roadDamage.getAddress());
		assertEquals("41.9989", roadDamage.getLatitude().toPlainString());
		assertEquals("21.417", roadDamage.getLongitude().toPlainString());

		Incident resolvedPoliceActivity = incidents.get(4);
		assertEquals(IncidentStatus.RESOLVED, resolvedPoliceActivity.getStatus());
		assertEquals(Instant.parse("2026-04-25T20:00:00Z"), resolvedPoliceActivity.getResolvedAt());
	}

	private IncidentRepository repositoryProxy(long count, AtomicReference<List<Incident>> savedIncidents) {
		return (IncidentRepository) Proxy.newProxyInstance(
			IncidentRepository.class.getClassLoader(),
			new Class<?>[] { IncidentRepository.class },
			(proxy, method, args) -> {
				switch (method.getName()) {
					case "count":
						return count;
					case "saveAll":
						@SuppressWarnings("unchecked")
						List<Incident> incidents = (List<Incident>) args[0];
						savedIncidents.set(List.copyOf(incidents));
						return incidents;
					default:
						throw new UnsupportedOperationException("Unsupported method: " + method.getName());
				}
			}
		);
	}

	private UserProfileRepository userProfileRepositoryProxy() {
		return (UserProfileRepository) Proxy.newProxyInstance(
			UserProfileRepository.class.getClassLoader(),
			new Class<?>[] { UserProfileRepository.class },
			(proxy, method, args) -> {
				switch (method.getName()) {
					case "findByEmail":
						UserProfile user = new UserProfile();
						user.setEmail("citizen1@smartcity.mk");
						return java.util.Optional.of(user);
					default:
						throw new UnsupportedOperationException(
							"Unsupported method: " + method.getName()
						);
				}
			}
		);
	}
}

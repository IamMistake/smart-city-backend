package com.smartcity.springservice.seeder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.smartcity.springservice.domain.core.entity.CityEvent;
import com.smartcity.springservice.domain.core.enums.EventStatus;
import com.smartcity.springservice.domain.core.repository.CityEventRepository;

class CityEventSeederTest {

	@Test
	void seedCreatesDeterministicEventsWithAddresses() {
		AtomicReference<List<CityEvent>> savedEvents = new AtomicReference<>(List.of());
		CityEventRepository cityEventRepository = repositoryProxy(0L, savedEvents);

		CityEventSeeder seeder = new CityEventSeeder(cityEventRepository);
		seeder.seed();

		List<CityEvent> events = savedEvents.get();
		assertEquals(6, events.size());
		assertFalse(events.stream().anyMatch(event -> event.getAddress() == null || event.getAddress().isBlank()));

		CityEvent marathon = events.get(0);
		assertEquals("Macedonia Square and Macedonia Street, Skopje", marathon.getAddress());
		assertEquals(Instant.parse("2026-05-03T06:00:00Z"), marathon.getStartTime());
		assertEquals(Instant.parse("2026-05-03T12:00:00Z"), marathon.getEndTime());

		CityEvent roadClosure = events.get(2);
		assertEquals("Blvd. Ilinden near the City Park footbridge, Skopje", roadClosure.getAddress());
		assertEquals(EventStatus.ACTIVE, roadClosure.getStatus());

		CityEvent resolvedDrill = events.get(5);
		assertEquals("Blvd. Aleksandar Makedonski near Zelezara, Skopje", resolvedDrill.getAddress());
		assertEquals(Instant.parse("2026-04-22T15:00:00Z"), resolvedDrill.getEndTime());
	}

	private CityEventRepository repositoryProxy(long count, AtomicReference<List<CityEvent>> savedEvents) {
		return (CityEventRepository) Proxy.newProxyInstance(
			CityEventRepository.class.getClassLoader(),
			new Class<?>[] { CityEventRepository.class },
			(proxy, method, args) -> {
				switch (method.getName()) {
					case "count":
						return count;
					case "saveAll":
						@SuppressWarnings("unchecked")
						List<CityEvent> events = (List<CityEvent>) args[0];
						savedEvents.set(List.copyOf(events));
						return events;
					default:
						throw new UnsupportedOperationException("Unsupported method: " + method.getName());
				}
			}
		);
	}
}

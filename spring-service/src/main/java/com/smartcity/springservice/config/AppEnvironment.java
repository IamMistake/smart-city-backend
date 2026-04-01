package com.smartcity.springservice.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppEnvironment {
	private final Environment environment;

	public AppEnvironment(Environment environment) {
		this.environment = environment;
	}

	public boolean isDev() {
		String[] activeProfiles = environment.getActiveProfiles();

		if (activeProfiles.length == 0) {
			return true;
		}

		for (String profile : activeProfiles) {
			String normalized = profile.toLowerCase();
			if (
				normalized.equals("dev")
					|| normalized.equals("development")
					|| normalized.equals("local")
			) {
				return true;
			}
		}

		return false;
	}
}

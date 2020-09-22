package com.vais.clients.system;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import com.vais.clients.ApiApplication;

public class SystemCheckReadiness implements HealthCheck {

	private static final String readinessCheck = ApiApplication.class.getSimpleName() + " Readiness Check";

	@Inject
	@ConfigProperty(name = "io_openliberty_app_inMaintenance")
	Provider<String> inMaintenance;

	@Override
	public HealthCheckResponse call() {
		if (inMaintenance != null && inMaintenance.get().equalsIgnoreCase("true")) {
			return HealthCheckResponse.down(readinessCheck);
		}
		return HealthCheckResponse.up(readinessCheck);
	}

}

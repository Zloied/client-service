package com.vais.clients.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import com.vais.clients.ApiApplication;

@Liveness
@ApplicationScoped
public class SystemCheckLiveness implements HealthCheck {

	@Override
	public HealthCheckResponse call() {
		MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
		long memUsed = memBean.getHeapMemoryUsage().getUsed();
		long memMax = memBean.getHeapMemoryUsage().getMax();

		return HealthCheckResponse.named(ApiApplication.class.getSimpleName() + "LivenessCheck")
				.withData("memory used", memUsed).withData("memory max", memMax).state(memUsed < memMax * 0.9).build();
	}

}

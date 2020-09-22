package com.vais.clients;

import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;

public class AppDeploymentConfig implements SharedContainerConfiguration {

	@Container
	public static ApplicationContainer appCont = new ApplicationContainer().withAppContextRoot("/")
			.withReadinessPath("/health/ready");
	
}

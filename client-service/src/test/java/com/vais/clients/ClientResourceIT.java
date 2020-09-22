package com.vais.clients;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;

import com.vais.clients.entities.Client;
import com.vais.clients.resources.ClientResource;

@MicroShedTest
@SharedContainerConfig(AppDeploymentConfig.class)
public class ClientResourceIT {

	@RESTClient
	public static ClientResource clientResource;
	
	private static final String CLIENT_NAME = "test";
	private static final String CLIENT_EMAIL = "test@email.com";
	private static final String CLIENT_NAME_SECOND = "testTwo";
	private static final String CLIENT_EMAIL_SECOND = "testTwo@email.com";

	private static final String JSONFIELD_NAME = "name";
	private static final String JSONFIELD_EMAIL = "email";
	private static final String JSONFIELD_ID = "id";
	
	@Test
	public void testCreateCLient() {
		
		Client client = new Client(CLIENT_NAME, CLIENT_EMAIL);
		Response response = clientResource.addClient(client);
		assertEquals(Status.CREATED.getStatusCode(), response.getStatus(), 
				"Creating a client should return HTTP code " + Status.CREATED.getStatusCode());
		assertEquals(Status.BAD_REQUEST.getStatusCode(), clientResource.addClient(client).getStatus(),
				"Creating a client with the same email should return HTTP response code " + Status.BAD_REQUEST.getStatusCode());
	}
	
	@Test
	public void testGetClient() {
		Long clientId = clientResource.addClient(new Client(CLIENT_NAME, CLIENT_EMAIL)).readEntity(new GenericType<Client>() {}).getId(); 
		JsonObject client = clientResource.getClient(clientId);
		
		assertEquals(CLIENT_NAME, client.getString(JSONFIELD_NAME));
		assertEquals(CLIENT_EMAIL, client.getString(JSONFIELD_EMAIL));
		assertNotNull(client.get(JSONFIELD_ID));
	}
	
	@Test
	public void testGetAllClients() {
		clientResource.addClient(new Client(CLIENT_NAME, CLIENT_EMAIL));
		clientResource.addClient(new Client(CLIENT_NAME_SECOND, CLIENT_EMAIL_SECOND));
		
		Map<String, String> clients = clientResource.getClients().stream()
				.collect(Collectors.toMap(e -> e.asJsonObject().getString(JSONFIELD_EMAIL), e -> e.asJsonObject().getString(JSONFIELD_NAME)));
		
		assertTrue(clients.size() >= 2, "Excpected at least 2 clients to be registered, but threre were only: " + clients);
		assertTrue(clients.containsKey(CLIENT_EMAIL_SECOND), 
				"Did not find client " + CLIENT_EMAIL_SECOND + " in all clients: " + clients);
		assertTrue(clients.containsKey(CLIENT_EMAIL), "Did not find client " + CLIENT_EMAIL + " in all clients: " + clients);		
	}
	
	@Test
	public void testUpdateName() {
		
		Client client = new Client(CLIENT_NAME, CLIENT_EMAIL);
		Long clientId = clientResource.addClient(client).readEntity(new GenericType<Client>() {}).getId();
		
		JsonObject originalClient = clientResource.getClient(clientId);
		assertEquals(CLIENT_NAME, originalClient.getString(JSONFIELD_NAME));
		assertEquals(CLIENT_EMAIL, originalClient.getString(JSONFIELD_EMAIL));
		assertEquals(clientId, Long.valueOf(originalClient.getInt(JSONFIELD_ID)));
		
		clientResource.updateClient(clientId, new Client(CLIENT_NAME_SECOND, CLIENT_EMAIL_SECOND));
		JsonObject updatedClient = clientResource.getClient(clientId);
		assertEquals(CLIENT_NAME_SECOND, updatedClient.getString(JSONFIELD_NAME));
		assertEquals(CLIENT_EMAIL, updatedClient.getString(JSONFIELD_EMAIL));
		assertEquals(clientId, Long.valueOf(updatedClient.getInt(JSONFIELD_ID)));
	}
	
	@AfterEach
	public void clearTestData() {
		if(!clientResource.findClientByEmail(CLIENT_EMAIL).isEmpty()) {
			Long foundId = Long.valueOf(clientResource.findClientByEmail(CLIENT_EMAIL).getInt(JSONFIELD_ID));
			if(foundId != null) {
				clientResource.deleteClient(foundId);
			}
		}
		
		if(!clientResource.findClientByEmail(CLIENT_EMAIL_SECOND).isEmpty()) {
			Long foundId = Long.valueOf(clientResource.findClientByEmail(CLIENT_EMAIL_SECOND).getInt(JSONFIELD_ID));
			if(foundId != null) {
				clientResource.deleteClient(foundId);
			}
		}
	}
}

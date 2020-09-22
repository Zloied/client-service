package com.vais.clients.resources;

import java.util.Enumeration;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.vais.clients.dao.ClientDao;
import com.vais.clients.entities.Client;

@RequestScoped
@Path("clients")
public class ClientResource {

	@Inject
	ClientDao clientDao;

	private static final String JSONFIELD_NAME = "name";
	private static final String JSONFIELD_EMAIL = "email";
	private static final String JSONFIELD_ID = "id";

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	@APIResponses(value = {
			@APIResponse(responseCode = "201", description = "Confirmation of client creation", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Client.class))),
			@APIResponse(responseCode = "400", description = "Unsuccsessful creation operation with error description", content = @Content(mediaType = MediaType.APPLICATION_JSON)) })
	@Operation(summary = "Create new client in database", description = "Retrieves information about creation result")
	public Response addClient(@Valid Client client) {

		if (client == null || client.getName() == null || client.getEmail() == null
				|| client.getName().length() >= 30 || client.getEmail().length() >= 50) {
			return Response.status(Response.Status.BAD_REQUEST).entity("{ \"error\" : " + "\"Users name or email can not be empty\"" + "}").build();
		}
		if (!clientDao.findClients(client.getEmail()).isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("{ \"error\" : " + "\"User with the same email already exists\"" + "}")
					.build();
		}

		clientDao.createClient(client);
		return Response.status(Response.Status.CREATED).type(MediaType.APPLICATION_JSON).entity(client).build();
	}

	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	@APIResponses(value = {
			@APIResponse(responseCode = "204", description = "Confirmation about successful client update without content"),
			@APIResponse(responseCode = "400", description = "Unsuccsessful creation operation with error description", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@APIResponse(responseCode = "404", description = "Client with defined id wasn't found", content = @Content(mediaType=MediaType.APPLICATION_JSON))
	})
	@Operation(summary = "Update client data in database ", description = "Retrieves information about update result")
	public Response updateClient(@PathParam("id") Long id, @Valid Client client) {

		Client presClient = clientDao.getClient(id);
		if (presClient == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("{ \"error\" : " + "\"User does not exist\"" + "}").build();
		}
		if (client == null || client.getName() == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("{ \"error\" : " + "\"Client's name can not be empty\"" + "}").build();
		}
		presClient.setName(client.getName());

		clientDao.updateClient(presClient);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Client object found by defined id", content = @Content(mediaType=MediaType.APPLICATION_JSON)),
	})
	@Operation(summary = "Retrieves client by id", description = "Retrieves client object in json format")
	public JsonObject getClient(@PathParam("id") Long clientId) {

		JsonObjectBuilder builder = Json.createObjectBuilder();
		Client client = clientDao.getClient(clientId);
		if (client != null) {
			builder.add(JSONFIELD_NAME, client.getName()).add(JSONFIELD_EMAIL, client.getEmail()).add(JSONFIELD_ID,
					client.getId());
		}
		return builder.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Retrieves all client from database as list of json objects", content = @Content(mediaType=MediaType.APPLICATION_JSON))
	})
	@Operation(summary = "Retrieves all clients", description = "Retrieves all client in json format")
	public JsonArray getClients() {

		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonArrayBuilder finalArray = Json.createArrayBuilder();
		clientDao.getAllClients().forEach(client -> {
			builder.add(JSONFIELD_NAME, client.getName()).add(JSONFIELD_EMAIL, client.getEmail()).add(JSONFIELD_ID,
					client.getId());
			finalArray.add(builder.build());
		});
		return finalArray.build();
	}

	@GET
	@Path("search/{email}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Client object found by matching email", content = @Content(mediaType=MediaType.APPLICATION_JSON))
	})
	@Operation(summary = "Retrieves client with matching email", description = "Retrieves client object in json format")
	public JsonObject findClientByEmail(@PathParam("email") @Size(min = 2, max = 50) String email) {

		JsonObjectBuilder builder = Json.createObjectBuilder();
		List<Client> clients = clientDao.findClients(email);

		if (clients != null && clients.size() > 0) {
			Client client = clients.get(0);
			builder.add(JSONFIELD_NAME, client.getName()).add(JSONFIELD_EMAIL, client.getEmail()).add(JSONFIELD_ID,
					client.getId());
		}
		return builder.build();
	}

	@DELETE
	@Path("{id}")
	@Transactional
	@APIResponses(value = {
			@APIResponse(responseCode = "204", description = "Confirmation about successful deletion without content"),
			@APIResponse(responseCode = "404", description = "Client with defined id wasn't found", content = @Content(mediaType=MediaType.APPLICATION_JSON))
	})
	@Operation(summary = "Delete client in database ", description = "Retrieves information about delete result")
	public Response deleteClient(@PathParam("id") Long clientId) {
		Client client = clientDao.getClient(clientId);
		if (client == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("{ \"error\" : " + "\"User does not exist\"" + "}" ).build();
		}
		clientDao.deleteClient(client);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	@POST
	@Path("/session")
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Call for demostration purpuse. Puts recieved user's data to current session")
	})
	@Operation(summary = "Put data to current session ", description = "Retrieves information operation result")
	public Response addToCart(@Context HttpServletRequest request, @Valid Client client) {
		HttpSession session = request.getSession();
		session.setAttribute(JSONFIELD_NAME, client.getName());
		session.setAttribute(JSONFIELD_EMAIL, client.getEmail());
		return Response.status(Response.Status.OK).entity("Client was addet to a cart").build();
	}

	@GET
	@Path("/session")
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Call for demostration purpuse. Returns data stored in current session")
	})
	@Operation(summary = "Retrieve data from session", description = "Retrieves data stored in session")
	public JsonObject checkCart(@Context HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		Enumeration<String> names = session.getAttributeNames();
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("pod-name", getHostname());
		builder.add("session-id", session.getId());
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String parameter = session.getAttribute(name).toString();
			arrayBuilder.add(name + " | " + parameter);
		}
		builder.add("data", arrayBuilder);
		return builder.build();
	}

	private String getHostname() {
		String hostname = System.getenv("HOSTNAME");
		if (hostname == null)
			hostname = "localhost";
		return hostname;
	}
}

package com.vais.clients.dao;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.vais.clients.entities.Client;

@RequestScoped
public class ClientDao {

	@PersistenceContext(name = "jpa-unit")
	private EntityManager em;

	public void createClient(Client client) {
		em.persist(client);
	}

	public Client getClient(Long id) {
		return em.find(Client.class, id);
	}

	public void updateClient(Client client) {
		em.merge(client);
	}

	public void deleteClient(Client client) {
		em.remove(client);
	}

	public List<Client> findClients(String email) {
		return em.createNamedQuery("clients.findClient", Client.class).setParameter("email", email).getResultList();
	}

	public List<Client> getAllClients() {
		return em.createNamedQuery("clients.findAll", Client.class).getResultList();
	}
}

package com.clearinghouse.dao;

import jakarta.persistence.EntityManager;
import org.mockito.Mockito;

public class AbstractDAOTestSetup {

    protected EntityManager mockEntityManager;

    public AbstractDAOTestSetup() {
        this.mockEntityManager = Mockito.mock(EntityManager.class);
    }

    protected EntityManager getMockEntityManager() {
        return this.mockEntityManager;
    }
}
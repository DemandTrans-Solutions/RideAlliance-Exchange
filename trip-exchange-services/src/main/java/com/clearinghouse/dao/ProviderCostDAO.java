package com.clearinghouse.dao;

import com.clearinghouse.entity.ProviderCost;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shankarI
 */
@Repository
public class ProviderCostDAO extends AbstractDAO<Integer, ProviderCost> {


    public List<ProviderCost> findAllProvidersCost() {
        List<ProviderCost> providersCost = getEntityManager()
                .createQuery("SELECT p FROM ProviderCost p order by p.providerCostId ").getResultList();
        return providersCost;
    }


    public ProviderCost findCostByProviderId(int providerId) {
        try {
            ProviderCost providerCostByProviderId = (ProviderCost) getEntityManager()
                    .createQuery(" SELECT p FROM ProviderCost p WHERE (p.provider.providerId=:providerId )")
                    .setParameter("providerId", providerId).getSingleResult();

            return providerCostByProviderId;
        } catch (NoResultException e) {
            return null;
        }
    }


    public ProviderCost createProviderCost(ProviderCost providerCostBO) {
        add(providerCostBO);
        return providerCostBO;
    }


    public ProviderCost updateProviderCost(ProviderCost providerCostBO) {
        update(providerCostBO);
        return providerCostBO;
    }

}

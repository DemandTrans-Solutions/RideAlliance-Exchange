package com.clearinghouse.dao;

import com.clearinghouse.entity.FundingSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shankar I
 */

@Repository
public class FundingSourceDAO extends AbstractDAO<Integer, FundingSource> {

    @PersistenceContext
    EntityManager entityManager;


    public List<FundingSource> getAllFundingSources() {

        Query query = entityManager.createNativeQuery(
                "SELECT f.*," +
                        "(CASE WHEN f.Name IN ('Other','OTHER','other') THEN 0 ELSE 1 END) AS sorted " +
                        " FROM fundingsource f " +
                        " ORDER BY sorted DESC , f.Name ASC ");

        List<Object[]> result = query.getResultList();

        List<FundingSource> fundingSourcesList = new ArrayList<FundingSource>();

        for (Object[] obj : result) {
            FundingSource fundingSource = new FundingSource();

            fundingSource.setFundingSourceId((int) obj[0]);
            fundingSource.setName((String) obj[1]);
            fundingSource.setDescription((String) obj[2]);
            fundingSource.setStatus((boolean) obj[3]);

            fundingSourcesList.add(fundingSource);
        }

        return fundingSourcesList;
    }


    public FundingSource createFundingSource(FundingSource fundingSource) {
        add(fundingSource);
        return fundingSource;
    }


    public boolean findFundingSourceByName(String name) {

        boolean checkStatus = false;

        long query = (long) getEntityManager()
                .createQuery("SELECT count(f.name) FROM FundingSource f WHERE f.name=:name").setParameter("name", name)
                .getSingleResult();

        if (query > 0) {
            checkStatus = true;
        }
        return checkStatus;
    }


    public FundingSource findFundingSourceById(int fundingSourceId) {
        FundingSource fundingSource = getByKey(fundingSourceId);
        return fundingSource;
    }


    public FundingSource updateFundingSource(FundingSource fundingSource) {
        return update(fundingSource);
    }


    public FundingSource activateFundingSource(int fundingSourceId) {
        FundingSource fundingSource = getByKey(fundingSourceId);
        return fundingSource;
    }


    public FundingSource deactivateFundingSource(int fundingSourceId) {
        FundingSource fundingSource = getByKey(fundingSourceId);
        return fundingSource;
    }

}

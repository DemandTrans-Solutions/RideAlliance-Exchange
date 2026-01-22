package com.clearinghouse.dao;

import com.clearinghouse.entity.HospitalityAreaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class HospitalityAreaProviderDAO extends AbstractDAO<Integer, HospitalityAreaProvider> {


    public HospitalityAreaProvider findById(int id) {
        return getByKey(id);
    }


    public HospitalityAreaProvider create(HospitalityAreaProvider hospitalityAreaProvider) {
        add(hospitalityAreaProvider);
        return hospitalityAreaProvider;
    }


}

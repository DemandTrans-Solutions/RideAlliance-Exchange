package com.clearinghouse.dao;

import com.clearinghouse.entity.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class StatusDAO extends AbstractDAO<Integer, Status> {


    public Status findStatusById(int statusId) {
        return getByKey(statusId);
    }


}

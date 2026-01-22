/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.ApplicationSetting;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class ApplicationSettingDAO extends AbstractDAO<Integer, ApplicationSetting> {


    public List<ApplicationSetting> findAllApplicationSettings() {

        List<ApplicationSetting> applicationSettings = getEntityManager()
                .createQuery("SELECT ast FROM ApplicationSetting ast")
                .getResultList();
        return applicationSettings;
    }


    public ApplicationSetting findApplicationSettingById(int applicationSettingid) {

        return getByKey(applicationSettingid);
    }


    public ApplicationSetting updateApplicationSetting(ApplicationSetting applicationSetting) {

        ApplicationSetting applicationSettingUpdated = update(applicationSetting);
        return applicationSettingUpdated;
    }

}

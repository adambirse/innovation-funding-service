package com.worth.ifs.application.service;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.user.domain.UserRoleType;

import java.util.List;

/**
 * Interface for CRUD operations on {@link Application} related data.
 */
public interface ApplicationRestService {
    public Application getApplicationById(Long applicationId);
    public List<Application> getApplicationsByUserId(Long userId);
    public List<Application> getApplicationsByCompetitionIdAndUserId(Long competitionID, Long userId, UserRoleType role);
    public void saveApplication(Application application);
    public void updateApplicationStatus(Long applicationId, Long statusId);
    public Double getCompleteQuestionsPercentage(Long applicationId);
}

package org.innovateuk.ifs.interview.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.invite.resource.*;

import java.util.List;

/**
 * REST service for managing to interview panels
 */
public interface InterviewAssignmentRestService {

    RestResult<AvailableApplicationPageResource> getAvailableApplications(long competitionId, int page);

    RestResult<List<Long>> getAvailableApplicationIds(long competitionId);

    RestResult<Void> assignApplications(StagedApplicationListResource stagedApplicationListResource);

    RestResult<InterviewAssignmentStagedApplicationPageResource> getStagedApplications(long competitionId, int page);

    RestResult<Void> unstageApplication(long applicationId);

    RestResult<Void> unstageApplications(long competitionId);

    RestResult<ApplicantInterviewInviteResource> getEmailTemplate();

    RestResult<Void> sendAllInvites(long competitionId, AssessorInviteSendResource assessorInviteSendResource);
}
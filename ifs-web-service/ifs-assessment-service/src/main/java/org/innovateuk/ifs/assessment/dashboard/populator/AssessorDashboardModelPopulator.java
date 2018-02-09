package org.innovateuk.ifs.assessment.dashboard.populator;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.assessment.dashboard.viewmodel.*;
import org.innovateuk.ifs.assessment.profile.viewmodel.AssessorProfileStatusViewModel;
import org.innovateuk.ifs.assessment.service.ReviewPanelInviteRestService;
import org.innovateuk.ifs.assessment.service.CompetitionParticipantRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.invite.resource.AssessmentReviewPanelParticipantResource;
import org.innovateuk.ifs.invite.resource.CompetitionParticipantResource;
import org.innovateuk.ifs.invite.resource.CompetitionParticipantRoleResource;
import org.innovateuk.ifs.profile.service.ProfileRestService;
import org.innovateuk.ifs.user.resource.UserProfileStatusResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Build the model for the Assessor Panel Dashboard view.
 */
@Component
public class AssessorDashboardModelPopulator {

    @Autowired
    private CompetitionParticipantRestService competitionParticipantRestService;

    @Autowired
    private ProfileRestService profileRestService;

    @Autowired
    private ReviewPanelInviteRestService reviewPanelInviteRestService;

    @Autowired
    private CompetitionService competitionService;

    public AssessorDashboardViewModel populateModel(Long userId) {
        List<CompetitionParticipantResource> participantResourceList = competitionParticipantRestService
                .getParticipants(userId, CompetitionParticipantRoleResource.ASSESSOR).getSuccessObject();

        UserProfileStatusResource profileStatusResource = profileRestService.getUserProfileStatus(userId).getSuccessObject();

        List<AssessmentReviewPanelParticipantResource> assessmentReviewPanelParticipantResourceList = reviewPanelInviteRestService.getAllInvitesByUser(userId).getSuccessObject();

        return new AssessorDashboardViewModel(
                getProfileStatus(profileStatusResource),
                getActiveCompetitions(participantResourceList),
                getUpcomingCompetitions(participantResourceList),
                getPendingParticipations(participantResourceList),
                getAssessmentPanelInvites(assessmentReviewPanelParticipantResourceList),
                getAssessmentPanelAccepted(assessmentReviewPanelParticipantResourceList)
        );
    }

    private AssessorProfileStatusViewModel getProfileStatus(UserProfileStatusResource assessorProfileStatusResource) {
        return new AssessorProfileStatusViewModel(assessorProfileStatusResource);
    }

    private List<AssessorDashboardActiveCompetitionViewModel> getActiveCompetitions(List<CompetitionParticipantResource> participantResourceList) {
        return participantResourceList.stream()
                .filter(CompetitionParticipantResource::isAccepted)
                .filter(CompetitionParticipantResource::isInAssessment)
                .map(cpr -> new AssessorDashboardActiveCompetitionViewModel(
                        cpr.getCompetitionId(),
                        cpr.getCompetitionName(),
                        cpr.getSubmittedAssessments(),
                        cpr.getTotalAssessments(),
                        cpr.getPendingAssessments(),
                        cpr.getAssessorDeadlineDate().toLocalDate(),
                        cpr.getAssessmentDaysLeft(),
                        cpr.getAssessmentDaysLeftPercentage()
                ))
                .collect(toList());
    }

    private List<AssessorDashboardUpcomingCompetitionViewModel> getUpcomingCompetitions(List<CompetitionParticipantResource> participantResources) {
        return participantResources.stream()
                .filter(CompetitionParticipantResource::isAccepted)
                .filter(CompetitionParticipantResource::isAnUpcomingAssessment)
                .map(p -> new AssessorDashboardUpcomingCompetitionViewModel(
                        p.getCompetitionId(),
                        p.getCompetitionName(),
                        p.getAssessorAcceptsDate().toLocalDate(),
                        p.getAssessorDeadlineDate().toLocalDate()
                ))
                .collect(toList());
    }

    private List<AssessorDashboardPendingInviteViewModel> getPendingParticipations(List<CompetitionParticipantResource> participantResourceList) {
        return participantResourceList.stream()
                .filter(CompetitionParticipantResource::isPending)
                .map(cpr -> new AssessorDashboardPendingInviteViewModel(
                        cpr.getInvite().getHash(),
                        cpr.getCompetitionName(),
                        cpr.getAssessorAcceptsDate().toLocalDate(),
                        cpr.getAssessorDeadlineDate().toLocalDate()
                ))
                .collect(toList());
    }

    private List<AssessorDashboardAssessmentPanelInviteViewModel> getAssessmentPanelInvites(List<AssessmentReviewPanelParticipantResource> assessmentReviewPanelParticipantResourceList) {
        return assessmentReviewPanelParticipantResourceList.stream()
                .filter(AssessmentReviewPanelParticipantResource::isPending)
                .filter(appr -> !isAfterPanelDate(appr.getCompetitionId()))
                .map(appr -> new AssessorDashboardAssessmentPanelInviteViewModel(
                        appr.getCompetitionName(),
                        appr.getCompetitionId(),
                        appr.getInvite().getHash()
                        ))
                .collect(toList());
    }

    private List<AssessorDashboardAssessmentPanelAcceptedViewModel> getAssessmentPanelAccepted(List<AssessmentReviewPanelParticipantResource> assessmentPanelAcceptedResourceList) {
        return assessmentPanelAcceptedResourceList.stream()
                .filter(AssessmentReviewPanelParticipantResource::isAccepted)
                .filter(appr -> !isAfterPanelDate(appr.getCompetitionId()))
                .map(appr -> new AssessorDashboardAssessmentPanelAcceptedViewModel(
                        appr.getCompetitionName(),
                        appr.getCompetitionId(),
                        appr.getInvite().getPanelDate().toLocalDate(),
                        appr.getInvite().getPanelDaysLeft(),
                        appr.getAwaitingApplications()
                        ))
                .collect(toList());
    }

    private boolean isAfterPanelDate(long competitionId) {
        CompetitionResource competition = competitionService.getById(competitionId);
        ZonedDateTime panelDate = competition.getFundersPanelDate();

        return ZonedDateTime.now().plusDays(1L).isAfter(panelDate);
    }
}

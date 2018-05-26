package org.innovateuk.ifs.assessment.security;

import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.competition.resource.CompetitionCompositeId;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.util.SecurityRuleUtil.isInternalAdmin;

@PermissionRules
@Component
public class AssessmentPermissionRules {

    @Autowired
    private CompetitionRestService competitionRestService;

    @PermissionRule(value = "ASSESSMENT", description = "Only project finance or competition admin can see assessment panels" +
            "if the competition is in the correct state.")
    public boolean interviewPanel(CompetitionCompositeId competitionCompositeId, UserResource loggedInUser) {
        CompetitionResource competition = competitionRestService.getCompetitionById(competitionCompositeId.id()).getSuccess();
        return isInternalAdmin(loggedInUser) &&
                competitionHasAssessmentPanel(competition) &&
                !competitionIsInInform(competition);
    }

    @PermissionRule(value = "ASSESSMENT_APPLICATIONS", description = "Only project finance or competition admin can " +
            "see assessment panel applications if the competition is in the correct state.")
    public boolean interviewPanelApplications(CompetitionCompositeId competitionCompositeId, UserResource loggedInUser) {
        CompetitionResource competition = competitionRestService.getCompetitionById(competitionCompositeId.id()).getSuccess();
        return isInternalAdmin(loggedInUser) &&
                competitionHasAssessmentPanel(competition) &&
                competitionIsInFundersPanel(competition);
    }

    private boolean competitionHasAssessmentPanel(CompetitionResource competition) {
        return competition.isHasAssessmentPanel();
    }

    private boolean competitionIsInFundersPanel(CompetitionResource competition) {
        return competition.getCompetitionStatus().equals(CompetitionStatus.FUNDERS_PANEL);
    }

    private boolean competitionIsInInform(CompetitionResource competition) {
        return competition.getCompetitionStatus().equals(CompetitionStatus.ASSESSOR_FEEDBACK);
    }
}
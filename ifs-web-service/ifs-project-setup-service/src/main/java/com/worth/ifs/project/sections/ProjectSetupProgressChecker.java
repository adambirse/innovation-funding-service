package com.worth.ifs.project.sections;

import com.worth.ifs.project.resource.ProjectPartnerStatusResource;
import com.worth.ifs.project.resource.ProjectTeamStatusResource;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.OrganisationTypeEnum;

import static com.worth.ifs.project.constant.ProjectActivityStates.COMPLETE;
import static com.worth.ifs.project.constant.ProjectActivityStates.PENDING;
import static com.worth.ifs.util.CollectionFunctions.simpleFindFirst;

/**
 * Component to check the progress of Project Setup.  This is used by the {@link ProjectSetupSectionPartnerAccessor} to
 * determine which sections are available at a given time
 */
class ProjectSetupProgressChecker {

    private ProjectTeamStatusResource projectTeamStatus;

    public ProjectSetupProgressChecker(ProjectTeamStatusResource projectTeamStatus) {
        this.projectTeamStatus = projectTeamStatus;
    }

    public boolean isCompaniesHouseDetailsComplete(OrganisationResource organisation) {
        return COMPLETE.equals(getMatchingPartnerStatus(organisation).getCompaniesHouseStatus());
    }

    public boolean isBusinessOrganisationType(OrganisationResource organisation) {
        return !OrganisationTypeEnum.isResearch(organisation.getOrganisationType());
    }

    public boolean isProjectDetailsSectionComplete() {
        return COMPLETE.equals(projectTeamStatus.getLeadPartnerStatus().getProjectDetailsStatus());
    }

    public boolean isFinanceContactSubmitted(OrganisationResource organisation) {
        return COMPLETE.equals(getMatchingPartnerStatus(organisation).getFinanceContactStatus());
    }

    public boolean isBankDetailsApproved(OrganisationResource organisation) {
        return COMPLETE.equals(getMatchingPartnerStatus(organisation).getBankDetailsStatus());
    }

    public boolean isBankDetailsQueried(OrganisationResource organisation) {
        return PENDING.equals(getMatchingPartnerStatus(organisation).getBankDetailsStatus());
    }

    public boolean isSpendProfileGenerated() {
        return COMPLETE.equals(projectTeamStatus.getLeadPartnerStatus().getSpendProfileStatus());
    }

    private ProjectPartnerStatusResource getMatchingPartnerStatus(OrganisationResource organisation) {
        return simpleFindFirst(projectTeamStatus.getPartnerStatuses(), status -> status.getOrganisationId().equals(organisation.getId())).get();
    }
}

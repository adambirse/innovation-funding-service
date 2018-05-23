package org.innovateuk.ifs.assessment.dashboard.controller;

import org.innovateuk.ifs.assessment.dashboard.populator.AssessorCompetitionForInterviewDashboardModelPopulator;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to manage interviews of Applications
 */
@Controller
@RequestMapping(value = "/assessor")
@SecuredBySpring(value = "Controller", description = "Assessors can access assessment interviews", securedType = AssessorCompetitionForInterviewDashboardController.class)
@PreAuthorize("hasAuthority('assessor')")
public class AssessorCompetitionForInterviewDashboardController {

    @Autowired
    private AssessorCompetitionForInterviewDashboardModelPopulator assessorCompetitionForInterviewDashboardModelPopulator;

    @GetMapping("/dashboard/competition/{competitionId}/interview")
    public String viewApplications( Model model,
                                    @PathVariable("competitionId") long competitionId,
                                    UserResource loggedInUser
                                ) {
        model.addAttribute("model", assessorCompetitionForInterviewDashboardModelPopulator.populateModel(competitionId, loggedInUser.getId()));
        return "assessor-interview-applications";
    }

}
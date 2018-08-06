package org.innovateuk.ifs.project.spendprofile.controller;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.ApplicationSummaryRestService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.controller.CaseInsensitiveConverter;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.spendprofile.form.ProjectSpendProfileApprovalForm;
import org.innovateuk.ifs.project.spendprofile.viewmodel.ProjectSpendProfileApprovalViewModel;
import org.innovateuk.ifs.spendprofile.SpendProfileService;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * This Controller handles Spend Profile activity for the Internal Competition team members
 */
@Controller
@RequestMapping("/project/{projectId}/spend-profile")
public class ProjectSpendProfileApprovalController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationSummaryRestService applicationSummaryRestService;

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private UserService userService;

    @Autowired
    private SpendProfileService spendProfileService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(ApprovalType.class, new CaseInsensitiveConverter<>(ApprovalType.class));
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_SPEND_PROFILE_SECTION')")
    @GetMapping("/approval")
    public String viewSpendProfileApproval(@P("projectId")@PathVariable Long projectId, Model model) {
        return doViewSpendProfileApproval(projectId, model);
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_SPEND_PROFILE_SECTION')")
    @PostMapping("/approval/{approvalType}")
    public String saveSpendProfileApproval(@P("projectId")@PathVariable Long projectId,
                                           @PathVariable ApprovalType approvalType,
                                           @ModelAttribute ProjectSpendProfileApprovalForm form,
                                           Model model,
                                           @SuppressWarnings("unused") BindingResult bindingResult,
                                           ValidationHandler validationHandler) {
        Supplier<String> failureView = () -> doViewSpendProfileApproval(projectId, model);
        ServiceResult<Void> generateResult = spendProfileService.approveOrRejectSpendProfile(projectId, approvalType);

        return validationHandler.addAnyErrors(generateResult).failNowOrSucceedWith(failureView, () ->
                redirectToCompetitionSummaryPage(projectId)
        );
    }

    private String doViewSpendProfileApproval(Long projectId, Model model) {
        ProjectSpendProfileApprovalViewModel viewModel = populateSpendProfileApprovalViewModel(projectId);

        model.addAttribute("model", viewModel);

        return "project/finance/spend-profile/approval";
    }

    private ProjectSpendProfileApprovalViewModel populateSpendProfileApprovalViewModel(Long projectId) {
        ProjectResource project = projectService.getById(projectId);
        ApplicationResource application = applicationService.getById(project.getApplication());
        CompetitionSummaryResource competitionSummary = applicationSummaryRestService.getCompetitionSummary(application.getCompetition()).getSuccess();
        CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();
        String leadTechnologist = competition.getLeadTechnologist() != null ? userService.findById(competition.getLeadTechnologist()).getName() : "";
        ApprovalType approvalType = spendProfileService.getSpendProfileStatusByProjectId(projectId);

        List<OrganisationResource> organisationResources = projectService.getPartnerOrganisationsForProject(projectId);

        return new ProjectSpendProfileApprovalViewModel(competitionSummary, leadTechnologist, approvalType, organisationResources);
    }

    private String redirectToCompetitionSummaryPage(Long projectId) {
        ProjectResource project = projectService.getById(projectId);
        ApplicationResource application = applicationService.getById(project.getApplication());
        CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();

        return "redirect:/competition/" + competition.getId() + "/status";
    }
}

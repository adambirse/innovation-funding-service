package org.innovateuk.ifs.project.projectdetails.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.resource.ProjectUserInviteResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.builder.PartnerOrganisationResourceBuilder;
import org.innovateuk.ifs.project.constant.ProjectActivityStates;
import org.innovateuk.ifs.project.projectdetails.viewmodel.LegacyProjectDetailsViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectUserInviteModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.SelectFinanceContactViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.SelectProjectManagerViewModel;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.service.PartnerOrganisationRestService;
import org.innovateuk.ifs.project.status.populator.SetupStatusViewModelPopulator;
import org.innovateuk.ifs.project.status.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.projectdetails.ProjectDetailsService;
import org.innovateuk.ifs.status.StatusService;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.innovateuk.ifs.user.service.UserRestService;
import org.innovateuk.ifs.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.invite.builder.ProjectUserInviteResourceBuilder.newProjectUserInviteResource;
import static org.innovateuk.ifs.invite.constant.InviteStatus.CREATED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.project.builder.ProjectPartnerStatusResourceBuilder.newProjectPartnerStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectTeamStatusResourceBuilder.newProjectTeamStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectUserInviteStatus.PENDING;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.PARTNER;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LegacyProjectDetailsControllerTest extends BaseControllerMockMVCTest<LegacyProjectDetailsController> {
    private static final String SAVE_FC = "save-fc";
    private static final String INVITE_FC = "invite-fc";
    private static final String SAVE_PM = "save-pm";
    private static final String INVITE_PM = "invite-pm";
    private static final String RESEND_FC_INVITE = "resend-fc-invite";
    private static final String RESEND_PM_INVITE = "resend-pm-invite";

    @Mock
    private SetupStatusViewModelPopulator setupStatusViewModelPopulatorMock;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private CompetitionRestService competitionRestService;

    @Mock
    private ProjectService projectService;

    @Mock
    private PartnerOrganisationRestService partnerOrganisationRestService;

    @Mock
    private StatusService statusService;

    @Mock
    private OrganisationRestService organisationRestService;

    @Mock
    private ProjectDetailsService projectDetailsService;

    @Mock
    private UserService userService;

    @Mock
    private UserRestService userRestService;

    @Override
    protected LegacyProjectDetailsController supplyControllerUnderTest() {
        return new LegacyProjectDetailsController();
    }

    @Test
    public void testProjectDetails() throws Exception {
        Long projectId = 20L;

        boolean partnerProjectLocationRequired = true;
        CompetitionResource competitionResource = newCompetitionResource()
                .withLocationPerPartner(partnerProjectLocationRequired)
                .build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withIsLeadPartner(true)
                        .withMonitoringOfficerStatus(ProjectActivityStates.NOT_STARTED)
                        .withSpendProfileStatus(ProjectActivityStates.PENDING)
                        .withGrantOfferStatus(ProjectActivityStates.NOT_REQUIRED).build()).
                build();

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        List<PartnerOrganisationResource> partnerOrganisationResourceList = PartnerOrganisationResourceBuilder.newPartnerOrganisationResource().build(3);
        when(partnerOrganisationRestService.getProjectPartnerOrganisations(projectId)).thenReturn(restSuccess(partnerOrganisationResourceList));

        when(statusService.getProjectTeamStatus(projectId, Optional.empty())).thenReturn(teamStatus);
        when(setupStatusViewModelPopulatorMock.checkLeadPartnerProjectDetailsProcessCompleted(teamStatus, partnerProjectLocationRequired)).thenReturn(true);

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));

        MvcResult result = mockMvc.perform(get("/project/{id}/details", projectId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/legacy-details"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();

        LegacyProjectDetailsViewModel model = (LegacyProjectDetailsViewModel) result.getModelAndView().getModel().get("model");
        assertEquals(applicationResource, model.getApp());
        assertEquals(competitionResource, model.getCompetition());
        assertEquals(project, model.getProject());
        assertEquals(singletonList(leadOrganisation), model.getOrganisations());
        assertEquals(null, model.getProjectManager());
        assertTrue(model.isAllProjectDetailsFinanceContactsAndProjectLocationsAssigned());
        assertTrue(model.isUserLeadPartner());
        assertFalse(model.isMonitoringOfficerAssigned());
        assertTrue(model.isSpendProfileGenerated());
        assertFalse(model.isReadOnly());
        assertFalse(model.isGrantOfferLetterGenerated());
    }

    @Test
    public void testProjectDetailsReadOnlyView() throws Exception {
        Long projectId = 20L;

        boolean partnerProjectLocationRequired = true;
        CompetitionResource competitionResource = newCompetitionResource()
                .withLocationPerPartner(partnerProjectLocationRequired)
                .build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withIsLeadPartner(true)
                        .withMonitoringOfficerStatus(ProjectActivityStates.NOT_STARTED)
                        .build()).
                build();

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        List<PartnerOrganisationResource> partnerOrganisationResourceList = PartnerOrganisationResourceBuilder.newPartnerOrganisationResource().build(3);
        when(partnerOrganisationRestService.getProjectPartnerOrganisations(projectId)).thenReturn(restSuccess(partnerOrganisationResourceList));

        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        when(statusService.getProjectTeamStatus(projectId, Optional.empty())).thenReturn(teamStatus);

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));

        MvcResult result = mockMvc.perform(get("/project/{id}/readonly", projectId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/legacy-details"))
                .andReturn();

        LegacyProjectDetailsViewModel model = (LegacyProjectDetailsViewModel) result.getModelAndView().getModel().get("model");
        Boolean readOnlyView = (Boolean) result.getModelAndView().getModel().get("readOnlyView");

        assertEquals(applicationResource, model.getApp());
        assertEquals(competitionResource, model.getCompetition());
        assertEquals(project, model.getProject());
        assertEquals(singletonList(leadOrganisation), model.getOrganisations());
        assertEquals(null, model.getProjectManager());
        assertTrue(model.isUserLeadPartner());
        assertFalse(model.isSpendProfileGenerated());
        assertFalse(model.isMonitoringOfficerAssigned());
        assertTrue(model.isReadOnly());
        assertTrue(model.isGrantOfferLetterGenerated());
    }

    @Test
    public void testProjectDetailsProjectManager() throws Exception {
        Long projectId = 20L;

        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        List<ProjectUserInviteResource> invitedUsers = newProjectUserInviteResource().build(2);

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(projectService.getById(projectId)).thenReturn(project);
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(leadOrganisation);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(invitedUsers));
        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));

        List<ProjectUserInviteModel> users = new ArrayList<>();

        List<ProjectUserInviteModel> invites = invitedUsers.stream()
                .filter(invite -> leadOrganisation.getId().equals(invite.getOrganisation()))
                .map(invite -> new ProjectUserInviteModel(PENDING, invite.getName() + " (Pending)", projectId))
                .collect(toList());

        SelectProjectManagerViewModel viewModel = new SelectProjectManagerViewModel(users, invites, project, 1L, applicationResource, competitionResource, false);

        mockMvc.perform(get("/project/{id}/details/project-manager", projectId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name("project/project-manager"));
    }

    @Test
    public void testProjectDetailsSetProjectManager() throws Exception {
        Long projectId = 20L;
        Long projectManagerUserId = 80L;

        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), projectManagerUserId).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(2);

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectDetailsService.updateProjectManager(projectId, projectManagerUserId)).thenReturn(serviceSuccess());

        ProcessRoleResource processRoleResource = new ProcessRoleResource();
        processRoleResource.setUser(projectManagerUserId);
        when(userService.getLeadPartnerOrganisationProcessRoles(applicationResource)).thenReturn(singletonList(processRoleResource));

        when(projectDetailsService.updateProjectManager(projectId, projectManagerUserId)).thenReturn(serviceSuccess());


        mockMvc.perform(post("/project/{id}/details/project-manager", projectId)
                .param(SAVE_PM, INVITE_FC)
                .param("projectManager", projectManagerUserId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/" + projectId + "/details"));

    }

    @Test
    public void testProjectManagerAndAddressCannotBeChangedWhenGOLAlreadyGenerated() throws Exception {
        Long projectId = 20L;

        boolean partnerProjectLocationRequired = true;
        CompetitionResource competitionResource = newCompetitionResource()
                .withLocationPerPartner(partnerProjectLocationRequired)
                .build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().withIsLeadPartner(true).withSpendProfileStatus(ProjectActivityStates.COMPLETE).build()).
                build();

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        List<PartnerOrganisationResource> partnerOrganisationResourceList = PartnerOrganisationResourceBuilder.newPartnerOrganisationResource().build(3);
        when(partnerOrganisationRestService.getProjectPartnerOrganisations(projectId)).thenReturn(restSuccess(partnerOrganisationResourceList));
        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        when(statusService.getProjectTeamStatus(projectId, Optional.empty())).thenReturn(teamStatus);

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));

        MvcResult result = mockMvc.perform(get("/project/{id}/details", projectId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/legacy-details"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();

        LegacyProjectDetailsViewModel model = (LegacyProjectDetailsViewModel) result.getModelAndView().getModel().get("model");
        assertTrue(model.isGrantOfferLetterGenerated());
    }

    @Test
    public void testUpdateFinanceContact() throws Exception {

        long competitionId = 1L;
        long applicationId = 1L;
        long projectId = 1L;
        long organisationId = 1L;
        long loggedInUserId = 1L;
        long invitedUserId = 2L;

        UserResource financeContactUserResource = newUserResource().withId(invitedUserId).withFirstName("First").withLastName("Last").withEmail("test@test.com").build();


        String invitedUserName = "First Last";
        String invitedUserEmail = "test@test.com";

        CompetitionResource competitionResource = newCompetitionResource().withId(competitionId).build();
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withCompetition(competitionId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();

        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);

        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();

        ProjectUserInviteResource projectUserInviteResource = new ProjectUserInviteResource(invitedUserName, invitedUserEmail, projectId);
        projectUserInviteResource.setUser(invitedUserId);
        projectUserInviteResource.setOrganisation(organisationId);
        projectUserInviteResource.setApplicationId(applicationId);
        projectUserInviteResource.setLeadOrganisationId(leadOrganisation.getId());

        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(projectDetailsService.updateFinanceContact(new ProjectOrganisationCompositeId(projectId, organisationId), invitedUserId)).thenReturn(serviceSuccess());
        when(userRestService.retrieveUserById(invitedUserId)).thenReturn(restSuccess(financeContactUserResource));
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(leadOrganisation);
        when(applicationService.getById(applicationId)).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(userService.getOrganisationProcessRoles(applicationResource, organisationId)).thenReturn(emptyList());
        when(projectDetailsService.saveProjectInvite(projectUserInviteResource)).thenReturn(serviceSuccess());
        when(projectDetailsService.inviteFinanceContact(projectId, projectUserInviteResource)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{id}/details/finance-contact", projectId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param(SAVE_FC, INVITE_FC).
                param("organisation", "1").
                param("financeContact", "2")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + projectId + "/details")).
                andReturn();

        verify(projectDetailsService).updateFinanceContact(new ProjectOrganisationCompositeId(projectId, organisationId), invitedUserId);
    }

    @Test
    public void testInviteSelfToProjectManager() throws Exception {

        long loggedInUserId = 1L;
        long projectId = 4L;
        long organisationId = 4L;
        long applicationId = 16L;

        UserResource loggedInUser = newUserResource().withId(loggedInUserId).withFirstName("Steve").withLastName("Smith").withEmail("Steve.Smith@empire.com").build();
        setLoggedInUser(loggedInUser);

        String invitedUserName = "Steve Smith";
        String invitedUserEmail = "Steve.Smith@empire.com";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        OrganisationResource organisationResource = newOrganisationResource().withId(organisationId).build();

        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.isUserLeadPartner(projectResource.getId(), loggedInUser.getId())).thenReturn(false);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(organisationResource);

        mockMvc.perform(post("/project/{id}/details/project-manager", projectId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param(INVITE_PM, INVITE_PM).
                param("name", invitedUserName).
                param("inviteEmail", invitedUserEmail)
        ).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + projectId + "/details")).
                andReturn();

        verify(projectDetailsService, never()).saveProjectInvite(any(ProjectUserInviteResource.class));
        verify(projectDetailsService, never()).inviteProjectManager(Mockito.anyLong(), Mockito.any(ProjectUserInviteResource.class));
    }

    @Test
    public void testInviteSelfToFinanceContact() throws Exception {

        long loggedInUserId = 1L;
        long projectId = 4L;
        long organisationId = 21L;
        long applicationId = 16L;

        UserResource loggedInUser = newUserResource().withId(loggedInUserId).withFirstName("Steve").withLastName("Smith").withEmail("Steve.Smith@empire.com").build();
        setLoggedInUser(loggedInUser);

        String invitedUserName = "Steve Smith";
        String invitedUserEmail = "Steve.Smith@empire.com";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();
        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withCompetition(competitionResource.getId()).build();

        List<ProjectUserInviteResource> existingInvites = newProjectUserInviteResource().withId(2L)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withLeadOrganisation(leadOrganisation.getId()).build(2);

        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));

        mockMvc.perform(post("/project/{id}/details/finance-contact", projectId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param(INVITE_FC, INVITE_FC).
                param("name", invitedUserName).
                param("inviteEmail", invitedUserEmail).
                param("organisation", organisationId + "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/finance-contact")).
                andReturn();

        verify(projectDetailsService, never()).saveProjectInvite(any(ProjectUserInviteResource.class));
        verify(projectDetailsService, never()).inviteFinanceContact(Mockito.anyLong(), Mockito.any(ProjectUserInviteResource.class));
    }

    @Test
    public void testInviteFinanceContactFails() throws Exception {
        long competitionId = 1L;
        long applicationId = 16L;
        long projectId = 4L;
        long organisationId = 21L;
        long loggedInUserId = 1L;
        long invitedUserId = 2L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();
        UserResource financeContactUserResource = newUserResource().withId(invitedUserId).withFirstName("First").withLastName("Last").withEmail("test@test.com").build();
        CompetitionResource competitionResource = newCompetitionResource().withId(competitionId).build();
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withCompetition(competitionId).build();

        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);

        ProjectUserInviteResource createdInvite = newProjectUserInviteResource().withId()
                .withProject(projectId).withName(invitedUserName)
                .withEmail(invitedUserEmail)
                .withOrganisation(organisationId)
                .withLeadOrganisation(leadOrganisation.getId()).build();

        createdInvite.setOrganisation(organisationId);
        createdInvite.setApplicationId(projectResource.getApplication());
        createdInvite.setApplicationId(applicationId);

        List<ProjectUserInviteResource> existingInvites = newProjectUserInviteResource().withId(2L)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withLeadOrganisation(leadOrganisation.getId()).build(2);

        when(userService.findUserByEmail(invitedUserEmail)).thenReturn(Optional.empty());
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(leadOrganisation);
        when(projectDetailsService.saveProjectInvite(createdInvite)).thenReturn(serviceSuccess());
        when(projectDetailsService.inviteFinanceContact(projectId, createdInvite)).thenReturn(serviceSuccess());
        when(projectDetailsService.updateFinanceContact(new ProjectOrganisationCompositeId(projectId, organisationId), invitedUserId)).thenReturn(serviceSuccess());
        when(userRestService.retrieveUserById(invitedUserId)).thenReturn(restSuccess(financeContactUserResource));
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        when(projectDetailsService.inviteFinanceContact(projectId, existingInvites.get(1))).thenReturn(serviceSuccess());
        when(organisationRestService.getOrganisationById(organisationId)).thenReturn(restSuccess(leadOrganisation));
        when(projectDetailsService.saveProjectInvite(any())).thenReturn(serviceSuccess());
        when(competitionRestService.getCompetitionById(competitionId)).thenReturn(restSuccess(competitionResource));
        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);

        InviteStatus testStatus = CREATED;

        mockMvc.perform(post("/project/{id}/details/finance-contact", projectId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param(INVITE_FC, INVITE_FC).
                param("userId", invitedUserId + "").
                param("name", invitedUserName).
                param("email", invitedUserEmail).
                param("financeContact", "-1").
                param("inviteStatus", testStatus.toString()).
                param("organisation", organisationId + "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/finance-contact")).
                andReturn();
    }

    public void testFinanceContactInviteNotYetAccepted() throws Exception {

        long applicationId = 16L;
        long projectId = 4L;
        long organisationId = 21L;
        long loggedInUserId = 1L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@test.com";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).withId(applicationId).build();

        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);

        List<ProjectUserInviteResource> existingInvites = newProjectUserInviteResource().withId(2L)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withStatus(CREATED)
                .withLeadOrganisation(leadOrganisation.getId()).build(2);

        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));

        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);

        MvcResult result = mockMvc.perform(get("/project/{id}/details/finance-contact", projectId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("organisation", String.valueOf(organisationId)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/finance-contact"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();

        SelectFinanceContactViewModel model = (SelectFinanceContactViewModel) result.getModelAndView().getModel().get("model");

        assertEquals("PENDING", model.getInvitedUsers().get(0).getStatus());
    }

    @Test
    public void testFinanceContactInviteAcceptedByInviteeSoNoLongerInInvitesList() throws Exception {

        long applicationId = 16L;
        long projectId = 4L;
        long organisationId = 21L;
        long loggedInUserId = 1L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@test.com";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).withId(applicationId).build();

        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);

        List<ProjectUserInviteResource> existingInvites = newProjectUserInviteResource().withId(2L)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withStatus(OPENED)
                .withLeadOrganisation(leadOrganisation.getId()).build(2);

        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));

        MvcResult result = mockMvc.perform(get("/project/{id}/details/finance-contact", projectId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("organisation", String.valueOf(organisationId)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/finance-contact"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();

        SelectFinanceContactViewModel model = (SelectFinanceContactViewModel) result.getModelAndView().getModel().get("model");

        assertTrue(model.getInvitedUsers().isEmpty());
    }

    @Test
    public void testFinanceContactResend() throws Exception {
        long projectId = 4L;
        long organisationId = 21L;
        long inviteId = 3L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@test.com";

        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();

        List<ProjectUserInviteResource> existingInvites = newProjectUserInviteResource().withId(inviteId)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withStatus(OPENED)
                .withLeadOrganisation(leadOrganisation.getId()).build(1);


        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        mockMvc.perform(post("/project/{id}/details/finance-contact", projectId)
                .param(RESEND_FC_INVITE, "3")
                .param("organisation", "21"))
                .andExpect(status().is3xxRedirection());
        verify(projectDetailsService).getInvitesByProject(projectId);
    }

    @Test
    public void testProjectManagerResend() throws Exception {
        long projectId = 4L;
        long organisationId = 21L;
        long inviteId = 12L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@test.com";

        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();

        List<ProjectUserInviteResource> existingInvites = newProjectUserInviteResource().withId(inviteId)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withStatus(OPENED)
                .withLeadOrganisation(leadOrganisation.getId()).build(1);


        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        mockMvc.perform(post("/project/{id}/details/project-manager", projectId)
                .param(RESEND_PM_INVITE, "12"))
                .andExpect(status().is3xxRedirection());
        verify(projectDetailsService).getInvitesByProject(projectId);
    }

}


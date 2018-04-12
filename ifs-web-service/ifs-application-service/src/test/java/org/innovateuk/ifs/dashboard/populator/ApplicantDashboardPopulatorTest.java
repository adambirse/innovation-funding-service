package org.innovateuk.ifs.dashboard.populator;

import org.innovateuk.ifs.BaseUnitTest;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.dashboard.viewmodel.ApplicantDashboardViewModel;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.innovateuk.ifs.user.resource.Role.APPLICANT;
import static org.innovateuk.ifs.user.resource.Role.LEADAPPLICANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing populator {@link ApplicantDashboardPopulator}
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicantDashboardPopulatorTest extends BaseUnitTest {

    @InjectMocks
    private ApplicantDashboardPopulator populator;

    private final static Long APPLICATION_ID_IN_PROGRESS = 1L;
    private final static Long APPLICATION_ID_IN_FINISH = 10L;
    private final static Long APPLICATION_ID_SUBMITTED = 100L;
    private final static Long PROJECT_ID_IN_PROJECT = 5L;
    private final static Long PROJECT_ID_IN_PROJECT_WITHDRAWN = 6L;
    private final static Long APPLICATION_ID_IN_PROJECT = 15L;
    private final static Long APPLICATION_ID_IN_PROJECT_WITHDRAWN = 150L;

    @Before
    public void setup() {
        super.setup();
        this.setupCompetition();
        CompetitionResource compInProjectSetup = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.PROJECT_SETUP)
                .build();

        List<ApplicationResource> allApplications = newApplicationResource()
                .withId(APPLICATION_ID_IN_PROGRESS, APPLICATION_ID_IN_FINISH, APPLICATION_ID_SUBMITTED, APPLICATION_ID_IN_PROJECT_WITHDRAWN, APPLICATION_ID_IN_PROJECT)
                .withCompetition(competitionResource.getId(), competitionResource.getId(), compInProjectSetup.getId(), compInProjectSetup.getId(), compInProjectSetup.getId())
                .withApplicationState(ApplicationState.OPEN, ApplicationState.REJECTED, ApplicationState.SUBMITTED, ApplicationState.APPROVED, ApplicationState.APPROVED)
                .withCompetitionStatus(CompetitionStatus.OPEN, CompetitionStatus.CLOSED, CompetitionStatus.PROJECT_SETUP, CompetitionStatus.PROJECT_SETUP, CompetitionStatus.PROJECT_SETUP)
                .withCompletion(BigDecimal.valueOf(50))
                .build(5);

        when(applicationRestService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(restSuccess(allApplications));

        when(projectService.findByUser(loggedInUser.getId())).thenReturn(ServiceResult.serviceSuccess(newProjectResource()
                .withId(PROJECT_ID_IN_PROJECT, PROJECT_ID_IN_PROJECT_WITHDRAWN)
                .withApplication(APPLICATION_ID_IN_PROJECT, APPLICATION_ID_IN_PROJECT_WITHDRAWN)
                .withProjectState(ProjectState.SETUP, ProjectState.WITHDRAWN)
                .build(2)));

        when(applicationService.getById(APPLICATION_ID_IN_PROJECT)).thenReturn(newApplicationResource()
                .withId(APPLICATION_ID_IN_PROJECT)
                .withApplicationState(ApplicationState.SUBMITTED)
                .withCompetition(competitionResource.getId()).build());


        when(applicationService.getById(APPLICATION_ID_IN_PROJECT_WITHDRAWN))
                .thenReturn(newApplicationResource()
                                    .withId(APPLICATION_ID_IN_PROJECT_WITHDRAWN)
                                    .withApplicationState(ApplicationState.SUBMITTED)
                                    .withCompetition(competitionResource.getId()).build());

        when(competitionRestService.getCompetitionsByUserId(loggedInUser.getId())).thenReturn(restSuccess(competitionResources));

        when(applicationRestService.getAssignedQuestionsCount(anyLong(), anyLong())).thenReturn(restSuccess(2));  

        when(processRoleService.getByUserId(loggedInUser.getId())).thenReturn(newProcessRoleResource()
                .withApplication(APPLICATION_ID_IN_PROGRESS, APPLICATION_ID_IN_PROJECT, APPLICATION_ID_IN_PROJECT_WITHDRAWN, APPLICATION_ID_IN_FINISH, APPLICATION_ID_SUBMITTED)
                .withRole(LEADAPPLICANT, LEADAPPLICANT, APPLICANT, APPLICANT, APPLICANT)
                .build(5));

    }

    @Test
    public void populate() {
        ApplicantDashboardViewModel viewModel = populator.populate(loggedInUser.getId());

        assertTrue(viewModel.getApplicationsInProgressNotEmpty());
        assertTrue(viewModel.getApplicationsInFinishedNotEmpty());
        assertTrue(viewModel.getProjectsInSetupNotEmpty());


        assertEquals(4, viewModel.getApplicationsFinished().size());
        assertEquals(1, viewModel.getApplicationsInProgress().size());
        assertFalse(viewModel.applicationIsApprovedAndProjectIsNotWithdrawn(viewModel.getApplicationsFinished().get(0).getId()));
        assertTrue(viewModel.applicationIsApprovedAndProjectIsNotWithdrawn(viewModel.getApplicationsFinished().get(3).getId()));

        verify(applicationService, times(1)).getById(APPLICATION_ID_IN_PROJECT);
        assertEquals("Application in progress", viewModel.getApplicationInProgressText());
    }
}

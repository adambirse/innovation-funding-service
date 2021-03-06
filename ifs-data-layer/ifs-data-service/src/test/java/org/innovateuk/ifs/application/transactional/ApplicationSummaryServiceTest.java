package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.BaseUnitTestMocksTest;
import org.innovateuk.ifs.PageableMatcher;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.AddressTypeResource;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.mapper.ApplicationMapper;
import org.innovateuk.ifs.application.mapper.ApplicationSummaryMapper;
import org.innovateuk.ifs.application.mapper.ApplicationSummaryPageMapper;
import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.application.resource.*;
import org.innovateuk.ifs.application.workflow.configuration.ApplicationWorkflowHandler;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.mapper.OrganisationAddressMapper;
import org.innovateuk.ifs.organisation.repository.OrganisationRepository;
import org.innovateuk.ifs.organisation.resource.OrganisationAddressResource;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.user.repository.ProcessRoleRepository;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;

import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.innovateuk.ifs.PageableMatcher.srt;
import static org.innovateuk.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static org.innovateuk.ifs.address.builder.AddressTypeResourceBuilder.newAddressTypeResource;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.resource.ApplicationState.*;
import static org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl.SUBMITTED_STATES;
import static org.innovateuk.ifs.fundingdecision.domain.FundingDecisionStatus.*;
import static org.innovateuk.ifs.organisation.builder.OrganisationAddressResourceBuilder.newOrganisationAddressResource;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.CollectionFunctions.asLinkedSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.ASC;

public class ApplicationSummaryServiceTest extends BaseUnitTestMocksTest {

    private static final Long COMP_ID = Long.valueOf(123L);

    private static final Collection<ApplicationState> INELIGIBLE_STATES = asLinkedSet(
            ApplicationState.INELIGIBLE,
            ApplicationState.INELIGIBLE_INFORMED);

    @InjectMocks
    private ApplicationSummaryService applicationSummaryService = new ApplicationSummaryServiceImpl();

    @Mock
    private ApplicationSummaryMapper applicationSummaryMapper;

    @Mock
    private ApplicationSummaryPageMapper applicationSummaryPageMapper;

    @Mock
    private OrganisationAddressMapper organisationAddressMapper;

    @Mock
    private ApplicationRepository applicationRepositoryMock;

    @Mock
    private OrganisationRepository organisationRepositoryMock;

    @Mock
    private CompetitionRepository competitionRepositoryMock;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private ProcessRoleRepository processRoleRepositoryMock;

    @Mock
    private ApplicationMapper applicationMapperMock;

    @Mock
    private ApplicationWorkflowHandler applicationWorkflowHandlerMock;

    @Mock
    private UserMapper userMapper;

    @Test
    public void findByCompetitionNoSortWillSortById() {

        Page<Application> page = mock(Page.class);
        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(eq(COMP_ID), eq(INELIGIBLE_STATES), eq("filter"), argThat(new PageableMatcher(6, 20, srt("id", ASC))))).thenReturn(page);

        ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
        when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, null, 6, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(resource, result.getSuccess());
    }

    @Test
    public void findByCompetitionNoFilterWillFilterByEmptyString() {

        Page<Application> page = mock(Page.class);
        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(eq(COMP_ID), eq(INELIGIBLE_STATES), eq(""), argThat(new PageableMatcher(6, 20, srt("id", ASC))))).thenReturn(page);
        ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
        when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, null, 6, 20, empty());

        assertTrue(result.isSuccess());
        assertEquals(resource, result.getSuccess());
    }

    @Test
    public void findByCompetitionSortById() {

        Page<Application> page = mock(Page.class);
        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(eq(COMP_ID), eq(INELIGIBLE_STATES), eq("filter"), argThat(new PageableMatcher(6, 20, srt("id", ASC))))).thenReturn(page);

        ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
        when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "id", 6, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(resource, result.getSuccess());
    }

    @Test
    public void findByCompetitionSortByName() {

        Page<Application> page = mock(Page.class);
        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(eq(COMP_ID), eq(INELIGIBLE_STATES), eq("filter"), argThat(new PageableMatcher(6, 20, srt("name", ASC), srt("id", ASC))))).thenReturn(page);

        ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
        when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "name", 6, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(resource, result.getSuccess());
    }

    @Test
    public void findByCompetitionSortByLead() {

        Application app1 = mock(Application.class);
        Application app2 = mock(Application.class);
        List<Application> applications = asList(app1, app2);

        ApplicationSummaryResource sum1 = sumLead("b");
        ApplicationSummaryResource sum2 = sumLead("a");
        when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
        when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 0, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().getNumber());
        assertEquals(2, result.getSuccess().getContent().size());
        assertEquals(sum2, result.getSuccess().getContent().get(0));
        assertEquals(sum1, result.getSuccess().getContent().get(1));
        assertEquals(20, result.getSuccess().getSize());
        assertEquals(2, result.getSuccess().getTotalElements());
        assertEquals(1, result.getSuccess().getTotalPages());
    }

    @Test
    public void findByCompetitionSortByLeadSameLeadWillSortById() {

        Application app1 = mock(Application.class);
        Application app2 = mock(Application.class);
        List<Application> applications = asList(app1, app2);

        ApplicationSummaryResource sum1 = sumLead("a", 2L);
        ApplicationSummaryResource sum2 = sumLead("a", 1L);
        when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
        when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 0, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().getNumber());
        assertEquals(2, result.getSuccess().getContent().size());
        assertEquals(sum2, result.getSuccess().getContent().get(0));
        assertEquals(sum1, result.getSuccess().getContent().get(1));
        assertEquals(20, result.getSuccess().getSize());
        assertEquals(2, result.getSuccess().getTotalElements());
        assertEquals(1, result.getSuccess().getTotalPages());
    }

    @Test
    public void findByCompetitionSortByLeadNotFirstPage() {

        List<Application> applications = new ArrayList<>();
        for (int i = 0; i < 22; i++) {
            Application app = mock(Application.class);
            applications.add(app);
            ApplicationSummaryResource sum = sumLead("a" + String.format("%02d", i));
            when(applicationSummaryMapper.mapToResource(app)).thenReturn(sum);
        }

        Collections.reverse(applications);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 1, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(1, result.getSuccess().getNumber());
        assertEquals(2, result.getSuccess().getContent().size());
        assertEquals("a20", result.getSuccess().getContent().get(0).getLead());
        assertEquals("a21", result.getSuccess().getContent().get(1).getLead());
        assertEquals(20, result.getSuccess().getSize());
        assertEquals(22, result.getSuccess().getTotalElements());
        assertEquals(2, result.getSuccess().getTotalPages());
    }

    @Test
    public void findByCompetitionSortByLeadHandlesNullLeads() {

        Application app1 = mock(Application.class);
        Application app2 = mock(Application.class);
        List<Application> applications = asList(app1, app2);

        ApplicationSummaryResource sum1 = sumLead(null);
        ApplicationSummaryResource sum2 = sumLead(null);
        when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
        when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 0, 20, of("filter"));

        assertEquals(2, result.getSuccess().getContent().size());
        assertEquals(sum1, result.getSuccess().getContent().get(0));
        assertEquals(sum2, result.getSuccess().getContent().get(1));
    }

    @Test
    public void findByCompetitionSortByLeadHandlesNullAndNotNullLead() {

        Application app1 = mock(Application.class);
        Application app2 = mock(Application.class);
        Application app3 = mock(Application.class);
        List<Application> applications = asList(app1, app2, app3);

        ApplicationSummaryResource sum1 = sumLead(null);
        ApplicationSummaryResource sum2 = sumLead("a");
        ApplicationSummaryResource sum3 = sumLead(null);
        when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
        when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
        when(applicationSummaryMapper.mapToResource(app3)).thenReturn(sum3);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "lead", 0, 20, of("filter"));

        assertEquals(3, result.getSuccess().getContent().size());
        assertEquals(sum1, result.getSuccess().getContent().get(0));
        assertEquals(sum3, result.getSuccess().getContent().get(1));
        assertEquals(sum2, result.getSuccess().getContent().get(2));
    }

    @Test
    public void findByCompetitionSortByLeadApplicant() {

        Application app1 = mock(Application.class);
        Application app2 = mock(Application.class);
        List<Application> applications = asList(app1, app2);

        ApplicationSummaryResource sum1 = sumLeadApplicant("b");
        ApplicationSummaryResource sum2 = sumLeadApplicant("a");
        when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
        when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 0, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().getNumber());
        assertEquals(2, result.getSuccess().getContent().size());
        assertEquals(sum2, result.getSuccess().getContent().get(0));
        assertEquals(sum1, result.getSuccess().getContent().get(1));
        assertEquals(20, result.getSuccess().getSize());
        assertEquals(2, result.getSuccess().getTotalElements());
        assertEquals(1, result.getSuccess().getTotalPages());
    }

    @Test
    public void findByCompetitionSortByLeadSameLeadApplicantWillSortById() {

        Application app1 = mock(Application.class);
        Application app2 = mock(Application.class);
        List<Application> applications = asList(app1, app2);

        ApplicationSummaryResource sum1 = sumLeadApplicant("a", 2L);
        ApplicationSummaryResource sum2 = sumLeadApplicant("a", 1L);
        when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
        when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 0, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().getNumber());
        assertEquals(2, result.getSuccess().getContent().size());
        assertEquals(sum2, result.getSuccess().getContent().get(0));
        assertEquals(sum1, result.getSuccess().getContent().get(1));
        assertEquals(20, result.getSuccess().getSize());
        assertEquals(2, result.getSuccess().getTotalElements());
        assertEquals(1, result.getSuccess().getTotalPages());
    }

    @Test
    public void findByCompetitionSortByLeadApplicantNotFirstPage() {

        List<Application> applications = new ArrayList<>();
        for (int i = 0; i < 22; i++) {
            Application app = mock(Application.class);
            applications.add(app);
            ApplicationSummaryResource sum = sumLeadApplicant("a" + String.format("%02d", i));
            when(applicationSummaryMapper.mapToResource(app)).thenReturn(sum);
        }

        Collections.reverse(applications);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 1, 20, of("filter"));

        assertTrue(result.isSuccess());
        assertEquals(1, result.getSuccess().getNumber());
        assertEquals(2, result.getSuccess().getContent().size());
        assertEquals("a20", result.getSuccess().getContent().get(0).getLeadApplicant());
        assertEquals("a21", result.getSuccess().getContent().get(1).getLeadApplicant());
        assertEquals(20, result.getSuccess().getSize());
        assertEquals(22, result.getSuccess().getTotalElements());
        assertEquals(2, result.getSuccess().getTotalPages());
    }

    @Test
    public void findByCompetitionSortByLeadApplicantHandlesNullLeadApplicants() {

        Application app1 = mock(Application.class);
        Application app2 = mock(Application.class);
        List<Application> applications = asList(app1, app2);

        ApplicationSummaryResource sum1 = sumLeadApplicant(null);
        ApplicationSummaryResource sum2 = sumLeadApplicant(null);
        when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
        when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 0, 20, of("filter"));

        assertEquals(2, result.getSuccess().getContent().size());
        assertEquals(sum1, result.getSuccess().getContent().get(0));
        assertEquals(sum2, result.getSuccess().getContent().get(1));
    }

    @Test
    public void findByCompetitionSortByLeadApplicantHandlesNullAndNotNullLead() {

        Application app1 = mock(Application.class);
        Application app2 = mock(Application.class);
        Application app3 = mock(Application.class);
        List<Application> applications = asList(app1, app2, app3);

        ApplicationSummaryResource sum1 = sumLeadApplicant(null);
        ApplicationSummaryResource sum2 = sumLeadApplicant("a");
        ApplicationSummaryResource sum3 = sumLeadApplicant(null);
        when(applicationSummaryMapper.mapToResource(app1)).thenReturn(sum1);
        when(applicationSummaryMapper.mapToResource(app2)).thenReturn(sum2);
        when(applicationSummaryMapper.mapToResource(app3)).thenReturn(sum3);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateNotIn(COMP_ID, INELIGIBLE_STATES, "filter")).thenReturn(applications);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getApplicationSummariesByCompetitionId(COMP_ID, "leadApplicant", 0, 20, of("filter"));

        assertEquals(3, result.getSuccess().getContent().size());
        assertEquals(sum1, result.getSuccess().getContent().get(0));
        assertEquals(sum3, result.getSuccess().getContent().get(1));
        assertEquals(sum2, result.getSuccess().getContent().get(2));
    }

    @Test
    public void findByCompetitionSubmittedApplications() {

        Page<Application> page = mock(Page.class);

        ApplicationSummaryPageResource resource = new ApplicationSummaryPageResource();
        when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateInAndIdLike(
                eq(COMP_ID),
                eq(asLinkedSet(APPROVED, REJECTED, SUBMITTED)),
                eq(""),
                eq(UNFUNDED),
                eq(null),
                argThat(new PageableMatcher(0, 20, srt("id", ASC)))))
                .thenReturn(page);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService
                .getSubmittedApplicationSummariesByCompetitionId(
                        COMP_ID,
                        "id",
                        0,
                        20,
                        of(""),
                        of(UNFUNDED),
                        empty());

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().getNumber());
        assertEquals(resource, result.getSuccess());
    }

    @Test
    public void findByCompetitionIneligibleApplications() {

        Page<Application> page = mock(Page.class);

        ApplicationSummaryPageResource resource = new ApplicationSummaryPageResource();
        when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateInAndIdLike(
                eq(COMP_ID),
                eq(asLinkedSet(ApplicationState.INELIGIBLE, ApplicationState.INELIGIBLE_INFORMED)),
                eq(""),
                eq(null),
                eq(null),
                argThat(new PageableMatcher(0, 20, srt("id", ASC)))))
                .thenReturn(page);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService
                .getIneligibleApplicationSummariesByCompetitionId(
                        COMP_ID,
                        "id",
                        0,
                        20,
                        of(""),
                        empty());

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().getNumber());
        assertEquals(resource, result.getSuccess());
    }

    @Test
    public void findByCompetitionIneligibleApplications_informFiltered() {

        Page<Application> page = mock(Page.class);

        ApplicationSummaryPageResource resource = new ApplicationSummaryPageResource();
        when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateInAndIdLike(
                eq(COMP_ID),
                eq(singleton(ApplicationState.INELIGIBLE_INFORMED)),
                eq(""),
                eq(null),
                eq(null),
                argThat(new PageableMatcher(0, 20, srt("id", ASC)))))
                .thenReturn(page);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService
                .getIneligibleApplicationSummariesByCompetitionId(
                        COMP_ID,
                        "id",
                        0,
                        20,
                        of(""),
                        of(true));

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().getNumber());
        assertEquals(resource, result.getSuccess());
    }

    @Test
    public void findByCompetitionWithFundingDecisionApplications() {

        Page<Application> page = mock(Page.class);

        ApplicationSummaryPageResource resource = mock(ApplicationSummaryPageResource.class);
        when(applicationSummaryPageMapper.mapToResource(page)).thenReturn(resource);

        when(applicationRepositoryMock.findByCompetitionIdAndFundingDecisionIsNotNull(eq(COMP_ID), eq("filter"), eq(false), eq(ON_HOLD), argThat(new PageableMatcher(0, 20, srt("id", ASC))))).thenReturn(page);

        ServiceResult<ApplicationSummaryPageResource> result = applicationSummaryService.getWithFundingDecisionApplicationSummariesByCompetitionId(COMP_ID, "id", 0, 20, of("filter"), of(false), of(ON_HOLD));

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().getNumber());
        assertEquals(resource, result.getSuccess());
    }

    @Test
    public void findWithFundingDecisionIsChangeableApplicationIdsByCompetitionId() {

        List<Application> applications = newApplication()
                .withManageFundingEmailDate(ZonedDateTime.now())
                .withFundingDecision(FUNDED)
                .build(2);

        when(applicationRepositoryMock.findByCompetitionIdAndFundingDecisionIsNotNull(eq(COMP_ID), eq("filter"), eq(false), eq(FUNDED))).thenReturn(applications);

        ServiceResult<List<Long>> result = applicationSummaryService.getWithFundingDecisionIsChangeableApplicationIdsByCompetitionId(COMP_ID, of("filter"), of(false), of(FUNDED));

        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccess().size());
    }

    @Test
    public void findWithFundingDecisionIsNotChangeableApplicationIdsByCompetitionId() {

        List<Application> applications = newApplication()
                .withFundingDecision(ON_HOLD)
                .build(2);

        when(applicationRepositoryMock.findByCompetitionIdAndFundingDecisionIsNotNull(eq(COMP_ID), eq("filter"), eq(false), eq(FUNDED))).thenReturn(applications);

        ServiceResult<List<Long>> result = applicationSummaryService.getWithFundingDecisionIsChangeableApplicationIdsByCompetitionId(COMP_ID, of("filter"), of(false), of(FUNDED));

        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccess().size());
        assertEquals(applications.get(0).getId(), result.getSuccess().get(0));
        assertEquals(applications.get(1).getId(), result.getSuccess().get(1));
    }

    @Test
    public void getAllSubmittedApplicationIdsByCompetitionId() {
        List<Application> applications = newApplication()
                .withFundingDecision(UNFUNDED)
                .build(2);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationProcessActivityStateInAndIdLike(
                eq(COMP_ID), eq(SUBMITTED_STATES),  eq("filter"), eq(UNFUNDED), eq(null))).thenReturn(applications);

        ServiceResult<List<Long>> result = applicationSummaryService.getAllSubmittedApplicationIdsByCompetitionId(COMP_ID, of("filter"), of(UNFUNDED));
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccess().size());
        assertEquals(applications.get(0).getId(), result.getSuccess().get(0));
        assertEquals(applications.get(1).getId(), result.getSuccess().get(1));
    }

    @Test
    public void getApplicationTeamSuccess() {
        User leadOrgLeadUser = newUser().withFirstName("Lee").withLastName("Der").withRoles(singleton(Role.LEADAPPLICANT)).build();
        User leadOrgNonLeadUser1 = newUser().withFirstName("A").withLastName("Bee").build();
        User leadOrgNonLeadUser2 = newUser().withFirstName("Cee").withLastName("Dee").build();
        User partnerOrgLeadUser1 = newUser().withFirstName("Zee").withLastName("Der").withRoles(singleton(Role.LEADAPPLICANT)).build();
        User partnerOrgLeadUser2 = newUser().withFirstName("Ay").withLastName("Der").withRoles(singleton(Role.LEADAPPLICANT)).build();

        ProcessRole lead = newProcessRole().withRole(Role.LEADAPPLICANT).withOrganisationId(234L).withUser(leadOrgLeadUser).build();
        ProcessRole leadOrgCollaborator1 = newProcessRole().withRole(Role.COLLABORATOR).withOrganisationId(234L).withUser(leadOrgNonLeadUser1).build();
        ProcessRole collaborator1 = newProcessRole().withRole(Role.COLLABORATOR).withOrganisationId(345L).withUser(partnerOrgLeadUser1).build();
        ProcessRole collaborator2 = newProcessRole().withRole(Role.COLLABORATOR).withOrganisationId(456L).withUser(partnerOrgLeadUser2).build();
        Application app = newApplication().withProcessRoles(lead, leadOrgCollaborator1, collaborator1, collaborator2).build();

        Organisation leadOrg = newOrganisation()
                .withName("Lead")
                .withOrganisationType(OrganisationTypeEnum.RESEARCH)
                .withUser(Arrays.asList(leadOrgLeadUser,leadOrgNonLeadUser1,leadOrgNonLeadUser2))
                .build();
        Organisation partnerOrgA = newOrganisation()
                .withName("A")
                .withOrganisationType(OrganisationTypeEnum.RESEARCH)
                .withUser(singletonList(partnerOrgLeadUser1))
                .build();
        Organisation partnerOrgB = newOrganisation()
                .withName("B")
                .withOrganisationType(OrganisationTypeEnum.BUSINESS)
                .withUser(singletonList(partnerOrgLeadUser2))
                .build();

        when(applicationRepositoryMock.findById(123L)).thenReturn(Optional.of(app));
        when(organisationRepositoryMock.findById(234L)).thenReturn(Optional.of(leadOrg));
        when(organisationRepositoryMock.findById(345L)).thenReturn(Optional.of(partnerOrgB));
        when(organisationRepositoryMock.findById(456L)).thenReturn(Optional.of(partnerOrgA));

        AddressTypeResource registeredAddressTypeResource = newAddressTypeResource().withName("REGISTERED").build();
        AddressTypeResource operatingAddressTypeResource = newAddressTypeResource().withName("OPERATING").build();
        AddressResource addressResource1 = newAddressResource()
                .withAddressLine1("1E")
                .withAddressLine2("2.16")
                .withAddressLine3("Polaris House")
                .withTown("Swindon")
                .withCounty("Wilts")
                .withPostcode("SN1 1AA")
                .build();
        AddressResource addressResource2 = newAddressResource()
                .withAddressLine1("2E")
                .withAddressLine2("2.17")
                .withAddressLine3("North Star House")
                .withTown("Swindon").withCounty("Wiltshire")
                .withPostcode("SN2 2AA")
                .build();
        OrganisationAddressResource leadOrgRegisteredAddressResource = newOrganisationAddressResource().withAddressType(registeredAddressTypeResource).withAddress(addressResource1).build();
        OrganisationAddressResource leadOrgOperatingAddressResource = newOrganisationAddressResource().withAddressType(operatingAddressTypeResource).withAddress(addressResource2).build();

        UserResource leadOrgLeadUserResource = newUserResource().withFirstName("Lee").withLastName("Der").build();
        UserResource leadOrgNonLeadUser1Resource = newUserResource().withFirstName("A").withLastName("Bee").build();
        UserResource leadOrgNonLeadUser2Resource = newUserResource().withFirstName("Cee").withLastName("Dee").build();
        UserResource partnerOrgLeadUser1Resource = newUserResource().withFirstName("Zee").withLastName("Der").build();
        UserResource partnerOrgLeadUser2Resource = newUserResource().withFirstName("Ay").withLastName("Der").build();
        when(userMapper.mapToResource(leadOrgLeadUser)).thenReturn(leadOrgLeadUserResource);
        when(userMapper.mapToResource(leadOrgNonLeadUser1)).thenReturn(leadOrgNonLeadUser1Resource);
        when(userMapper.mapToResource(leadOrgNonLeadUser2)).thenReturn(leadOrgNonLeadUser2Resource);
        when(userMapper.mapToResource(partnerOrgLeadUser1)).thenReturn(partnerOrgLeadUser1Resource);
        when(userMapper.mapToResource(partnerOrgLeadUser2)).thenReturn(partnerOrgLeadUser2Resource);

        ServiceResult<ApplicationTeamResource> result = applicationSummaryService.getApplicationTeamByApplicationId(123L);
        assertTrue(result.isSuccess());

        ApplicationTeamOrganisationResource leadOrganisation = result.getSuccess().getLeadOrganisation();
        assertTrue(leadOrganisation.getOrganisationName().equals("Lead"));
        assertTrue(leadOrganisation.getUsers().get(0).getName().equals("Lee Der"));

        List<ApplicationTeamOrganisationResource> partnerOrganisations = result.getSuccess().getPartnerOrganisations();
        assertEquals(2, partnerOrganisations.size());
        assertTrue(partnerOrganisations.get(0).getOrganisationName().equals("A"));
        assertTrue(partnerOrganisations.get(0).getUsers().get(0).getName().equals("Ay Der"));

        assertTrue(partnerOrganisations.get(1).getOrganisationName().equals("B"));
        assertTrue(partnerOrganisations.get(1).getUsers().get(0).getName().equals("Zee Der"));
    }

    @Test
    public void getApplicationTeamFailsNoApplication() {
        when(applicationRepositoryMock.findById(123L)).thenReturn(Optional.empty());

        ServiceResult<ApplicationTeamResource> result = applicationSummaryService.getApplicationTeamByApplicationId(123L);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().getErrors().get(0).getErrorKey().equals(CommonFailureKeys.GENERAL_NOT_FOUND.getErrorKey()));
    }

    private ApplicationSummaryResource sumLead(String lead) {
        ApplicationSummaryResource res = new ApplicationSummaryResource();
        res.setLead(lead);
        return res;
    }

    private ApplicationSummaryResource sumLead(String lead, Long id) {
        ApplicationSummaryResource res = sumLead(lead);
        res.setId(id);
        return res;
    }

    private ApplicationSummaryResource sumLeadApplicant(String leadApplicant) {
        ApplicationSummaryResource res = new ApplicationSummaryResource();
        res.setLeadApplicant(leadApplicant);
        return res;
    }

    private ApplicationSummaryResource sumLeadApplicant(String leadApplicant, Long id) {
        ApplicationSummaryResource res = sumLeadApplicant(leadApplicant);
        res.setId(id);
        return res;
    }
}
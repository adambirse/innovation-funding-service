package org.innovateuk.ifs.management.controller;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.category.resource.InnovationSectorResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.management.form.InterviewPanelSelectionForm;
import org.innovateuk.ifs.management.form.InviteNewAssessorsForm;
import org.innovateuk.ifs.management.form.InviteNewAssessorsRowForm;
import org.innovateuk.ifs.management.model.AssessorProfileModelPopulator;
import org.innovateuk.ifs.management.model.InterviewPanelInviteAssessorsFindModelPopulator;
import org.innovateuk.ifs.management.model.InterviewPanelInviteAssessorsInviteModelPopulator;
import org.innovateuk.ifs.management.viewmodel.*;
import org.innovateuk.ifs.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.is;
import static org.innovateuk.ifs.category.builder.InnovationAreaResourceBuilder.newInnovationAreaResource;
import static org.innovateuk.ifs.category.builder.InnovationSectorResourceBuilder.newInnovationSectorResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.IN_ASSESSMENT;
import static org.innovateuk.ifs.invite.builder.AssessorCreatedInvitePageResourceBuilder.newAssessorCreatedInvitePageResource;
import static org.innovateuk.ifs.invite.builder.AssessorCreatedInviteResourceBuilder.newAssessorCreatedInviteResource;
import static org.innovateuk.ifs.invite.builder.AssessorInviteOverviewResourceBuilder.newAssessorInviteOverviewResource;
import static org.innovateuk.ifs.invite.builder.AvailableAssessorPageResourceBuilder.newAvailableAssessorPageResource;
import static org.innovateuk.ifs.invite.builder.AvailableAssessorResourceBuilder.newAvailableAssessorResource;
import static org.innovateuk.ifs.invite.resource.ParticipantStatusResource.ACCEPTED;
import static org.innovateuk.ifs.user.resource.BusinessType.ACADEMIC;
import static org.innovateuk.ifs.user.resource.BusinessType.BUSINESS;
import static org.innovateuk.ifs.util.CollectionFunctions.asLinkedSet;
import static org.innovateuk.ifs.util.CollectionFunctions.forEachWithIndex;
import static org.innovateuk.ifs.util.CompressionUtil.getCompressedString;
import static org.innovateuk.ifs.util.CompressionUtil.getDecompressedString;
import static org.innovateuk.ifs.util.JsonUtil.getObjectFromJson;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class CompetitionManagementInterviewPanelInviteAssessorsControllerTest extends BaseControllerMockMVCTest<CompetitionManagementInterviewPanelInviteAssessorsController> {

    @Spy
    @InjectMocks
    private InterviewPanelInviteAssessorsInviteModelPopulator interviewPanelInviteAssessorsInviteModelPopulator;

    @Spy
    @InjectMocks
    private AssessorProfileModelPopulator assessorProfileModelPopulator;

    @Spy
    @InjectMocks
    private InterviewPanelInviteAssessorsFindModelPopulator interviewPanelInviteAssessorsFindModelPopulator;

    private CompetitionResource competition;

    @Override
    protected CompetitionManagementInterviewPanelInviteAssessorsController supplyControllerUnderTest() {
        return new CompetitionManagementInterviewPanelInviteAssessorsController();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        this.setupCookieUtil();

        competition = newCompetitionResource()
                .withCompetitionStatus(IN_ASSESSMENT)
                .withName("Technology inspired")
                .withInnovationSectorName("Infrastructure systems")
                .withInnovationAreaNames(asLinkedSet("Transport Systems", "Urban living"))
                .build();

        when(competitionRestService.getCompetitionById(competition.getId())).thenReturn(restSuccess(competition));
    }

    @Test
    public void assessors() throws Exception {
        Long competitionId = 1L;
        mockMvc.perform(get("/assessment/interview/competition/{competitionId}/assessors", competitionId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/competition/%s/assessors/interview-find", competitionId)));
    }

    @Test
    public void find() throws Exception {
        int page = 2;

        AvailableAssessorPageResource availableAssessorPageResource = newAvailableAssessorPageResource()
                .withContent(setUpAvailableAssessorResources())
                .build();

        when(interviewPanelInviteRestService.getAvailableAssessors(competition.getId(), page)).thenReturn(restSuccess(availableAssessorPageResource));
        when(interviewPanelInviteRestService.getAvailableAssessorIds(competition.getId())).thenReturn(restSuccess(emptyList()));

        MvcResult result = mockMvc.perform(get("/assessment/interview/competition/{competitionId}/assessors/find", competition.getId())
                .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/interview-find"))
                .andReturn();

        InterviewPanelSelectionForm selectionForm = (InterviewPanelSelectionForm) result.getModelAndView().getModel().get("assessorInterviewPanelSelectionForm");
        assertTrue(selectionForm.getSelectedAssessorIds().isEmpty());

        assertCompetitionDetails(competition, result);
        assertAvailableAssessors(availableAssessorPageResource.getContent(), result);

        InOrder inOrder = inOrder(competitionRestService, interviewPanelInviteRestService);
        inOrder.verify(interviewPanelInviteRestService).getAvailableAssessorIds(competition.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(interviewPanelInviteRestService).getAvailableAssessors(competition.getId(), page);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void find_defaultParams() throws Exception {
        int page = 0;

        AvailableAssessorPageResource availableAssessorPageResource = newAvailableAssessorPageResource()
                .withContent(emptyList())
                .build();

        when(interviewPanelInviteRestService.getAvailableAssessors(competition.getId(), page)).thenReturn(restSuccess(availableAssessorPageResource));
        when(interviewPanelInviteRestService.getAvailableAssessorIds(competition.getId())).thenReturn(restSuccess(emptyList()));

        MvcResult result = mockMvc.perform(get("/assessment/interview/competition/{competitionId}/assessors/find", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/interview-find"))
                .andReturn();

        InterviewPanelSelectionForm selectionForm = (InterviewPanelSelectionForm) result.getModelAndView().getModel().get("assessorInterviewPanelSelectionForm");
        assertTrue(selectionForm.getSelectedAssessorIds().isEmpty());

        assertCompetitionDetails(competition, result);
        assertAvailableAssessors(availableAssessorPageResource.getContent(), result);

        InOrder inOrder = inOrder(competitionRestService, interviewPanelInviteRestService);
        inOrder.verify(interviewPanelInviteRestService).getAvailableAssessorIds(competition.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(interviewPanelInviteRestService).getAvailableAssessors(competition.getId(), page);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void find_existingCookie() throws Exception {
        int page = 0;
        long expectedAssessorId = 1L;

        InterviewPanelSelectionForm expectedSelectionForm = new InterviewPanelSelectionForm();
        expectedSelectionForm.getSelectedAssessorIds().add(expectedAssessorId);
        Cookie selectionFormCookie = createFormCookie(expectedSelectionForm);

        AvailableAssessorPageResource availableAssessorPageResource = newAvailableAssessorPageResource()
                .withTotalPages(1)
                .withContent(setUpAvailableAssessorResources())
                .build();

        when(interviewPanelInviteRestService.getAvailableAssessors(competition.getId(), page)).thenReturn(restSuccess(availableAssessorPageResource));
        when(interviewPanelInviteRestService.getAvailableAssessorIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(get("/assessment/interview/competition/{competitionId}/assessors/find", competition.getId())
                .cookie(selectionFormCookie))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/interview-find"))
                .andReturn();

        InterviewPanelSelectionForm selectionForm = (InterviewPanelSelectionForm) result.getModelAndView().getModel().get("assessorInterviewPanelSelectionForm");
        assertEquals(expectedSelectionForm, selectionForm);

        Optional<InterviewPanelSelectionForm> resultForm = getAssessorPanelSelectionFormFromCookie(result.getResponse(), format("assessorInterviewPanelSelectionForm_comp_%s", competition.getId()));
        assertTrue(resultForm.get().getSelectedAssessorIds().contains(expectedAssessorId));

        assertCompetitionDetails(competition, result);
        assertAvailableAssessors(availableAssessorPageResource.getContent(), result);

        InOrder inOrder = inOrder(competitionRestService, interviewPanelInviteRestService);
        inOrder.verify(interviewPanelInviteRestService).getAvailableAssessorIds(competition.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(interviewPanelInviteRestService).getAvailableAssessors(competition.getId(), page);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void addAssessorSelectionFromFindView() throws Exception {
        long assessorId = 1L;
        Cookie formCookie = createFormCookie(new InterviewPanelSelectionForm());

        when(interviewPanelInviteRestService.getAvailableAssessorIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview/competition/{competitionId}/assessors/find", competition.getId())
                .param("selectionId", valueOf(assessorId))
                .param("isSelected", "true")
                .param("page", "1")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(1)))
                .andExpect(jsonPath("allSelected", is(false)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewPanelSelectionForm> resultForm = getAssessorPanelSelectionFormFromCookie(result.getResponse(), format("assessorInterviewPanelSelectionForm_comp_%s", competition.getId()));
        assertTrue(resultForm.get().getSelectedAssessorIds().contains(assessorId));
    }

    @Test
    public void addAssessorSelectionFromFindView_defaultParams() throws Exception {
        long assessorId = 1L;
        Cookie formCookie = createFormCookie(new InterviewPanelSelectionForm());

        when(interviewPanelInviteRestService.getAvailableAssessorIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview/competition/{competitionId}/assessors/find", competition.getId())
                .param("selectionId", valueOf(assessorId))
                .param("isSelected", "true")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(1)))
                .andExpect(jsonPath("allSelected", is(false)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewPanelSelectionForm> resultForm = getAssessorPanelSelectionFormFromCookie(result.getResponse(), format("assessorInterviewPanelSelectionForm_comp_%s", competition.getId()));
        assertTrue(resultForm.get().getSelectedAssessorIds().contains(assessorId));
    }

    @Test
    public void addAllAssessorsFromFindView_defaultParams() throws Exception {
        Cookie formCookie = createFormCookie(new InterviewPanelSelectionForm());

        when(interviewPanelInviteRestService.getAvailableAssessorIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview/competition/{competitionId}/assessors/find", competition.getId())
                .param("addAll", "true")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(2)))
                .andExpect(jsonPath("allSelected", is(true)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewPanelSelectionForm> resultForm = getAssessorPanelSelectionFormFromCookie(result.getResponse(), format("assessorInterviewPanelSelectionForm_comp_%s", competition.getId()));
        assertEquals(2, resultForm.get().getSelectedAssessorIds().size());
    }

    @Test
    public void removeAssessorSelectionFromFindView() throws Exception {
        long assessorId = 1L;
        InterviewPanelSelectionForm form = new InterviewPanelSelectionForm();
        form.getSelectedAssessorIds().add(assessorId);
        Cookie formCookie = createFormCookie(form);

        when(interviewPanelInviteRestService.getAvailableAssessorIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview/competition/{competitionId}/assessors/find", competition.getId())
                .param("selectionId", valueOf(assessorId))
                .param("isSelected", "false")
                .param("page", "1")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(0)))
                .andExpect(jsonPath("allSelected", is(false)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewPanelSelectionForm> resultForm = getAssessorPanelSelectionFormFromCookie(result.getResponse(), format("assessorInterviewPanelSelectionForm_comp_%s", competition.getId()));
        assertFalse(resultForm.get().getSelectedAssessorIds().contains(assessorId));
    }

    @Test
    public void removeAssessorSelectionFromFindView_defaultParams() throws Exception {
        long assessorId = 1L;
        Cookie formCookie;
        InterviewPanelSelectionForm form = new InterviewPanelSelectionForm();
        form.getSelectedAssessorIds().add(assessorId);
        formCookie = createFormCookie(form);

        when(interviewPanelInviteRestService.getAvailableAssessorIds(competition.getId())).thenReturn(restSuccess(asList(1L, 2L)));

        MvcResult result = mockMvc.perform(post("/assessment/interview/competition/{competitionId}/assessors/find", competition.getId())
                .param("selectionId", valueOf(assessorId))
                .param("isSelected", "false")
                .cookie(formCookie))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("selectionCount", is(0)))
                .andExpect(jsonPath("allSelected", is(false)))
                .andExpect(jsonPath("limitExceeded", is(false)))
                .andReturn();

        Optional<InterviewPanelSelectionForm> resultForm = getAssessorPanelSelectionFormFromCookie(result.getResponse(), format("assessorInterviewPanelSelectionForm_comp_%s", competition.getId()));
        assertFalse(resultForm.get().getSelectedAssessorIds().contains(assessorId));
    }

    @Test
    public void invite() throws Exception {
        int page = 0;

        List<AssessorCreatedInviteResource> assessorCreatedInviteResources = setUpAssessorCreatedInviteResources();
        AssessorCreatedInvitePageResource assessorCreatedInvitePageResource = newAssessorCreatedInvitePageResource()
                .withContent(assessorCreatedInviteResources)
                .build();

        List<InnovationAreaResource> categoryResources = setupCategoryResources();

        setupDefaultInviteViewExpectations(page, assessorCreatedInvitePageResource, categoryResources);

        InviteNewAssessorsForm expectedForm = new InviteNewAssessorsForm();
        expectedForm.setInvites(singletonList(new InviteNewAssessorsRowForm()));

        MvcResult result = mockMvc.perform(get("/assessment/interview/competition/{competitionId}/assessors/invite", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/interview-invite"))
                .andReturn();

        assertCompetitionDetails(competition, result);
        assertInvitedAssessors(assessorCreatedInviteResources, result);

        InOrder inOrder = inOrder(competitionRestService, interviewPanelInviteRestService, categoryRestServiceMock);
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(interviewPanelInviteRestService).getCreatedInvites(competition.getId(), page);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void removeInviteFromInviteView() throws Exception {
        String email = "firstname.lastname@example.com";

        when(interviewPanelInviteRestService.deleteInvite(email, competition.getId())).thenReturn(restSuccess());

        mockMvc.perform(post("/assessment/interview/competition/{competitionId}/assessors/invite", competition.getId())
                .param("remove", email)
                .param("page", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/assessment/interview/competition/%s/assessors/invite?page=5", competition.getId())))
                .andReturn();

        verify(interviewPanelInviteRestService, only()).deleteInvite(email, competition.getId());
    }

    @Test
    public void removeInviteFromInviteView_defaultParams() throws Exception {
        String email = "firstname.lastname@example.com";

        when(interviewPanelInviteRestService.deleteInvite(email, competition.getId())).thenReturn(restSuccess());

        mockMvc.perform(post("/assessment/interview/competition/{competitionId}/assessors/invite", competition.getId())
                .param("remove", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/assessment/interview/competition/%s/assessors/invite?page=0", competition.getId())))
                .andReturn();

        verify(interviewPanelInviteRestService, only()).deleteInvite(email, competition.getId());
    }

    @Test
    public void removeAllInvitesFromInviteView() throws Exception {
        when(interviewPanelInviteRestService.deleteAllInvites(competition.getId())).thenReturn(restSuccess());

        mockMvc.perform(post("/assessment/interview/competition/{competitionId}/assessors/invite", competition.getId())
                .param("removeAll", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/assessment/interview/competition/%s/assessors/invite?page=0", competition.getId())))
                .andReturn();

        verify(interviewPanelInviteRestService).deleteAllInvites(competition.getId());
    }

    private List<AvailableAssessorResource> setUpAvailableAssessorResources() {
        return newAvailableAssessorResource()
                .withId(1L, 2L)
                .withName("Dave Smith", "John Barnes")
                .withInnovationAreas(asList(newInnovationAreaResource()
                        .withName("Earth Observation", "Healthcare, Analytical science")
                        .buildArray(2, InnovationAreaResource.class)))
                .withCompliant(TRUE, FALSE)
                .withEmail("dave@email.com", "john@email.com")
                .withBusinessType(BUSINESS, ACADEMIC)
                .build(2);
    }

    private List<AssessorCreatedInviteResource> setUpAssessorCreatedInviteResources() {
        return newAssessorCreatedInviteResource()
                .withId(1L, 2L)
                .withInviteId(3L, 4L)
                .withName("Dave Smith", "John Barnes")
                .withInnovationAreas(asList(newInnovationAreaResource()
                        .withName("Earth Observation", "Healthcare, Analytical science")
                        .buildArray(2, InnovationAreaResource.class)))
                .withCompliant(TRUE, FALSE)
                .withEmail("dave@email.com", "john@email.com")
                .build(2);
    }

    private List<AssessorInviteOverviewResource> setUpAssessorInviteOverviewResources() {
        return newAssessorInviteOverviewResource()
                .withId(1L, 2L)
                .withInviteId(3L, 4L)
                .withName("Dave Smith", "John Barnes")
                .withInnovationAreas(asList(newInnovationAreaResource()
                        .withName("Earth Observation", "Healthcare, Analytical science")
                        .buildArray(2, InnovationAreaResource.class)))
                .withCompliant(TRUE, FALSE)
                .withBusinessType(BUSINESS, ACADEMIC)
                .withStatus(ACCEPTED, ACCEPTED)
                .withDetails("", "")
                .build(2);
    }

    private void assertCompetitionDetails(CompetitionResource expectedCompetition, MvcResult result) {
        InviteAssessorsViewModel model = (InviteAssessorsViewModel) result.getModelAndView().getModel().get("model");

        assertEquals( expectedCompetition.getId(), model.getCompetitionId());
        assertEquals(expectedCompetition.getName(), model.getCompetitionName());
        assertInnovationSectorAndArea(expectedCompetition, model);
    }

    private void assertInnovationSectorAndArea(CompetitionResource expectedCompetition, InviteAssessorsViewModel model) {
        assertEquals(expectedCompetition.getInnovationSectorName(), model.getInnovationSector());
        assertEquals(StringUtils.join(expectedCompetition.getInnovationAreaNames(), ", "), model.getInnovationArea());
    }

    private void assertAvailableAssessors(List<AvailableAssessorResource> expectedAvailableAssessors, MvcResult result) {
        assertTrue(result.getModelAndView().getModel().get("model") instanceof InterviewPanelInviteAssessorsFindViewModel);
        InterviewPanelInviteAssessorsFindViewModel model = (InterviewPanelInviteAssessorsFindViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(expectedAvailableAssessors.size(), model.getAssessors().size());

        forEachWithIndex(expectedAvailableAssessors, (i, availableAssessorResource) -> {
            InterviewPanelAvailableAssessorRowViewModel availableAssessorRowViewModel = model.getAssessors().get(i);
            assertEquals(availableAssessorResource.getName(), availableAssessorRowViewModel.getName());
            assertEquals(formatInnovationAreas(availableAssessorResource.getInnovationAreas()), availableAssessorRowViewModel.getInnovationAreas());
            assertEquals(availableAssessorResource.isCompliant(), availableAssessorRowViewModel.isCompliant());
            assertEquals(availableAssessorResource.getBusinessType(), availableAssessorRowViewModel.getBusinessType());
        });
    }

    private String formatInnovationAreas(List<InnovationAreaResource> innovationAreas) {
        return innovationAreas == null ? EMPTY : innovationAreas.stream()
                .map(CategoryResource::getName)
                .collect(joining(", "));
    }

    private void assertInvitedAssessors(List<AssessorCreatedInviteResource> expectedCreatedInvites, MvcResult result) {
        assertTrue(result.getModelAndView().getModel().get("model") instanceof InterviewPanelInviteAssessorsInviteViewModel);
        InterviewPanelInviteAssessorsInviteViewModel model = (InterviewPanelInviteAssessorsInviteViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(expectedCreatedInvites.size(), model.getAssessors().size());

        forEachWithIndex(expectedCreatedInvites, (i, createdInviteResource) -> {
            InvitedAssessorRowViewModel invitedAssessorRowViewModel = model.getAssessors().get(i);
            assertEquals(createdInviteResource.getName(), invitedAssessorRowViewModel.getName());
            assertEquals(formatInnovationAreas(createdInviteResource.getInnovationAreas()), invitedAssessorRowViewModel.getInnovationAreas());
            assertEquals(createdInviteResource.isCompliant(), invitedAssessorRowViewModel.isCompliant());
            assertEquals(createdInviteResource.getEmail(), invitedAssessorRowViewModel.getEmail());
        });
    }

    private List<InnovationAreaResource> setupCategoryResources() {
        return newInnovationAreaResource()
                .withName("Innovation Area 1", "Innovation Area 2")
                .build(2);
    }

    private void setupDefaultInviteViewExpectations(int page,
                                                    AssessorCreatedInvitePageResource assessorCreatedInvitePageResource,
                                                    List<InnovationAreaResource> children) {
        List<InnovationSectorResource> innovationSectors = newInnovationSectorResource()
                .withName("Innovation Sector 1")
                .withChildren(children)
                .build(1);

        when(interviewPanelInviteRestService.getCreatedInvites(competition.getId(), page)).thenReturn(restSuccess(assessorCreatedInvitePageResource));
        when(categoryRestServiceMock.getInnovationSectors()).thenReturn(restSuccess(innovationSectors));
    }

    private Cookie createFormCookie(InterviewPanelSelectionForm form) throws Exception {
        String cookieContent = JsonUtil.getSerializedObject(form);
        return new Cookie(format("assessorInterviewPanelSelectionForm_comp_%s", competition.getId()), getCompressedString(cookieContent));
    }

    private Optional<InterviewPanelSelectionForm> getAssessorPanelSelectionFormFromCookie(MockHttpServletResponse response, String cookieName) throws Exception {
        String value = getDecompressedString(response.getCookie(cookieName).getValue());
        String decodedFormJson  = URLDecoder.decode(value, CharEncoding.UTF_8);

        if (isNotBlank(decodedFormJson)) {
            return Optional.ofNullable(getObjectFromJson(decodedFormJson, InterviewPanelSelectionForm.class));
        } else {
            return Optional.empty();
        }
    }
}
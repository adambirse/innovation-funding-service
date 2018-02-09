package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.invite.resource.AssessorInviteSendResource;
import org.innovateuk.ifs.invite.resource.AssessorInvitesToSendResource;
import org.innovateuk.ifs.management.form.PanelOverviewSelectionForm;
import org.innovateuk.ifs.management.form.ResendInviteForm;
import org.innovateuk.ifs.management.form.SendInviteForm;
import org.innovateuk.ifs.management.viewmodel.SendInvitesViewModel;
import org.innovateuk.ifs.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;

import static com.google.common.primitives.Longs.asList;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.IN_ASSESSMENT;
import static org.innovateuk.ifs.invite.builder.AssessorInviteSendResourceBuilder.newAssessorInviteSendResource;
import static org.innovateuk.ifs.invite.builder.AssessorInvitesToSendResourceBuilder.newAssessorInvitesToSendResource;
import static org.innovateuk.ifs.util.CollectionFunctions.asLinkedSet;
import static org.innovateuk.ifs.util.CompressionUtil.getCompressedString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AssessmentPanelSendInviteControllerTest extends BaseControllerMockMVCTest<AssessmentPanelSendInviteController> {

    private CompetitionResource competition;

    @Override
    protected AssessmentPanelSendInviteController supplyControllerUnderTest() {
        return new AssessmentPanelSendInviteController();
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
    }

    @Test
    public void getInvitesToSend() throws Exception {
        long competitionId = 1L;

        AssessorInvitesToSendResource invites = newAssessorInvitesToSendResource()
                .withRecipients(singletonList("Jessica Doe"))
                .withCompetitionId(competitionId)
                .withCompetitionName("Photonics for health")
                .withContent("Readonly content")
                .build();

        when(reviewPanelInviteRestService.getAllInvitesToSend(competitionId)).thenReturn(restSuccess(invites));

        SendInviteForm expectedForm = new SendInviteForm();
        expectedForm.setSubject("Invitation to assessment panel for 'Photonics for health'");

        SendInvitesViewModel expectedViewModel = new SendInvitesViewModel(competitionId, "Photonics for health", singletonList( "Jessica Doe"), "Readonly content");

        mockMvc.perform(get("/panel/competition/{competitionId}/assessors/invite/send", competitionId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("assessors/panel-send-invites"));

        verify(reviewPanelInviteRestService, only()).getAllInvitesToSend(competitionId);
    }

    @Test
    public void getInvitesToResend() throws Exception {
        List<Long> inviteIds = asList(1L, 2L);
        List<String> recipients = Arrays.asList("Jessica Doe", "Fred Smith");
        AssessorInvitesToSendResource invite = newAssessorInvitesToSendResource()
                .withRecipients(recipients)
                .withCompetitionId(competition.getId())
                .withCompetitionName("Photonics for health")
                .withContent("Readonly content")
                .build();

        PanelOverviewSelectionForm expectedSelectionForm = new PanelOverviewSelectionForm();
        expectedSelectionForm.setSelectedInviteIds(inviteIds);
        Cookie selectionFormCookie = createFormCookie(expectedSelectionForm);

        when(reviewPanelInviteRestService.getAllInvitesToResend(competition.getId(), inviteIds)).thenReturn(restSuccess(invite));

        ResendInviteForm expectedForm = new ResendInviteForm();
        expectedForm.setSubject("Invitation to assessment panel for 'Photonics for health'");
        expectedForm.setInviteIds(inviteIds);

        SendInvitesViewModel expectedViewModel = new SendInvitesViewModel(competition.getId(), "Photonics for health", recipients, "Readonly content");

        mockMvc.perform(post("/panel/competition/{competitionId}/assessors/invite/reviewResend", competition.getId())
                .cookie(selectionFormCookie))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("assessors/panel-resend-invites"));

        verify(reviewPanelInviteRestService, only()).getAllInvitesToResend(competition.getId(), inviteIds);
    }

    @Test
    public void sendInvites() throws Exception {
        long competitionId = 5L;

        AssessorInviteSendResource expectedAssessorInviteSendResource = newAssessorInviteSendResource()
                .withSubject("Subject...")
                .withContent("Editable content...")
                .build();

        when(reviewPanelInviteRestService.sendAllInvites(competitionId, expectedAssessorInviteSendResource)).thenReturn(restSuccess());

        mockMvc.perform(post("/panel/competition/{competitionId}/assessors/invite/send", competitionId)
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("subject", "Subject...")
                .param("content", "Editable content..."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/assessment/panel/competition/%s/assessors/overview", competitionId)));

        InOrder inOrder = inOrder(reviewPanelInviteRestService);
        inOrder.verify(reviewPanelInviteRestService).sendAllInvites(competitionId, expectedAssessorInviteSendResource);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void resendInvites() throws Exception {
        List<Long> inviteIds = asList(1L, 2L);

        AssessorInvitesToSendResource invite = newAssessorInvitesToSendResource().withCompetitionId(competition.getId()).build();

        AssessorInviteSendResource expectedAssessorInviteSendResource = newAssessorInviteSendResource()
                .withSubject("Subject...")
                .withContent("Editable content...")
                .build();

        when(reviewPanelInviteRestService.resendInvites(inviteIds, expectedAssessorInviteSendResource)).thenReturn(restSuccess());

        mockMvc.perform(post("/panel/competition/{competitionId}/assessors/invite/resend", competition.getId())
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("inviteIds[0]", inviteIds.get(0).toString())
                .param("inviteIds[1]", inviteIds.get(1).toString())
                .param("subject", "Subject...")
                .param("content", "Editable content..."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/assessment/panel/competition/%s/assessors/overview?page=0", competition.getId())));

        InOrder inOrder = inOrder(reviewPanelInviteRestService);
        inOrder.verify(reviewPanelInviteRestService).resendInvites(inviteIds, expectedAssessorInviteSendResource);
        inOrder.verifyNoMoreInteractions();
    }

    private Cookie createFormCookie(PanelOverviewSelectionForm form) throws Exception {
        String cookieContent = JsonUtil.getSerializedObject(form);
        return new Cookie(format("assessorPanelOverviewSelectionForm_comp_%s", competition.getId()), getCompressedString(cookieContent));
    }
}

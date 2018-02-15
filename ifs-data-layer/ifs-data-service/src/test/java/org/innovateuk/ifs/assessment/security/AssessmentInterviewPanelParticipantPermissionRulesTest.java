package org.innovateuk.ifs.assessment.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.invite.resource.AssessmentInterviewPanelParticipantResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;

import static freemarker.template.utility.Collections12.singletonList;
import static org.innovateuk.ifs.assessment.builder.AssessmentInterviewPanelInviteResourceBuilder.newAssessmentInterviewPanelInviteResource;
import static org.innovateuk.ifs.invite.builder.AssessmentInterviewPanelParticipantResourceBuilder.newAssessmentInterviewPanelParticipantResource;
import static org.innovateuk.ifs.invite.constant.InviteStatus.SENT;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AssessmentInterviewPanelParticipantPermissionRulesTest extends BasePermissionRulesTest<AssessmentInterviewPanelParticipantPermissionRules> {

    @Override
    protected AssessmentInterviewPanelParticipantPermissionRules supplyPermissionRulesUnderTest() {
        return new AssessmentInterviewPanelParticipantPermissionRules();
    }

    @Test
    public void userCanAcceptAssessmentPanelInvite() {
        AssessmentInterviewPanelParticipantResource assessmentInterviewPanelParticipantResource = newAssessmentInterviewPanelParticipantResource()
                .withUser(1L)
                .build();
        UserResource userResource = newUserResource()
                .withId(1L)
                .withRolesGlobal(singletonList(assessorRole()))
                .build();

        assertTrue(rules.userCanAcceptAssessmentInterviewPanelInvite(assessmentInterviewPanelParticipantResource, userResource));
    }

    @Test
    public void userCanAcceptAssessmentPanelInvite_differentParticipantUser() {
        AssessmentInterviewPanelParticipantResource assessmentInterviewPanelParticipantResource = newAssessmentInterviewPanelParticipantResource()
                .withUser(1L)
                .build();
        UserResource userResource = newUserResource()
                .withId(2L)
                .withRolesGlobal(singletonList(assessorRole()))
                .build();

        assertFalse(rules.userCanAcceptAssessmentInterviewPanelInvite(assessmentInterviewPanelParticipantResource, userResource));
    }

    @Test
    public void userCanAcceptAssessmentPanelInvite_noParticipantUserAndSameEmail() {
        AssessmentInterviewPanelParticipantResource assessmentInterviewPanelParticipantResource = newAssessmentInterviewPanelParticipantResource()
                .withInvite(newAssessmentInterviewPanelInviteResource().withEmail("tom@poly.io"))
                .build();
        UserResource userResource = newUserResource()
                .withEmail("tom@poly.io")
                .withRolesGlobal(singletonList(assessorRole()))
                .build();

        assertTrue(rules.userCanAcceptAssessmentInterviewPanelInvite(assessmentInterviewPanelParticipantResource, userResource));
    }

    @Test
    public void userCanAcceptCompetitionInvite_noParticipantUserAndDifferentEmail() {
        AssessmentInterviewPanelParticipantResource assessmentInterviewPanelParticipantResource = newAssessmentInterviewPanelParticipantResource()
                .withInvite(newAssessmentInterviewPanelInviteResource().withEmail("tom@poly.io"))
                .build();
        UserResource userResource = newUserResource()
                .withEmail("non-existent-email@poly.io")
                .withRolesGlobal(singletonList(assessorRole()))
                .build();

        assertFalse(rules.userCanAcceptAssessmentInterviewPanelInvite(assessmentInterviewPanelParticipantResource, userResource));
    }

    @Test
    public void userCanViewTheirOwnAssessmentPanelParticipation() {
        AssessmentInterviewPanelParticipantResource assessmentInterviewPanelParticipantResource = newAssessmentInterviewPanelParticipantResource()
                .withUser(7L)
                .withInvite(newAssessmentInterviewPanelInviteResource().withStatus(SENT).build())
                .build();
        UserResource userResource = newUserResource()
                .withId(7L)
                .withRolesGlobal(singletonList(assessorRole()))
                .build();

        assertTrue(rules.userCanViewTheirOwnAssessmentPanelParticipation(assessmentInterviewPanelParticipantResource, userResource));
    }

    @Test
    public void userCanViewTheirOwnAssessmentPanelParticipation_differentUser() {
        AssessmentInterviewPanelParticipantResource assessmentInterviewPanelParticipantResource = newAssessmentInterviewPanelParticipantResource()
                .withUser(7L)
                .build();
        UserResource userResource = newUserResource()
                .withId(11L)
                .withRolesGlobal(singletonList(assessorRole()))
                .build();

        assertFalse(rules.userCanViewTheirOwnAssessmentPanelParticipation(assessmentInterviewPanelParticipantResource, userResource));
    }
}
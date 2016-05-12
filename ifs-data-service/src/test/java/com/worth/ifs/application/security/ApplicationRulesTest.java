package com.worth.ifs.application.security;

import com.worth.ifs.BasePermissionRulesTest;
import com.worth.ifs.application.builder.ApplicationStatusResourceBuilder;
import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.ApplicationStatusResource;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.resource.UserRoleType;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.RoleBuilder.newRole;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static com.worth.ifs.user.resource.UserRoleType.LEADAPPLICANT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;

public class ApplicationRulesTest extends BasePermissionRulesTest<ApplicationRules> {

    @Override
    protected ApplicationRules supplyPermissionRulesUnderTest() {
        return new ApplicationRules();
    }

    private ApplicationStatusResource applicationStatusOpen;
    private ApplicationResource applicationResource1;
    private ApplicationResource applicationResource2;
    private Application application1;
    private Application application2;
    private ProcessRole processRole1;
    private ProcessRole processRole2;
    private ProcessRole assessorProcessRole;
    private UserResource user1;
    private UserResource user2;
    private UserResource user3;
    private UserResource assessor;
    private UserResource compAdmin;

    private Role leadApplicantRole = newRole().withType(LEADAPPLICANT).build();
    private Role collaboratorRole = newRole().withType(UserRoleType.COLLABORATOR).build();
    private Role applicantRole = newRole().withType(UserRoleType.APPLICANT).build();
    private Role assessorRole = newRole().withType(UserRoleType.ASSESSOR).build();
    private List<Role> applicantRoles = new ArrayList<>();

    @Before
    public void setup() {
        user1 = newUserResource().build();
        user2 = newUserResource().build();
        user3 = newUserResource().build();
        compAdmin = compAdminUser();
        assessor = assessorUser();

        processRole1 = newProcessRole().withRole(leadApplicantRole).build();
        processRole2 = newProcessRole().withRole(applicantRole).build();
        assessorProcessRole = newProcessRole().withRole(assessorRole).build();
        applicationStatusOpen = ApplicationStatusResourceBuilder.newApplicationStatusResource().withName(ApplicationStatusConstants.OPEN).build();
        applicationResource1 = newApplicationResource().withProcessRoles(asList(processRole1.getId())).withApplicationStatus(ApplicationStatusConstants.OPEN).build();
        applicationResource2 = newApplicationResource().withProcessRoles(asList(processRole2.getId())).build();
        application1 = newApplication().withId(applicationResource1.getId()).withProcessRoles(processRole1).build();
        application2 = newApplication().withId(applicationResource2.getId()).withProcessRoles(processRole2).build();
        processRole1.setApplication(application1);
        processRole2.setApplication(application2);

        applicantRoles.add(leadApplicantRole);
        applicantRoles.add(collaboratorRole);

        when(applicationRepositoryMock.exists(applicationResource1.getId())).thenReturn(true);
        when(applicationRepositoryMock.exists(applicationResource2.getId())).thenReturn(true);
        when(applicationRepositoryMock.exists(null)).thenReturn(false);

        when(roleRepositoryMock.findByNameIn(anyList())).thenReturn(applicantRoles);
        when(roleRepositoryMock.findByName(leadApplicantRole.getName())).thenReturn(singletonList(leadApplicantRole));

        when(processRoleRepositoryMock.findByUserIdAndApplicationId(user1.getId(), applicationResource1.getId())).thenReturn(processRole1);
        when(processRoleRepositoryMock.findByUserIdAndApplicationId(user1.getId(), applicationResource2.getId())).thenReturn(null);
        when(processRoleRepositoryMock.findByUserIdAndApplicationId(user2.getId(), applicationResource1.getId())).thenReturn(null);
        when(processRoleRepositoryMock.findByUserIdAndApplicationId(user2.getId(), applicationResource2.getId())).thenReturn(processRole1);
        when(processRoleRepositoryMock.findByUserIdAndApplicationId(user3.getId(), applicationResource2.getId())).thenReturn(processRole2);

        when(processRoleRepositoryMock.findByUserIdAndRoleAndApplicationId(user1.getId(), leadApplicantRole, applicationResource1.getId())).thenReturn(singletonList(processRole1));
        when(processRoleRepositoryMock.findByUserIdAndRoleAndApplicationId(user1.getId(), leadApplicantRole, applicationResource2.getId())).thenReturn(emptyList());
        when(processRoleRepositoryMock.findByUserIdAndRoleAndApplicationId(user2.getId(), leadApplicantRole, applicationResource1.getId())).thenReturn(emptyList());
        when(processRoleRepositoryMock.findByUserIdAndRoleAndApplicationId(user2.getId(), leadApplicantRole, applicationResource2.getId())).thenReturn(emptyList());
        when(processRoleRepositoryMock.findByUserIdAndRoleAndApplicationId(user3.getId(), leadApplicantRole, applicationResource2.getId())).thenReturn(emptyList());
        when(processRoleRepositoryMock.findByUserIdAndRoleInAndApplicationId(user1.getId(), applicantRoles, applicationResource1.getId())).thenReturn(singletonList(processRole1));
        when(processRoleRepositoryMock.findByUserIdAndRoleInAndApplicationId(user2.getId(), applicantRoles, applicationResource1.getId())).thenReturn(singletonList(processRole1));
        when(processRoleRepositoryMock.findByUserIdAndRoleInAndApplicationId(user3.getId(), applicantRoles, applicationResource1.getId())).thenReturn(emptyList());
        when(processRoleRepositoryMock.findByUserIdAndApplicationId(assessor.getId(), applicationResource1.getId())).thenReturn(assessorProcessRole);
    }

    @Test
    public void applicantCanSeeConnectedApplicationResourceTest() {
        assertTrue(rules.applicantCanSeeConnectedApplicationResource(applicationResource1, user1));
        assertTrue(rules.applicantCanSeeConnectedApplicationResource(applicationResource2, user2));
    }

    @Test
    public void applicantCannotSeeUnconnectedApplicationResourceTest() {
        assertFalse(rules.applicantCanSeeConnectedApplicationResource(applicationResource1, user2));
        assertFalse(rules.applicantCanSeeConnectedApplicationResource(applicationResource2, user1));
    }

    @Test
    public void onlyUsersPartOfTheApplicationCanChangeApplicationResourceTest() {
        assertTrue(rules.applicantCanUpdateApplicationResource(applicationResource1, user1));
        assertTrue(rules.applicantCanUpdateApplicationResource(applicationResource1, user2));
        assertFalse(rules.applicantCanUpdateApplicationResource(applicationResource1, user3));
    }

    @Test
    public void userIsConnectedToApplicationResourceTest() {
        assertTrue(rules.userIsConnectedToApplicationResource(applicationResource1, user1));
    }


    @Test
    public void assessorCanSeeTheResearchParticipantPercentageInApplicationsTheyAssessTest() {
        assertTrue(rules.assessorCanSeeTheResearchParticipantPercentageInApplicationsTheyAssess(applicationResource1, assessor));
        assertFalse(rules.assessorCanSeeTheResearchParticipantPercentageInApplicationsTheyAssess(applicationResource1, compAdmin));
    }

    @Test
    public void consortiumCanSeeTheResearchParticipantPercentageTest() {
        assertTrue(rules.consortiumCanSeeTheResearchParticipantPercentage(applicationResource1, user1));
        assertFalse(rules.consortiumCanSeeTheResearchParticipantPercentage(applicationResource1, compAdmin));
    }

    @Test
    public void compAdminCanSeeTheResearchParticipantPercentageInApplications() {
        assertTrue(rules.compAdminCanSeeTheResearchParticipantPercentageInApplications(applicationResource1, compAdmin));
        assertFalse(rules.compAdminCanSeeTheResearchParticipantPercentageInApplications(applicationResource1, user1));
    }


    @Test
    public void applicationExistsTest() {
        ApplicationResource noId = newApplicationResource().build();

        assertTrue("applicationExists should return true when called with existing application", rules.applicationExists(applicationResource1));
        assertFalse("applicationExists should return false when called with non-existing application", rules.applicationExists(noId));
        noId.setId(null);
        assertFalse("applicationExists should return false when called with application without an id", rules.applicationExists(noId));
    }


}

package org.innovateuk.ifs.invite.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.resource.InviteProjectResource;
import org.innovateuk.ifs.invite.transactional.ProjectInviteService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.method.P;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.builder.ProjectInviteResourceBuilder.newInviteProjectResource;
import static org.innovateuk.ifs.invite.security.ProjectInviteServiceSecurityTest.TestProjectInviteService.ARRAY_SIZE_FOR_POST_FILTER_TESTS;
import static org.innovateuk.ifs.user.resource.UserRoleType.SYSTEM_REGISTRATION_USER;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Testing how the secured methods in ProjectInviteService interact with Spring Security
 */
public class ProjectInviteServiceSecurityTest extends BaseServiceSecurityTest<ProjectInviteService> {

    private ProjectInvitePermissionRules projectInvitePermissionRules;


    @Before
    public void lookupPermissionRules() {
        projectInvitePermissionRules = getMockPermissionRulesBean(ProjectInvitePermissionRules.class);

    }

    @Test
    public void testAcceptProjectInviteOnlyAllowedForSystemRegistrar() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.acceptProjectInvite("hash", 1L), SYSTEM_REGISTRATION_USER);
    }

    @Test
    public void testCheckUserExistingByInviteHashOnlyAllowedForSystemRegistrar() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.checkExistingUser("hash"), SYSTEM_REGISTRATION_USER);
    }

    @Test
    public void testGetInviteByHashOnlyAllowedForSystemRegistrar() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.getInviteByHash("hash"), SYSTEM_REGISTRATION_USER);
    }

    @Test
    public void testGetUserByInviteHashOnlyAllowedForSystemRegistrar() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.getUserByInviteHash("hash"), SYSTEM_REGISTRATION_USER);
    }

    @Test
    public void testSaveFinanceContact() {
        final InviteProjectResource invite = newInviteProjectResource().build();
        assertAccessDenied(
                () -> classUnderTest.saveProjectInvite(invite),
                () -> {
                    verify(projectInvitePermissionRules).partnersOnProjectCanSaveInvite(any(InviteProjectResource.class), any(UserResource.class));
                });
    }

    @Test
    public void testGetInvitesByProject() {
        long projectId = 1L;
        ServiceResult<List<InviteProjectResource>> invitesByProject = classUnderTest.getInvitesByProject(projectId);
        verify(projectInvitePermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).partnersOnProjectCanViewInvite(any(InviteProjectResource.class), any(UserResource.class));
        assertTrue(invitesByProject.getSuccess().isEmpty());
    }

    @Override
    protected Class<? extends ProjectInviteService> getClassUnderTest() {
        return TestProjectInviteService.class;
    }

    public static class TestProjectInviteService implements ProjectInviteService {

        static final int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

        @Override
        public ServiceResult<Void> saveProjectInvite(@P("inviteProjectResource") InviteProjectResource inviteProjectResource)  {
            return null;
        }

        @Override
        public ServiceResult<List<InviteProjectResource>> getInvitesByProject(Long projectId) {
            return serviceSuccess(newInviteProjectResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<Void> acceptProjectInvite(String inviteHash, Long userId) {
            return null;
        }

        @Override
        public ServiceResult<InviteProjectResource> getInviteByHash(String hash) {
            return null;
        }

        @Override
        public ServiceResult<Boolean> checkExistingUser(String hash) {
            return null;
        }

        @Override
        public ServiceResult<UserResource> getUserByInviteHash(String hash) {
            return null;
        }
    }
}

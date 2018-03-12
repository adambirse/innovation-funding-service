package org.innovateuk.ifs.invite.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.resource.ExternalInviteResource;
import org.innovateuk.ifs.invite.resource.RoleInvitePageResource;
import org.innovateuk.ifs.invite.resource.RoleInviteResource;
import org.innovateuk.ifs.invite.transactional.InviteUserService;
import org.innovateuk.ifs.invite.transactional.InviteUserServiceImpl;
import org.innovateuk.ifs.user.builder.UserResourceBuilder;
import org.innovateuk.ifs.user.resource.SearchCategory;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class InviteUserServiceSecurityTest extends BaseServiceSecurityTest<InviteUserService> {

    private InviteUserPermissionRules inviteUserPermissionRules;

    @Before
    public void lookupPermissionRules() {
        inviteUserPermissionRules = getMockPermissionRulesBean(InviteUserPermissionRules.class);
    }

    @Test
    public void testSaveUserInvite() {
        UserResource invitedUser = UserResourceBuilder.newUserResource().build();

        assertAccessDenied(
                () -> classUnderTest.saveUserInvite(invitedUser, UserRoleType.SUPPORT),
                () -> {
                    verify(inviteUserPermissionRules)
                            .ifsAdminCanSaveNewUserInvite(any(UserResource.class), any(UserResource.class));
                    verifyNoMoreInteractions(inviteUserPermissionRules);
                });
    }

    @Test
    public void testFindPendingInternalUserInvites() {
        Pageable pageable = new PageRequest(0, 5);

        when(classUnderTestMock.findPendingInternalUserInvites(pageable))
                .thenReturn(serviceSuccess(new RoleInvitePageResource()));

        assertAccessDenied(
                () -> classUnderTest.findPendingInternalUserInvites(pageable),
                () -> {
                    verify(inviteUserPermissionRules)
                            .internalUsersCanViewPendingInternalUserInvites(any(RoleInvitePageResource.class), any(UserResource.class));
                    verifyNoMoreInteractions(inviteUserPermissionRules);
                });
    }

    @Test
    public void testResendPendingInternalUserInvites() {
        assertRolesCanPerform(() -> classUnderTest.resendInternalUserInvite(123L),
                UserRoleType.IFS_ADMINISTRATOR);
    }

    @Override
    protected Class<? extends InviteUserService> getClassUnderTest() {
        return InviteUserServiceImpl.class;
    }
}

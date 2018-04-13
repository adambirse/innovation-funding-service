package org.innovateuk.ifs.thread.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.finance.domain.ProjectFinance;
import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.project.domain.ProjectProcess;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.threads.resource.FinanceChecksSectionType;
import org.innovateuk.ifs.threads.resource.PostResource;
import org.innovateuk.ifs.threads.resource.QueryResource;
import org.innovateuk.ifs.threads.security.ProjectFinanceQueryPermissionRules;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.workflow.domain.ActivityState;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.finance.domain.builder.ProjectFinanceBuilder.newProjectFinance;
import static org.innovateuk.ifs.project.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.builder.ProjectProcessBuilder.newProjectProcess;
import static org.innovateuk.ifs.thread.security.ProjectFinanceThreadsTestData.projectFinanceWithUserAsFinanceContact;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.PARTNER;
import static org.innovateuk.ifs.workflow.domain.ActivityType.PROJECT_SETUP;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

public class ProjectFinanceQueryPermissionRulesTest extends BasePermissionRulesTest<ProjectFinanceQueryPermissionRules> {
    private QueryResource queryResource;
    private UserResource projectFinanceUser;
    private UserResource partner;
    private UserResource incorrectPartner;
    private Project project;
    private ProjectFinance projectFinance;
    private ProjectProcess projectProcess;

    @Before
    public void setUp() throws Exception {
        projectFinanceUser = projectFinanceUser();
        partner = getUserWithRole(PARTNER);

        queryResource = queryWithoutPosts();
        queryResource.posts.add(new PostResource(1L, projectFinanceUser, "The body", new ArrayList<>(), ZonedDateTime.now()));

        incorrectPartner = newUserResource().withId(1993L).withRolesGlobal(singletonList(PARTNER)).build();
        incorrectPartner.setId(1993L);

        project = newProject().build();
        projectFinance = newProjectFinance().withProject(project).build();
        projectProcess = newProjectProcess().withActivityState(new ActivityState(PROJECT_SETUP, ProjectState.LIVE.getBackingState())).build();

        when(projectFinanceRepositoryMock.findOne(anyLong())).thenReturn(projectFinance);
        when(projectProcessRepositoryMock.findOneByTargetId(anyLong())).thenReturn(projectProcess);
    }

    private QueryResource queryWithoutPosts() {
        return new QueryResource(1L, 22L, new ArrayList<>(),
                FinanceChecksSectionType.VIABILITY, "First Query", true, ZonedDateTime.now(), null, null);
    }

    @Override
    protected ProjectFinanceQueryPermissionRules supplyPermissionRulesUnderTest() {
        return new ProjectFinanceQueryPermissionRules();
    }

    @Test
    public void testThatOnlyProjectFinanceProjectFinanceUsersCanCreateQueries() throws Exception {
        assertTrue(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, projectFinanceUser));
        assertFalse(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, partner));
    }

    @Test
    public void testThatNewQueryMustContainInitialPost() throws Exception {
        assertTrue(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, projectFinanceUser));
        assertFalse(rules.onlyProjectFinanceUsersCanCreateQueries(queryWithoutPosts(), partner));
    }

    @Test
    public void testThatNewQueryInitialPostAuthorMustBeTheCurrentUser() throws Exception {
        assertTrue(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, projectFinanceUser));
        UserResource anotherProjectFinanceUser = newUserResource().withId(675L)
                .withRolesGlobal(singletonList(Role.PROJECT_FINANCE)).build();
        assertFalse(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, anotherProjectFinanceUser));
    }

    @Test
    public void testThatFirstPostMustComeFromTheProjectFinanceUser() throws Exception {
        QueryResource queryWithoutPosts = queryWithoutPosts();
        assertTrue(rules.projectFinanceUsersCanAddPostToTheirQueries(queryWithoutPosts, projectFinanceUser));
        when(projectFinanceRepositoryMock.findOne(queryWithoutPosts.contextClassPk))
                .thenReturn(projectFinanceWithUserAsFinanceContact(partner));
        assertFalse(rules.projectFinanceUsersCanAddPostToTheirQueries(queryWithoutPosts, partner));
    }

    @Test
    public void testThatOnlyTheProjectFinanceUserOrTheCorrectFinanceContactCanReplyToAQuery() throws Exception {
        when(projectFinanceRepositoryMock.findOne(queryResource.contextClassPk))
                .thenReturn(projectFinanceWithUserAsFinanceContact(partner));
        assertTrue(rules.projectFinanceUsersCanAddPostToTheirQueries(queryResource, projectFinanceUser));
        assertTrue(rules.projectPartnersCanAddPostToTheirQueries(queryResource, partner));
        assertFalse(rules.projectPartnersCanAddPostToTheirQueries(queryResource, incorrectPartner));
    }

    @Test
    public void testThatOnlyProjectFinanceUsersOrProjectUsersCanViewTheirQueries() {
        assertTrue(rules.projectFinanceUsersCanViewQueries(queryResource, projectFinanceUser));
        when(projectFinanceRepositoryMock.findOne(queryResource.contextClassPk))
                .thenReturn(projectFinanceWithUserAsFinanceContact(partner));
        assertTrue(rules.projectPartnersCanViewQueries(queryResource, partner));
        assertFalse(rules.projectPartnersCanViewQueries(queryResource, incorrectPartner));
    }
}

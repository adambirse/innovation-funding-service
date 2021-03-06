package org.innovateuk.ifs.application.overview;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.commons.exception.ObjectNotFoundException;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class ApplicationOverviewData {

    private final CompetitionResource competition;
    private final ApplicationResource application;
    private final Map<Long, SectionResource> sections;
    private final Map<Long, QuestionResource> questions;
    private final Map<Long, ProcessRoleResource> processRoles;
    private final OrganisationResource organisation;
    private final Multimap<Long, QuestionStatusResource> statuses;
    private final ProcessRoleResource userProcessRole;
    private final ProcessRoleResource leadApplicant;
    private final List<ApplicationInviteResource> invites;
    private final List<Long> completedSectionIds;
    private final UserResource user;

    public ApplicationOverviewData(CompetitionResource competition, ApplicationResource application, List<SectionResource> sections,
                                   List<QuestionResource> questions, List<ProcessRoleResource> processRoles,
                                   OrganisationResource organisation, List<QuestionStatusResource> statuses,
                                   List<ApplicationInviteResource> invites, List<Long> completedSectionIds, UserResource user) {
        this.competition = competition;
        this.application = application;
        this.sections = sections.stream()
            .collect(toMap(SectionResource::getId, Function.identity()));;
        this.questions = questions.stream()
            .collect(toMap(QuestionResource::getId, Function.identity()));
        this.processRoles = processRoles.stream()
            .collect(toMap(ProcessRoleResource::getId, Function.identity()));
        this.organisation = organisation;
        this.statuses = Multimaps.index(statuses, QuestionStatusResource::getQuestion);
        this.userProcessRole = processRoles.stream().filter(role -> role.getUser().equals(user.getId())).findFirst().orElseThrow(ObjectNotFoundException::new);
        this.leadApplicant = processRoles.stream().filter(role -> role.getRole().isLeadApplicant()).findAny().orElseThrow(ObjectNotFoundException::new);
        this.invites = invites;
        this.completedSectionIds = completedSectionIds;
        this.user = user;
    }

    public CompetitionResource getCompetition() {
        return competition;
    }

    public ApplicationResource getApplication() {
        return application;
    }

    public Map<Long, SectionResource> getSections() {
        return sections;
    }

    public Map<Long, QuestionResource> getQuestions() {
        return questions;
    }

    public Map<Long, ProcessRoleResource> getProcessRoles() {
        return processRoles;
    }

    public OrganisationResource getOrganisation() {
        return organisation;
    }

    public Multimap<Long, QuestionStatusResource> getStatuses() {
        return statuses;
    }

    public ProcessRoleResource getUserProcessRole() {
        return userProcessRole;
    }

    public ProcessRoleResource getLeadApplicant() {
        return leadApplicant;
    }

    public List<ApplicationInviteResource> getInvites() {
        return invites;
    }

    public List<Long> getCompletedSectionIds() {
        return completedSectionIds;
    }

    public UserResource getUser() {
        return user;
    }
}

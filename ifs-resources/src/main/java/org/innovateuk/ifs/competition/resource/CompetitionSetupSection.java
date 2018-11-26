package org.innovateuk.ifs.competition.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection.*;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.PROJECT_SETUP;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;

/**
 * This enum defines all sections of competition setup.
 * It is used when recording which sections are marked as complete during the competition setup process.
 */
public enum CompetitionSetupSection {
    HOME(1L, "home", "Home Page", emptyList(), false),
    INITIAL_DETAILS(2L, "initial", "Initial details", emptyList(), true),
    TERMS_AND_CONDITIONS(9L, "terms-and-conditions", "Terms and conditions", emptyList(), false),
    ADDITIONAL_INFO(3L, "additional", "Funding information", emptyList(), true),
    ELIGIBILITY(4L, "eligibility", "Eligibility", emptyList(), false),
    COMPLETION_STAGE(11L, "completion-stage", "Milestones", emptyList(), false, Optional.empty()),
    MILESTONES(5L, "milestones", "Milestones", emptyList(), true, Optional.of(COMPLETION_STAGE)),
    APPLICATION_FORM(6L, "application", "Application", asList(PROJECT_DETAILS, QUESTIONS, FINANCES, APPLICATION_DETAILS), false),
    ASSESSORS(7L, "assessors", "Assessors", emptyList(), true),
    CONTENT(8L, "content", "Public content", emptyList(), true),
    PROJECT_DOCUMENT(10L, "project-document", "Documents in project setup", emptyList(), false);

    private Long id;
    private String path;
    private String name;
    private List<CompetitionSetupSubsection> subsections;
    private Optional<CompetitionSetupSection> previousSection;

    private boolean editableAfterSetupAndLive;

    private static Map<String, CompetitionSetupSection> PATH_MAP;

    static {
        PATH_MAP = new HashMap<>();
        for (CompetitionSetupSection section : values()) {
            PATH_MAP.put(section.getPath(), section);
        }
    }

    CompetitionSetupSection(Long id, String sectionPath, String sectionName, List<CompetitionSetupSubsection> subsections, boolean editableAfterSetupAndLive) {
        this(id, sectionPath, sectionName, subsections, editableAfterSetupAndLive, Optional.empty());
    }

    CompetitionSetupSection(Long id, String sectionPath,
                            String sectionName, List<CompetitionSetupSubsection> subsections,
                            boolean editableAfterSetupAndLive,
                            Optional<CompetitionSetupSection> previousSection) {
        this.id = id;
        this.path = sectionPath;
        this.name = sectionName;
        this.subsections = subsections;
        this.editableAfterSetupAndLive = editableAfterSetupAndLive;
        this.previousSection = previousSection;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public List<CompetitionSetupSubsection> getSubsections() {
        return subsections;
    }

    public static CompetitionSetupSection fromPath(String path) {
        return PATH_MAP.get(path);
    }

    public Boolean getEditableAfterSetupAndLive() {
        return editableAfterSetupAndLive;
    }

    public boolean preventEdit(CompetitionResource competitionResource) {
        if (competitionResource.isSetupAndAfterNotifications()) {
            return true;
        }
        if (this == ASSESSORS) {
            return competitionResource.isAssessmentClosed();
        } else if (this == PROJECT_DOCUMENT) {
            return competitionResource.getCompetitionStatus().equals(PROJECT_SETUP);
        } else if (competitionResource.isSetupAndLive()) {
            return !this.getEditableAfterSetupAndLive();
        }

        return false;
    }

    public boolean hasDisplayableSetupFragment() {
        return HOME != this;
    }

    public Long getId() {
        return id;
    }

    public String getPostMarkCompletePath() {

        Optional<CompetitionSetupSection> nextSection = simpleFindFirst(CompetitionSetupSection.values(),
                section -> section.getPreviousSection().equals(Optional.of(this)));

        return nextSection.map(CompetitionSetupSection::getPath).orElse(path);
    }

    public String getPostMarkIncompletePath() {
        return getFirstSectionInChain(this).getPath();
    }

    public Optional<CompetitionSetupSection> getPreviousSection() {
        return previousSection;
    }

    private static CompetitionSetupSection getFirstSectionInChain(CompetitionSetupSection section) {
        return section.getPreviousSection().map(CompetitionSetupSection::getFirstSectionInChain).orElse(section);
    }
}

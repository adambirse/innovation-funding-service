package com.worth.ifs.application.builder;

import static com.worth.ifs.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.function.BiConsumer;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.application.resource.CompetitionSummaryResource;
import com.worth.ifs.competition.resource.CompetitionResource.Status;

public class CompetitionSummaryResourceBuilder extends BaseBuilder<CompetitionSummaryResource, CompetitionSummaryResourceBuilder> {

    private CompetitionSummaryResourceBuilder(List<BiConsumer<Integer, CompetitionSummaryResource>> multiActions) {
        super(multiActions);
    }

    public static CompetitionSummaryResourceBuilder newApplicationStatus() {
        return new CompetitionSummaryResourceBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected CompetitionSummaryResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CompetitionSummaryResource>> actions) {
        return new CompetitionSummaryResourceBuilder(actions);
    }

    @Override
    protected CompetitionSummaryResource createInitial() {
        return new CompetitionSummaryResource();
    }

    public CompetitionSummaryResourceBuilder withCompetitionStatus(Status... competitionStatus) {
        return withArray((competitionState, competition) -> competition.setCompetitionStatus(competitionState), competitionStatus);
    }

}

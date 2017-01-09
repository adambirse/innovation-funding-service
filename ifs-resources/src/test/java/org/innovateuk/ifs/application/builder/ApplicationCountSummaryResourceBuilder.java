package org.innovateuk.ifs.application.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryResource;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;

public class ApplicationCountSummaryResourceBuilder extends BaseBuilder<ApplicationCountSummaryResource, ApplicationCountSummaryResourceBuilder>{

    private ApplicationCountSummaryResourceBuilder(List<BiConsumer<Integer, ApplicationCountSummaryResource>> multiActions) {
        super(multiActions);
    }

    public static ApplicationCountSummaryResourceBuilder newApplicationCountSummaryResource() {
        return new ApplicationCountSummaryResourceBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected ApplicationCountSummaryResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ApplicationCountSummaryResource>> actions) {
        return new ApplicationCountSummaryResourceBuilder(actions);
    }

    @Override
    protected ApplicationCountSummaryResource createInitial() {
        return new ApplicationCountSummaryResource();
    }

    public ApplicationCountSummaryResourceBuilder withId(Long... ids) {
        return withArray((id, application) -> setField("id", id, application), ids);
    }

    public ApplicationCountSummaryResourceBuilder withName(String... names) {
        return withArray((name, application) -> setField("name", name, application), names);
    }

    public ApplicationCountSummaryResourceBuilder withLeadOrganisation(String... leadOrganisations) {
        return withArray((leadOrganisation, application) -> setField("leadOrganisation", leadOrganisation, application), leadOrganisations);
    }

    public ApplicationCountSummaryResourceBuilder withAssessors(Long... assessorss) {
        return withArray((assessors, application) -> setField("assessors", assessors, application), assessorss);
    }

    public ApplicationCountSummaryResourceBuilder withAccepted(Long... accepteds) {
        return withArray((accepted, application) -> setField("accepted", accepted, application), accepteds);
    }

    public ApplicationCountSummaryResourceBuilder withSubmitted(Long... submitteds) {
        return withArray((submitted, application) -> setField("submitted", submitted, application), submitteds);
    }

}

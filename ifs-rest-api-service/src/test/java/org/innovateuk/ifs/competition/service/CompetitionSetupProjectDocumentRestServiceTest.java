package org.innovateuk.ifs.competition.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.builder.ProjectDocumentResourceBuilder;
import org.innovateuk.ifs.competition.resource.CompetitionDocumentResource;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.projectDocumentResourceListType;
import static org.junit.Assert.assertTrue;

public class CompetitionSetupProjectDocumentRestServiceTest extends BaseRestServiceUnitTest<CompetitionSetupProjectDocumentRestServiceImpl> {

    private static final String competitionSetupProjectDocumentRestURL = "/competition/setup/project-document";

    @Override
    protected CompetitionSetupProjectDocumentRestServiceImpl registerRestServiceUnderTest() {
        return new CompetitionSetupProjectDocumentRestServiceImpl();
    }

    @Test
    public void save() {
        CompetitionDocumentResource requestBody = ProjectDocumentResourceBuilder.newProjectDocumentResource().build();
        CompetitionDocumentResource responseBody = ProjectDocumentResourceBuilder.newProjectDocumentResource().build();

        setupPostWithRestResultExpectations(String.format("%s/save", competitionSetupProjectDocumentRestURL), CompetitionDocumentResource.class, requestBody, responseBody, HttpStatus.OK);

        CompetitionDocumentResource response = service.save(requestBody).getSuccess();
        assertNotNull(response);
        assertEquals(responseBody, response);

        setupPostWithRestResultVerifications(String.format("%s/save", competitionSetupProjectDocumentRestURL), CompetitionDocumentResource.class, requestBody);
    }

    @Test
    public void saveAll() {
        List<CompetitionDocumentResource> requestBody = ProjectDocumentResourceBuilder.newProjectDocumentResource().build(2);
        List<CompetitionDocumentResource> responseBody = ProjectDocumentResourceBuilder.newProjectDocumentResource().build(2);

        setupPostWithRestResultExpectations(String.format("%s/save-all", competitionSetupProjectDocumentRestURL), projectDocumentResourceListType(), requestBody, responseBody, HttpStatus.OK);

        List<CompetitionDocumentResource> response = service.save(requestBody).getSuccess();
        assertNotNull(response);
        assertEquals(responseBody, response);

        setupPostWithRestResulVerifications(String.format("%s/save-all", competitionSetupProjectDocumentRestURL), projectDocumentResourceListType(), requestBody);
    }

    @Test
    public void findOne() {

        long projectDocumentId = 1L;

        CompetitionDocumentResource responseBody = ProjectDocumentResourceBuilder.newProjectDocumentResource().build();

        setupGetWithRestResultExpectations(String.format("%s/%s", competitionSetupProjectDocumentRestURL, projectDocumentId), CompetitionDocumentResource.class, responseBody);

        CompetitionDocumentResource response = service.findOne(projectDocumentId).getSuccess();
        assertNotNull(response);
        assertEquals(responseBody, response);

        setupGetWithRestResultVerifications(String.format("%s/%s", competitionSetupProjectDocumentRestURL, projectDocumentId), null, CompetitionDocumentResource.class);
    }

    @Test
    public void findByCompetitionId() {

        long competitionId = 1L;

        List<CompetitionDocumentResource> responseBody = ProjectDocumentResourceBuilder.newProjectDocumentResource().build(2);

        setupGetWithRestResultExpectations(String.format("%s/find-by-competition-id/%s", competitionSetupProjectDocumentRestURL, competitionId), projectDocumentResourceListType(), responseBody);

        List<CompetitionDocumentResource> response = service.findByCompetitionId(competitionId).getSuccess();
        assertNotNull(response);
        assertEquals(responseBody, response);
    }

    @Test
    public void delete() {

        long projectDocumentId = 1L;

        setupDeleteWithRestResultExpectations(String.format("%s/%s", competitionSetupProjectDocumentRestURL, projectDocumentId));

        RestResult<Void> response = service.delete(projectDocumentId);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}


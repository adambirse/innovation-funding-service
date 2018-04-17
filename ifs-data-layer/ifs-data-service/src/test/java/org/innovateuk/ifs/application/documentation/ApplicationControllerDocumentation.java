package org.innovateuk.ifs.application.documentation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.builder.ApplicationResourceBuilder;
import org.innovateuk.ifs.application.controller.ApplicationController;
import org.innovateuk.ifs.application.resource.ApplicationIneligibleSendResource;
import org.innovateuk.ifs.application.resource.ApplicationPageResource;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.resource.CompletedPercentageResource;
import org.innovateuk.ifs.documentation.PageResourceDocs;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.util.JsonMappingUtil;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.ApplicationDocs.applicationResourceBuilder;
import static org.innovateuk.ifs.documentation.ApplicationDocs.applicationResourceFields;
import static org.innovateuk.ifs.documentation.ApplicationIneligibleSendResourceDocs.applicationIneligibleSendResourceBuilder;
import static org.innovateuk.ifs.documentation.ApplicationIneligibleSendResourceDocs.applicationIneligibleSendResourceFields;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationControllerDocumentation extends BaseControllerMockMVCTest<ApplicationController> {

    @Override
    protected ApplicationController supplyControllerUnderTest() {
        return new ApplicationController();
    }


    @Test
    public void getApplicationById() throws Exception {
        Long application1Id = 1L;
        ApplicationResource testApplicationResource1 = applicationResourceBuilder.build();

        when(applicationServiceMock.getApplicationById(application1Id)).thenReturn(serviceSuccess(testApplicationResource1));

        mockMvc.perform(get("/application/{id}", application1Id))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("Id of the application that is being requested")
                        ),
                        responseFields(applicationResourceFields)
                ));
    }

    @Test
    public void findAll() throws Exception {
        int applicationNumber = 3;
        List<ApplicationResource> applications = applicationResourceBuilder.build(applicationNumber);
        when(applicationServiceMock.findAll()).thenReturn(serviceSuccess(applications));

        mockMvc.perform(get("/application/").contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document("application/{method-name}",
                                responseFields(
                                        fieldWithPath("[]").description("List of applications the user is allowed to see")
                                )
                        ));
    }

    @Test
    public void findByUserId() throws Exception {
        Long userId = 1L;
        User testUser1 = new User(userId, "test", "User1", "email1@email.nl", "testToken123abc", "my-uid2");

        List<ApplicationResource> applications = applicationResourceBuilder.build(2);

        when(applicationServiceMock.findByUserId(testUser1.getId())).thenReturn(serviceSuccess(applications));

        mockMvc.perform(get("/application/findByUser/{id}", userId))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("Id of the user the applications are being requested for")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of applications linked to the user id used in the request. Only contains applications the requesting user can see")
                        )));
    }

    @Test
    public void wildcardSearchById() throws Exception {
        String searchString = "12";
        int pageNumber = 1;
        int pageSize = 20;

        List<ApplicationResource> applicationResources = ApplicationResourceBuilder.newApplicationResource().build(4);
        ApplicationPageResource applicationPageResource = new ApplicationPageResource(applicationResources.size(), 5, applicationResources, pageNumber, pageSize);

        PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
        when(applicationServiceMock.wildcardSearchById(searchString, pageRequest)).thenReturn(serviceSuccess(applicationPageResource));

        mockMvc.perform(get("/application/wildcardSearchById?searchString=" + searchString + "&page=" + pageNumber + "&size=" + pageSize))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonMappingUtil.toJson(applicationPageResource)))
                .andDo(document("application/{method-name}",
                                requestParameters(
                                        parameterWithName("searchString").description("The application id on which wildcard search will be performed"),
                                        parameterWithName("page").description("The page number to be retrieved"),
                                        parameterWithName("size").description("The page size")
                                )
                                ,
                                responseFields(PageResourceDocs.pageResourceFields)
                ));
    }

    @Test
    public void saveApplicationDetails() throws Exception {
        Long applicationId = 1L;

        ApplicationResource testApplicationResource1 = applicationResourceBuilder.build();

        when(applicationServiceMock.saveApplicationDetails(applicationId, testApplicationResource1)).thenReturn(serviceSuccess(testApplicationResource1));

        mockMvc.perform(post("/application/saveApplicationDetails/{id}", applicationId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testApplicationResource1)))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("Id of the application that needs to be saved")
                        )
                ));
    }

    @Test
    public void getProgressPercentageByApplicationId() throws Exception {
        Long applicationId = 1L;

        CompletedPercentageResource resource = new CompletedPercentageResource();
        resource.setCompletedPercentage(new BigDecimal("10"));

        when(applicationServiceMock.getProgressPercentageByApplicationId(applicationId)).thenReturn(serviceSuccess(resource));

        mockMvc.perform(get("/application/getProgressPercentageByApplicationId/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("applicationId").description("Id of the application of which the percentage is requested")
                        ),
                        responseFields(
                                fieldWithPath("completedPercentage").description("application completion percentage")
                        )
                ));
    }

    @Test
    public void updateApplicationStatus() throws Exception {
        Long applicationId = 1L;
        ApplicationState state = ApplicationState.APPROVED;

        ApplicationResource applicationResource = applicationResourceBuilder.build();

        when(applicationServiceMock.updateApplicationState(applicationId, state)).thenReturn(serviceSuccess(applicationResource));

        mockMvc.perform(put("/application/updateApplicationState?applicationId={applicationId}&state={state}", applicationId, state))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        requestParameters(
                                parameterWithName("applicationId").description("id of the application for which to update the application state"),
                                parameterWithName("state").description("new state id")
                        )
                ));
    }

    @Test
    public void applicationReadyForSubmit() throws Exception {
        Long applicationId = 1L;

        when(applicationProgressServiceMock.applicationReadyForSubmit(applicationId)).thenReturn(true);

        mockMvc.perform(get("/application/applicationReadyForSubmit/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("applicationId").description("Id of the application")
                        )
                ));
    }

    @Test
    public void getApplicationsByCompetitionIdAndUserId() throws Exception {
        Long competitionId = 1L;
        Long userId = 1L;
        List<ApplicationResource> applicationResources = applicationResourceBuilder.build(2);
        Role role= Role.LEADAPPLICANT;

        when(applicationServiceMock.getApplicationsByCompetitionIdAndUserId(competitionId, userId, role)).thenReturn(serviceSuccess(applicationResources));

        mockMvc.perform(get("/application/getApplicationsByCompetitionIdAndUserId/{competitionId}/{userId}/{role}", competitionId, userId, role))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Competition Id"),
                                parameterWithName("userId").description("User Id"),
                                parameterWithName("role").description("UserRoleType")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of applications")
                        )
                ));
    }

    @Test
    public void createApplicationByApplicationNameForUserIdAndCompetitionId() throws Exception {
        Long competitionId = 1L;
        Long userId = 1L;
        String applicationName = "testApplication";

        ApplicationResource applicationResource = applicationResourceBuilder.build();

        ObjectNode applicationNameNode = objectMapper.createObjectNode().put("name", applicationName);

        when(applicationServiceMock.createApplicationByApplicationNameForUserIdAndCompetitionId(applicationName, competitionId, userId)).thenReturn(serviceSuccess(applicationResource));

        mockMvc.perform(post("/application/createApplicationByName/{competitionId}/{userId}", competitionId, userId, "json")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationNameNode)))
                .andExpect(status().isCreated())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of the competition the new application is being created for."),
                                parameterWithName("userId").description("Id of the user the new application is being created for.")
                        ),
                        requestFields(
                                fieldWithPath("name").description("name of the application that will be created")
                        ),
                        responseFields(applicationResourceFields)
                ));
    }

    @Test
    public void informIneligible() throws Exception {
        long applicationId = 1L;
        ApplicationIneligibleSendResource applicationIneligibleSendResource = applicationIneligibleSendResourceBuilder.build();

        when(applicationNotificationServiceMock.informIneligible(applicationId, applicationIneligibleSendResource)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/application/informIneligible/{applicationId}", applicationId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationIneligibleSendResource)))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("applicationId").description("Id of the application to inform of being ineligible")
                        ),
                        requestFields(applicationIneligibleSendResourceFields)
                ));
    }

    @Test
    public void showApplicationTeam() throws Exception {
        Long applicationId = 1L;
        Long userId = 2L;

        when(applicationServiceMock.showApplicationTeam(applicationId, userId)).thenReturn(serviceSuccess(Boolean.TRUE));

        mockMvc.perform(get("/application/showApplicationTeam/{applicationId}/{userId}", applicationId, userId))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("applicationId").description("Id of the application"),
                                parameterWithName("userId").description("Id of the user who wants to view the application team")
                        )
                ));
    }

    @Test
    public void findUnsuccessfulApplications() throws Exception {
        final Long competitionId = 1L;
        int pageIndex = 0;
        int pageSize = 20;
        String sortField = "id";

        ApplicationPageResource applicationPage = new ApplicationPageResource();

        when(applicationServiceMock.findUnsuccessfulApplications(competitionId, pageIndex, pageSize, sortField)).thenReturn(serviceSuccess(applicationPage));

        mockMvc.perform(get("/application/{id}/unsuccessful-applications?page={page}&size={pageSize}&sort={sortField}", competitionId, pageIndex, pageSize, sortField))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(applicationPage)))
                .andDo(document(
                        "application/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("The competition for which unsuccessful applications need to be found")
                        )
                ));

        verify(applicationServiceMock, only()).findUnsuccessfulApplications(competitionId, pageIndex, pageSize, sortField);

    }

    @Test
    public void getLatestEmailFundingDate() throws Exception {
        Long competitionId = 1L;

        when(applicationServiceMock.findLatestEmailFundingDateByCompetitionId(competitionId)).thenReturn(serviceSuccess(ZonedDateTime.now()));

        mockMvc.perform(get("/application/getLatestEmailFundingDate/{competitionId}", competitionId))
                .andExpect(status().isOk())
                .andDo(document("application/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of the competition we want the latest email funding date from")
                        )
                ));
    }
}

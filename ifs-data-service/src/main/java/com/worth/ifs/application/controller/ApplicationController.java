package com.worth.ifs.application.controller;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.transactional.ApplicationService;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.domain.UserRoleType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.rest.RestResult.restFailure;

/**
 * ApplicationController exposes Application data and operations through a REST API.
 */
@RestController
@ExposesResourceFor(ApplicationResource.class)
@RequestMapping("/application")
public class ApplicationController {

    private static Log LOG = LogFactory.getLog(ApplicationController.class);

    @Autowired
    private ApplicationService applicationService;

    @RequestMapping("/{id}")
    public RestResult<ApplicationResource> getApplicationById(@PathVariable("id") final Long id) {
        try {
            return applicationService.getApplicationById(id).toGetResponse();
        }catch(Exception e){
            LOG.error(e);
            return restFailure(notFoundError(ApplicationResource.class, id));
        }
    }

    @RequestMapping("/")
    public RestResult<List<ApplicationResource>> findAll() {
        return applicationService.findAll().toGetResponse();
    }

    @RequestMapping("/findByUser/{userId}")
    public RestResult<List<ApplicationResource>> findByUserId(@PathVariable("userId") final Long userId) {
        return applicationService.findByUserId(userId).toGetResponse();
    }

    @RequestMapping("/saveApplicationDetails/{id}")
    public RestResult<Void> saveApplicationDetails(@PathVariable("id") final Long id,
                                                   @RequestBody ApplicationResource application) {

        return applicationService.saveApplicationDetails(id, application).toPostUpdateResponse();
    }

    @RequestMapping("/getProgressPercentageByApplicationId/{applicationId}")
    public RestResult<ObjectNode> getProgressPercentageByApplicationId(@PathVariable("applicationId") final Long applicationId) {
        return applicationService.getProgressPercentageNodeByApplicationId(applicationId).toGetResponse();
    }

    @RequestMapping(value = "/updateApplicationStatus", method = RequestMethod.PUT)
    public RestResult<Void> updateApplicationStatus(@RequestParam("applicationId") final Long id,
                                                          @RequestParam("statusId") final Long statusId) {

        return applicationService.updateApplicationStatus(id, statusId).toPutResponse();
    }


    @RequestMapping("/applicationReadyForSubmit/{applicationId}")
    public RestResult<ObjectNode> applicationReadyForSubmit(@PathVariable("applicationId") final Long id){
        return applicationService.applicationReadyForSubmit(id).toGetResponse();
    }


    @RequestMapping("/getApplicationsByCompetitionIdAndUserId/{competitionId}/{userId}/{role}")
    public RestResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(@PathVariable("competitionId") final Long competitionId,
                                                                     @PathVariable("userId") final Long userId,
                                                                     @PathVariable("role") final UserRoleType role) {

        return applicationService.getApplicationsByCompetitionIdAndUserId(competitionId, userId, role).toGetResponse();
    }

    @RequestMapping(value = "/createApplicationByName/{competitionId}/{userId}", method = RequestMethod.POST)
    public RestResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(
            @PathVariable("competitionId") final Long competitionId,
            @PathVariable("userId") final Long userId,
            @RequestBody JsonNode jsonObj) {

        String name = jsonObj.get("name").textValue();
        ServiceResult<ApplicationResource> applicationResult =
                applicationService.createApplicationByApplicationNameForUserIdAndCompetitionId(competitionId, userId, name);
        return applicationResult.toPostCreateResponse();
    }
}
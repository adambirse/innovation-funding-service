package com.worth.ifs.assessment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.assessment.dto.Score;
import com.worth.ifs.assessment.workflow.AssessmentWorkflowEventHandler;
import com.worth.ifs.user.controller.ProcessRoleController;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.workflow.domain.ProcessOutcome;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * AssessmentController is REST controller that handles requests and delegates their intent
 * to the AssessmentHandler and only that.
 */
@RestController
@RequestMapping("/assessment")
public class AssessmentController {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    AssessmentHandler assessmentHandler;

    @Autowired
    ProcessRoleController processRoleController;

    @Autowired
    AssessmentWorkflowEventHandler assessmentWorkflowEventHandler;


    @RequestMapping("/findAssessmentsByCompetition/{assessorId}/{competitionId}")
    public List<Assessment> findAssessmentsByProcessRole( @PathVariable("assessorId") final Long assessorId, @PathVariable("competitionId") final Long competitionId) {
        return assessmentHandler.getAllByCompetitionAndAssessor(competitionId, assessorId);
    }

    @RequestMapping("/findAssessmentByProcessRole/{processRoleId}")
    public Assessment getAssessmentByProcessRole( @PathVariable("processRoleId") final Long processRoleId) {
        return assessmentHandler.getOneByProcessRole(processRoleId);
    }

    @RequestMapping("/totalAssignedAssessmentsByCompetition/{userId}/{competitionId}")
    public Integer getTotalAssignedAssessmentsByCompetition( @PathVariable("userId") final Long userId, @PathVariable("competitionId") final Long competitionId ) {
        return assessmentHandler.getTotalAssignedAssessmentsByCompetition(competitionId, userId);
    }

    @RequestMapping("/totalSubmittedAssessmentsByCompetition/{userId}/{competitionId}")
    public Integer getTotalSubmittedAssessmentsByCompetition( @PathVariable("userId") final Long userId, @PathVariable("competitionId") final Long competitionId ) {
        return assessmentHandler.getTotalSubmittedAssessmentsByCompetition(competitionId, userId);
    }

    @RequestMapping(value = "/acceptAssessmentInvitation/{processRoleId}")
    public void acceptAssessmentInvitation(@PathVariable("processRoleId") final Long processRoleId,
                                           @RequestBody Assessment assessment) {
        Assessment assessmentOriginal = assessmentHandler.getOneByProcessRole(processRoleId);
        assessment.setProcessStatus(assessmentOriginal.getProcessStatus());
        assessmentWorkflowEventHandler.acceptInvitation(processRoleId, assessment);
    }

    @RequestMapping(value = "/rejectAssessmentInvitation/{processRoleId}")
    public void rejectAssessmentInvitation(@PathVariable("processRoleId") final Long processRoleId,
                                           @RequestBody ProcessOutcome processOutcome) {
        Assessment assessmentOriginal = assessmentHandler.getOneByProcessRole(processRoleId);
        String currentProcessStatus = assessmentOriginal.getProcessStatus();
        assessmentWorkflowEventHandler.rejectInvitation(processRoleId, currentProcessStatus, processOutcome);
    }

    @RequestMapping(value = "/submitAssessments", method = RequestMethod.POST)
    public Boolean submitAssessments(@RequestBody JsonNode formData) {

        //unpacks all the response form data fields
        Long assessorId = formData.get("assessorId").asLong();
        ArrayNode assessmentsIds = (ArrayNode) formData.get("assessmentsToSubmit");
        Set<Long> assessments = fromArrayNodeToSet(assessmentsIds);
        for(Long assessmentId : assessments) {
            Assessment assessment = assessmentHandler.getOne(assessmentId);
            assessmentWorkflowEventHandler.submit(assessment);
        }
        return true;
    }

    private Set<Long> fromArrayNodeToSet(ArrayNode array) {
        Set<Long> longsSet = new HashSet<>();
        Iterator<JsonNode> iterator = array.elements();

        while ( iterator.hasNext() ) {
            JsonNode aValue = iterator.next();
            longsSet.add(aValue.asLong());
        }

        return longsSet;
    }

    @RequestMapping(value = "/saveAssessmentSummary", method = RequestMethod.POST)
    public Boolean submitAssessment(@RequestBody JsonNode formData) {
        Long assessorId = formData.get("assessorId").asLong();
        Long applicationId = formData.get("applicationId").asLong();

        String suitableValue = formData.get("suitableValue").asText();
        String suitableFeedback =  HtmlUtils.htmlUnescape(formData.get("suitableFeedback").asText());
        String comments =  HtmlUtils.htmlUnescape(formData.get("comments").textValue());

        // delegates to the handler and returns its operation success
        // TODO DW - 1555 - fix the bwlow processrole code to use its serviceresult wrapper
        ProcessRole processRole = processRoleController.findByUserApplication(assessorId, applicationId).getSuccessObject();
        Assessment assessment = assessmentHandler.getOneByProcessRole(processRole.getId());
        Assessment newAssessment = new Assessment();
        ProcessOutcome processOutcome = new ProcessOutcome();
        processOutcome.setOutcome(assessmentHandler.getRecommendedValueFromString(suitableValue).name());
        processOutcome.setDescription(suitableFeedback);
        processOutcome.setComment(comments);
        newAssessment.setProcessStatus(assessment.getProcessStatus());

        assessmentWorkflowEventHandler.recommend(processRole.getId(), newAssessment, processOutcome);
        return true;
    }

    @RequestMapping(value = "{assessmentId}/score")
    public Score scoreForAssessment(@PathVariable("assessmentId") Long id){
        return assessmentHandler.getScore(id);
    }





}

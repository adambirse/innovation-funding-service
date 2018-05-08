package org.innovateuk.ifs.documentation;

import org.innovateuk.ifs.invite.builder.InterviewAcceptedAssessorsPageResourceBuilder;
import org.springframework.restdocs.payload.FieldDescriptor;

import static org.innovateuk.ifs.invite.builder.InterviewAcceptedAssessorsPageResourceBuilder.newInterviewAssessorAllocateApplicationsPageResource;
import static org.innovateuk.ifs.invite.builder.InterviewAcceptedAssessorsResourceBuilder.newInterviewAssessorAllocateApplicationsResource;

public class InterviewAcceptedAssessorsPageResourceDocs extends PageResourceDocs {

    public static final FieldDescriptor[] interviewAssessorAllocateApplicationsPageResourceFields = pageResourceFields;

    public static final InterviewAcceptedAssessorsPageResourceBuilder INTERVIEW_ACCEPTED_ASSESSORS_PAGE_RESOURCE_BUILDER =
            newInterviewAssessorAllocateApplicationsPageResource()
                    .withContent(newInterviewAssessorAllocateApplicationsResource().build(2))
                    .withSize(20)
                    .withTotalPages(5)
                    .withTotalElements(100L)
                    .withNumber(0);
}

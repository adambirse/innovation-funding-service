package org.innovateuk.ifs.documents.viewModel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.file.controller.viewmodel.FileDetailsViewModel;
import org.innovateuk.ifs.project.document.resource.DocumentStatus;

/**
 * View model for viewing/actions on each document
 */
public class DocumentViewModel {

    private long projectId;
    private String projectName;
    private long applicationId;
    private long documentConfigId;
    private String title;
    private String guidance;
    private FileDetailsViewModel fileDetails;
    private DocumentStatus status;
    private String statusComments;
    private boolean projectManager;

    public DocumentViewModel(long projectId,
                             String projectName,
                             long applicationId,
                             long documentConfigId,
                             String title,
                             String guidance,
                             FileDetailsViewModel fileDetails,
                             DocumentStatus status,
                             String statusComments,
                             boolean projectManager) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.applicationId = applicationId;
        this.documentConfigId = documentConfigId;
        this.title = title;
        this.guidance = guidance;
        this.fileDetails = fileDetails;
        this.status = status;
        this.statusComments = statusComments;
        this.projectManager = projectManager;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public long getDocumentConfigId() {
        return documentConfigId;
    }

    public String getTitle() {
        return title;
    }

    public FileDetailsViewModel getFileDetails() {
        return fileDetails;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public String getStatusComments() {
        return statusComments;
    }

    public String getGuidance() {
        return guidance;
    }

    public boolean isProjectManager() {
        return projectManager;
    }

    /* view model logic. */
    public boolean isEditable() {
        return projectManager && status != DocumentStatus.APPROVED && status != DocumentStatus.SUBMITTED;
    }

    public boolean isShowSubmitDocumentsButton() {
        return projectManager && status == DocumentStatus.UPLOADED;
    }

    public boolean isShowDisabledSubmitDocumentsButton() {
        return projectManager && status == DocumentStatus.UNSET;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DocumentViewModel that = (DocumentViewModel) o;

        return new EqualsBuilder()
                .append(projectId, that.projectId)
                .append(projectName, that.projectName)
                .append(documentConfigId, that.documentConfigId)
                .append(title, that.title)
                .append(fileDetails, that.fileDetails)
                .append(status, that.status)
                .append(guidance, that.guidance)
                .append(projectManager, that.projectManager)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(projectId)
                .append(projectName)
                .append(documentConfigId)
                .append(title)
                .append(fileDetails)
                .append(status)
                .append(guidance)
                .append(projectManager)
                .toHashCode();
    }
}
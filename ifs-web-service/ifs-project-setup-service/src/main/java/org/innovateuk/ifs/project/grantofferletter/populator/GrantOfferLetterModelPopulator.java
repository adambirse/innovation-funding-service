package org.innovateuk.ifs.project.grantofferletter.populator;

import org.innovateuk.ifs.file.controller.viewmodel.FileDetailsViewModel;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.grantofferletter.GrantOfferLetterService;
import org.innovateuk.ifs.project.grantofferletter.viewmodel.GrantOfferLetterModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GrantOfferLetterModelPopulator {

    @Autowired
    public ProjectService projectService;

    @Autowired
    public GrantOfferLetterService grantOfferLetterService;

    public GrantOfferLetterModel populateGrantOfferLetterViewModel(Long projectId, UserResource loggedInUser) {
        ProjectResource project = projectService.getById(projectId);
        boolean leadPartner = projectService.isUserLeadPartner(projectId, loggedInUser.getId());

        Optional<FileEntryResource> signedGrantOfferLetterFile = grantOfferLetterService.getSignedGrantOfferLetterFileDetails(projectId);
        Optional<FileEntryResource> grantOfferFileDetails = grantOfferLetterService.getGrantOfferFileDetails(projectId);
        Optional<FileEntryResource> additionalContractFile = grantOfferLetterService.getAdditionalContractFileDetails(projectId);
        boolean projectManager = projectService.isProjectManager(loggedInUser.getId(), projectId);
        boolean grantOfferLetterApproved = grantOfferLetterService.isSignedGrantOfferLetterApproved(projectId).getSuccess();
        boolean grantOfferLetterSent = grantOfferLetterService.isGrantOfferLetterAlreadySent(projectId).getSuccess();
        boolean grantOfferLetterRejected = grantOfferLetterService.isSignedGrantOfferLetterRejected(projectId).getSuccess();

        return new GrantOfferLetterModel(projectId, project.getName(),
                leadPartner,
                grantOfferFileDetails.map(FileDetailsViewModel::new).orElse(null),
                signedGrantOfferLetterFile.map(FileDetailsViewModel::new).orElse(null),
                additionalContractFile.map(FileDetailsViewModel::new).orElse(null),
                project.getOfferSubmittedDate(),
                projectManager,
                grantOfferLetterApproved,
                grantOfferLetterSent,
                grantOfferLetterRejected);
    }

}

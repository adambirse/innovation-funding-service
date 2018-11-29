package org.innovateuk.ifs.competitionsetup.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.resource.CompetitionDocumentResource;
import org.innovateuk.ifs.competitionsetup.transactional.CompetitionSetupProjectDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller for handling project documents during competition setup
 */
@RestController
@RequestMapping("/competition/setup/project-document")
public class CompetitionSetupProjectDocumentController {

    @Autowired
    private CompetitionSetupProjectDocumentService competitionSetupProjectDocumentService;

    @PostMapping("/save")
    public RestResult<CompetitionDocumentResource> save(@Valid @RequestBody CompetitionDocumentResource competitionDocumentResource) {
        return competitionSetupProjectDocumentService.save(competitionDocumentResource).toGetResponse();
    }

    @PostMapping("/save-all")
    public RestResult<List<CompetitionDocumentResource>> save(@Valid @RequestBody List<CompetitionDocumentResource> competitionDocumentResources) {
        return competitionSetupProjectDocumentService.saveAll(competitionDocumentResources).toGetResponse();
    }

    @GetMapping("/{id}")
    public RestResult<CompetitionDocumentResource> findOne(@PathVariable("id") final long id) {
        return competitionSetupProjectDocumentService.findOne(id).toGetResponse();
    }

    @GetMapping("/find-by-competition-id/{competitionId}")
    public RestResult<List<CompetitionDocumentResource>> findByCompetitionId(@PathVariable("competitionId") final long competitionId) {
        return competitionSetupProjectDocumentService.findByCompetitionId(competitionId).toGetResponse();
    }

    @DeleteMapping("/{id}")
    public RestResult<Void> delete(@PathVariable("id") final long id) {
        return competitionSetupProjectDocumentService.delete(id).toGetResponse();
    }
}

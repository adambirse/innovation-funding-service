package org.innovateuk.ifs.management.service;

import org.innovateuk.ifs.competition.resource.CompetitionCountResource;
import org.innovateuk.ifs.competition.resource.CompetitionSearchResult;
import org.innovateuk.ifs.competition.resource.CompetitionSearchResultItem;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.application.service.CompetitionServiceImpl.COMPETITION_PAGE_SIZE;

/**
 * Implementation of the competition dashboard searches.
 */
@Service
public class CompetitionDashboardSearchServiceImpl implements CompetitionDashboardSearchService {

    @Autowired
    private CompetitionRestService competitionRestService;
    @Override
    public Map<CompetitionStatus, List<CompetitionSearchResultItem>> getLiveCompetitions() {
        return mapToStatus(competitionRestService.findLiveCompetitions().getSuccessObjectOrThrowException());
    }

    @Override
    public List<CompetitionSearchResultItem> getProjectSetupCompetitions() {
        return competitionRestService.findProjectSetupCompetitions().getSuccessObjectOrThrowException();
    }

    @Override
    public Map<CompetitionStatus, List<CompetitionSearchResultItem>> getUpcomingCompetitions() {
        return mapToStatus(competitionRestService.findUpcomingCompetitions().getSuccessObjectOrThrowException());
    }

    @Override
    public List<CompetitionSearchResultItem> getNonIfsCompetitions() {
        return competitionRestService.findNonIfsCompetitions().getSuccessObjectOrThrowException();
    }

    @Override
    public List<CompetitionSearchResultItem> getPreviousCompetitions() {
        return competitionRestService.findFeedbackReleasedCompetitions().getSuccessObjectOrThrowException();
    }

    @Override
    public CompetitionSearchResult searchCompetitions(String searchQuery, int page) {
        CompetitionSearchResult searchResult = competitionRestService.searchCompetitions(searchQuery, page, COMPETITION_PAGE_SIZE).getSuccessObjectOrThrowException();
        searchResult.setMappedCompetitions(mapToStatus(searchResult.getContent()));
        return searchResult;
    }

    @Override
    public CompetitionCountResource getCompetitionCounts() {
        return competitionRestService.countCompetitions().getSuccessObjectOrThrowException();
    }

    private Map<CompetitionStatus, List<CompetitionSearchResultItem>> mapToStatus(List<CompetitionSearchResultItem> resources) {
        return resources.stream().collect(Collectors.groupingBy(CompetitionSearchResultItem::getCompetitionStatus));
    }
}

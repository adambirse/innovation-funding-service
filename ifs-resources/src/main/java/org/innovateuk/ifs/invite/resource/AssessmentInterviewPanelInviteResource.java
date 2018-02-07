package org.innovateuk.ifs.invite.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.invite.constant.InviteStatus;

import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * DTO for {@link org.innovateuk.ifs.invite.domain.competition.AssessmentInterviewPanelInvite}s.
 */
public class AssessmentInterviewPanelInviteResource extends InviteResource {

    private InviteStatus status;
    private String hash;
    private long competitionId;
    private String competitionName;
    private long userId;
    private String email;
    private ZonedDateTime interviewDate;

    public AssessmentInterviewPanelInviteResource(String hash,
                                                  long competitionId,
                                                  String competitionName,
                                                  InviteStatus status,
                                                  long userId,
                                                  String email,
                                                  ZonedDateTime interviewDate
    ) {
        this.hash = hash;
        this.competitionId = competitionId;
        this.competitionName = competitionName;
        this.status = status;
        this.userId = userId;
        this.email = email;
        this.interviewDate = interviewDate;
    }

    public AssessmentInterviewPanelInviteResource() {
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(long competitionId) {
        this.competitionId = competitionId;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ZonedDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(ZonedDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    @JsonIgnore
    public long getInterviewDaysLeft() {
        return DAYS.between(ZonedDateTime.now(), interviewDate);
    }

    @JsonIgnore
    public boolean isPending() {
        return status == InviteStatus.SENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AssessmentInterviewPanelInviteResource that = (AssessmentInterviewPanelInviteResource) o;

        return new EqualsBuilder()
                .append(status, that.status)
                .append(hash, that.hash)
                .append(competitionId, that.competitionId)
                .append(userId, that.userId)
                .append(competitionName, that.competitionName)
                .append(email, that.email)
                .append(interviewDate, that.interviewDate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(status)
                .append(hash)
                .append(competitionId)
                .append(userId)
                .append(competitionName)
                .append(email)
                .append(interviewDate)
                .toHashCode();
    }
}

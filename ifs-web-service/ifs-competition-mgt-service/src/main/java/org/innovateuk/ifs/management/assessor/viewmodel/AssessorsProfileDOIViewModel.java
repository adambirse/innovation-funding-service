package org.innovateuk.ifs.management.assessor.viewmodel;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.user.resource.AffiliationResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder of model attributes for the Declaration of Interest view.
 */
public class AssessorsProfileDOIViewModel {

    private CompetitionResource competition;
    private long assessorId;
    private String name;
    private String email;
    private String phone;
    private AddressResource address;
    private String businessType;
    private String originQuery;
    private boolean completed;
    private String principalEmployer;
    private String role;
    private String professionalAffiliations;
    private List<AffiliationResource> appointments = new ArrayList<>();
    private String financialInterests;
    private List<AffiliationResource> familyAffiliations = new ArrayList<>();
    private String familyFinancialInterests;

    public AssessorsProfileDOIViewModel(
            CompetitionResource competition,
            long assessorId,
            String name,
            String email,
            String phone,
            AddressResource address,
            String businessType,
            String originQuery,
            boolean completed,
            String principalEmployer,
            String role,
            String professionalAffiliations,
            List<AffiliationResource> appointments,
            String financialInterests,
            List<AffiliationResource> familyAffiliations,
            String familyFinancialInterests
    ) {
        this.competition = competition;
        this.assessorId = assessorId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.businessType = businessType;
        this.originQuery = originQuery;
        this.completed = completed;
        this.principalEmployer = principalEmployer;
        this.role = role;
        this.professionalAffiliations = professionalAffiliations;
        this.financialInterests = financialInterests;
        this.familyFinancialInterests = familyFinancialInterests;

        if (appointments != null) {
            this.appointments.addAll(appointments);
        }

        if (familyAffiliations != null) {
            this.familyAffiliations.addAll(familyAffiliations);
        }
    }

    public CompetitionResource getCompetition() {
        return competition;
    }

    public long getAssessorId() {
        return assessorId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getPrincipalEmployer() {
        return principalEmployer;
    }

    public String getRole() {
        return role;
    }

    public String getProfessionalAffiliations() {
        return professionalAffiliations;
    }

    public List<AffiliationResource> getAppointments() {
        return appointments;
    }

    public String getFinancialInterests() {
        return financialInterests;
    }

    public List<AffiliationResource> getFamilyAffiliations() {
        return familyAffiliations;
    }

    public String getFamilyFinancialInterests() {
        return familyFinancialInterests;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public AddressResource getAddress() {
        return address;
    }

    public String getBusinessType() {
        return businessType;
    }

    public String getOriginQuery() {
        return originQuery;
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//
//        if (o == null || getClass() != o.getClass()) return false;
//
//        AssessorsProfileDOIViewModel viewModel = (AssessorsProfileDOIViewModel) o;
//
//        return new EqualsBuilder()
//                .append(competition, viewModel.competition)
//                .append(assessorId, viewModel.assessorId)
//                .append(completed, viewModel.completed)
//                .append(principalEmployer, viewModel.principalEmployer)
//                .append(role, viewModel.role)
//                .append(professionalAffiliations, viewModel.professionalAffiliations)
//                .append(appointments, viewModel.appointments)
//                .append(financialInterests, viewModel.financialInterests)
//                .append(familyAffiliations, viewModel.familyAffiliations)
//                .append(familyFinancialInterests, viewModel.familyFinancialInterests)
//                .isEquals();
//    }
//
//    @Override
//    public int hashCode() {
//        return new HashCodeBuilder(17, 37)
//                .append(competition)
//                .append(assessorId)
//                .append(completed)
//                .append(principalEmployer)
//                .append(role)
//                .append(professionalAffiliations)
//                .append(appointments)
//                .append(financialInterests)
//                .append(familyAffiliations)
//                .append(familyFinancialInterests)
//                .toHashCode();
//    }
}
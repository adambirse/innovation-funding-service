package org.innovateuk.ifs.organisation.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.address.domain.AddressType;
import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.invite.domain.InviteOrganisation;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * organisation defines database relations and a model to use client side and server side.
 */
@Entity
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ZeroDowntime(description = "Remove when contracting", reference = "IFS-3195")
    @Column(name = "company_house_number")
    private String companyHouseNumber;

    @Column(name = "companies_house_number")
    private String companiesHouseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrganisationType organisationType;

    @OneToMany(mappedBy="organisationId")
    private List<ProcessRole> processRoles = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "user_organisation",
            joinColumns = @JoinColumn(name = "organisation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "organisation",
            cascade = CascadeType.ALL)
    private List<OrganisationAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy="organisation")
    private List<InviteOrganisation> inviteOrganisations = new ArrayList<>();

    public Organisation() {
    }

    public Organisation(String name) {
        this.name = name;
    }
    public Organisation(String name, String companiesHouseNumber) {
        this.name = name;
        this.companyHouseNumber = companiesHouseNumber;
        this.companiesHouseNumber = companiesHouseNumber;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public List<ProcessRole> getProcessRoles() {
        return processRoles;
    }

    public void setProcessRoles(List<ProcessRole> processRoles) {
        this.processRoles = processRoles;
    }

    public List<User> getUsers() {
        return users;
    }

    @ZeroDowntime(description = "change to companiesHouseNumber on migrate", reference = "IFS-3195")
    public String getCompaniesHouseNumber() {
        return companyHouseNumber;
    }

    public void setCompaniesHouseNumber(String companiesHouseNumber) {
        this.companyHouseNumber = companiesHouseNumber;
        this.companiesHouseNumber = companiesHouseNumber;
    }

    public List<OrganisationAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<OrganisationAddress> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(Address address, AddressType addressType){
        OrganisationAddress organisationAddress = new OrganisationAddress(this, address, addressType);
        this.addresses.add(organisationAddress);
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        if (users == null) {
            users = new ArrayList<>();
        }
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(OrganisationType organisationType) {
        this.organisationType = organisationType;
    }
}

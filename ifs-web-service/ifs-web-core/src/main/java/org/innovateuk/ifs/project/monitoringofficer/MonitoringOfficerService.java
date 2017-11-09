package org.innovateuk.ifs.project.monitoringofficer;

import org.innovateuk.ifs.commons.security.NotSecured;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.project.monitoringofficer.resource.MonitoringOfficerResource;
import java.util.Optional;

/**
 * A service for dealing with Monitoring Officer ProjectResources via the appropriate Rest services
 */
public interface MonitoringOfficerService {

    @NotSecured(value = "Not currently secured", mustBeSecuredByOtherServices = false)
    Optional<MonitoringOfficerResource> getMonitoringOfficerForProject(Long projectId);

    @NotSecured(value = "Not currently secured", mustBeSecuredByOtherServices = false)
    ServiceResult<Void> updateMonitoringOfficer(Long projectId, String firstName, String lastName, String emailAddress, String phoneNumber);

}

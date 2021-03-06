package org.innovateuk.ifs.project.monitoring.transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.mapper.ProjectMapper;
import org.innovateuk.ifs.project.core.repository.ProjectRepository;
import org.innovateuk.ifs.project.monitoring.domain.MonitoringOfficer;
import org.innovateuk.ifs.project.monitoring.repository.MonitoringOfficerRepository;
import org.innovateuk.ifs.project.monitoring.resource.MonitoringOfficerAssignedProjectResource;
import org.innovateuk.ifs.project.monitoring.resource.MonitoringOfficerAssignmentResource;
import org.innovateuk.ifs.project.monitoring.resource.MonitoringOfficerResource;
import org.innovateuk.ifs.project.monitoring.resource.MonitoringOfficerUnassignedProjectResource;
import org.innovateuk.ifs.project.monitoringofficer.transactional.LegacyMonitoringOfficerService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.user.resource.Role.MONITORING_OFFICER;
import static org.innovateuk.ifs.user.resource.UserStatus.ACTIVE;
import static org.innovateuk.ifs.user.resource.UserStatus.PENDING;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

@Service
public class MonitoringOfficerServiceImpl implements MonitoringOfficerService {

    private static final Log LOG = LogFactory.getLog(MonitoringOfficerInviteService.class);

    private final MonitoringOfficerRepository monitoringOfficerRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MonitoringOfficerInviteService monitoringOfficerInviteService;
    private final ProjectMapper projectMapper;
    private final LegacyMonitoringOfficerService legacyMonitoringOfficerService;
    private final UserMapper userMapper;

    public MonitoringOfficerServiceImpl(MonitoringOfficerRepository monitoringOfficerRepository, ProjectRepository projectRepository, UserRepository userRepository, MonitoringOfficerInviteService monitoringOfficerInviteService, ProjectMapper projectMapper, LegacyMonitoringOfficerService legacyMonitoringOfficerService, UserMapper userMapper) {
        this.monitoringOfficerRepository = monitoringOfficerRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.monitoringOfficerInviteService = monitoringOfficerInviteService;
        this.projectMapper = projectMapper;
        this.legacyMonitoringOfficerService = legacyMonitoringOfficerService;
        this.userMapper = userMapper;
    }

    @Override
    public ServiceResult<List<UserResource>> findAll() {
        return serviceSuccess(userRepository.findByRolesAndStatusIn(MONITORING_OFFICER, EnumSet.of(PENDING, ACTIVE))
                .stream()
                .map(userMapper::mapToResource)
                .collect(toList()));
    }

    private MonitoringOfficerAssignmentResource mapToProjectMonitoringOfficerResource(User user) {
        List<MonitoringOfficerUnassignedProjectResource> unassignedProjects = monitoringOfficerRepository.findUnassignedProject();
        List<MonitoringOfficerAssignedProjectResource> assignedProjects = monitoringOfficerRepository.findAssignedProjects(user.getId());
        return new MonitoringOfficerAssignmentResource(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                unassignedProjects,
                assignedProjects);
    }

    @Override
    @Transactional
    public ServiceResult<MonitoringOfficerAssignmentResource> getProjectMonitoringOfficer(long userId) {
        return getMonitoringOfficerUser(userId)
                .andOnSuccessReturn(this::mapToProjectMonitoringOfficerResource);
    }

    @Override
    @Transactional
    public ServiceResult<Void> assignProjectToMonitoringOfficer(long userId, long projectId) {
        return getMonitoringOfficerUser(userId)
                .andOnSuccess(user -> getProject(projectId)
                        .andOnSuccess(project -> (monitoringOfficerInviteService.inviteMonitoringOfficer(user, project))
                                .andOnSuccessReturnVoid(() -> monitoringOfficerRepository.save(new MonitoringOfficer(user, project)))
                        )
                );
    }

    @Override
    @Transactional
    public ServiceResult<Void> unassignProjectFromMonitoringOfficer(long userId, long projectId) {
        monitoringOfficerRepository.deleteByUserIdAndProjectId(userId, projectId);
        return serviceSuccess();
    }

    @Override
    public ServiceResult<List<ProjectResource>> getMonitoringOfficerProjects(long userId) {
        List<MonitoringOfficer> monitoringOfficers = monitoringOfficerRepository.findByUserId(userId);
        return serviceSuccess(monitoringOfficers.stream()
                .map(MonitoringOfficer::getProcess)
                .map(projectMapper::mapToResource)
                .collect(toList()));
    }

    @Override
    public ServiceResult<MonitoringOfficerResource> findMonitoringOfficerForProject(long projectId) {
        return find(projectRepository.findById(projectId), notFoundError(Project.class))
                .andOnSuccess(project -> {
                    if (project.getProjectMonitoringOfficer().isPresent()) {
                        return toMonitoringOfficerResource(project.getProjectMonitoringOfficer().get(), projectId);
                    } else {
                        return legacyMonitoringOfficer(projectId);
                    }
                });
    }


    private ServiceResult<MonitoringOfficerResource> toMonitoringOfficerResource(MonitoringOfficer monitoringOfficer, long projectId) {
        return serviceSuccess(new MonitoringOfficerResource(monitoringOfficer.getId(),
                monitoringOfficer.getUser().getFirstName(),
                monitoringOfficer.getUser().getLastName(),
                monitoringOfficer.getUser().getEmail(),
                monitoringOfficer.getUser().getPhoneNumber(),
                projectId));
    }

    private ServiceResult<MonitoringOfficerResource> legacyMonitoringOfficer(long projectId) {
        return legacyMonitoringOfficerService.getMonitoringOfficer(projectId)
                .andOnSuccessReturn(legacyMonitoringOfficer -> new MonitoringOfficerResource(legacyMonitoringOfficer.getId(),
                        legacyMonitoringOfficer.getFirstName(),
                        legacyMonitoringOfficer.getLastName(),
                        legacyMonitoringOfficer.getEmail(),
                        legacyMonitoringOfficer.getPhoneNumber(),
                        legacyMonitoringOfficer.getProject()));
    }

    private ServiceResult<User> getMonitoringOfficerUser(long userId) {
        return find(userRepository.findByIdAndRoles(userId, MONITORING_OFFICER), notFoundError(User.class, userId));
    }

    private ServiceResult<Project> getProject(long projectId) {
        return find(projectRepository.findById(projectId), notFoundError(Project.class, projectId));
    }
}
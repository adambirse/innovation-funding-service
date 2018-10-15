package org.innovateuk.ifs.competitionsetup.transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceFailure;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.domain.Stakeholder;
import org.innovateuk.ifs.competition.domain.StakeholderInvite;
import org.innovateuk.ifs.competition.repository.StakeholderInviteRepository;
import org.innovateuk.ifs.competition.repository.StakeholderRepository;
import org.innovateuk.ifs.invite.mapper.StakeholderInviteMapper;
import org.innovateuk.ifs.invite.resource.StakeholderInviteResource;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.notifications.resource.UserNotificationTarget;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.registration.resource.StakeholderRegistrationResource;
import org.innovateuk.ifs.security.LoggedInUserSupplier;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.STAKEHOLDER_INVITE_EMAIL_TAKEN;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.STAKEHOLDER_INVITE_INVALID;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.STAKEHOLDER_INVITE_INVALID_EMAIL;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.STAKEHOLDER_INVITE_TARGET_USER_ALREADY_INVITED;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.constant.InviteStatus.CREATED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.SENT;
import static org.innovateuk.ifs.invite.domain.Invite.generateInviteHash;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * Transactional and secured service implementation providing operations around stakeholders.
 */
@Service
public class CompetitionSetupStakeholderServiceImpl extends BaseTransactionalService implements CompetitionSetupStakeholderService {

    private static final Log LOG = LogFactory.getLog(CompetitionSetupStakeholderServiceImpl.class);

    @Autowired
    private StakeholderInviteRepository stakeholderInviteRepository;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LoggedInUserSupplier loggedInUserSupplier;

    @Autowired
    private StakeholderRepository stakeholderRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StakeholderInviteMapper stakeholderInviteMapper;

    @Value("${ifs.system.internal.user.email.domain}")
    private String internalUserEmailDomain;

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    private static final String DEFAULT_INTERNAL_USER_EMAIL_DOMAIN = "innovateuk.gov.uk";
    private static final String WEB_CONTEXT = "/management/stakeholder";

    enum Notifications {
        STAKEHOLDER_INVITE,
        ADD_STAKEHOLDER
    }

    @Override
    @Transactional
    public ServiceResult<Void> inviteStakeholder(UserResource invitedUser, long competitionId) {

        return validateInvite(invitedUser)
                .andOnSuccess(() -> validateEmail(invitedUser.getEmail()))
                .andOnSuccess(() -> validateUserEmailAvailable(invitedUser))
                .andOnSuccess(() -> validateUserNotAlreadyInvited(invitedUser)).andOnSuccess(() -> getCompetition(competitionId))
                .andOnSuccess(competition -> saveInvite(invitedUser, competition)
                                    .andOnSuccess(stakeholderInvite -> sendStakeholderInviteNotification(stakeholderInvite, competition))
                             );
    }

    private ServiceResult<Void> validateInvite(UserResource invitedUser) {

        if (StringUtils.isEmpty(invitedUser.getEmail()) || StringUtils.isEmpty(invitedUser.getFirstName())
                || StringUtils.isEmpty(invitedUser.getLastName())){
            return serviceFailure(STAKEHOLDER_INVITE_INVALID);
        }
        return serviceSuccess();
    }

    private ServiceResult<Void> validateEmail(String email) {

        internalUserEmailDomain = StringUtils.defaultIfBlank(internalUserEmailDomain, DEFAULT_INTERNAL_USER_EMAIL_DOMAIN);

        String domain = StringUtils.substringAfter(email, "@");

        if (internalUserEmailDomain.equalsIgnoreCase(domain)) {
            return serviceFailure(STAKEHOLDER_INVITE_INVALID_EMAIL);
        }

        return serviceSuccess();
    }

    private ServiceResult<Void> validateUserEmailAvailable(UserResource invitedUser) {
        return userRepository.findByEmail(invitedUser.getEmail()).isPresent() ? serviceFailure(STAKEHOLDER_INVITE_EMAIL_TAKEN) : serviceSuccess() ;
    }

    private ServiceResult<Void> validateUserNotAlreadyInvited(UserResource invitedUser) {

        List<StakeholderInvite> existingInvites = stakeholderInviteRepository.findByEmail(invitedUser.getEmail());
        return existingInvites.isEmpty() ? serviceSuccess() : serviceFailure(STAKEHOLDER_INVITE_TARGET_USER_ALREADY_INVITED);
    }

    private ServiceResult<StakeholderInvite> saveInvite(UserResource invitedUser, Competition competition) {

        StakeholderInvite stakeholderInvite = new StakeholderInvite(competition,
                invitedUser.getFirstName() + " " + invitedUser.getLastName(),
                invitedUser.getEmail(),
                generateInviteHash(),
                CREATED);

        StakeholderInvite savedStakeholderInvite = stakeholderInviteRepository.save(stakeholderInvite);

        return serviceSuccess(savedStakeholderInvite);
    }

    private ServiceResult<Void> sendStakeholderInviteNotification(StakeholderInvite stakeholderInvite, Competition competition) {

        Map<String, Object> globalArgs = createGlobalArgsForStakeholderInvite(stakeholderInvite, competition);

        Notification notification = new Notification(systemNotificationSource,
                                        singletonList(createStakeholderInviteNotificationTarget(stakeholderInvite)),
                                        Notifications.STAKEHOLDER_INVITE, globalArgs);

        ServiceResult<Void> stakeholderInviteEmailSendResult = notificationService.sendNotificationWithFlush(notification, EMAIL);

        stakeholderInviteEmailSendResult.handleSuccessOrFailure(
                                            failure -> handleInviteError(stakeholderInvite, failure),
                                            success -> handleInviteSuccess(stakeholderInvite)
                                            );

        return stakeholderInviteEmailSendResult;
    }

    private Map<String, Object> createGlobalArgsForStakeholderInvite(StakeholderInvite stakeholderInvite, Competition competition) {
        Map<String, Object> globalArguments = new HashMap<>();
        globalArguments.put("competitionName", competition.getName());
        globalArguments.put("inviteUrl", getInviteUrl(webBaseUrl + WEB_CONTEXT, stakeholderInvite));
        return globalArguments;
    }

    private String getInviteUrl(String baseUrl, StakeholderInvite stakeholderInvite) {
        return String.format("%s/%s/%s", baseUrl, stakeholderInvite.getHash(), "register");
    }

    private NotificationTarget createStakeholderInviteNotificationTarget(StakeholderInvite stakeholderInvite) {
        return new UserNotificationTarget(stakeholderInvite.getName(), stakeholderInvite.getEmail());
    }

    private ServiceResult<Void> handleInviteError(StakeholderInvite i, ServiceFailure failure) {
        LOG.error(String.format("Invite failed %s, %s, %s (error count: %s)", i.getId(), i.getEmail(), i.getTarget().getName(), failure.getErrors().size()));
        List<Error> errors = failure.getErrors();
        return serviceFailure(errors);
    }

    private ServiceResult<Void> handleInviteSuccess(StakeholderInvite stakeholderInvite) {
        stakeholderInviteRepository.save(stakeholderInvite.sendOrResend(loggedInUserSupplier.get(), ZonedDateTime.now()));
        return serviceSuccess();
    }

    @Override
    public ServiceResult<List<UserResource>> findStakeholders(long competitionId) {

        List<Stakeholder> stakeholders = stakeholderRepository.findStakeholders(competitionId);
        List<UserResource> stakeholderUsers = simpleMap(stakeholders, stakeholder -> userMapper.mapToResource(stakeholder.getUser()));

        return serviceSuccess(stakeholderUsers);
    }

    @Override
    public ServiceResult<StakeholderInviteResource> getInviteByHash(String hash) {
        StakeholderInvite stakeholderInvite = stakeholderInviteRepository.getByHash(hash);
        return serviceSuccess(stakeholderInviteMapper.mapToResource(stakeholderInvite));
    }

    @Override
    @Transactional
    public ServiceResult<Void> addStakeholder(long competitionId, long stakeholderUserId) {
        return getCompetition(competitionId)
                .andOnSuccessReturnVoid(competition ->
                        find(userRepository.findOne(stakeholderUserId),
                                notFoundError(User.class, stakeholderUserId))
                        .andOnSuccess(stakeholder -> {
                            Stakeholder savedStakeholder = stakeholderRepository.save(new Stakeholder(competition, stakeholder));
                            return sendAddStakeholderNotification(savedStakeholder, competition);
                        })
                );
    }

    private ServiceResult<Void> sendAddStakeholderNotification(Stakeholder stakeholder, Competition competition) {

        Map<String, Object> globalArgs = createGlobalArgsForAddStakeholder(competition);

        Notification notification = new Notification(systemNotificationSource,
                singletonList(createAddStakeholderNotificationTarget(stakeholder)),
                Notifications.ADD_STAKEHOLDER, globalArgs);

        return notificationService.sendNotificationWithFlush(notification, EMAIL);
    }

    private Map<String, Object> createGlobalArgsForAddStakeholder(Competition competition) {
        Map<String, Object> globalArguments = new HashMap<>();
        globalArguments.put("competitionName", competition.getName());
        globalArguments.put("dashboardUrl", webBaseUrl + "/management/dashboard/live");
        return globalArguments;
    }

    private NotificationTarget createAddStakeholderNotificationTarget(Stakeholder stakeholder) {
        return new UserNotificationTarget(stakeholder.getUser().getName(), stakeholder.getUser().getEmail());
    }

    @Override
    @Transactional
    public ServiceResult<Void> removeStakeholder(long competitionId, long stakeholderUserId) {
        stakeholderRepository.deleteStakeholder(competitionId, stakeholderUserId);
        return serviceSuccess();
    }

    @Override
    @Transactional
    public ServiceResult<List<UserResource>> findPendingStakeholderInvites(long competitionId) {
        List<StakeholderInvite> pendingStakeholderInvites = stakeholderInviteRepository.findByCompetitionIdAndStatus(competitionId, SENT);

        List<UserResource> pendingStakeholderInviteUsers = simpleMap(pendingStakeholderInvites,
                pendingStakeholderInvite -> convert(pendingStakeholderInvite));

        return serviceSuccess(pendingStakeholderInviteUsers);
    }

    private UserResource convert(StakeholderInvite stakeholderInvite) {
        UserResource userResource = new UserResource();
        userResource.setFirstName(stakeholderInvite.getName());
        userResource.setEmail(stakeholderInvite.getEmail());
        return userResource;
    }
}

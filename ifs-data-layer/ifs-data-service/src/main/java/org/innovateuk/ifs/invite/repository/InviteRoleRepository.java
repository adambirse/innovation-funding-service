package org.innovateuk.ifs.invite.repository;

import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.RoleInvite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface InviteRoleRepository extends PagingAndSortingRepository<RoleInvite, Long> {

    List<RoleInvite> findByRoleId(Long roleId);

    List<RoleInvite> findByEmail(String email);

    Optional<RoleInvite> findOneByRoleIdAndEmail(Long roleId, String email);

    Page<RoleInvite> findByStatus(InviteStatus status, Pageable pageable);

    RoleInvite getByHash(String hash);
}


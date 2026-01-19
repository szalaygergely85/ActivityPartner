package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.AccountDeletionRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountDeletionRequestRepository
    extends JpaRepository<AccountDeletionRequest, Long> {

  boolean existsByEmail(String email);

  List<AccountDeletionRequest> findByProcessedAtIsNullOrderByRequestedAtAsc();
}

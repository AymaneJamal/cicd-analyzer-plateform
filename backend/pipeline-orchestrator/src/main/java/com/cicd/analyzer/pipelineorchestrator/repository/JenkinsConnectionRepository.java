package com.cicd.analyzer.pipelineorchestrator.repository;

import com.cicd.analyzer.pipelineorchestrator.entity.JenkinsConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JenkinsConnectionRepository extends JpaRepository<JenkinsConnection, Long> {

    List<JenkinsConnection> findByUserId(Long userId);

    List<JenkinsConnection> findByUserIdAndIsActive(Long userId, Boolean isActive);

    Optional<JenkinsConnection> findByIdAndUserId(Long id, Long userId);
}
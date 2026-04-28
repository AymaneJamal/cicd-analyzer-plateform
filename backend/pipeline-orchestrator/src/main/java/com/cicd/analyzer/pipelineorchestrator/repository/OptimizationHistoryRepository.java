package com.cicd.analyzer.pipelineorchestrator.repository;

import com.cicd.analyzer.pipelineorchestrator.entity.OptimizationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OptimizationHistoryRepository extends JpaRepository<OptimizationHistory, Long> {

    List<OptimizationHistory> findByJenkinsConnectionIdOrderByCompletedAtDesc(Long jenkinsConnectionId);

    @Query("SELECT oh FROM OptimizationHistory oh WHERE oh.jenkinsConnection.user.id = :userId ORDER BY oh.completedAt DESC")
    List<OptimizationHistory> findByUserIdOrderByCompletedAtDesc(Long userId);

    List<OptimizationHistory> findByJenkinsConnectionIdAndPipelineNameOrderByCompletedAtDesc(Long jenkinsConnectionId, String pipelineName);
}
package com.cicd.analyzer.jenkinsconnector.model;

import lombok.Data;
import java.util.List;

@Data
public class BuildStatistics {
    private Integer buildNumber;
    private String jobName;
    private String result;
    private Long duration;
    private Long queueDuration;
    private Long timestamp;
    private List<Stage> stages;
    private String triggeredBy;
    private CommitInfo commit;
    private TestResults testResults;

    @Data
    public static class Stage {
        private String name;
        private Long duration;
        private String status;
    }

    @Data
    public static class CommitInfo {
        private String id;
        private String message;
    }

    @Data
    public static class TestResults {
        private Integer total;
        private Integer passed;
        private Integer failed;
        private Integer skipped;
    }
}
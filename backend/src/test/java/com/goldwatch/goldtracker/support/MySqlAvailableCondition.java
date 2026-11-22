package com.goldwatch.goldtracker.support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/** Enables MySQL integration tests when an external URL is configured or a Docker daemon is reachable. */
public class MySqlAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (MySqlContainerConfig.externalUrl() != null) {
            return ConditionEvaluationResult.enabled("Using external MySQL " + MySqlContainerConfig.externalUrl());
        }
        if (isDockerAvailable()) {
            return ConditionEvaluationResult.enabled("Docker available, using Testcontainers MySQL");
        }
        return ConditionEvaluationResult.disabled("No Docker and no -Dit.mysql.url; MySQL integration tests skipped");
    }

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException e) {
            return false;
        }
    }
}

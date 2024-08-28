/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.sonom.flm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;
import com.ericsson.oss.services.sonom.common.env.Environment;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;

/**
 * Class to implement methods of {@link DatabaseViewDao}.
 */
public class DatabaseViewDaoImpl implements DatabaseViewDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseViewDaoImpl.class);
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    private static final String SCHEMA_NAME_FOR_VIEW = "public";
    private static final String FLM_DATABASE_NAME = Environment.getEnvironmentValue("PGDATABASE", "flm_service_db");
    private static final String EXTERNAL_DB_USER = Environment.getEnvironmentValue("FLM_EXPORT_USER", "flm_exporter");
    private static final String EXTERNAL_DB_PWD = Environment.getEnvironmentValue("FLM_EXPORT_PASSWORD", "");
    private static final Retry RETRY = Retry.of("createViewRetry", RetryConfig.custom().retryExceptions(Exception.class)
            .retryOnResult(result -> ((Boolean) result))
            .maxAttempts(Integer.MAX_VALUE).waitDuration(Duration.ofSeconds(5)).build());

    @Override
    public void createExternalDatabaseRoleAndGrantAccess() {
        if (EXTERNAL_DB_PWD.isEmpty() || EXTERNAL_DB_USER.isEmpty()) {
            LOGGER.warn("External FLM database user cannot be created as no credentials were provided");
        } else {
            this.createExternalDatabaseRole();
            this.updateExternalViewAccess();
        }
    }

    public void createExternalDatabaseRole() {
        final Supplier<Boolean> createRoleSupplier = Retry.decorateSupplier(RETRY, getCreateRoleSupplier());
        Try.ofSupplier(createRoleSupplier);
    }

    private void updateExternalViewAccess() {
        final Supplier<Boolean> grantAccessSupplier = Retry.decorateSupplier(RETRY, getGrantAccessSupplier());
        Try.ofSupplier(grantAccessSupplier);
    }

    private static Supplier<Boolean> getCreateRoleSupplier() {
        return () -> {
            try (final Connection connection = DriverManager.getConnection(DatabaseProperties.getFlmJdbcConnection(),
                    DatabaseProperties.getFlmJdbcProperties());
                 final PreparedStatement createRoleStatement = connection.prepareStatement(String.format(
                         "CREATE ROLE %s LOGIN PASSWORD '%s'",
                         EXTERNAL_DB_USER, EXTERNAL_DB_PWD))) {
                createRoleStatement.execute();
                LOGGER.info("Created role");
                return false;
            } catch (final SQLException e) {
                if (e.getSQLState().equals(DUPLICATE_OBJECT_ERROR_CODE)) {
                    LOGGER.info("The role has already been created.");
                    return false;
                }
                final String message = String.format("Failed to create role: '%s' - %s", EXTERNAL_DB_USER, e.getClass());
                LOGGER.warn(message, e.getMessage());
                LOGGER.debug(message, e);
                return true;
            }
        };
    }

    private static Supplier<Boolean> getGrantAccessSupplier() {
        return () -> {
            try (final Connection connection = DriverManager.getConnection(DatabaseProperties.getFlmJdbcConnection(),
                    DatabaseProperties.getFlmJdbcProperties());
                 final PreparedStatement grantConnectStatement = connection.prepareStatement(String.format(
                         "GRANT CONNECT ON DATABASE %s TO %s",
                         FLM_DATABASE_NAME, EXTERNAL_DB_USER));
                 final PreparedStatement grantUsageStatement = connection.prepareStatement(String.format(
                         "GRANT USAGE ON SCHEMA %s TO %s",
                         SCHEMA_NAME_FOR_VIEW, EXTERNAL_DB_USER));
                 final PreparedStatement createViewStatement = connection.prepareStatement(String.format(
                         "GRANT SELECT ON ALL TABLES IN SCHEMA %s TO %s",
                         SCHEMA_NAME_FOR_VIEW, EXTERNAL_DB_USER))) {
                grantConnectStatement.execute();
                grantUsageStatement.execute();
                createViewStatement.execute();
                LOGGER.info("External access granted");
                return false;
            } catch (final SQLException e) {
                final String message = String.format("Failed to grant access to user '%s' - %s", EXTERNAL_DB_USER, e.getClass());
                LOGGER.warn(message, e.getMessage());
                LOGGER.debug(message, e);
                return true;
            }
        };
    }

}

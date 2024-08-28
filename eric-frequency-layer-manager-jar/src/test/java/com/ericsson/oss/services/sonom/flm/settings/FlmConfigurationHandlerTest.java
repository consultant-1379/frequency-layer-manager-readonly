/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.settings;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDao;
import com.ericsson.oss.services.sonom.flm.scheduler.FlmAlgorithmScheduleOperation;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsException;
import com.google.gson.Gson;

/**
 * Unit tests for {@link FlmConfigurationHandler} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlmConfigurationHandlerTest {

    private static final String CONFIG_NAME = "ConfigName";
    private static final int ONCE = 1;

    @Mock
    private ConfigurationDao configurationDao;

    @InjectMocks
    private static final FlmConfigurationHandler OBJECT_UNDER_TEST = new FlmConfigurationHandler();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void whenConfigurationIsCreatedWithEnabledTrue_theConfigurationIsScheduled() throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(true);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);
        doNothing().when(spyFlmConfigurationHandler).createFlmAlgorithmExecutionSchedule(configuration);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.CREATE);

        verify(spyFlmConfigurationHandler, times(ONCE)).createFlmAlgorithmExecutionSchedule(configuration);
        verify(spyFlmConfigurationHandler, never()).updateFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationIsDeletedWithEnabledTrue_thenTheConfigurationIsRemoved() throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(true);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);
        doNothing().when(spyFlmConfigurationHandler).deleteFlmAlgorithmExecutionSchedule(configuration);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.DELETE);

        verify(spyFlmConfigurationHandler, times(ONCE)).deleteFlmAlgorithmExecutionSchedule(configuration);
        verify(spyFlmConfigurationHandler, never()).updateFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationIsDeletedWithEnabledFalse_thenTheConfigurationIsCheckedForExistenceAndRemovedIfExists()
            throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(false);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.DELETE);

        verify(spyFlmConfigurationHandler, never()).deleteFlmAlgorithmExecutionSchedule(configuration);
        verify(spyFlmConfigurationHandler, never()).updateFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationIsCreatedWithEnabledFalse_theConfigurationIsNotScheduled() throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(false);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.CREATE);

        verify(spyFlmConfigurationHandler, never()).createFlmAlgorithmExecutionSchedule(configuration);
        verify(spyFlmConfigurationHandler, never()).updateFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationIsCreatedWithEnabledNull_theConfigurationIsNotScheduled() throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(null);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.CREATE);

        verify(spyFlmConfigurationHandler, never()).createFlmAlgorithmExecutionSchedule(configuration);
        verify(spyFlmConfigurationHandler, never()).updateFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationIsUpdatedWithEnabledTrue_theConfigurationScheduleIsUpdated() throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(true);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);
        doNothing().when(spyFlmConfigurationHandler).updateFlmAlgorithmExecutionSchedule(configuration);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.UPDATE);

        verify(spyFlmConfigurationHandler, never()).createFlmAlgorithmExecutionSchedule(configuration);
        verify(spyFlmConfigurationHandler, times(ONCE)).updateFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationIsUpdatedWithEnabledFalse_theConfigurationScheduleIsUpdated() throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(false);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.UPDATE);

        verify(spyFlmConfigurationHandler, never()).createFlmAlgorithmExecutionSchedule(configuration);
        verify(spyFlmConfigurationHandler, never()).updateFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationIsUpdatedWithEnabledNull_theConfigurationScheduleIsUpdated() throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(false);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.UPDATE);

        verify(spyFlmConfigurationHandler, never()).createFlmAlgorithmExecutionSchedule(configuration);
        verify(spyFlmConfigurationHandler, never()).updateFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationIsCreatedWithEnabledFalse_thenNoScheduleIsFoundWhenRemovingConfiguration() throws ActivitySchedulerException {
        final Configuration configuration = new Configuration();
        configuration.setId(1);
        configuration.setEnabled(false);
        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.CREATE);

        verify(spyFlmConfigurationHandler, never()).createFlmAlgorithmExecutionSchedule(configuration);

        spyFlmConfigurationHandler.updateFlmAlgorithmSchedules(configuration, FlmAlgorithmScheduleOperation.DELETE);
        verify(spyFlmConfigurationHandler, never()).deleteFlmAlgorithmExecutionSchedule(configuration);
    }

    @Test
    public void whenConfigurationCreatedWithIdInJson_andIdIsNotInUse_theConfigurationIsCreatedWithId() throws FlmConfigurationSettingsException,
            ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException {
        final int configurationId = 3;
        final String configurationWithIdInJson = getConfigurationInJson(configurationId, CONFIG_NAME);

        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);
        doNothing().when(spyFlmConfigurationHandler).updateFlmAlgorithmSchedules(any(), any());
        doReturn(new Configuration()).when(configurationDao).createWithId(any());

        spyFlmConfigurationHandler.createConfiguration(configurationWithIdInJson);

        verify(configurationDao, times(ONCE)).createWithId(any());
        verify(configurationDao, times(ONCE)).get(anyInt());
        verify(configurationDao, times(ONCE)).get(anyString());
        verify(configurationDao, times(ONCE)).getAll();
        verify(spyFlmConfigurationHandler, times(ONCE)).updateFlmAlgorithmSchedules(any(), any());
    }

    @Test
    public void whenConfigurationCreatedWithIdInJson_andIdIsInUse_theConfigurationIsNotCreatedAndErrorIsThrown() throws SQLException {
        final int configurationId = 3;
        final String configurationWithIdInJson = getConfigurationInJson(configurationId, CONFIG_NAME);

        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);
        doReturn(new Configuration()).when(configurationDao).get(3);

        assertThatThrownBy(() -> spyFlmConfigurationHandler.createConfiguration(configurationWithIdInJson))
                .isInstanceOf(FlmConfigurationSettingsException.class);
        verify(configurationDao, never()).create(any());
        verify(configurationDao, never()).createWithId(any());
    }

    @Test
    public void whenConfigurationCreatedWithMatchingIds_theConfigurationIsCreatedWithGivenId() throws FlmConfigurationSettingsException,
            ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException {
        final int configurationId = 3;
        final String configuration = getConfigurationInJson(configurationId, CONFIG_NAME);

        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);
        doNothing().when(spyFlmConfigurationHandler).updateFlmAlgorithmSchedules(any(), any());
        doReturn(new Configuration()).when(configurationDao).createWithId(any());

        spyFlmConfigurationHandler.createConfiguration(configurationId, configuration);

        verify(configurationDao, times(ONCE)).createWithId(any());
        verify(configurationDao, never()).create(any());
        verify(spyFlmConfigurationHandler, times(ONCE)).updateFlmAlgorithmSchedules(any(), any());
    }

    @Test
    public void whenConfigurationCreatedWithDifferentIds_thenExceptionIsThrown() throws FlmConfigurationSettingsException,
            ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException {
        final String configurationWithoutIdInJson = getConfigurationInJson(3, CONFIG_NAME);

        final FlmConfigurationHandler spyFlmConfigurationHandler = spy(OBJECT_UNDER_TEST);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        spyFlmConfigurationHandler.createConfiguration(7, configurationWithoutIdInJson);
    }

    @Test
    public void whenMoreThan10ConfigurationCreated_theExceptionIsThrown() throws FlmConfigurationSettingsException,
            ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException {
        final String configurationWithoutIdInJson = getConfigurationInJson(null, CONFIG_NAME);
        when(configurationDao.getAll()).thenReturn(Arrays.asList(null, null, null, null, null, null, null, null, null, null));
        thrown.expect(FlmConfigurationSettingsException.class);
        OBJECT_UNDER_TEST.createConfiguration(configurationWithoutIdInJson);
    }

    @Test
    public void whenConfigurationNameIsUpdatedToAnAlreadyExistingName_thenFlmConfigurationSettingsExceptionIsThrown() throws SQLException {
        final int configurationIdToUpdate = 1;
        final int persistedConfigurationId = 2;
        final String alreadyExistingName = "alreadyExistingName";

        final String configurationToUpdate = getConfigurationInJson(configurationIdToUpdate, alreadyExistingName);

        final Configuration persistedConfiguration = new Configuration();
        persistedConfiguration.setId(persistedConfigurationId);
        persistedConfiguration.setName(alreadyExistingName);

        when(configurationDao.get(alreadyExistingName)).thenReturn(persistedConfiguration);

        assertThatThrownBy(() -> OBJECT_UNDER_TEST.updateConfiguration(configurationIdToUpdate, configurationToUpdate))
                .hasMessage("A configuration with this name already exists")
                .isExactlyInstanceOf(FlmConfigurationSettingsException.class);

        verify(configurationDao).get(alreadyExistingName);
    }

    private static String getConfigurationInJson(final Integer id, final String configName) {
        final Configuration configuration = new Configuration();
        configuration.setEnabled(true);
        if (id != null) {
            configuration.setId(id);
        }
        configuration.setName(configName);
        configuration.setSchedule("0 0 2 ? * * *");
        configuration.setOpenLoop(true);
        return new Gson().toJson(configuration, Configuration.class);
    }

}
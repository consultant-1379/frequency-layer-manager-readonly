/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.startup.policy.deploy;

import static com.ericsson.oss.services.sonom.flm.service.startup.executions.FlmExecutionsController.FLM_POLICY_FILE_PATH;
import static com.ericsson.oss.services.sonom.flm.service.startup.executions.FlmExecutionsController.FLM_POLICY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.policy.PolicyCreator;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyCreationException;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * Unit tests for {@link PolicyCreatorImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlmAndPaPolicyCreatorTest {
    private static final Logger OBJECT_UNDER_TEST_LOGGER = (Logger) LoggerFactory.getLogger(PolicyCreatorImpl.class);
    private final RestExecutor mockedRestExecutor = mock(RestExecutor.class);
    private final RestResponse<String> mockedGoodRestResponse = mock(RestResponse.class);
    private final RestResponse<String> mockedBadRestResponse = mock(RestResponse.class);
    private final Appender<ILoggingEvent> logAppender = mock(Appender.class);
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private PolicyCreatorImpl objectUnderTest;

    @Before
    public void setUp() {
        System.setProperty("POLICY_REST_USER", "healthcheck");
        System.setProperty("POLICY_REST_PASSWORD", "zb!XztG34");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_HOST", "localhost");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_PORT", "6969");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_HOST", "localhost");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_PORT", "6969");
        System.setProperty("BOOTSTRAP_SERVER", "localhost");

        objectUnderTest = new PolicyCreatorImpl(new PolicyCreator(mockedRestExecutor));
    }

    @After
    public void tearDown() {
        OBJECT_UNDER_TEST_LOGGER.detachAppender(logAppender);
    }

    @Test
    public void whenCreatingFlmPolicyCreatorAndDeployer_andUsingDefaultConstructor_thenDoNotThrowException() {
        assertThat(objectUnderTest).isNotNull()
                .isNotEqualTo(new PolicyCreatorImpl());
    }

    @Test
    public void whenCreatingPolicyIsSuccessful_andNoExceptionIsThrown() throws PolicyCreationException, IOException {
        final PolicyCreator mockedFlmPolicyCreator = mock(PolicyCreator.class);
        new PolicyCreatorImpl(mockedFlmPolicyCreator).createPolicy(FLM_POLICY_NAME, FLM_POLICY_FILE_PATH);
        verify(mockedFlmPolicyCreator, times(1)).createPolicy(FLM_POLICY_FILE_PATH);
    }

    @Test
    public void whenCreatingAndDeployingPolicy_andExceptionIsCaught() throws PolicyCreationException, IOException {
        final PolicyCreator mockedFlmPolicyCreatorAndDeployer = mock(PolicyCreator.class);
        doThrow(mock(PolicyCreationException.class)).when(mockedFlmPolicyCreatorAndDeployer).createPolicy(FLM_POLICY_FILE_PATH);

        new PolicyCreatorImpl(mockedFlmPolicyCreatorAndDeployer).createPolicy(FLM_POLICY_NAME, FLM_POLICY_FILE_PATH);
        verify(mockedFlmPolicyCreatorAndDeployer, times(1)).createPolicy(FLM_POLICY_FILE_PATH);
    }

    @Test
    public void whenCreatingPolicyExecutor_andApexUsernameEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("POLICY_REST_USER");
        thrown.expect(NullPointerException.class);
        new PolicyCreatorImpl();
    }

    @Test
    public void whenCreatingFlmPolicyCreator_andApexPasswordEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("POLICY_REST_PASSWORD");
        thrown.expect(NullPointerException.class);
        new PolicyCreatorImpl();
    }

    @Test
    public void whenCreatingFlmPolicyCreator_andApiHostEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_HOST");
        thrown.expect(NullPointerException.class);
        new PolicyCreatorImpl();
    }

    @Test
    public void whenCreatingFlmPolicyCreator_andApiPortEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_PORT");
        thrown.expect(NullPointerException.class);
        new PolicyCreatorImpl();
    }

    @Test
    public void whenCreatingFlmPolicyCreator_andPapHostEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_HOST");
        thrown.expect(NullPointerException.class);
        new PolicyCreatorImpl();
    }

    @Test
    public void whenCreatingFlmPolicyCreator_andPapPortEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_PORT");
        thrown.expect(NullPointerException.class);
        new PolicyCreatorImpl();
    }

    @Test
    public void whenPostRequestReturnsValidStatusCode_thenCreatePolicyIsSuccessful() throws IOException {
        OBJECT_UNDER_TEST_LOGGER.addAppender(logAppender);

        mockSuccessOfClearPreviousPolicy();
        when(mockedGoodRestResponse.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedGoodRestResponse);
        objectUnderTest.createPolicy(FLM_POLICY_NAME, FLM_POLICY_FILE_PATH);
        verify(logAppender)
                .doAppend(argThat(argument -> String.valueOf(argument).equals("[INFO] onap.policies.apex.Flm Policy is created")));
        verifyNoMoreInteractions(logAppender);
    }

    @Test
    public void whenPostRequestReturnsInvalidStatusCode_thenCreatePolicyIsNotSuccessful_andAnExceptionIsLogged() throws IOException {
        OBJECT_UNDER_TEST_LOGGER.addAppender(logAppender);

        mockSuccessOfClearPreviousPolicy();
        when(mockedBadRestResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedBadRestResponse);
        objectUnderTest.createPolicy(FLM_POLICY_NAME, FLM_POLICY_FILE_PATH);

        verify(logAppender)
                .doAppend(argThat(argument -> String.valueOf(argument).equals("[ERROR] Failed to create onap.policies.apex.Flm policy")));
        verifyNoMoreInteractions(logAppender);
    }

    private void mockSuccessOfClearPreviousPolicy() throws IOException {
        when(mockedGoodRestResponse.getEntity()).thenReturn("");
        when(mockedGoodRestResponse.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(mockedRestExecutor.sendGetRequest(any(HttpGet.class))).thenReturn(mockedGoodRestResponse);
        when(mockedRestExecutor.sendDeleteRequest(any(HttpDelete.class))).thenReturn(mockedGoodRestResponse);
    }

}
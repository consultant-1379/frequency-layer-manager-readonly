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
package com.ericsson.oss.services.sonom.flm.policy;

import static com.ericsson.oss.services.sonom.flm.service.startup.executions.FlmExecutionsController.FLM_POLICY_FILE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyCreationException;
import com.ericsson.oss.services.sonom.policy.client.PolicyRestExecutor;

/**
 * Unit tests for {@link PolicyCreator} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PolicyCreatorTest {

    private final RestExecutor mockedRestExecutor = mock(RestExecutor.class);
    private final PolicyRestExecutor mockedPolicyRestExecutor = mock(PolicyRestExecutor.class);
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private PolicyCreator objectUnderTest;
    @Mock
    private RestExecutor mockRestExecutor;

    @Before
    public void setUp() {
        System.setProperty("POLICY_REST_USER", "healthcheck");
        System.setProperty("POLICY_REST_PASSWORD", "zb!XztG34");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_HOST", "localhost");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_PORT", "6969");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_HOST", "localhost");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_PORT", "6969");
        System.setProperty("BOOTSTRAP_SERVER", "localhost");
        objectUnderTest = new PolicyCreator(mockRestExecutor);
    }

    @Test
    public void whenCreatingPolicyCreatorAndDeployer_andUsingDefaultConstructor_thenDoNotThrowException() {
        assertThat(objectUnderTest).isNotNull()
                .isNotEqualTo(new PolicyCreator());
    }

    @Test
    public void whenCreatePolicyIsSuccessful_thenPostRequestAreSent_andNoExceptionIsThrown() throws IOException, PolicyCreationException {
        ReflectionTestUtils.setField(objectUnderTest, "policyRestExecutor", mockedPolicyRestExecutor);
        objectUnderTest.createPolicy(FLM_POLICY_FILE_PATH);
        verify(mockedPolicyRestExecutor, times(1)).createPolicy(anyString());
    }

    @Test
    public void whenCreatingPolicyExecutor_andApexUsernameEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("POLICY_REST_USER");
        thrown.expect(NullPointerException.class);
        new PolicyCreator();
    }

    @Test
    public void whenCreatingPolicyCreator_andApexPasswordEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("POLICY_REST_PASSWORD");
        thrown.expect(NullPointerException.class);
        new PolicyCreator();
    }

    @Test
    public void whenCreatingPolicyCreator_andApiHostEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_HOST");
        thrown.expect(NullPointerException.class);
        new PolicyCreator();
    }

    @Test
    public void whenCreatingPolicyCreator_andApiPortEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_PORT");
        thrown.expect(NullPointerException.class);
        new PolicyCreator();
    }

    @Test
    public void whenCreatingPolicyCreator_andPapHostEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_HOST");
        thrown.expect(NullPointerException.class);
        new PolicyCreator();
    }

    @Test
    public void whenCreatingPolicyCreator_andPapPortEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated() {
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_PORT");
        thrown.expect(NullPointerException.class);
        new PolicyCreator();
    }

    @Test
    public void whenCreatingPolicy_andBootstrapServerEnvironmentVariableIsNotSet_thenNullPointerExceptionIsPropagated()
            throws PolicyCreationException, IOException {
        System.clearProperty("BOOTSTRAP_SERVER");
        thrown.expect(NullPointerException.class);
        objectUnderTest.createPolicy(FLM_POLICY_FILE_PATH);
    }

    @Test
    public void whenCreatingPolicy_andFlmPolicyFilePathIsIncorrect_thenIOExceptionIsPropagated() throws PolicyCreationException, IOException {
        thrown.expect(IOException.class);
        new PolicyCreator(mockedRestExecutor).createPolicy("nonExistingFile");
    }
}

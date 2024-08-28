/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service;

import javax.ejb.Stateless;

import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * Ejb {@link FlmMetricHelperBean} extends {@link FlmMetricHelper}.
 */
@Stateless
public class FlmMetricHelperBean extends FlmMetricHelper implements MetricHelper {

}

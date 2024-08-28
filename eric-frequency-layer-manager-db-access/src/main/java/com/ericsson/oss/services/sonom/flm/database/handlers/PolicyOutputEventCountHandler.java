/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants;

/**
 * Creates a number of policy output events.
 */
public class PolicyOutputEventCountHandler implements ResultHandler<Integer> {

    @Override
    public Integer populate(final ResultSet resultSet) throws SQLException {
        Integer policyOutputEventCount = null;

        while (resultSet.next()) {
            policyOutputEventCount = resultSet.getInt(OptimizationsDbConstants.NUMBER_OF_SECTORS);
        }
        return policyOutputEventCount;
    }
}
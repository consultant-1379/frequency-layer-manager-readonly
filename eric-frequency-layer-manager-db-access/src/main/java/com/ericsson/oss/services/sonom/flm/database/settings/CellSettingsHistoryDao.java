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

package com.ericsson.oss.services.sonom.flm.database.settings;

import java.sql.SQLException;

import com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings;


/**
 * Interface defining the <code>CellSettingsHistoryDao</code>.
 */
public interface CellSettingsHistoryDao {
    /**
     * This method inserts the cell settings configurations, used in the execution, in the database.
     *
     * @param executionId {@link CellSettings} for the execution which is to be added.
     * @return number of rows copied.
     * @throws SQLException SqlException thrown when there is issue updating database.
     */
    int copyCellSettings(String executionId) throws SQLException;
}

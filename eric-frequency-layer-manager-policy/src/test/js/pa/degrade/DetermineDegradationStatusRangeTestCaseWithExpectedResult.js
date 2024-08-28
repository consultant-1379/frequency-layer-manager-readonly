/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

 //Test Cases
 //TC_33_whenSectorKpisDegrade_andTheKpiValueIsOutsideTheRange_thenSectorIsMarkedAsNotDegraded
 //TC_34_whenSectorCellKpisDegrade_andTheKpiValueIsOutsideTheRange_thenSectorIsMarkedAsNotDegraded
 //
var possibleInputEventsToPaPolicyRangeTest = [
    {
        description: "TC_33_whenSectorKpisDegrade_andTheKpiValueIsOutsideTheRange_thenSectorIsMarkedAsNotDegraded",
        result:{
            "verdict": "NOT DEGRADED",
            "degradedSectorKpis": {},
            "degradedCellKpis": {}
        },
        data:{
            "sectorId": "173290459927812150",
            "settings": {
                "numberOfKpiDegradedHoursThreshold": "2"
            },
            "kpis": {
                "test_sector_kpi_name": {
                    "kpiValue": [
                        {
                            "value": "1.3",
                            "timestamp": "1970-01-01 00:00:00.0",
                            "threshold": "1.4"
                        },
                        {
                            "value": "1.3",
                            "timestamp": "1970-01-01 00:01:00.0",
                            "threshold": "1.4"
                        }
                    ],
                    "enabled": true,
                    "lowerRangeLimit": "0",
                    "upperRangeLimit": "1.2"
                }
            },
            "cells": [
                {
                    "fdn": "cell_one",
                    "ossId": 1,
                    "kpis": {
                        "test_cell_kpi_name": {
                            "kpiValue": [
                                {
                                    "value": "91",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "90"
                                },
                                {
                                    "value": "91",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "90"
                                }
                            ],
                            "enabled": true,
                            "relevanceThreshold": "99",
                            "relevanceThresholdType": "MIN"
                        }
                    }
                },
                {
                    "fdn": "cell_two",
                    "ossId": 1,
                    "kpis": {
                        "test_cell_kpi_name": {
                            "kpiValue": [
                                {
                                    "value": "91",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "90"
                                },
                                {
                                    "value": "91",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "90"
                                }
                            ],
                            "enabled": true,
                            "relevanceThreshold": "99",
                            "relevanceThresholdType": "MIN"
                        }
                    }
                }
            ]
        }
    },
     {
        description: "TC_34_whenSectorCellKpisDegrade_andTheKpiValueIsOutsideTheRange_thenSectorIsMarkedAsNotDegraded",
        result:{
            "verdict": "NOT DEGRADED",
            "degradedSectorKpis": {},
            "degradedCellKpis": {}
        },
        data:{
            "sectorId": "173290459927812150",
            "settings": {
                "numberOfKpiDegradedHoursThreshold": "2"
            },
            "kpis": {
                "test_sector_kpi_name": {
                    "kpiValue": [
                        {
                            "value": "0.6",
                            "timestamp": "1970-01-01 00:00:00.0",
                            "threshold": "0.5"
                        },
                        {
                            "value": "0.6",
                            "timestamp": "1970-01-01 00:01:00.0",
                            "threshold": "0.5"
                        }
                    ],
                    "enabled": true,
                    "lowerRangeLimit": "0",
                    "upperRangeLimit": "1.2"
                }
            },
            "cells": [
                {
                    "fdn": "cell_one",
                    "ossId": 1,
                    "kpis": {
                        "test_cell_kpi_name": {
                            "kpiValue": [
                                {
                                    "value": "101",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "102"
                                },
                                {
                                    "value": "101",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "102"
                                }
                            ],
                            "enabled": true,
                            "relevanceThreshold": "150",
                            "relevanceThresholdType": "MIN"
                        }
                    }
                },
                {
                    "fdn": "cell_two",
                    "ossId": 1,
                    "kpis": {
                        "test_cell_kpi_name": {
                            "kpiValue": [
                                {
                                    "value": "101",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "102"
                                },
                                {
                                    "value": "101",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "102"
                                }
                            ],
                            "enabled": true,
                            "relevanceThreshold": "150",
                            "relevanceThresholdType": "MIN"
                        }
                    }
                }
            ]
        }
    }
];
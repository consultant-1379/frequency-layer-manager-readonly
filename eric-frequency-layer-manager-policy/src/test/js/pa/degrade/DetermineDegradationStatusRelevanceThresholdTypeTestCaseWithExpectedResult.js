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
 //TC_25_whenRelevanceThresholdTypeIsSetToMin_andAKpiValueExceedsTheThresholdValue_thenItIsNotConsideredDegraded
 //TC_26_whenRelevanceThresholdTypeIsSetToMax_andAKpiValueIsLessThanTheThresholdValue_thenItIsNotConsideredDegraded
 //TC_27_whenRelevanceThresholdTypeIsSetToMin_andAKpiValueIsLessThanTheThresholdValue_thenItIsConsideredDegraded
 //TC_28_whenRelevanceThresholdTypeIsSetToMax_andAKpiValueExceedsTheThresholdValue_thenItIsConsideredDegraded
 //TC_29_whenRelevanceThresholdHasUnknownValue_thenKpiIsNotConsideredToBeDegraded
 //
var possibleInputEventsToPaPolicyRelevanceThresholdTypeTest = [
    {
        description: "TC_25_whenRelevanceThresholdTypeIsSetToMin_andAKpiValueExceedsTheThresholdValue_thenItIsNotConsideredDegraded",
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
        description: "TC_26_whenRelevanceThresholdTypeIsSetToMax_andAKpiValueIsLessThanTheThresholdValue_thenItIsNotConsideredDegraded",
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
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "91"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "91"
                                }
                            ],
                            "enabled": true,
                            "relevanceThreshold": "80",
                            "relevanceThresholdType": "MAX"
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
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "91"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "91"
                                }
                            ],
                            "enabled": true,
                            "relevanceThreshold": "80",
                            "relevanceThresholdType": "MAX"
                        }
                    }
                }
            ]
        }
    },
    {
        description: "TC_27_whenRelevanceThresholdTypeIsSetToMin_andAKpiValueIsLessThanTheThresholdValue_thenItIsConsideredDegraded",
        result:{
            "verdict": "DEGRADED",
            "degradedSectorKpis": {},
            "degradedCellKpis": {
                "test_cell_kpi_name": {
                    "ossIdToFdnToDegradedTimestamps": {
                        "1": {
                            "cell_one": [
                                "1970-01-01 00:00:00.0",
                                "1970-01-01 00:01:00.0"
                            ],
                            "cell_two": [
                                "1970-01-01 00:00:00.0",
                                "1970-01-01 00:01:00.0"
                            ]
                        }
                    }
                }
            }
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
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "91"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "91"
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
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "91"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "91"
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
        description: "TC_28_whenRelevanceThresholdTypeIsSetToMax_andAKpiValueExceedsTheThresholdValue_thenItIsConsideredDegraded",
        result:{
            "verdict": "DEGRADED",
            "degradedSectorKpis": {},
            "degradedCellKpis": {
                "test_cell_kpi_name": {
                    "ossIdToFdnToDegradedTimestamps": {
                        "1": {
                            "cell_one": [
                                "1970-01-01 00:00:00.0",
                                "1970-01-01 00:01:00.0"
                            ],
                            "cell_two": [
                                "1970-01-01 00:00:00.0",
                                "1970-01-01 00:01:00.0"
                            ]
                        }
                    }
                }
            }
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
                            "relevanceThreshold": "80",
                            "relevanceThresholdType": "MAX"
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
                            "relevanceThreshold": "80",
                            "relevanceThresholdType": "MAX"
                        }
                    }
                }
            ]
        }
    },
    {
        description: "TC_29_whenRelevanceThresholdHasUnknownValue_thenKpiIsNotConsideredToBeDegraded",
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
                            "relevanceThreshold": "80",
                            "relevanceThresholdType": "INVALID"
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
                            "relevanceThreshold": "80",
                            "relevanceThresholdType": "INVALID"
                        }
                    }
                }
            ]
        }
    }
];
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
 //TC_08_whenAllKpisHaveNullStringValues_thenSectorIsMarkedAsNotDegraded_andNoKpisAreAddedToOutput
 //TC_09_whenAllKpisHaveNullValues_thenSectorIsMarkedAsNotDegraded_andNoKpisAreAddedToOutput
 //TC_10_whenAllThresholdsHaveNullValues_thenSectorIsMarkedAsNotDegraded_andNoKpisAreAddedToOutput
 //TC_11_whenOneKpiHasNullValues_AndOtherKpisHaveDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput
 //TC_12_whenOneThresholdHasNullValues_AndOtherKpisHaveDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput
 //TC_13_whenKpiIsNullForOneHour_AndOtherHoursHaveDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput
 //TC_14_whenSettingNumHoursDegradeIsNull_thenSectorIsMarkedAsNotDegraded_andKpisAreNotAddedToOutput
 //
var possibleInputEventsToPaPolicyNullHandlingTest = [
    {
        description: "TC_08_whenAllKpisHaveNullStringValues_thenSectorIsMarkedAsNotDegraded_andNoKpisAreAddedToOutput",
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
                            "value": "null",
                            "timestamp": "1970-01-01 00:00:00.0",
                            "threshold": "0.6"
                        },
                        {
                            "value": "null",
                            "timestamp": "1970-01-01 00:01:00.0",
                            "threshold": "0.6"
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
                                    "value": "null",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "99"
                                },
                                {
                                    "value": "null",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "99"
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
                                    "value": "null",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "99"
                                },
                                {
                                    "value": "null",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "99"
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
        description: "TC_09_whenAllKpisHaveNullValues_thenSectorIsMarkedAsNotDegraded_andNoKpisAreAddedToOutput",
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
                            "value": null,
                            "timestamp": "1970-01-01 00:00:00.0",
                            "threshold": "0.6"
                        },
                        {
                            "value": null,
                            "timestamp": "1970-01-01 00:01:00.0",
                            "threshold": "0.6"
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
                                    "value": null,
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "99"
                                },
                                {
                                    "value": null,
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "99"
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
                                    "value": null,
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "99"
                                },
                                {
                                    "value": null,
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "99"
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
        description: "TC_10_whenAllThresholdsHaveNullValues_thenSectorIsMarkedAsNotDegraded_andNoKpisAreAddedToOutput",
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
                            "value": "0.5",
                            "timestamp": "1970-01-01 00:00:00.0",
                            "threshold": "null"
                        },
                        {
                            "value": "0.5",
                            "timestamp": "1970-01-01 00:01:00.0",
                            "threshold": "null"
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
                                    "threshold": "null"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "null"
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
                                    "threshold": "null"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "null"
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
        description: "TC_11_whenOneKpiHasNullValues_AndOtherKpisHaveDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput",
        result:{
            "verdict": "DEGRADED",
            "degradedSectorKpis": {},
            "degradedCellKpis": {
                "test_cell_kpi_name": {
                    "ossIdToFdnToDegradedTimestamps": {
                        "1": {
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
                                    "value": "null",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "99"
                                },
                                {
                                    "value": "null",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "99"
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
        description: "TC_12_whenOneThresholdHasNullValue_AndOtherKpisHaveDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput",
        result:{
            "verdict": "DEGRADED",
            "degradedSectorKpis": {},
            "degradedCellKpis": {
                "test_cell_kpi_name": {
                    "ossIdToFdnToDegradedTimestamps": {
                        "1": {
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
                                    "threshold": "null"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "null"
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
        description: "TC_13_whenKpiIsNullForOneHour_AndOtherHoursHaveDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput",
        result:{
            "verdict": "DEGRADED",
            "degradedSectorKpis": {},
            "degradedCellKpis": {
                "test_cell_kpi_name": {
                    "ossIdToFdnToDegradedTimestamps": {
                        "1": {
                            "cell_two": [
                                "1970-01-01 00:01:00.0",
                                "1970-01-01 00:02:00.0"
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
                                    "value": "null",
                                    "timestamp": "1970-01-01 00:00:00.0",
                                    "threshold": "91"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:01:00.0",
                                    "threshold": "91"
                                },
                                {
                                    "value": "90",
                                    "timestamp": "1970-01-01 00:02:00.0",
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
        description: "TC_14_whenSettingNumHoursDegradeIsNull_thenSectorIsMarkedAsNotDegraded_andKpisAreNotAddedToOutput",
        result:{
            "verdict": "NOT DEGRADED",
            "degradedSectorKpis": {},
            "degradedCellKpis": {}
        },
        data:{
            "sectorId": "173290459927812150",
            "settings": {
                "numberOfKpiDegradedHoursThreshold": "null"
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
    }
];
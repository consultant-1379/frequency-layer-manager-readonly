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
 //TC_01_whenAllKpisDegrade_thenSectorIsMarkedAsDegraded_andAllKpisAreAddedToOutput
 //TC_02_whenOneSectorLevelKpiHasDegraded_thenSectorIsMarkedAsDegraded_andOneKpiIsAddedToOutput
 //TC_03_whenOneCellLevelKpiHasDegraded_thenSectorIsMarkedAsDegraded_andOneKpiIsAddedToOutput
 //TC_04_whenTwoCellLevelKpisHaveDegraded_thenSectorIsMarkedAsDegraded_andTwoKpisAreAddedToOutput
 //TC_05_whenNoKpisHaveDegraded_thenSectorIsMarkedAsNotDegraded_andNoKpisAreAddedToOutput
 //TC_06_whenACellHasMultipleKpis_andOneHasDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput
 //TC_07_whenMultipleCellHasMultipleKpis_andAllHaveDegraded_thenSectorIsMarkedAsDegraded_andAllKpisAreAddedToOutput
 //
var possibleInputEventsToPaPolicyThresholdComparisonTest = [
    {
        description: "TC_01_whenAllKpisDegrade_thenSectorIsMarkedAsDegraded_andAllKpisAreAddedToOutput",
        result:{
            "verdict": "DEGRADED",
            "degradedSectorKpis": {
                "test_sector_kpi_name": {
                    "sectorIdToDegradedTimestamps": {
                        "173290459927812150": [
                            "1970-01-01 00:00:00.0",
                            "1970-01-01 00:01:00.0"
                        ]
                    }
                }
            },
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
                            "value": "0.5",
                            "timestamp": "1970-01-01 00:00:00.0",
                            "threshold": "0.6"
                        },
                        {
                            "value": "0.5",
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
        description: "TC_02_whenOneSectorLevelKpiHasDegraded_thenSectorIsMarkedAsDegraded_andOneKpiIsAddedToOutput",
        result:{
            "verdict": "DEGRADED",
            "degradedSectorKpis": {
                "test_sector_kpi_name": {
                    "sectorIdToDegradedTimestamps": {
                        "173290459927812150": [
                            "1970-01-01 00:00:00.0",
                            "1970-01-01 00:01:00.0"
                        ]
                    }
                }
            },
            "degradedCellKpis": {

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
                            "value": "0.5",
                            "timestamp": "1970-01-01 00:00:00.0",
                            "threshold": "0.6"
                        },
                        {
                            "value": "0.5",
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
                            "relevanceThreshold": "10",
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
                            "relevanceThreshold": "10",
                            "relevanceThresholdType": "MIN"
                        }
                    }
                }
            ]
        }
    },
    {
        description: "TC_03_whenOneCellLevelKpiHasDegraded_thenSectorIsMarkedAsDegraded_andOneKpiIsAddedToOutput",
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
        description: "TC_04_whenTwoCellLevelKpisHaveDegraded_thenSectorIsMarkedAsDegraded_andTwoKpisAreAddedToOutput",
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
        description: "TC_05_whenNoKpisHaveDegraded_thenSectorIsMarkedAsNotDegraded_andNoKpisAreAddedToOutput",
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
        description: "TC_06_whenACellHasMultipleKpis_andOneHasDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput",
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
                        },
                        "test_cell_kpi_name_two": {
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
        description: "TC_07_whenMultipleCellHasMultipleKpis_andAllHaveDegraded_thenSectorIsMarkedAsDegraded_andAllKpisAreAddedToOutput",
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
                },
                "test_cell_kpi_name_two": {
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
                        },
                        "test_cell_kpi_name_two": {
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
                        },
                        "test_cell_kpi_name_two": {
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
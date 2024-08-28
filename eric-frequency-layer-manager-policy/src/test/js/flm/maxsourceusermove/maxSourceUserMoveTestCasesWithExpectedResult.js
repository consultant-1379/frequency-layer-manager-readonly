/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
var maxSourceUserMoveTestCases = [{
        description: "Calculate max source user from source cell with 40 connected, and expect no of users to move from source cell 0, as delta of p_failing_r_mbps is 0",
        result: 0,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.1",
                    "connected_users": "40"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "4",
                    "unhappy_users": "1",
                    "p_failing_r_mbps": "0.1"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }]
        }]
    },
    {
        description: "Calculate source user move with connected user 21 and p_failing_r_mbps delta 0.8. verify source cell user move 16.8 ",
        result: 16.8,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move with connected user 40 and p_failing_r_mbps delta 0.49., verify for integer value no rounding of integer 24",
        result: 19.999999999999996,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.7",
                    "connected_users": "40"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.4"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8 and endc_spid115_ues is empty. Verify max source user move is 16.8.",
        result: 16.8,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": ""
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8 and endc_spid115_ues is null. Verify max source user move is 16.8.",
        result: 16.8,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": "null"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8 and endc_spid115_ues is 10.5. Verify max source user move is 8.4.",
        result: 8.4,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": "10.5"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8 and endc_spid115_ues is 21. Verify max source user move is 0.",
        result: 0,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": "21"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8 and endc_spid115_ues is 0. Verify max source user move is 16.8.",
        result: 16.8,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": "0"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8 and endc_spid115_ues is -10.5. Verify max source user move is 16.8 (negative endc_spid115_ues is ignored and treated as 0).",
        result: 16.8,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": "-10.5"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8, endc_spid115_ues is 0 and CAIMC is Deactivated. Verify max source user move is 16.8.",
        result: 16.8,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": "0"
                },
                "cmAttributes": {
                    "caimc": "DEACTIVATED"
                },
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when CAIMC is Deactivated, connected_users is 21, p_failing_r_mbps delta is 0.8 and endc_spid115_ues is 6. Verify max source user move is 12.",
        result: 12,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": "6"
                },
                "cmAttributes": {
                    "caimc": "DEACTIVATED"
                },
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when CAIMC is Deactivated, connected_users is 21, p_failing_r_mbps delta is 0.8 and endc_spid115_ues is -6. Verify max source user move is 16.8.",
        result: 16.8,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "21",
                    "endc_spid115_ues": "-6",
                },
                "cmAttributes": {
                    "caimc": "DEACTIVATED"
                },
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.1"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "4",
                        "unhappy_users": "1",
                        "p_failing_r_mbps": "0.2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8, CAIMC is Activated, and percentage_endc_users is 20. Verify max source user move is 19.2.",
        result: 19.2,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "0.224184080627752",
                    "unhappy_users": "0.592511239070919",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "40",
                    "percentage_endc_users": "20",
                },
                "cmAttributes": {
                    "caimc": "ACTIVATED"
                },
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.602045667098208",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.3"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.734909938121602",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 4,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.758874105661037",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.3"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8, CAIMC is Activated, and percentage_endc_users is 0. Verify max source user move is 24.",
        result: 24,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "0.224184080627752",
                    "unhappy_users": "0.592511239070919",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "40",
                    "percentage_endc_users": "",
                },
                "cmAttributes": {
                    "caimc": "ACTIVATED"
                },
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.602045667098208",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.3"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.734909938121602",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 4,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.758874105661037",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.3"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8, CAIMC is Activated, and percentage_endc_users is set to string value of null. Verify max source user move is 24.",
        result: 24,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "0.224184080627752",
                    "unhappy_users": "0.592511239070919",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "40",
                    "percentage_endc_users": "null",
                },
                "cmAttributes": {
                    "caimc": "ACTIVATED"
                },
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.602045667098208",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.3"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.734909938121602",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 4,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.758874105661037",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.3"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
    {
        description: "Calculate source user move when connected_users is 21, p_failing_r_mbps delta is 0.8, CAIMC is Activated, and percentage_endc_users is null. Verify max source user move is 24.",
        result: 24,
        data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "0.224184080627752",
                    "unhappy_users": "0.592511239070919",
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "40",
                    "percentage_endc_users": null,
                },
                "cmAttributes": {
                    "caimc": "ACTIVATED"
                },
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.602045667098208",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.3"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.734909938121602",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 4,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.758874105661037",
                        "unhappy_users": "13.4191211495669",
                        "p_failing_r_mbps": "0.3"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "3"
                    }
                },
            ]
        }]
    },
];
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
var transienceScreeningTestCells = [{
    description: "Source and Target cells do not have upper and lower detrended probability thresholds set",
    size: 1,
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "0.179748885407809429"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.179748885407809429"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.158756407809429"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "0.179748885407809429"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.179748885407809429"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.158756407809429"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }]
}, {
    description: "Possible source and target cells, where all cells are within threshold bounds.",
    size: 1,
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "17.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.48863585498979765",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "17.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.48863585498979765",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }]
}, {
    description: "Possible source and target cells, where source cell breaks the upper bound",
    size: 0,
    result: [],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "8.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.48863585498979765",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }]
}, {
    description: "Possible source and target cells, where a target cell breaks the lower bound.",
    size: 1,
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.41214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }]
}, {
    description: "Possible source and target cells, where one source cell is excluded as all targets breaks the lower bound.",
    size: 1,
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "1.48863585498979765",
                "lower_threshold_for_transient": "0.21214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "1.48863585498979765",
                "lower_threshold_for_transient": "0.21214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }, {
        "sourceCell": {
            "fdn": "005",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "[11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "007",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.41214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "008",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.41214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }]
}, {
    description: "Possible source and target cells, where a target cell breaks the upper bound.",
    size: 1,
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "1.48863585498979765",
                "lower_threshold_for_transient": "0.41214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "11.3000000000000007",
                "lower_threshold_for_transient": "-1.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "1.48863585498979765",
                "lower_threshold_for_transient": "0.41214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }]
}, {
    description: "Possible source and target cells, where a source cell breaks the lower bound.",
    size: 1,
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "2.200000000000018",
                "lower_threshold_for_transient": "4.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.44148885407809429",
                "lower_threshold_for_transient": "0.41214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "2.200000000000018",
                "lower_threshold_for_transient": "4.8744768441988633",
                "upper_threshold_for_transient": "18.325000000000001"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.34148885407809429",
                "lower_threshold_for_transient": "0.11214888540780943",
                "upper_threshold_for_transient": "0.73214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }, {
            "fdn": "002",
            "ossId": 2,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1",
                "p_failing_r_mbps_detrended": "0.44148885407809429",
                "lower_threshold_for_transient": "0.41214888540780943",
                "upper_threshold_for_transient": "1.43214888540780943"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5"
            }
        }]
    }]
}];
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
var possibleSourceCellsAndTargetCellsCoverageBalanceTest = [{
    description: "Possible Source Cell And Target cells where one target cell doesn't satisfy coverage balance rule 1 and one target cell doesn't satisfy coverage balance rule 2  is screened out",
    size: 1,
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "contiguity": "5",
                "coverage_balance_ratio_distance": "4",
                "distance_q1": "324.8",
                "distance_q2": "649.6",
                "distance_q3": "974.4",
                "distance_q4": "1299.2",
                "ue_percentage_q1": "42.79",
                "ue_percentage_q2": "12.84",
                "ue_percentage_q3": "0",
                "ue_percentage_q4": "44.3",
                "synthetic_counter_cell_reliability_daily": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "target_source_coverage_balance_ratio_threshold": "0.9",
                "source_target_samples_overlap_threshold": "70",
                "synthetic_counters_cell_reliability_threshold_in_rops": "1"
            }
        },
        "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "2",
                    "coverage_balance_ratio_distance": "3.3",
                    "distance_q1": "299.2",
                    "distance_q2": "598.4",
                    "distance_q3": "897.6",
                    "distance_q4": "1196.8",
                    "ue_percentage_q1": "5.8823",
                    "ue_percentage_q2": "75.63025",
                    "ue_percentage_q3": "1.68",
                    "ue_percentage_q4": "16.8067",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "1.2",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            {
                "fdn": "003",
                "ossId": 3,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "3",
                    "coverage_balance_ratio_distance": "4",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.7",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }
        ]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "contiguity": "5",
                "coverage_balance_ratio_distance": "4",
                "distance_q1": "324.8",
                "distance_q2": "649.6",
                "distance_q3": "974.4",
                "distance_q4": "1299.2",
                "ue_percentage_q1": "42.79",
                "ue_percentage_q2": "12.84",
                "ue_percentage_q3": "0",
                "ue_percentage_q4": "44.3",
                "synthetic_counter_cell_reliability_daily": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "target_source_coverage_balance_ratio_threshold": "0.9",
                "source_target_samples_overlap_threshold": "70",
                "synthetic_counters_cell_reliability_threshold_in_rops": "1"
            }
        },
        "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "2",
                    "coverage_balance_ratio_distance": "3.3",
                    "distance_q1": "299.2",
                    "distance_q2": "598.4",
                    "distance_q3": "897.6",
                    "distance_q4": "1196.8",
                    "ue_percentage_q1": "5.8823",
                    "ue_percentage_q2": "75.63025",
                    "ue_percentage_q3": "1.68",
                    "ue_percentage_q4": "16.8067",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "1.2",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            {
                "fdn": "003",
                "ossId": 3,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "3",
                    "coverage_balance_ratio_distance": "4",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.7",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            {
                "fdn": "004",
                "ossId": 4,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "3",
                    "coverage_balance_ratio_distance": "1",
                    "distance_q1": "101.4",
                    "distance_q2": "240.3",
                    "distance_q3": "420.8",
                    "distance_q4": "640.6",
                    "ue_percentage_q1": "37.79",
                    "ue_percentage_q2": "12.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "49.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.7",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }
        ]
    }]
}, {
    description: "Possible Source Cell And Target cells where all target cell satisfies coverage balance rule no target cell is screened out",
    size: 1,
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "contiguity": "5",
                "coverage_balance_ratio_distance": "2",
                "distance_q1": "324.8",
                "distance_q2": "649.6",
                "distance_q3": "974.4",
                "distance_q4": "1299.2",
                "ue_percentage_q1": "42.79",
                "ue_percentage_q2": "12.84",
                "ue_percentage_q3": "0",
                "ue_percentage_q4": "44.3",
                "synthetic_counter_cell_reliability_daily": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "target_source_coverage_balance_ratio_threshold": "1.9",
                "source_target_samples_overlap_threshold": "70",
                "synthetic_counters_cell_reliability_threshold_in_rops": "1"
            }
        },
        "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "4",
                    "coverage_balance_ratio_distance": "5",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.8",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            {
                "fdn": "003",
                "ossId": 3,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "3",
                    "coverage_balance_ratio_distance": "4",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.6",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }
        ]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "contiguity": "5",
                "coverage_balance_ratio_distance": "2",
                "distance_q1": "324.8",
                "distance_q2": "649.6",
                "distance_q3": "974.4",
                "distance_q4": "1299.2",
                "ue_percentage_q1": "42.79",
                "ue_percentage_q2": "12.84",
                "ue_percentage_q3": "0",
                "ue_percentage_q4": "44.3",
                "synthetic_counter_cell_reliability_daily": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "target_source_coverage_balance_ratio_threshold": "1.9",
                "source_target_samples_overlap_threshold": "70",
                "synthetic_counters_cell_reliability_threshold_in_rops": "1"
            }
        },
        "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "4",
                    "coverage_balance_ratio_distance": "5",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.8",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            {
                "fdn": "003",
                "ossId": 3,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "3",
                    "coverage_balance_ratio_distance": "4",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.6",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }
        ]
    }]
}, {
    description: "Possible Source Cell And Target cells where all target cells doesn't satisfy coverage balance rule the whole sector is then excluded",
    size: 0,
    result: [],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "contiguity": "5",
                "coverage_balance_ratio_distance": "7",
                "distance_q1": "324.8",
                "distance_q2": "649.6",
                "distance_q3": "974.4",
                "distance_q4": "1299.2",
                "ue_percentage_q1": "42.79",
                "ue_percentage_q2": "12.84",
                "ue_percentage_q3": "0",
                "ue_percentage_q4": "44.3",
                "synthetic_counter_cell_reliability_daily": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "target_source_coverage_balance_ratio_threshold": "0.6",
                "source_target_samples_overlap_threshold": "70",
                "synthetic_counters_cell_reliability_threshold_in_rops": "1"
            }
        },
        "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "2",
                    "coverage_balance_ratio_distance": "4",
                    "distance_q1": "101.4",
                    "distance_q2": "240.3",
                    "distance_q3": "420.8",
                    "distance_q4": "640.6",
                    "ue_percentage_q1": "37.79",
                    "ue_percentage_q2": "12.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "49.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.4",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            {
                "fdn": "003",
                "ossId": 3,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "1",
                    "coverage_balance_ratio_distance": "3",
                    "distance_q1": "101.4",
                    "distance_q2": "240.3",
                    "distance_q3": "420.8",
                    "distance_q4": "640.6",
                    "ue_percentage_q1": "37.79",
                    "ue_percentage_q2": "12.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "49.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.5",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }
        ]
    }]
}, {
    description: "Possible Source Cell And Target cells where source cell has coverage balance of zero, cell and sector is excluded ",
    size: 0,
    result: [],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "contiguity": "5",
                "coverage_balance_ratio_distance": "0",
                "distance_q1": "324.8",
                "distance_q2": "649.6",
                "distance_q3": "974.4",
                "distance_q4": "1299.2",
                "ue_percentage_q1": "42.79",
                "ue_percentage_q2": "12.84",
                "ue_percentage_q3": "0",
                "ue_percentage_q4": "44.3",
                "synthetic_counter_cell_reliability_daily": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "target_source_coverage_balance_ratio_threshold": "0.6",
                "source_target_samples_overlap_threshold": "70",
                "synthetic_counters_cell_reliability_threshold_in_rops": "1"
            }
        },
        "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "2",
                    "coverage_balance_ratio_distance": "4",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.4",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            {
                "fdn": "003",
                "ossId": 3,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "1",
                    "coverage_balance_ratio_distance": "3",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.5",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }
        ]
    }]
}, {
    description: "Possible Source Cell And Target cells where two source cells each with a different threshold value, ensure screening is based off unique source threshold value",
    size: 2,
    result: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "2",
                    "unhappy_users": "2",
                    "contiguity": "5",
                    "coverage_balance_ratio_distance": "6",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "42.79",
                    "ue_percentage_q2": "12.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "44.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.3",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "3",
                        "unhappy_users": "1",
                        "contiguity": "4",
                        "coverage_balance_ratio_distance": "2",
                        "distance_q1": "324.8",
                        "distance_q2": "649.6",
                        "distance_q3": "974.4",
                        "distance_q4": "1299.2",
                        "ue_percentage_q1": "67.79",
                        "ue_percentage_q2": "22.84",
                        "ue_percentage_q3": "0",
                        "ue_percentage_q4": "9.3",
                        "synthetic_counter_cell_reliability_daily": "2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "target_source_contiguity_ratio_threshold": "0.5",
                        "target_source_coverage_balance_ratio_threshold": "0.8",
                        "source_target_samples_overlap_threshold": "70",
                        "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "3",
                        "unhappy_users": "1",
                        "contiguity": "3",
                        "coverage_balance_ratio_distance": "3",
                        "distance_q1": "324.8",
                        "distance_q2": "649.6",
                        "distance_q3": "974.4",
                        "distance_q4": "1299.2",
                        "ue_percentage_q1": "67.79",
                        "ue_percentage_q2": "22.84",
                        "ue_percentage_q3": "0",
                        "ue_percentage_q4": "9.3",
                        "synthetic_counter_cell_reliability_daily": "2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "target_source_contiguity_ratio_threshold": "0.5",
                        "target_source_coverage_balance_ratio_threshold": "0.6",
                        "source_target_samples_overlap_threshold": "70",
                        "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                    }
                }
            ]
        },
        {
            "sourceCell": {
                "fdn": "010",
                "ossId": 10,
                "kpis": {
                    "goal_function_resource_efficiency": "2",
                    "unhappy_users": "2",
                    "contiguity": "5",
                    "coverage_balance_ratio_distance": "6",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "42.79",
                    "ue_percentage_q2": "12.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "44.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.4",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            "targetCells": [{
                "fdn": "012",
                "ossId": 12,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "1",
                    "coverage_balance_ratio_distance": "3",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.5",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }]
        }
    ],
    data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "2",
                    "unhappy_users": "2",
                    "contiguity": "5",
                    "coverage_balance_ratio_distance": "6",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "42.79",
                    "ue_percentage_q2": "12.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "44.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.3",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            "targetCells": [{
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                        "goal_function_resource_efficiency": "3",
                        "unhappy_users": "1",
                        "contiguity": "4",
                        "coverage_balance_ratio_distance": "2",
                        "distance_q1": "324.8",
                        "distance_q2": "649.6",
                        "distance_q3": "974.4",
                        "distance_q4": "1299.2",
                        "ue_percentage_q1": "67.79",
                        "ue_percentage_q2": "22.84",
                        "ue_percentage_q3": "0",
                        "ue_percentage_q4": "9.3",
                        "synthetic_counter_cell_reliability_daily": "2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "target_source_contiguity_ratio_threshold": "0.5",
                        "target_source_coverage_balance_ratio_threshold": "0.8",
                        "source_target_samples_overlap_threshold": "70",
                        "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                        "goal_function_resource_efficiency": "3",
                        "unhappy_users": "1",
                        "contiguity": "3",
                        "coverage_balance_ratio_distance": "3",
                        "distance_q1": "324.8",
                        "distance_q2": "649.6",
                        "distance_q3": "974.4",
                        "distance_q4": "1299.2",
                        "ue_percentage_q1": "67.79",
                        "ue_percentage_q2": "22.84",
                        "ue_percentage_q3": "0",
                        "ue_percentage_q4": "9.3",
                        "synthetic_counter_cell_reliability_daily": "2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "target_source_contiguity_ratio_threshold": "0.5",
                        "target_source_coverage_balance_ratio_threshold": "0.6",
                        "source_target_samples_overlap_threshold": "70",
                        "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                    }
                }
            ]
        },
        {
            "sourceCell": {
                "fdn": "010",
                "ossId": 10,
                "kpis": {
                    "goal_function_resource_efficiency": "2",
                    "unhappy_users": "2",
                    "contiguity": "5",
                    "coverage_balance_ratio_distance": "6",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "42.79",
                    "ue_percentage_q2": "12.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "44.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.4",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            "targetCells": [{
                    "fdn": "011",
                    "ossId": 11,
                    "kpis": {
                        "goal_function_resource_efficiency": "3",
                        "unhappy_users": "1",
                        "contiguity": "4",
                        "coverage_balance_ratio_distance": "2",
                        "distance_q1": "101.4",
                        "distance_q2": "240.3",
                        "distance_q3": "420.8",
                        "distance_q4": "640.6",
                        "ue_percentage_q1": "37.79",
                        "ue_percentage_q2": "12.84",
                        "ue_percentage_q3": "0",
                        "ue_percentage_q4": "49.3",
                        "synthetic_counter_cell_reliability_daily": "2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "target_source_contiguity_ratio_threshold": "0.5",
                        "target_source_coverage_balance_ratio_threshold": "0.4",
                        "source_target_samples_overlap_threshold": "70",
                        "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                    }
                },
                {
                    "fdn": "012",
                    "ossId": 12,
                    "kpis": {
                        "goal_function_resource_efficiency": "3",
                        "unhappy_users": "1",
                        "contiguity": "1",
                        "coverage_balance_ratio_distance": "3",
                        "distance_q1": "324.8",
                        "distance_q2": "649.6",
                        "distance_q3": "974.4",
                        "distance_q4": "1299.2",
                        "ue_percentage_q1": "67.79",
                        "ue_percentage_q2": "22.84",
                        "ue_percentage_q3": "0",
                        "ue_percentage_q4": "9.3",
                        "synthetic_counter_cell_reliability_daily": "2"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "target_source_contiguity_ratio_threshold": "0.5",
                        "target_source_coverage_balance_ratio_threshold": "0.5",
                        "source_target_samples_overlap_threshold": "70",
                        "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                    }
                }
            ]
        }
    ]
},{
    description: "Possible Source Cell And Target cells where the source cell synthetic_counters_cell_reliability_threshold_in_rops is null",
    size: 0,
    result: [],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "contiguity": "5",
                "coverage_balance_ratio_distance": "0",
                "distance_q1": "324.8",
                "distance_q2": "649.6",
                "distance_q3": "974.4",
                "distance_q4": "1299.2",
                "ue_percentage_q1": "42.79",
                "ue_percentage_q2": "12.84",
                "ue_percentage_q3": "0",
                "ue_percentage_q4": "44.3",
                "synthetic_counter_cell_reliability_daily": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "target_source_coverage_balance_ratio_threshold": "0.6",
                "source_target_samples_overlap_threshold": "70",
                "synthetic_counters_cell_reliability_threshold_in_rops": "null"
            }
        },
        "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "2",
                    "coverage_balance_ratio_distance": "4",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.4",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            },
            {
                "fdn": "003",
                "ossId": 3,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "1",
                    "coverage_balance_ratio_distance": "3",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.5",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }
        ]
    }]
},{
    description: "Possible Source Cell And Target cells where the target cell synthetic_counters_cell_reliability_threshold_in_rops is null",
    size: 0,
    result: [],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "contiguity": "5",
                "coverage_balance_ratio_distance": "0",
                "distance_q1": "324.8",
                "distance_q2": "649.6",
                "distance_q3": "974.4",
                "distance_q4": "1299.2",
                "ue_percentage_q1": "42.79",
                "ue_percentage_q2": "12.84",
                "ue_percentage_q3": "0",
                "ue_percentage_q4": "44.3",
                "synthetic_counter_cell_reliability_daily": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "target_source_coverage_balance_ratio_threshold": "0.6",
                "source_target_samples_overlap_threshold": "70",
                "synthetic_counters_cell_reliability_threshold_in_rops": "1"
            }
        },
        "targetCells": [{
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "2",
                    "coverage_balance_ratio_distance": "4",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.4",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "null"
                }
            },
            {
                "fdn": "003",
                "ossId": 3,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1",
                    "contiguity": "1",
                    "coverage_balance_ratio_distance": "3",
                    "distance_q1": "324.8",
                    "distance_q2": "649.6",
                    "distance_q3": "974.4",
                    "distance_q4": "1299.2",
                    "ue_percentage_q1": "67.79",
                    "ue_percentage_q2": "22.84",
                    "ue_percentage_q3": "0",
                    "ue_percentage_q4": "9.3",
                    "synthetic_counter_cell_reliability_daily": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3",
                    "target_source_contiguity_ratio_threshold": "0.5",
                    "target_source_coverage_balance_ratio_threshold": "0.5",
                    "source_target_samples_overlap_threshold": "70",
                    "synthetic_counters_cell_reliability_threshold_in_rops": "1"
                }
            }
        ]
    }]
}];
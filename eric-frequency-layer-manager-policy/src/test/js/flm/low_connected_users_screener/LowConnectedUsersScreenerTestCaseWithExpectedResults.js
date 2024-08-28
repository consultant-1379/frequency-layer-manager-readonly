/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

var lowConnectedUsersScreeningTestCells = [{
    description: "The number of connected users on the source cell is lower than the minimum threshold and all cells are screened out.",
    result: [],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "0.179748885407809429",
                "connected_users": "9"
            },
           "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "min_connected_users": "10"
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
},{
    description: "The number of connected users on the source cell exceeds the minimum threshold and no cells are screened out.",
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "0.179748885407809429",
                "connected_users": "11"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "min_connected_users": "10"
            }
        },
        "targetCells": [{
            "fdn": "003",
            "ossId": 3,
            "kpis": {
                "unhappy_users": "1",
                "goal_function_resource_efficiency": "3",
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
    result: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "0.179748885407809429",
                "connected_users": "11"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "min_connected_users": "10"
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
},{
    description: "The number of connected users on 1 source cell is lower than the minimum threshold, only that cell and its target cells are filtered out.",
    result: [{
        "sourceCell": {
            "fdn": "004",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "0.179748885407809429",
                "connected_users": "12"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "min_connected_users": "10"
            }
        },
        "targetCells": [{
            "fdn": "005",
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
        }]
    }],
    data: [{
        "sourceCell": {
            "fdn": "001",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "0.179748885407809429",
                "connected_users": "9"
            },
           "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "min_connected_users": "10"
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
    },{
        "sourceCell": {
            "fdn": "004",
            "ossId": 1,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "2",
                "p_failing_r_mbps_detrended": "0.179748885407809429",
                "connected_users": "12"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "0.3",
                "target_source_contiguity_ratio_threshold": "0.5",
                "min_connected_users": "10"
            }
        },
        "targetCells": [
        {
            "fdn": "005",
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
        }]
    }]
}];
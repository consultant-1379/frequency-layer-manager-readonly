/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

var optimizationCellsArray = [
    {
        description: "Optimization Cells with One cell",
        result: false,
        data: [
            {
                "fdn": "001",
                "ossId": 11,
                "kpis": {
                    "goal_function_resource_efficiency": "2",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }

        ]
},
    {
        description: "Optimization Cells with GF difference less than GF Threshold",
        result: false,
        data: [
            {
                "fdn": "001",
                "ossId": 11,
                "kpis": {
                    "goal_function_resource_efficiency": "2",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            {
                "fdn": "002",
                "ossId": 22,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }
        ]
},

    {
        description: "Optimization Cells with GF difference greater than GF Threshold",
        result: true,
        data: [
            {
                "fdn": "001",
                "ossId": 11,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            {
                "fdn": "002",
                "ossId": 22,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "1"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }
        ]
},
    {
        description: "Optimization Cells with GF difference equal to GF Threshold",
        result: true,
        data: [
            {
                "fdn": "001",
                "ossId": 11,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            {
                "fdn": "002",
                "ossId": 22,
                "kpis": {
                    "goal_function_resource_efficiency": "4",
                    "unhappy_users": "1"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }
        ]
},

    {
        description: "3 Optimization Cells with GF difference less than GF Threshold",
        result: false,
        data:  [
            {
                "fdn": "001",
                "ossId": 11,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            {
                "fdn": "002",
                "ossId": 22,
                "kpis": {
                    "goal_function_resource_efficiency": "6",
                    "unhappy_users": "1"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            {
                "fdn": "003",
                "ossId": 33,
                "kpis": {
                    "goal_function_resource_efficiency": "8",
                    "unhappy_users": "1"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }

        ]
},
    {
        description: "3 Optimization Cells with GF difference greater than GF Threshold",
        result: true,
        data:  [
        {
            "fdn": "001",
            "ossId": 11,
            "kpis": {
                "goal_function_resource_efficiency": "7",
                "unhappy_users": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "3"
            }
        },
        {
            "fdn": "002",
            "ossId": 22,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "3"
            }
        },
        {
            "fdn": "003",
            "ossId": 33,
            "kpis": {
                "goal_function_resource_efficiency": "2",
                "unhappy_users": "1"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "3"
            }
        }

    ]
},

    {
        description: "Optimization Cells with inconsistent target_throughput_r value",
        result: false,
        data:  [
        {
            "fdn": "001",
            "ossId": 11,
            "kpis": {
                "goal_function_resource_efficiency": "7",
                "unhappy_users": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "3"
            }
        },
        {
            "fdn": "002",
            "ossId": 22,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "2",
                "delta_gfs_optimization_threshold": "3"
            }
        }

    ]
},

    {
        description: "Optimization Cells with inconsistent delta_gfs_optimization_threshold value",
        result: false,
        data:  [
        {
            "fdn": "001",
            "ossId": 11,
            "kpis": {
                "goal_function_resource_efficiency": "7",
                "unhappy_users": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "3",
                "delta_gfs_optimization_threshold": "3"
            }
        },
        {
            "fdn": "002",
            "ossId": 22,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "2",
                "delta_gfs_optimization_threshold": "2"
            }
        }

    ]
},

    {
        description: "Optimization Cells with inconsistent target_throughput_r and delta_gfs_optimization_threshold values",
        result: false,
        data:  [
        {
            "fdn": "001",
            "ossId": 11,
            "kpis": {
                "goal_function_resource_efficiency": "7",
                "unhappy_users": "2"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "2",
                "delta_gfs_optimization_threshold": "2"
            }
        },
        {
            "fdn": "002",
            "ossId": 22,
            "kpis": {
                "goal_function_resource_efficiency": "3",
                "unhappy_users": "1"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "2",
                "delta_gfs_optimization_threshold": "3"
            }
        }

    ]
},

    {
        description: "10 Cell Optimization with GF difference greater than GF Threshold",
        result: true,
        data: [
            {
                "fdn": "001",
                "ossId": 11,
                "kpis": {
                    "goal_function_resource_efficiency": "1",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "002",
                "ossId": 22,
                "kpis": {
                    "goal_function_resource_efficiency": "2",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "003",
                "ossId": 33,
                "kpis": {
                    "goal_function_resource_efficiency": "3",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "004",
                "ossId": 44,
                "kpis": {
                    "goal_function_resource_efficiency": "4",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "005",
                "ossId": 55,
                "kpis": {
                    "goal_function_resource_efficiency": "5",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "006",
                "ossId": 66,
                "kpis": {
                    "goal_function_resource_efficiency": "6",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "007",
                "ossId": 77,
                "kpis": {
                    "goal_function_resource_efficiency": "7",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "008",
                "ossId": 88,
                "kpis": {
                    "goal_function_resource_efficiency": "8",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            {
                "fdn": "009",
                "ossId": 99,
                "kpis": {
                    "goal_function_resource_efficiency": "9",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "010",
                "ossId": 10,
                "kpis": {
                    "goal_function_resource_efficiency": "10",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }


        ]
},
 {
        description: "10 Cell Optimization with GF difference less than GF Threshold",
        result: false,
        data: [
            {
                "fdn": "001",
                "ossId": 11,
                "kpis": {
                    "goal_function_resource_efficiency": "25",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "002",
                "ossId": 22,
                "kpis": {
                    "goal_function_resource_efficiency": "25",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "003",
                "ossId": 33,
                "kpis": {
                    "goal_function_resource_efficiency": "25",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "004",
                "ossId": 44,
                "kpis": {
                    "goal_function_resource_efficiency": "25",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "005",
                "ossId": 55,
                "kpis": {
                    "goal_function_resource_efficiency": "23",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "006",
                "ossId": 66,
                "kpis": {
                    "goal_function_resource_efficiency": "23",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "007",
                "ossId": 77,
                "kpis": {
                    "goal_function_resource_efficiency": "23",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "008",
                "ossId": 88,
                "kpis": {
                    "goal_function_resource_efficiency": "23",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            },
            {
                "fdn": "009",
                "ossId": 99,
                "kpis": {
                    "goal_function_resource_efficiency": "23",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }, {
                "fdn": "010",
                "ossId": 10,
                "kpis": {
                    "goal_function_resource_efficiency": "23",
                    "unhappy_users": "2"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "3"
                }
            }


        ]
}

];

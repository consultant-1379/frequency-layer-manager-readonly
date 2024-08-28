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
var OptimizationCellsArrayForSourceCellsTest = [{
        description: "Optimization Cells with One source cell",
        size: 1,
        result: [{
            "sourceCell": {
                "fdn": "043331_1",
                "ossId": 433311,
                "kpis": {
                    "goal_function_resource_efficiency": "0.623887082020531",
                    "unhappy_users": "21.8882046892469"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "0.3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            "targetCells": [{
                "fdn": "043331_1_2",
                "ossId": 4333112,
                "stepSize": "",
                "numUsersToMove": "",
                "kpis": {
                    "goal_function_resource_efficiency": "0.981093974070063",
                    "unhappy_users": "0.136359712019667"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "0.3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            }]
        }],
        data: [{
                "fdn": "043331_1",
                "ossId": 433311,
                "kpis": {
                    "goal_function_resource_efficiency": "0.623887082020531",
                    "unhappy_users": "21.8882046892469"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "0.3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "043331_1_2",
                "ossId": 4333112,
                "kpis": {
                    "goal_function_resource_efficiency": "0.981093974070063",
                    "unhappy_users": "0.136359712019667"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "0.3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            }
        ]
    },
    {
        description: "Sector has no source cells as threshold is not met",
        size: 0,
        result: [],
        data: [{
                "fdn": "043331_1",
                "ossId": 433311,
                "kpis": {
                    "goal_function_resource_efficiency": "0.887520953402003",
                    "unhappy_users": "1.55517904011536"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "0.3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "002",
                "ossId": 2,
                "kpis": {
                    "goal_function_resource_efficiency": "0.787520953402003",
                    "unhappy_users": "0.276245714505055"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "0.3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            }
        ]
    },
    {
        description: "Sector with only one cell",
        size: 0,
        result: [],
        data: [{
            "fdn": "043331_1",
            "ossId": 433311,
            "kpis": {
                "goal_function_resource_efficiency": "0.887520953402003",
                "unhappy_users": "1.55517904011536"
            },
            "cmAttributes": {},
            "settings": {
                "target_throughput_r": "0.3",
                "delta_gfs_optimization_threshold": "0.3"
            }
        }]
    },
    {
        description: "Sector where the GFS is below the delta",
        size: 0,
        result: [],
        data: [{
                "fdn": "043331_1",
                "ossId": 433311,
                "kpis": {
                    "goal_function_resource_efficiency": "0.887520953402003",
                    "unhappy_users": "1.55517904011536"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "043331_1_2",
                "ossId": 4333112,
                "kpis": {
                    "goal_function_resource_efficiency": "0.989776040174584",
                    "unhappy_users": "0.276245714505055"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            }
        ]
    },
    {
        description: "Ensure correct ranking when comparing unhappy_users",
        size: 2,
        result: [{
                "sourceCell": {
                    "fdn": "043521_3",
                    "ossId": 435213,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.269458069941943",
                        "unhappy_users": "203.736780105308"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    }
                },
                "targetCells": [{
                        "fdn": "043521_2",
                        "ossId": 435212,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.574458634092171",
                            "unhappy_users": "11.5965932506632"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    },
                    {
                        "fdn": "643040_3",
                        "ossId": 6430403,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.717824218051773",
                            "unhappy_users": "17.1668691342753"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    }
                ]
            },
            {
                "sourceCell": {
                    "fdn": "343040_2",
                    "ossId": 3430402,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.116854414955919",
                        "unhappy_users": "30.2647287357941"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    }
                },
                "targetCells": [{
                        "fdn": "043521_2",
                        "ossId": 435212,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.574458634092171",
                            "unhappy_users": "11.5965932506632"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    },
                    {
                        "fdn": "643040_3",
                        "ossId": 6430403,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.717824218051773",
                            "unhappy_users": "17.1668691342753"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    }
                ]
            }
        ],
        data: [{
                "fdn": "043521_2",
                "ossId": 435212,
                "kpis": {
                    "goal_function_resource_efficiency": "0.574458634092171",
                    "unhappy_users": "11.5965932506632"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "043521_3",
                "ossId": 435213,
                "kpis": {
                    "goal_function_resource_efficiency": "0.269458069941943",
                    "unhappy_users": "203.736780105308"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "343040_2",
                "ossId": 3430402,
                "kpis": {
                    "goal_function_resource_efficiency": "0.116854414955919",
                    "unhappy_users": "30.2647287357941"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "643040_3",
                "ossId": 6430403,
                "kpis": {
                    "goal_function_resource_efficiency": "0.717824218051773",
                    "unhappy_users": "17.1668691342753"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            }
        ]
    },
    {
        description: "Sector with 3 cells (1 source, 2 targets)",
        size: 1,
        result: [{
            "sourceCell": {
                "fdn": "051017_3",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "0.447884075944469",
                    "unhappy_users": "12.6403873501825"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            "targetCells": [{
                    "fdn": "051017_3_2",
                    "ossId": 1,
                    "stepSize": "",
                    "numUsersToMove": "",
                    "kpis": {
                        "goal_function_resource_efficiency": "0.980834728689175",
                        "unhappy_users": "1.53093251968166"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    },
                },
                {
                    "fdn": "051017_3_4",
                    "ossId": 1,
                    "stepSize": "",
                    "numUsersToMove": "",
                    "kpis": {
                        "goal_function_resource_efficiency": "0.978888304995677",
                        "unhappy_users": "0.747764508333663"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    }
                }
            ]
        }],
        data: [{
                "fdn": "051017_3",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "0.447884075944469",
                    "unhappy_users": "12.6403873501825"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "051017_3_2",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "0.980834728689175",
                    "unhappy_users": "1.53093251968166"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "051017_3_4",
                "ossId": 1,
                "kpis": {
                    "goal_function_resource_efficiency": "0.978888304995677",
                    "unhappy_users": "0.747764508333663"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            }
        ]
    },
    {
        description: "Ensure correct ranking when comparing goal_function_resource_efficiency",
        size: 2,
        result: [{
                "sourceCell": {
                    "fdn": "343040_2",
                    "ossId": 3430402,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.116854414955919",
                        "unhappy_users": "203.736780105308"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    }
                },
                "targetCells": [{
                        "fdn": "043521_2",
                        "ossId": 435212,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.574458634092171",
                            "unhappy_users": "11.5965932506632"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    },
                    {
                        "fdn": "643040_3",
                        "ossId": 6430403,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.717824218051773",
                            "unhappy_users": "17.1668691342753"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    }
                ]
            },
            {
                "sourceCell": {
                    "fdn": "043521_3",
                    "ossId": 435213,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.269458069941943",
                        "unhappy_users": "203.736780105308"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    }
                },
                "targetCells": [{
                        "fdn": "043521_2",
                        "ossId": 435212,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.574458634092171",
                            "unhappy_users": "11.5965932506632"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    },
                    {
                        "fdn": "643040_3",
                        "ossId": 6430403,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.717824218051773",
                            "unhappy_users": "17.1668691342753"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    }
                ]
            }
        ],
        data: [{
                "fdn": "043521_2",
                "ossId": 435212,
                "kpis": {
                    "goal_function_resource_efficiency": "0.574458634092171",
                    "unhappy_users": "11.5965932506632"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "043521_3",
                "ossId": 435213,
                "kpis": {
                    "goal_function_resource_efficiency": "0.269458069941943",
                    "unhappy_users": "203.736780105308"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "343040_2",
                "ossId": 3430402,
                "kpis": {
                    "goal_function_resource_efficiency": "0.116854414955919",
                    "unhappy_users": "203.736780105308"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "643040_3",
                "ossId": 6430403,
                "kpis": {
                    "goal_function_resource_efficiency": "0.717824218051773",
                    "unhappy_users": "17.1668691342753"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            }
        ]
    },
    {
        description: "Ensure correct ranking when both unhappy_users and goal_function_resource_efficiency are the same",
        size: 3,
        result: [{
                "sourceCell": {
                    "fdn": "343040_2",
                    "ossId": 3430402,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.116854414955919",
                        "unhappy_users": "203.736780105308"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    }
                },
                "targetCells": [{
                        "fdn": "043521_2",
                        "ossId": 435212,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.574458634092171",
                            "unhappy_users": "11.5965932506632"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    },
                    {
                        "fdn": "643040_3",
                        "ossId": 6430403,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.717824218051773",
                            "unhappy_users": "17.1668691342753"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    }
            ]
            },
            {
                "sourceCell": {
                    "fdn": "043521_3",
                    "ossId": 435213,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.269458069941943",
                        "unhappy_users": "30.2647287357941"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    }
                },
                "targetCells": [{
                        "fdn": "043521_2",
                        "ossId": 435212,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.574458634092171",
                            "unhappy_users": "11.5965932506632"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    },
                    {
                        "fdn": "643040_3",
                        "ossId": 6430403,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.717824218051773",
                            "unhappy_users": "17.1668691342753"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    }
                ]
            },
            {
                "sourceCell": {
                    "fdn": "043521_4",
                    "ossId": 435214,
                    "kpis": {
                        "goal_function_resource_efficiency": "0.269458069941943",
                        "unhappy_users": "30.2647287357941"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3"
                    }
                },
                "targetCells": [{
                        "fdn": "043521_2",
                        "ossId": 435212,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.574458634092171",
                            "unhappy_users": "11.5965932506632"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    },
                    {
                        "fdn": "643040_3",
                        "ossId": 6430403,
                        "stepSize": "",
                        "numUsersToMove": "",
                        "kpis": {
                            "goal_function_resource_efficiency": "0.717824218051773",
                            "unhappy_users": "17.1668691342753"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3"
                        }
                    }
                ]
            }
        ],
        data: [{
                "fdn": "043521_2",
                "ossId": 435212,
                "kpis": {
                    "goal_function_resource_efficiency": "0.574458634092171",
                    "unhappy_users": "11.5965932506632"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "043521_3",
                "ossId": 435213,
                "kpis": {
                    "goal_function_resource_efficiency": "0.269458069941943",
                    "unhappy_users": "30.2647287357941"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "043521_4",
                "ossId": 435214,
                "kpis": {
                    "goal_function_resource_efficiency": "0.269458069941943",
                    "unhappy_users": "30.2647287357941"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "343040_2",
                "ossId": 3430402,
                "kpis": {
                    "goal_function_resource_efficiency": "0.116854414955919",
                    "unhappy_users": "203.736780105308"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            },
            {
                "fdn": "643040_3",
                "ossId": 6430403,
                "kpis": {
                    "goal_function_resource_efficiency": "0.717824218051773",
                    "unhappy_users": "17.1668691342753"
                },
                "cmAttributes": {},
                "settings": {
                    "target_throughput_r": "3",
                    "delta_gfs_optimization_threshold": "0.3"
                }
            }
        ]
    }
];
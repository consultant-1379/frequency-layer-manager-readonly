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
var possibleSourceCellsAndTargetCellsTest = [{
        description: "Possible Source Cell And Target cells where one target cell that doesn't satisfies contiguity rule is screened out",
        size: 1,
        result: [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2",
                        "contiguity": "5"
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
                            "contiguity": "3"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "target_source_contiguity_ratio_threshold": "0.5"
                        }
                    }
                ]
            }
        ],
        data: [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2",
                        "contiguity": "5"
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
                            "contiguity": "3"
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
                            "contiguity": "2"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "target_source_contiguity_ratio_threshold": "0.5"
                        }
                    }
                ]
            }
        ]
    }, {
        description: "Possible Source Cell And Target cells where all target cell satisfies contiguity rule no target cell is screened out",
        size: 1,
        result:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2",
                        "contiguity": "5"
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
                            "contiguity": "3"
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
                            "contiguity": "4"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "target_source_contiguity_ratio_threshold": "0.5"
                        }
                    }
                ]
            }
        ],
        data:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2",
                        "contiguity": "5"
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
                            "contiguity": "3"
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
                            "contiguity": "4"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "target_source_contiguity_ratio_threshold": "0.5"
                        }
                    }
                ]
            }
        ]
    }, {
        description: "Possible Source Cell And Target cells where all target cells doesn't satisfy contiguity rule  and there is single source cell in Sector then whole sector is excluded",
        size: 0,
        result: [],
        data:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2",
                        "contiguity": "5"
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
                            "contiguity": "1"
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
                            "contiguity": "2"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "target_source_contiguity_ratio_threshold": "0.5"
                        }
                    }
                ]
            }
        ]
    }, {
        description: "Possible Source Cell And Target cells where source cell contiguity value is 0",
        size: 0,
        result: [],
        data:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2",
                        "contiguity": "0"
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
                            "contiguity": "1"
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
                            "contiguity": "2"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "target_source_contiguity_ratio_threshold": "0.5"
                        }
                    }
                ]
            }
        ]
    }
];
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
        description: "Possible Source Cell And Target cells where one target cell that doesn't satisfy ESS rule is screened out",
        size: 1,
        result: [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2"
                    },
                    "cmAttributes": {
                        "lteNrSpectrumShared": "no"
                    },
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "ess_enabled": "t"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "no"
                        },
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
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
                        "unhappy_users": "2"
                    },
                    "cmAttributes": {
                        "lteNrSpectrumShared": "no"
                    },
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "ess_enabled": "t"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "no"
                         },
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "yes"
                         },
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
                        }
                    }
                ]
            }
        ]
    }, {
        description: "Possible Source Cell And Target cells where all target cells satisfy ESS rule so no target cell is screened out",
        size: 1,
        result:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2"
                    },
                    "cmAttributes": {
                        "lteNrSpectrumShared": "no"
                    },
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "ess_enabled": "t"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "no"
                        },
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "undefined"
                        },
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
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
                        "unhappy_users": "2"
                    },
                    "cmAttributes": {
                        "lteNrSpectrumShared": "no"
                    },
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "ess_enabled": "t"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "no"
                        },
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "undefined"
                        },
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
                        }
                    }
                ]
            }
        ]
    }, {
        description: "Possible Source Cell And Target cells where all target cells do not satisfy ESS rule and there is single source cell in Sector then whole sector is excluded",
        size: 0,
        result: [],
        data:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "goal_function_resource_efficiency": "2",
                        "unhappy_users": "2"
                    },
                    "cmAttributes": {
                        "lteNrSpectrumShared": "no"
                    },
                    "settings": {
                        "target_throughput_r": "3",
                        "delta_gfs_optimization_threshold": "0.3",
                        "ess_enabled": "t"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "yes"},
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "goal_function_resource_efficiency": "3",
                            "unhappy_users": "1"
                        },
                        "cmAttributes": {
                            "lteNrSpectrumShared": "yes"
                        },
                        "settings": {
                            "target_throughput_r": "3",
                            "delta_gfs_optimization_threshold": "0.3",
                            "ess_enabled": "t"
                        }
                    }
                ]
            }
        ]
    }
];
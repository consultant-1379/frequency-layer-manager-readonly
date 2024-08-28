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
var possibleSourceCellsAndTargetCellsTest = [{
     description: "Possible Source Cell And Target cell where one target cell doesn't satisfy ul pusch sinr ratio rule is screened out",
     size: 1,
     result: [{
             "sourceCell": {
                 "fdn": "001",
                 "ossId": 1,
                 "kpis": {
                     "ul_pusch_sinr_hourly": "100"
                 },
                 "cmAttributes": {},
                 "settings": {
                     "uplink_pusch_sinr_ratio_threshold": "0.8",
                     "min_target_uplink_pusch_sinr": "5"
                 }
             },
             "targetCells": [{
                     "fdn": "003",
                     "ossId": 3,
                     "kpis": {
                         "ul_pusch_sinr_hourly": "96"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "uplink_pusch_sinr_ratio_threshold": "0.8",
                         "min_target_uplink_pusch_sinr": "5"
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
                     "ul_pusch_sinr_hourly": "100"
                 },
                 "cmAttributes": {},
                 "settings": {
                     "uplink_pusch_sinr_ratio_threshold": "0.8",
                     "min_target_uplink_pusch_sinr": "5"
                 }
             },
             "targetCells": [{
                     "fdn": "003",
                     "ossId": 3,
                     "kpis": {
                        "ul_pusch_sinr_hourly": "96"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "uplink_pusch_sinr_ratio_threshold": "0.8",
                         "min_target_uplink_pusch_sinr": "5"
                     }
                 }, {
                     "fdn": "002",
                     "ossId": 2,
                     "kpis": {
                         "ul_pusch_sinr_hourly": "79"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "uplink_pusch_sinr_ratio_threshold": "0.8",
                         "min_target_uplink_pusch_sinr": "5"
                     }
                 }
             ]
         }
     ]
    }, {
        description: "Possible Source Cell And Target cell where one target cell doesn't satisfy ul pusch sinr min target rule is screened out",
        size: 1,
        result: [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "ul_pusch_sinr_hourly": "10"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "uplink_pusch_sinr_ratio_threshold": "0.1",
                        "min_target_uplink_pusch_sinr": "5"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "ul_pusch_sinr_hourly": "9"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "uplink_pusch_sinr_ratio_threshold": "0.1",
                            "min_target_uplink_pusch_sinr": "5"
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
                        "ul_pusch_sinr_hourly": "10"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "uplink_pusch_sinr_ratio_threshold": "0.1",
                        "min_target_uplink_pusch_sinr": "5"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                           "ul_pusch_sinr_hourly": "9"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "uplink_pusch_sinr_ratio_threshold": "0.1",
                            "min_target_uplink_pusch_sinr": "5"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "ul_pusch_sinr_hourly": "4"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "uplink_pusch_sinr_ratio_threshold": "0.1",
                            "min_target_uplink_pusch_sinr": "5"
                        }
                    }
                ]
            }
        ]
       }, {
            description: "Possible Source Cell And Target cell where all target cells satisfy ul pusch sinr rule and no target cell is screened out",
            size: 1,
            result: [{
                    "sourceCell": {
                        "fdn": "001",
                        "ossId": 1,
                        "kpis": {
                            "ul_pusch_sinr_hourly": "100"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "uplink_pusch_sinr_ratio_threshold": "0.8",
                            "min_target_uplink_pusch_sinr": "5"
                        }
                    },
                    "targetCells": [{
                            "fdn": "003",
                            "ossId": 3,
                            "kpis": {
                               "ul_pusch_sinr_hourly": "96"
                            },
                            "cmAttributes": {},
                            "settings": {
                                "uplink_pusch_sinr_ratio_threshold": "0.8",
                                "min_target_uplink_pusch_sinr": "5"
                            }
                        }, {
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                                "ul_pusch_sinr_hourly": "81"
                            },
                            "cmAttributes": {},
                            "settings": {
                                "uplink_pusch_sinr_ratio_threshold": "0.8",
                                "min_target_uplink_pusch_sinr": "5"
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
                            "ul_pusch_sinr_hourly": "100"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "uplink_pusch_sinr_ratio_threshold": "0.8",
                            "min_target_uplink_pusch_sinr": "5"
                        }
                    },
                    "targetCells": [{
                            "fdn": "003",
                            "ossId": 3,
                            "kpis": {
                               "ul_pusch_sinr_hourly": "96"
                            },
                            "cmAttributes": {},
                            "settings": {
                                "uplink_pusch_sinr_ratio_threshold": "0.8",
                                "min_target_uplink_pusch_sinr": "5"
                            }
                        }, {
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                                "ul_pusch_sinr_hourly": "81"
                            },
                            "cmAttributes": {},
                            "settings": {
                                "uplink_pusch_sinr_ratio_threshold": "0.8",
                                "min_target_uplink_pusch_sinr": "5"
                            }
                        }
                    ]
                }
            ]
           }, {
            description: "When all target cells do not satisfy UL PUSCH SINR rule and there is single source cell in Sector then whole sector is excluded",
            size: 0,
            result: [],
            data:
            [{
                    "sourceCell": {
                        "fdn": "001",
                        "ossId": 1,
                        "kpis": {
                            "ul_pusch_sinr_hourly": "10"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "uplink_pusch_sinr_ratio_threshold": "0.9",
                            "min_target_uplink_pusch_sinr": "5"
                        }
                    },
                    "targetCells": [{
                            "fdn": "003",
                            "ossId": 3,
                            "kpis": {
                                "ul_pusch_sinr_hourly": "7"
                            },
                            "cmAttributes": {},
                            "settings": {
                                "uplink_pusch_sinr_ratio_threshold": "0.9",
                                "min_target_uplink_pusch_sinr": "5"
                            }
                        }, {
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                                "ul_pusch_sinr_hourly": "4"
                            },
                            "cmAttributes": {},
                            "settings": {
                                "uplink_pusch_sinr_ratio_threshold": "0.9",
                                "min_target_uplink_pusch_sinr": "5"
                            }
                        }
                    ]
                }
            ]
        }
];

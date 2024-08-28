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
var possibleSourceCellsAndTargetCellsTest = [
    {
        description: "When one target cell ENDC % Users is greater than the source cells threshold it is screened out",
        size: 1,
        result: [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "percentage_endc_users": "23.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "lb_threshold_for_endc_users": "100.0"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "percentage_endc_users": "0.0"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
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
                        "percentage_endc_users": "23.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "lb_threshold_for_endc_users": "100.0"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "percentage_endc_users": "0.0"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "percentage_endc_users": "102.0"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }
                ]
            }
        ]
    },
    {
        description: "When all target cells satisfy ENDC rule then no target cell is screened out",
        size: 1,
        result:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "percentage_endc_users": "23.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "lb_threshold_for_endc_users": "50.0"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "percentage_endc_users": "8.5"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "percentage_endc_users": "50.0"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
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
                        "percentage_endc_users": "23.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "lb_threshold_for_endc_users": "50.0"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "percentage_endc_users": "8.5"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "percentage_endc_users": "50.0"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }
                ]
            }
        ]
    },
    {
        description: "When all target cells do not satisfy ENDC rule and there is single source cell in Sector then whole sector is excluded",
        size: 0,
        result: [],
        data:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "percentage_endc_users": "63.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "lb_threshold_for_endc_users": "50.0"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "percentage_endc_users": "50.1"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "percentage_endc_users": "84.3"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }
                ]
            }
        ]
    },
    {
        description: "When target cells ENDC kpi is missing/empty/null, screening is skipped for those target cell",
        size: 1,
        result:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "contiguity": "5",
                        "percentage_endc_users": "23.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "lb_threshold_for_endc_users": "50.0"
                    }
                },
                "targetCells": [{
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "contiguity": "5"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }, {
                       "fdn": "003",
                       "ossId": 3,
                       "kpis": {
                           "contiguity": "5",
                           "percentage_endc_users": ""
                       },
                       "cmAttributes": {},
                       "settings": {
                           "lb_threshold_for_endc_users": "50.0"
                       }
                    }, {
                       "fdn": "004",
                       "ossId": 4,
                       "kpis": {
                           "contiguity": "5",
                           "percentage_endc_users": "null"
                       },
                       "cmAttributes": {},
                       "settings": {
                           "lb_threshold_for_endc_users": "50.0"
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
                        "contiguity": "5",
                        "percentage_endc_users": "23.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "lb_threshold_for_endc_users": "50.0"
                    }
                },
                "targetCells": [{
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "contiguity": "5"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }, {
                       "fdn": "003",
                       "ossId": 3,
                       "kpis": {
                           "contiguity": "5",
                           "percentage_endc_users": ""
                       },
                       "cmAttributes": {},
                       "settings": {
                           "lb_threshold_for_endc_users": "50.0"
                       }
                    }, {
                       "fdn": "004",
                       "ossId": 4,
                       "kpis": {
                           "contiguity": "5",
                           "percentage_endc_users": "null"
                       },
                       "cmAttributes": {},
                       "settings": {
                           "lb_threshold_for_endc_users": "50.0"
                       }
                    }
                ]
            }
        ]
    },
    {
        description: "When source cells threshold setting is null, the target cells are screened out",
        size: 1,
        result:
        [],
        data:
        [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "contiguity": "5",
                        "percentage_endc_users": "23.5"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "contiguity": "50.0"
                    }
                },
                "targetCells": [{
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "percentage_endc_users": "15.0"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "lb_threshold_for_endc_users": "50.0"
                        }
                    }, {
                       "fdn": "003",
                       "ossId": 3,
                       "kpis": {
                           "contiguity": "5",
                           "percentage_endc_users": "0"
                       },
                       "cmAttributes": {},
                       "settings": {
                           "lb_threshold_for_endc_users": "50.0"
                       }
                    }
                ]
            }
        ]
    }
];
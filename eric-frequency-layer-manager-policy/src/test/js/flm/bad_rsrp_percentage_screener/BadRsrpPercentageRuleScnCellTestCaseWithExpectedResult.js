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
var possibleSourceCellsAndTargetCellsTest = [
    {
        description: "Possible Source Cell And Target cell where one target cell doesn't satisfy Bad Rsrp ratio rule is screened out",
        size: 1,
        result: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "distance_q1": "208.72000000000008",
                    "distance_q2": "417.44000000000017",
                    "distance_q3": "626.1600000000003",
                    "distance_q4": "834.8800000000003",
                    "num_samples_rsrp_ta_q1":"100",
                    "num_samples_rsrp_ta_q2":"100",
                    "num_samples_rsrp_ta_q3":"100",
                    "num_samples_rsrp_ta_q4":"100",
                    "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                    "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                    "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                    "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                    "percentage_bad_rsrp_ratio_threshold": "1.2"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "830.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                   "distance_q1": "208.72000000000008",
                   "distance_q2": "417.44000000000017",
                   "distance_q3": "626.1600000000003",
                   "distance_q4": "834.8800000000003",
                   "num_samples_rsrp_ta_q1":"100",
                   "num_samples_rsrp_ta_q2":"100",
                   "num_samples_rsrp_ta_q3":"100",
                   "num_samples_rsrp_ta_q4":"100",
                   "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                   "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                   "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                   "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                   "percentage_bad_rsrp_ratio_threshold": "1.2"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "830.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
                    }
                }, {
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                       "distance_q1": "49.360000000000014",
                       "distance_q2": "98.72000000000003",
                       "distance_q3": "148.08000000000004",
                       "distance_q4": "197.44000000000005",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"90.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"80.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"69.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"80.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
                    }
                }
            ]
        }
    ]
   },
   {
    description: "Possible Source Cell and Target Cells where all target cells satisfy Bad Rsrp ratio rule",
    size: 1,
    result: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "distance_q1": "208.72000000000008",
                    "distance_q2": "417.44000000000017",
                    "distance_q3": "626.1600000000003",
                    "distance_q4": "834.8800000000003",
                    "num_samples_rsrp_ta_q1":"100",
                    "num_samples_rsrp_ta_q2":"100",
                    "num_samples_rsrp_ta_q3":"100",
                    "num_samples_rsrp_ta_q4":"100",
                    "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                    "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                    "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                    "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                    "percentage_bad_rsrp_ratio_threshold": "1.9"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "830.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.9"
                    }
                },
                {
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                       "distance_q1": "596.48",
                       "distance_q2": "1192.96",
                       "distance_q3": "1789.44",
                       "distance_q4": "2385.92",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"26.14213197969543",
                       "num_bad_samples_rsrp_ta_q2":"93.14868804664724",
                       "num_bad_samples_rsrp_ta_q3":"100",
                       "num_bad_samples_rsrp_ta_q4":"100"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.9"
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
                   "distance_q1": "208.72000000000008",
                   "distance_q2": "417.44000000000017",
                   "distance_q3": "626.1600000000003",
                   "distance_q4": "834.8800000000003",
                   "num_samples_rsrp_ta_q1":"100",
                   "num_samples_rsrp_ta_q2":"100",
                   "num_samples_rsrp_ta_q3":"100",
                   "num_samples_rsrp_ta_q4":"100",
                   "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                   "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                   "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                   "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                   "percentage_bad_rsrp_ratio_threshold": "1.9"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "830.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.9"
                    }
                }, {
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                       "distance_q1": "596.48",
                       "distance_q2": "1192.96",
                       "distance_q3": "1789.44",
                       "distance_q4": "2385.92",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"26.14213197969543",
                       "num_bad_samples_rsrp_ta_q2":"93.14868804664724",
                       "num_bad_samples_rsrp_ta_q3":"100",
                       "num_bad_samples_rsrp_ta_q4":"100"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.9"
                    }
                }
            ]
        }
    ]
   },
   {
    description: "Possible Source Cell and Target Cells where all target cells doesn't satisfy Bad Rsrp ratio rule and the sector is screened out",
    size: 1,
    result: [],
    data: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                   "distance_q1": "208.72000000000008",
                   "distance_q2": "417.44000000000017",
                   "distance_q3": "626.1600000000003",
                   "distance_q4": "834.8800000000003",
                   "num_samples_rsrp_ta_q1":"100",
                   "num_samples_rsrp_ta_q2":"100",
                   "num_samples_rsrp_ta_q3":"100",
                   "num_samples_rsrp_ta_q4":"100",
                   "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                   "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                   "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                   "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                   "percentage_bad_rsrp_ratio_threshold": "1.2"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "101.77999999999997",
                       "distance_q2": "203.55999999999995",
                       "distance_q3": "305.3399999999999",
                       "distance_q4": "407.1199999999999",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"18.995633187772924",
                       "num_bad_samples_rsrp_ta_q2":"52.06751054852321",
                       "num_bad_samples_rsrp_ta_q3":"82.8125",
                       "num_bad_samples_rsrp_ta_q4":"79.89949748743719"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
                    }
                }, {
                    "fdn": "002",
                    "ossId": 2,
                    "kpis": {
                       "distance_q1": "49.360000000000014",
                       "distance_q2": "98.72000000000003",
                       "distance_q3": "148.08000000000004",
                       "distance_q4": "197.44000000000005",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                       "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                       "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                       "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
                    }
                }
            ]
        }
    ]
   },
   {
    description: "Possible Source Cell And Target cell where Source Cell distance_q4 is less than Target Cell distance_q4",
    size: 1,
    result: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "distance_q1": "208.72000000000008",
                    "distance_q2": "417.44000000000017",
                    "distance_q3": "626.1600000000003",
                    "distance_q4": "834.8800000000003",
                    "num_samples_rsrp_ta_q1":"100",
                    "num_samples_rsrp_ta_q2":"100",
                    "num_samples_rsrp_ta_q3":"100",
                    "num_samples_rsrp_ta_q4":"100",
                    "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                    "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                    "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                    "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                    "percentage_bad_rsrp_ratio_threshold": "0.8"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "250.88",
                       "distance_q2": "501.76",
                       "distance_q3": "752.64",
                       "distance_q4": "1003.52",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"11.920529801324504",
                       "num_bad_samples_rsrp_ta_q2":"0",
                       "num_bad_samples_rsrp_ta_q3":"0",
                       "num_bad_samples_rsrp_ta_q4":"20.50"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                   "distance_q1": "208.72000000000008",
                   "distance_q2": "417.44000000000017",
                   "distance_q3": "626.1600000000003",
                   "distance_q4": "834.8800000000003",
                   "num_samples_rsrp_ta_q1":"100",
                   "num_samples_rsrp_ta_q2":"100",
                   "num_samples_rsrp_ta_q3":"100",
                   "num_samples_rsrp_ta_q4":"100",
                   "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                   "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                   "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                   "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                   "percentage_bad_rsrp_ratio_threshold": "0.8"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "250.88",
                       "distance_q2": "501.76",
                       "distance_q3": "752.64",
                       "distance_q4": "1003.52",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"11.920529801324504",
                       "num_bad_samples_rsrp_ta_q2":"0",
                       "num_bad_samples_rsrp_ta_q3":"0",
                       "num_bad_samples_rsrp_ta_q4":"20.50"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
                    }
                }
            ]
        }
    ]
   },
   {
    description: "Possible Source Cell And Target cell where Target Cell distance_q4 is less than Source Cell distance_q4",
    size: 1,
    result: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "distance_q1": "208.72000000000008",
                    "distance_q2": "417.44000000000017",
                    "distance_q3": "626.1600000000003",
                    "distance_q4": "834.8800000000003",
                    "num_samples_rsrp_ta_q1":"100",
                    "num_samples_rsrp_ta_q2":"100",
                    "num_samples_rsrp_ta_q3":"100",
                    "num_samples_rsrp_ta_q4":"100",
                    "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                    "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                    "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                    "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                    "percentage_bad_rsrp_ratio_threshold": "6"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "49.360000000000014",
                       "distance_q2": "98.72000000000003",
                       "distance_q3": "148.08000000000004",
                       "distance_q4": "197.44000000000005",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                       "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                       "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                       "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                   "distance_q1": "208.72000000000008",
                   "distance_q2": "417.44000000000017",
                   "distance_q3": "626.1600000000003",
                   "distance_q4": "834.8800000000003",
                   "num_samples_rsrp_ta_q1":"100",
                   "num_samples_rsrp_ta_q2":"100",
                   "num_samples_rsrp_ta_q3":"100",
                   "num_samples_rsrp_ta_q4":"100",
                   "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                   "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                   "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                   "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                   "percentage_bad_rsrp_ratio_threshold": "6"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "49.360000000000014",
                       "distance_q2": "98.72000000000003",
                       "distance_q3": "148.08000000000004",
                       "distance_q4": "197.44000000000005",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                       "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                       "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                       "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
                    }
                }
            ]
        }
    ]
   },
   {
    description: "Possible Source Cell And Target cell where Source Cell distance_q4 is equal to Target Cell distance_q4",
    size: 1,
    result: [{
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "distance_q1": "208.72000000000008",
                    "distance_q2": "417.44000000000017",
                    "distance_q3": "626.1600000000003",
                    "distance_q4": "834.8800000000003",
                    "num_samples_rsrp_ta_q1":"100",
                    "num_samples_rsrp_ta_q2":"100",
                    "num_samples_rsrp_ta_q3":"100",
                    "num_samples_rsrp_ta_q4":"100",
                    "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                    "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                    "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                    "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                    "percentage_bad_rsrp_ratio_threshold": "1.1"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "834.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                   "distance_q1": "208.72000000000008",
                   "distance_q2": "417.44000000000017",
                   "distance_q3": "626.1600000000003",
                   "distance_q4": "834.8800000000003",
                   "num_samples_rsrp_ta_q1":"100",
                   "num_samples_rsrp_ta_q2":"100",
                   "num_samples_rsrp_ta_q3":"100",
                   "num_samples_rsrp_ta_q4":"100",
                   "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                   "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                   "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                   "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                },
                "cmAttributes": {},
                "settings": {
                   "percentage_bad_rsrp_ratio_threshold": "1.1"
                }
            },
            "targetCells": [{
                    "fdn": "003",
                    "ossId": 3,
                    "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "834.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "1.2"
                    }
                }
            ]
        }
    ]
   },
   {
       description: "Possible Source Cell And Target cells where source cell Percentage Bad RSRP Ratio Threshold is null",
       size: 1,
       result: [],
       data: [{
               "sourceCell": {
                   "fdn": "001",
                   "ossId": 1,
                   "kpis": {
                      "distance_q1": "208.72000000000008",
                      "distance_q2": "417.44000000000017",
                      "distance_q3": "626.1600000000003",
                      "distance_q4": "834.8800000000003",
                      "num_samples_rsrp_ta_q1":"100",
                      "num_samples_rsrp_ta_q2":"100",
                      "num_samples_rsrp_ta_q3":"100",
                      "num_samples_rsrp_ta_q4":"100",
                      "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                      "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                      "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                      "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                   },
                   "cmAttributes": {},
                   "settings": {}
               },
               "targetCells": [{
                       "fdn": "003",
                       "ossId": 3,
                       "kpis": {
                          "distance_q1": "208.72000000000008",
                          "distance_q2": "417.44000000000017",
                          "distance_q3": "626.1600000000003",
                          "distance_q4": "830.8800000000003",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                          "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                          "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                          "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   }, {
                       "fdn": "002",
                       "ossId": 2,
                       "kpis": {
                          "distance_q1": "49.360000000000014",
                          "distance_q2": "98.72000000000003",
                          "distance_q3": "148.08000000000004",
                          "distance_q4": "197.44000000000005",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                          "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                          "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                          "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   }
               ]
           }
       ]
      },
      {
       description: "Possible Source Cell And Target cell where one target cell with distance_q4 KPI null is screened out",
       size: 1,
       result: [{
               "sourceCell": {
                   "fdn": "001",
                   "ossId": 1,
                   "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "834.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                   },
                   "cmAttributes": {},
                   "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "6"
                   }
               },
               "targetCells": [{
                       "fdn": "002",
                       "ossId": 2,
                       "kpis": {
                          "distance_q1": "49.360000000000014",
                          "distance_q2": "98.72000000000003",
                          "distance_q3": "148.08000000000004",
                          "distance_q4": "197.44000000000005",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                          "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                          "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                          "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                      "distance_q1": "208.72000000000008",
                      "distance_q2": "417.44000000000017",
                      "distance_q3": "626.1600000000003",
                      "distance_q4": "834.8800000000003",
                      "num_samples_rsrp_ta_q1":"100",
                      "num_samples_rsrp_ta_q2":"100",
                      "num_samples_rsrp_ta_q3":"100",
                      "num_samples_rsrp_ta_q4":"100",
                      "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                      "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                      "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                      "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                   },
                   "cmAttributes": {},
                   "settings": {
                      "percentage_bad_rsrp_ratio_threshold": "6"
                   }
               },
               "targetCells": [{
                       "fdn": "003",
                       "ossId": 3,
                       "kpis": {
                          "distance_q1": "208.72000000000008",
                          "distance_q2": "417.44000000000017",
                          "distance_q3": "626.1600000000003",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                          "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                          "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                          "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   }, {
                       "fdn": "002",
                       "ossId": 2,
                       "kpis": {
                          "distance_q1": "49.360000000000014",
                          "distance_q2": "98.72000000000003",
                          "distance_q3": "148.08000000000004",
                          "distance_q4": "197.44000000000005",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                          "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                          "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                          "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   }
               ]
           }
       ]
      },
      {
       description: "Possible Source Cell And Target cell where one source cell with distance_q4 KPI null is screened out",
       size: 1,
       result: [{
               "sourceCell": {
                   "fdn": "004",
                   "ossId": 1,
                   "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "834.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                   },
                   "cmAttributes": {},
                   "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "6"
                   }
               },
               "targetCells": [{
                       "fdn": "005",
                       "ossId": 2,
                       "kpis": {
                          "distance_q1": "49.360000000000014",
                          "distance_q2": "98.72000000000003",
                          "distance_q3": "148.08000000000004",
                          "distance_q4": "197.44000000000005",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                          "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                          "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                          "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                      "distance_q1": "208.72000000000008",
                      "distance_q2": "417.44000000000017",
                      "distance_q3": "626.1600000000003",
                      "num_samples_rsrp_ta_q1":"100",
                      "num_samples_rsrp_ta_q2":"100",
                      "num_samples_rsrp_ta_q3":"100",
                      "num_samples_rsrp_ta_q4":"100",
                      "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                      "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                      "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                      "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                   },
                   "cmAttributes": {},
                   "settings": {
                      "percentage_bad_rsrp_ratio_threshold": "6"
                   }
               },
               "targetCells": [{
                       "fdn": "003",
                       "ossId": 3,
                       "kpis": {
                          "distance_q1": "208.72000000000008",
                          "distance_q2": "417.44000000000017",
                          "distance_q3": "626.1600000000003",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                          "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                          "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                          "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   }, {
                       "fdn": "002",
                       "ossId": 2,
                       "kpis": {
                          "distance_q1": "49.360000000000014",
                          "distance_q2": "98.72000000000003",
                          "distance_q3": "148.08000000000004",
                          "distance_q4": "197.44000000000005",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                          "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                          "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                          "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   }
               ]
           },
           {
               "sourceCell": {
                   "fdn": "004",
                   "ossId": 1,
                   "kpis": {
                      "distance_q1": "208.72000000000008",
                      "distance_q2": "417.44000000000017",
                      "distance_q3": "626.1600000000003",
                      "distance_q4": "834.8800000000003",
                      "num_samples_rsrp_ta_q1":"100",
                      "num_samples_rsrp_ta_q2":"100",
                      "num_samples_rsrp_ta_q3":"100",
                      "num_samples_rsrp_ta_q4":"100",
                      "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                      "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                      "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                      "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                   },
                   "cmAttributes": {},
                   "settings": {
                      "percentage_bad_rsrp_ratio_threshold": "6"
                   }
               },
               "targetCells": [{
                       "fdn": "006",
                       "ossId": 3,
                       "kpis": {
                          "distance_q1": "208.72000000000008",
                          "distance_q2": "417.44000000000017",
                          "distance_q3": "626.1600000000003",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                          "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                          "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                          "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   }, {
                       "fdn": "005",
                       "ossId": 2,
                       "kpis": {
                          "distance_q1": "49.360000000000014",
                          "distance_q2": "98.72000000000003",
                          "distance_q3": "148.08000000000004",
                          "distance_q4": "197.44000000000005",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                          "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                          "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                          "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                       },
                       "cmAttributes": {},
                       "settings": {
                          "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   }
               ]
           }
       ]
      },
      {
        description: "Possible Source Cell And Target cell where one target cell with distance KPI null is screened out",
        size: 1,
        result: [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "distance_q1": "208.72000000000008",
                        "distance_q2": "417.44000000000017",
                        "distance_q3": "626.1600000000003",
                        "distance_q4": "834.8800000000003",
                        "num_samples_rsrp_ta_q1":"100",
                        "num_samples_rsrp_ta_q2":"100",
                        "num_samples_rsrp_ta_q3":"100",
                        "num_samples_rsrp_ta_q4":"100",
                        "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                        "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                        "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                        "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                        "percentage_bad_rsrp_ratio_threshold": "6"
                    }
                },
                "targetCells": [{
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                           "distance_q1": "49.360000000000014",
                           "distance_q2": "98.72000000000003",
                           "distance_q3": "148.08000000000004",
                           "distance_q4": "197.44000000000005",
                           "num_samples_rsrp_ta_q1":"100",
                           "num_samples_rsrp_ta_q2":"100",
                           "num_samples_rsrp_ta_q3":"100",
                           "num_samples_rsrp_ta_q4":"100",
                           "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                           "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                           "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                           "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                        },
                        "cmAttributes": {},
                        "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q3": "626.1600000000003",
                       "distance_q4": "834.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "6"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                           "distance_q1": "208.72000000000008",
                           "distance_q2": "417.44000000000017",
                           "distance_q4": "856.1600000000003",
                           "num_samples_rsrp_ta_q1":"100",
                           "num_samples_rsrp_ta_q2":"100",
                           "num_samples_rsrp_ta_q3":"100",
                           "num_samples_rsrp_ta_q4":"100",
                           "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                           "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                           "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                           "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                        },
                        "cmAttributes": {},
                        "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    }, {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                           "distance_q1": "49.360000000000014",
                           "distance_q2": "98.72000000000003",
                           "distance_q3": "148.08000000000004",
                           "distance_q4": "197.44000000000005",
                           "num_samples_rsrp_ta_q1":"100",
                           "num_samples_rsrp_ta_q2":"100",
                           "num_samples_rsrp_ta_q3":"100",
                           "num_samples_rsrp_ta_q4":"100",
                           "num_bad_samples_rsrp_ta_q1": "0.6756756756756757",
                           "num_bad_samples_rsrp_ta_q2": "6.696428571428571",
                           "num_bad_samples_rsrp_ta_q3": "9.489051094890511",
                           "num_bad_samples_rsrp_ta_q4": "30.76923076923077"
                        },
                        "cmAttributes": {},
                        "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    }
                ]
            }
        ]
       },
       {
        description: "Possible Source Cell And Target cell where one source cell with distance KPI null is screened out",
        size: 1,
        result: [],
        data: [{
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                       "distance_q1": "208.72000000000008",
                       "distance_q2": "417.44000000000017",
                       "distance_q4": "834.8800000000003",
                       "num_samples_rsrp_ta_q1":"100",
                       "num_samples_rsrp_ta_q2":"100",
                       "num_samples_rsrp_ta_q3":"100",
                       "num_samples_rsrp_ta_q4":"100",
                       "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                       "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                       "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                       "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                    },
                    "cmAttributes": {},
                    "settings": {
                       "percentage_bad_rsrp_ratio_threshold": "6"
                    }
                },
                "targetCells": [{
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                           "distance_q1": "208.72000000000008",
                           "distance_q2": "417.44000000000017",
                           "distance_q3": "625.1600000000003",
                           "distance_q4": "830.1600000000003",
                           "num_samples_rsrp_ta_q1":"100",
                           "num_samples_rsrp_ta_q2":"100",
                           "num_samples_rsrp_ta_q3":"100",
                           "num_samples_rsrp_ta_q4":"100",
                           "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                           "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                           "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                           "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                        },
                        "cmAttributes": {},
                        "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    }
                ]
            }
        ]
       },
       {
           description: "Possible Source Cell And Target cells where source cell Percentage Bad RSRP KPI is null",
           size: 1,
           result: [],
           data: [{
                   "sourceCell": {
                       "fdn": "001",
                       "ossId": 1,
                       "kpis": {
                          "distance_q1": "208.72000000000008",
                          "distance_q2": "417.44000000000017",
                          "distance_q3": "626.1600000000003",
                          "distance_q4": "834.8800000000003",
                          "num_samples_rsrp_ta_q1":"100",
                          "num_samples_rsrp_ta_q2":"100",
                          "num_samples_rsrp_ta_q3":"100",
                          "num_samples_rsrp_ta_q4":"100",
                          "num_bad_samples_rsrp_ta_q1":"",
                          "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                          "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                          "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                       },
                       "cmAttributes": {},
                       "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
                       }
                   },
                   "targetCells": [{
                           "fdn": "003",
                           "ossId": 3,
                           "kpis": {
                              "distance_q1": "208.72000000000008",
                              "distance_q2": "417.44000000000017",
                              "distance_q3": "626.1600000000003",
                              "distance_q4": "830.8800000000003",
                              "num_samples_rsrp_ta_q1":"100",
                              "num_samples_rsrp_ta_q2":"100",
                              "num_samples_rsrp_ta_q3":"100",
                              "num_samples_rsrp_ta_q4":"100",
                              "num_bad_samples_rsrp_ta_q1":"",
                              "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                              "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                              "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                           },
                           "cmAttributes": {},
                           "settings": {
                              "percentage_bad_rsrp_ratio_threshold": "1.2"
                           }
                       }, {
                           "fdn": "002",
                           "ossId": 2,
                           "kpis": {
                              "distance_q1": "49.360000000000014",
                              "distance_q2": "98.72000000000003",
                              "distance_q3": "148.08000000000004",
                              "distance_q4": "197.44000000000005",
                              "num_samples_rsrp_ta_q1":"100",
                              "num_samples_rsrp_ta_q2":"100",
                              "num_samples_rsrp_ta_q3":"100",
                              "num_samples_rsrp_ta_q4":"100",
                              "num_bad_samples_rsrp_ta_q1":"0.6756756756756757",
                              "num_bad_samples_rsrp_ta_q2":"6.696428571428571",
                              "num_bad_samples_rsrp_ta_q3":"9.489051094890511",
                              "num_bad_samples_rsrp_ta_q4":"30.76923076923077"
                           },
                           "cmAttributes": {},
                           "settings": {
                              "percentage_bad_rsrp_ratio_threshold": "1.2"
                           }
                       }
                   ]
               }
           ]
       },
       {
            description: "Possible Source Cell and Target Cells where 1 target cell is missing bad rsrp kpi value",
            size: 1,
            result: [{
                    "sourceCell": {
                        "fdn": "001",
                        "ossId": 1,
                        "kpis": {
                            "distance_q1": "208.72000000000008",
                            "distance_q2": "417.44000000000017",
                            "distance_q3": "626.1600000000003",
                            "distance_q4": "834.8800000000003",
                            "num_samples_rsrp_ta_q1":"100",
                            "num_samples_rsrp_ta_q2":"100",
                            "num_samples_rsrp_ta_q3":"100",
                            "num_samples_rsrp_ta_q4":"100",
                            "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                            "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                            "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                            "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    },
                    "targetCells": [
                        {
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                               "distance_q1": "596.48",
                               "distance_q2": "1192.96",
                               "distance_q3": "1789.44",
                               "distance_q4": "2385.92",
                               "num_samples_rsrp_ta_q1":"100",
                               "num_samples_rsrp_ta_q2":"100",
                               "num_samples_rsrp_ta_q3":"100",
                               "num_samples_rsrp_ta_q4":"100",
                               "num_bad_samples_rsrp_ta_q1":"5",
                               "num_bad_samples_rsrp_ta_q2":"6",
                               "num_bad_samples_rsrp_ta_q3":"2",
                               "num_bad_samples_rsrp_ta_q4":"1"
                            },
                            "cmAttributes": {},
                            "settings": {
                               "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                           "distance_q1": "208.72000000000008",
                           "distance_q2": "417.44000000000017",
                           "distance_q3": "626.1600000000003",
                           "distance_q4": "834.8800000000003",
                           "num_samples_rsrp_ta_q1":"100",
                           "num_samples_rsrp_ta_q2":"100",
                           "num_samples_rsrp_ta_q3":"100",
                           "num_samples_rsrp_ta_q4":"100",
                           "num_bad_samples_rsrp_ta_q1":"9.731543624161073",
                           "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                           "num_bad_samples_rsrp_ta_q3":"39.737991266375545",
                           "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                        },
                        "cmAttributes": {},
                        "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    },
                    "targetCells": [{
                            "fdn": "003",
                            "ossId": 3,
                            "kpis": {
                               "distance_q1": "208.72000000000008",
                               "distance_q2": "417.44000000000017",
                               "distance_q3": "626.1600000000003",
                               "distance_q4": "830.8800000000003",
                               "num_samples_rsrp_ta_q1":"100",
                               "num_samples_rsrp_ta_q2":"100",
                               "num_samples_rsrp_ta_q3":"100",
                               "num_samples_rsrp_ta_q4":"100",
                               "num_bad_samples_rsrp_ta_q1":"",
                               "num_bad_samples_rsrp_ta_q2":"37.95221843003413",
                               "num_bad_samples_rsrp_ta_q3":"39.73",
                               "num_bad_samples_rsrp_ta_q4":"10.96774193548387"
                            },
                            "cmAttributes": {},
                            "settings": {
                               "percentage_bad_rsrp_ratio_threshold": "1.2"
                            }
                        }, {
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                               "distance_q1": "596.48",
                               "distance_q2": "1192.96",
                               "distance_q3": "1789.44",
                               "distance_q4": "2385.92",
                               "num_samples_rsrp_ta_q1":"100",
                               "num_samples_rsrp_ta_q2":"100",
                               "num_samples_rsrp_ta_q3":"100",
                               "num_samples_rsrp_ta_q4":"100",
                               "num_bad_samples_rsrp_ta_q1":"5",
                               "num_bad_samples_rsrp_ta_q2":"6",
                               "num_bad_samples_rsrp_ta_q3":"2",
                               "num_bad_samples_rsrp_ta_q4":"1"
                            },
                            "cmAttributes": {},
                            "settings": {
                               "percentage_bad_rsrp_ratio_threshold": "1.2"
                            }
                        }
                    ]
                }
           ]
       },
       {
            description: "Possible Source Cell and Target Cells where both target and source cells have bad_rsrp_percentage kpis of 0",
            size: 1,
            result: [{
                    "sourceCell": {
                        "fdn": "001",
                        "ossId": 1,
                        "kpis": {
                            "distance_q1": "208.72000000000008",
                            "distance_q2": "417.44000000000017",
                            "distance_q3": "626.1600000000003",
                            "distance_q4": "834.8800000000003",
                            "num_samples_rsrp_ta_q1":"100",
                            "num_samples_rsrp_ta_q2":"100",
                            "num_samples_rsrp_ta_q3":"100",
                            "num_samples_rsrp_ta_q4":"100",
                            "num_bad_samples_rsrp_ta_q1":"0",
                            "num_bad_samples_rsrp_ta_q2":"0",
                            "num_bad_samples_rsrp_ta_q3":"0",
                            "num_bad_samples_rsrp_ta_q4":"0"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    },
                    "targetCells": [
                        {
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                               "distance_q1": "596.48",
                               "distance_q2": "1192.96",
                               "distance_q3": "1789.44",
                               "distance_q4": "2385.92",
                               "num_samples_rsrp_ta_q1":"100",
                               "num_samples_rsrp_ta_q2":"100",
                               "num_samples_rsrp_ta_q3":"100",
                               "num_samples_rsrp_ta_q4":"100",
                               "num_bad_samples_rsrp_ta_q1":"0",
                               "num_bad_samples_rsrp_ta_q2":"0",
                               "num_bad_samples_rsrp_ta_q3":"0",
                               "num_bad_samples_rsrp_ta_q4":"0"
                            },
                            "cmAttributes": {},
                            "settings": {
                               "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                           "distance_q1": "208.72000000000008",
                           "distance_q2": "417.44000000000017",
                           "distance_q3": "626.1600000000003",
                           "distance_q4": "834.8800000000003",
                           "num_samples_rsrp_ta_q1":"100",
                           "num_samples_rsrp_ta_q2":"100",
                           "num_samples_rsrp_ta_q3":"100",
                           "num_samples_rsrp_ta_q4":"100",
                           "num_bad_samples_rsrp_ta_q1":"0",
                           "num_bad_samples_rsrp_ta_q2":"0",
                           "num_bad_samples_rsrp_ta_q3":"0",
                           "num_bad_samples_rsrp_ta_q4":"0"
                        },
                        "cmAttributes": {},
                        "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    },
                    "targetCells": [{
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                               "distance_q1": "596.48",
                               "distance_q2": "1192.96",
                               "distance_q3": "1789.44",
                               "distance_q4": "2385.92",
                               "num_samples_rsrp_ta_q1":"100",
                               "num_samples_rsrp_ta_q2":"100",
                               "num_samples_rsrp_ta_q3":"100",
                               "num_samples_rsrp_ta_q4":"100",
                               "num_bad_samples_rsrp_ta_q1":"0",
                               "num_bad_samples_rsrp_ta_q2":"0",
                               "num_bad_samples_rsrp_ta_q3":"0",
                               "num_bad_samples_rsrp_ta_q4":"0"
                            },
                            "cmAttributes": {},
                            "settings": {
                               "percentage_bad_rsrp_ratio_threshold": "1.2"
                            }
                        }
                    ]
                }
           ]
       },
       {
            description: "Possible Source Cell and Target Cell where target bad rsrp % is 0 and source bad rsrp is > 0, therefore is not screened out",
            size: 1,
            result: [{
                    "sourceCell": {
                        "fdn": "001",
                        "ossId": 1,
                        "kpis": {
                            "distance_q1": "208.72000000000008",
                            "distance_q2": "417.44000000000017",
                            "distance_q3": "626.1600000000003",
                            "distance_q4": "834.8800000000003",
                            "num_samples_rsrp_ta_q1":"100",
                            "num_samples_rsrp_ta_q2":"100",
                            "num_samples_rsrp_ta_q3":"100",
                            "num_samples_rsrp_ta_q4":"100",
                            "num_bad_samples_rsrp_ta_q1":"100",
                            "num_bad_samples_rsrp_ta_q2":"100",
                            "num_bad_samples_rsrp_ta_q3":"100",
                            "num_bad_samples_rsrp_ta_q4":"100"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    },
                    "targetCells": [
                         {
                             "fdn": "002",
                             "ossId": 2,
                             "kpis": {
                                "distance_q1": "596.48",
                                "distance_q2": "1192.96",
                                "distance_q3": "1789.44",
                                "distance_q4": "2385.92",
                                "num_samples_rsrp_ta_q1":"100",
                                "num_samples_rsrp_ta_q2":"100",
                                "num_samples_rsrp_ta_q3":"100",
                                "num_samples_rsrp_ta_q4":"100",
                                "num_bad_samples_rsrp_ta_q1":"0",
                                "num_bad_samples_rsrp_ta_q2":"0",
                                "num_bad_samples_rsrp_ta_q3":"0",
                                "num_bad_samples_rsrp_ta_q4":"0"
                             },
                             "cmAttributes": {},
                             "settings": {
                                "percentage_bad_rsrp_ratio_threshold": "1.2"
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
                           "distance_q1": "208.72000000000008",
                           "distance_q2": "417.44000000000017",
                           "distance_q3": "626.1600000000003",
                           "distance_q4": "834.8800000000003",
                           "num_samples_rsrp_ta_q1":"100",
                           "num_samples_rsrp_ta_q2":"100",
                           "num_samples_rsrp_ta_q3":"100",
                           "num_samples_rsrp_ta_q4":"100",
                           "num_bad_samples_rsrp_ta_q1":"100",
                           "num_bad_samples_rsrp_ta_q2":"100",
                           "num_bad_samples_rsrp_ta_q3":"100",
                           "num_bad_samples_rsrp_ta_q4":"100"
                        },
                        "cmAttributes": {},
                        "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    },
                    "targetCells": [{
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                               "distance_q1": "596.48",
                               "distance_q2": "1192.96",
                               "distance_q3": "1789.44",
                               "distance_q4": "2385.92",
                                "num_samples_rsrp_ta_q1":"100",
                                "num_samples_rsrp_ta_q2":"100",
                                "num_samples_rsrp_ta_q3":"100",
                                "num_samples_rsrp_ta_q4":"100",
                               "num_bad_samples_rsrp_ta_q1":"0",
                               "num_bad_samples_rsrp_ta_q2":"0",
                               "num_bad_samples_rsrp_ta_q3":"0",
                               "num_bad_samples_rsrp_ta_q4":"0"
                            },
                            "cmAttributes": {},
                            "settings": {
                               "percentage_bad_rsrp_ratio_threshold": "1.2"
                            }
                        }
                    ]
                }
           ]
       },
       {
            description: "Possible Source Cell and Target Cell where target source ratio is Infinity, therefore is screened out",
            size: 1,
            result: [],
            data: [{
                    "sourceCell": {
                        "fdn": "001",
                        "ossId": 1,
                        "kpis": {
                           "distance_q1": "208.72000000000008",
                           "distance_q2": "417.44000000000017",
                           "distance_q3": "626.1600000000003",
                           "distance_q4": "834.8800000000003",
                           "num_samples_rsrp_ta_q1":"10",
                           "num_samples_rsrp_ta_q2":"10",
                           "num_samples_rsrp_ta_q3":"10",
                           "num_samples_rsrp_ta_q4":"10",
                           "num_bad_samples_rsrp_ta_q1":"0",
                           "num_bad_samples_rsrp_ta_q2":"0",
                           "num_bad_samples_rsrp_ta_q3":"0",
                           "num_bad_samples_rsrp_ta_q4":"0"
                        },
                        "cmAttributes": {},
                        "settings": {
                           "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    },
                    "targetCells": [{
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                               "distance_q1": "596.48",
                               "distance_q2": "1192.96",
                               "distance_q3": "1789.44",
                               "distance_q4": "2385.92",
                                "num_samples_rsrp_ta_q1":"100",
                                "num_samples_rsrp_ta_q2":"100",
                                "num_samples_rsrp_ta_q3":"100",
                                "num_samples_rsrp_ta_q4":"100",
                               "num_bad_samples_rsrp_ta_q1":"10",
                               "num_bad_samples_rsrp_ta_q2":"10",
                               "num_bad_samples_rsrp_ta_q3":"10",
                               "num_bad_samples_rsrp_ta_q4":"10"
                            },
                            "cmAttributes": {},
                            "settings": {
                               "percentage_bad_rsrp_ratio_threshold": "1.2"
                            }
                        }
                    ]
                }
           ]
       },
       {
            description: "Possible Source Cell and Target Cell where target bad rsrp % is NaN and source bad rsrp is 0, therefore is screened out",
            size: 1,
            result: [],
            data: [{
                    "sourceCell": {
                        "fdn": "001",
                        "ossId": 1,
                        "kpis": {
                            "distance_q1": "208.72000000000008",
                            "distance_q2": "417.44000000000017",
                            "distance_q3": "626.1600000000003",
                            "distance_q4": "834.8800000000003",
                            "num_samples_rsrp_ta_q1":"100",
                            "num_samples_rsrp_ta_q2":"100",
                            "num_samples_rsrp_ta_q3":"100",
                            "num_samples_rsrp_ta_q4":"100",
                            "num_bad_samples_rsrp_ta_q1":"0",
                            "num_bad_samples_rsrp_ta_q2":"0",
                            "num_bad_samples_rsrp_ta_q3":"0",
                            "num_bad_samples_rsrp_ta_q4":"0"
                        },
                        "cmAttributes": {},
                        "settings": {
                            "percentage_bad_rsrp_ratio_threshold": "1.2"
                        }
                    },
                    "targetCells": [{
                            "fdn": "002",
                            "ossId": 2,
                            "kpis": {
                               "distance_q1": "596.48",
                               "distance_q2": "1192.96",
                               "distance_q3": "1789.44",
                               "distance_q4": "2385.92",
                               "num_samples_rsrp_ta_q1":"0",
                               "num_samples_rsrp_ta_q2":"0",
                               "num_samples_rsrp_ta_q3":"0",
                               "num_samples_rsrp_ta_q4":"0",
                               "num_bad_samples_rsrp_ta_q1":"0",
                               "num_bad_samples_rsrp_ta_q2":"0",
                               "num_bad_samples_rsrp_ta_q3":"0",
                               "num_bad_samples_rsrp_ta_q4":"0"
                            },
                            "cmAttributes": {},
                            "settings": {
                               "percentage_bad_rsrp_ratio_threshold": "1.2"
                            }
                        }
                    ]
                }
           ]
       }
];
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
     description: "TC_1 Possible Source Cell  And Target cells where all target cell that doesn't satisfies performance and availability rule and not screened out",
     TestCase: "TestAllConditions",
     result: [ {
                  "sourceCell": {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "initial_and_added_e_rab_establishment_sr": "8",
                           "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                           "e_rab_retainability_percentage_lost": "6",
                           "e_rab_retainability_percentage_lost_qci1": "6",
                           "cell_handover_success_rate": "11",
                           "cell_availability": "11"
                      },
                      "settings": {
                         "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                          "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                          "lb_threshold_for_erab_percentage_lost": "9",
                          "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                          "lb_threshold_for_cell_ho_succ_rate": "10",
                          "lb_threshold_for_cell_availability": "10"
                      }
                  },
                  "targetCells": [
                      {
                          "fdn": "002",
                          "ossId": 2,
                          "kpis": {
                              "initial_and_added_e_rab_establishment_sr": "8",
                              "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                              "e_rab_retainability_percentage_lost": "6",
                              "e_rab_retainability_percentage_lost_qci1": "6",
                              "cell_handover_success_rate": "11",
                              "cell_availability": "11"
                          },
                          "settings": {
                              "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                              "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                              "lb_threshold_for_erab_percentage_lost": "9",
                              "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                              "lb_threshold_for_cell_ho_succ_rate": "10",
                              "lb_threshold_for_cell_availability": "10"
                          }
                      },
                      {
                           "fdn": "003",
                           "ossId": 3,
                           "kpis": {
                               "initial_and_added_e_rab_establishment_sr": "9",
                               "initial_and_added_e_rab_establishment_sr_for_qci1": "9",
                               "e_rab_retainability_percentage_lost": "5",
                               "e_rab_retainability_percentage_lost_qci1": "5",
                               "cell_handover_success_rate": "15",
                               "cell_availability": "15"
                           },
                           "settings": {
                               "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                               "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                               "lb_threshold_for_erab_percentage_lost": "13",
                               "lb_threshold_for_erab_percentage_lost_for_qci1": "13",
                               "lb_threshold_for_cell_ho_succ_rate": "9",
                               "lb_threshold_for_cell_availability": "9"
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
                         "initial_and_added_e_rab_establishment_sr": "8",
                          "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                          "e_rab_retainability_percentage_lost": "6",
                          "e_rab_retainability_percentage_lost_qci1": "6",
                          "cell_handover_success_rate": "11",
                          "cell_availability": "11"
                     },
                     "settings": {
                        "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                         "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                         "lb_threshold_for_erab_percentage_lost": "9",
                         "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                         "lb_threshold_for_cell_ho_succ_rate": "10",
                         "lb_threshold_for_cell_availability": "10"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "kpis": {
                             "initial_and_added_e_rab_establishment_sr": "8",
                             "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                             "e_rab_retainability_percentage_lost": "6",
                             "e_rab_retainability_percentage_lost_qci1": "6",
                             "cell_handover_success_rate": "11",
                             "cell_availability": "11"
                         },
                         "settings": {
                             "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                             "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                             "lb_threshold_for_erab_percentage_lost": "9",
                             "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                             "lb_threshold_for_cell_ho_succ_rate": "10",
                             "lb_threshold_for_cell_availability": "10"
                         }
                     },
                     {
                          "fdn": "003",
                          "ossId": 3,
                          "kpis": {
                              "initial_and_added_e_rab_establishment_sr": "9",
                              "initial_and_added_e_rab_establishment_sr_for_qci1": "9",
                              "e_rab_retainability_percentage_lost": "5",
                              "e_rab_retainability_percentage_lost_qci1": "5",
                              "cell_handover_success_rate": "15",
                              "cell_availability": "15"
                          },
                          "settings": {
                              "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                              "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                              "lb_threshold_for_erab_percentage_lost": "13",
                              "lb_threshold_for_erab_percentage_lost_for_qci1": "13",
                              "lb_threshold_for_cell_ho_succ_rate": "9",
                              "lb_threshold_for_cell_availability": "9"
                          }
                     }
                 ]
             }
     ]},{
    description: "TC_2 Possible Source Cell And Target cells where all target cell that doesn't satisfies performance and availability rule and not screened out",
    TestCase: "TestAllConditions",
    result: [],
    data: [{
            "sourceCell": {
                    "fdn": "001",
                    "ossId": 1,
                    "kpis": {
                        "initial_and_added_e_rab_establishment_sr": "8",
                         "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                         "e_rab_retainability_percentage_lost": "6",
                         "e_rab_retainability_percentage_lost_qci1": "6",
                         "cell_handover_success_rate": "11",
                         "cell_availability": "11"
                    },
                    "settings": {
                       "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                        "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                        "lb_threshold_for_erab_percentage_lost": "9",
                        "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                        "lb_threshold_for_cell_ho_succ_rate": "10",
                        "lb_threshold_for_cell_availability": "10"
                    }
                },
                "targetCells": [
                    {
                        "fdn": "002",
                        "ossId": 2,
                        "kpis": {
                            "initial_and_added_e_rab_establishment_sr": "6",
                            "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                            "e_rab_retainability_percentage_lost": "6",
                            "e_rab_retainability_percentage_lost_qci1": "6",
                            "cell_handover_success_rate": "11",
                            "cell_availability": "11"
                        },
                        "settings": {
                            "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                            "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                            "lb_threshold_for_erab_percentage_lost": "9",
                            "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                            "lb_threshold_for_cell_ho_succ_rate": "10",
                            "lb_threshold_for_cell_availability": "10"
                        }
                    },
                    {
                         "fdn": "003",
                         "ossId": 3,
                         "kpis": {
                             "initial_and_added_e_rab_establishment_sr": "6",
                             "initial_and_added_e_rab_establishment_sr_for_qci1": "9",
                             "e_rab_retainability_percentage_lost": "5",
                             "e_rab_retainability_percentage_lost_qci1": "5",
                             "cell_handover_success_rate": "15",
                             "cell_availability": "15"
                         },
                         "settings": {
                             "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                             "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                             "lb_threshold_for_erab_percentage_lost": "13",
                             "lb_threshold_for_erab_percentage_lost_for_qci1": "13",
                             "lb_threshold_for_cell_ho_succ_rate": "9",
                             "lb_threshold_for_cell_availability": "9"
                         }
                    }
                ]
            }
    ]},{
      description: "TC_3 Possible Source Cell And Target cells where all target cell that doesn't satisfies performance and availability rule and not screened out",
      TestCase: "TestAllConditions",
      result: [],
      data: [{
              "sourceCell": {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "initial_and_added_e_rab_establishment_sr": "8",
                           "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                           "e_rab_retainability_percentage_lost": "6",
                           "e_rab_retainability_percentage_lost_qci1": "6",
                           "cell_handover_success_rate": "11",
                           "cell_availability": "11"
                      },
                      "settings": {
                         "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                          "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                          "lb_threshold_for_erab_percentage_lost": "9",
                          "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                          "lb_threshold_for_cell_ho_succ_rate": "10",
                          "lb_threshold_for_cell_availability": "10"
                      }
                  },
                  "targetCells": [
                      {
                          "fdn": "002",
                          "ossId": 2,
                          "kpis": {
                              "initial_and_added_e_rab_establishment_sr": "6",
                              "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                              "e_rab_retainability_percentage_lost": "6",
                              "e_rab_retainability_percentage_lost_qci1": "6",
                              "cell_handover_success_rate": "11",
                              "cell_availability": null
                          },
                          "settings": {
                              "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                              "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                              "lb_threshold_for_erab_percentage_lost": "9",
                              "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                              "lb_threshold_for_cell_ho_succ_rate": "10",
                              "lb_threshold_for_cell_availability": "10"
                          }
                      }
                  ]
              }
      ]},{
      description: "TC_4 Possible Source Cell And Target cells where all target cell that doesn't satisfies performance and availability rule and not screened out",
      TestCase: "TestAllConditions",
      result: [],
      data: [{
              "sourceCell": {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "initial_and_added_e_rab_establishment_sr": "8",
                           "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                           "e_rab_retainability_percentage_lost": "6",
                           "e_rab_retainability_percentage_lost_qci1": "6",
                           "cell_handover_success_rate": "11",
                           "cell_availability": "11"
                      },
                      "settings": {
                         "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                          "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                          "lb_threshold_for_erab_percentage_lost": "9",
                          "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                          "lb_threshold_for_cell_ho_succ_rate": "10",
                          "lb_threshold_for_cell_availability": "10"
                      }
                  },
                  "targetCells": [
                      {
                          "fdn": "002",
                          "ossId": 2,
                          "kpis": {
                              "initial_and_added_e_rab_establishment_sr": "6",
                              "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                              "e_rab_retainability_percentage_lost": "6",
                              "e_rab_retainability_percentage_lost_qci1": "6",
                              "cell_handover_success_rate": "11",
                              "cell_availability": "null"
                          },
                          "settings": {
                              "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                              "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                              "lb_threshold_for_erab_percentage_lost": "9",
                              "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                              "lb_threshold_for_cell_ho_succ_rate": "10",
                              "lb_threshold_for_cell_availability": "10"
                          }
                      }
                  ]
              }
      ]},{
      description: "TC_5 Possible Source Cell And Target cells where all target cell that doesn't satisfies performance and availability rule and not screened out",
      TestCase: "TestAllConditions",
      result: [],
      data: [{
              "sourceCell": {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "initial_and_added_e_rab_establishment_sr": "8",
                           "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                           "e_rab_retainability_percentage_lost": "6",
                           "e_rab_retainability_percentage_lost_qci1": "6",
                           "cell_handover_success_rate": "11",
                           "cell_availability": "11"
                      },
                      "settings": {
                         "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                          "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                          "lb_threshold_for_erab_percentage_lost": "9",
                          "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                          "lb_threshold_for_cell_ho_succ_rate": "10",
                          "lb_threshold_for_cell_availability": "10"
                      }
                  },
                  "targetCells": [
                      {
                          "fdn": "002",
                          "ossId": 2,
                          "kpis": {
                              "initial_and_added_e_rab_establishment_sr": "6",
                              "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                              "e_rab_retainability_percentage_lost": "6",
                              "e_rab_retainability_percentage_lost_qci1": "6",
                              "cell_handover_success_rate": "11",
                              "cell_availability": ""
                          },
                          "settings": {
                              "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                              "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                              "lb_threshold_for_erab_percentage_lost": "9",
                              "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                              "lb_threshold_for_cell_ho_succ_rate": "10",
                              "lb_threshold_for_cell_availability": "10"
                          }
                      }
                  ]
              }
      ]},{
      description: "TC_6 Possible Source Cell And Target cells where all target cell that doesn't satisfies performance and availability rule and not screened out",
      TestCase: "TestAllConditions",
      result: [],
      data: [{
              "sourceCell": {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "initial_and_added_e_rab_establishment_sr": "8",
                           "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                           "e_rab_retainability_percentage_lost": "6",
                           "e_rab_retainability_percentage_lost_qci1": "6",
                           "cell_handover_success_rate": "11",
                           "cell_availability": "11"
                      },
                      "settings": {
                         "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                          "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                          "lb_threshold_for_erab_percentage_lost": "9",
                          "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                          "lb_threshold_for_cell_ho_succ_rate": "10",
                          "lb_threshold_for_cell_availability": "10"
                      }
                  },
                  "targetCells": [
                      {
                          "fdn": "002",
                          "ossId": 2,
                          "kpis": {
                              "initial_and_added_e_rab_establishment_sr": "6",
                              "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                              "e_rab_retainability_percentage_lost": "6",
                              "e_rab_retainability_percentage_lost_qci1": "6",
                              "cell_handover_success_rate": "11",
                              "cell_availability": "4"
                          },
                          "settings": {
                              "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                              "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                              "lb_threshold_for_erab_percentage_lost": "9",
                              "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                              "lb_threshold_for_cell_ho_succ_rate": "10",
                              "lb_threshold_for_cell_availability": "10"
                          }
                      }
                  ]
              }
      ]},{
      description: "TC_7 Possible Source Cell And Target cells where all target cell that doesn't satisfies performance and availability rule and not screened out",
      TestCase: "TestAllConditions",
      result: [{
      "sourceCell": {
             "fdn": "001",
             "ossId": 1,
             "kpis": {
                 "initial_and_added_e_rab_establishment_sr": "8",
                  "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                  "e_rab_retainability_percentage_lost": "6",
                  "e_rab_retainability_percentage_lost_qci1": "6",
                  "cell_handover_success_rate": "11",
                  "cell_availability": "11"
             },
             "settings": {
                "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                 "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                 "lb_threshold_for_erab_percentage_lost": "9",
                 "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                 "lb_threshold_for_cell_ho_succ_rate": "10",
                 "lb_threshold_for_cell_availability": "10"
             }
         },
         "targetCells": [
             {
                 "fdn": "002",
                 "ossId": 2,
                 "kpis": {
                     "initial_and_added_e_rab_establishment_sr": "8",
                     "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                     "e_rab_retainability_percentage_lost": "8",
                     "e_rab_retainability_percentage_lost_qci1": "8",
                     "cell_handover_success_rate": "11",
                     "cell_availability": "11"
                 },
                 "settings": {
                     "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                     "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                     "lb_threshold_for_erab_percentage_lost": "9",
                     "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                     "lb_threshold_for_cell_ho_succ_rate": "10",
                     "lb_threshold_for_cell_availability": "10"
                 }
             }
         ]
    }],
    data: [{
      "sourceCell": {
              "fdn": "001",
              "ossId": 1,
              "kpis": {
                  "initial_and_added_e_rab_establishment_sr": "8",
                   "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                   "e_rab_retainability_percentage_lost": "6",
                   "e_rab_retainability_percentage_lost_qci1": "6",
                   "cell_handover_success_rate": "11",
                   "cell_availability": "11"
              },
              "settings": {
                 "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                  "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                  "lb_threshold_for_erab_percentage_lost": "9",
                  "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                  "lb_threshold_for_cell_ho_succ_rate": "10",
                  "lb_threshold_for_cell_availability": "10"
              }
          },
          "targetCells": [
              {
                  "fdn": "002",
                  "ossId": 2,
                  "kpis": {
                      "initial_and_added_e_rab_establishment_sr": "8",
                      "initial_and_added_e_rab_establishment_sr_for_qci1": "8",
                      "e_rab_retainability_percentage_lost": "8",
                      "e_rab_retainability_percentage_lost_qci1": "8",
                      "cell_handover_success_rate": "11",
                      "cell_availability": "11"
                  },
                  "settings": {
                      "lb_threshold_for_initial_erab_estab_succ_rate": "7",
                      "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7",
                      "lb_threshold_for_erab_percentage_lost": "9",
                      "lb_threshold_for_erab_percentage_lost_for_qci1": "9",
                      "lb_threshold_for_cell_ho_succ_rate": "10",
                      "lb_threshold_for_cell_availability": "10"
                  }
              }
          ]
      }
    ]},{
     description: "TC_8 Possible Source Cell kpi initial_and_added_e_rab_establishment_sr should be greater than lb_threshold_for_initial_erab_estab_succ_rate else performance and availability rule will screened out",
     TestCase: "TestLessThanConditions",
     data: [{
             "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {},
                     "settings": {
                        "lb_threshold_for_initial_erab_estab_succ_rate": "7"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "kpis": {
                             "initial_and_added_e_rab_establishment_sr": "9",
                             "cell_availability": "11"
                         },
                         "settings": {}
                     },
                     {
                          "fdn": "003",
                          "ossId": 3,
                          "kpis": {
                              "initial_and_added_e_rab_establishment_sr": "9",
                              "cell_availability": "15"
                          },
                          "settings": {}
                     }
                 ]
             }
        ]},{
         description: "TC_9 Possible Source Cell kpi initial_and_added_e_rab_establishment_sr_for_qci1 should be greater than lb_threshold_for_initial_erab_estab_succ_rate_for_qci1 else performance and availability rule will screened out",
         TestCase: "TestLessThanConditions",
         data: [{
                 "sourceCell": {
                         "fdn": "001",
                         "ossId": 1,
                         "kpis": {},
                         "settings": {
                            "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "7"
                         }
                     },
                     "targetCells": [
                         {
                             "fdn": "002",
                             "ossId": 2,
                             "kpis": {
                                 "initial_and_added_e_rab_establishment_sr_for_qci1": "9",
                                 "cell_availability": "11"
                             },
                             "settings": {}
                         },
                         {
                              "fdn": "003",
                              "ossId": 3,
                              "kpis": {
                                  "initial_and_added_e_rab_establishment_sr_for_qci1": "9",
                                  "cell_availability": "15"
                              },
                              "settings": {}
                         }
                     ]
                 }
    ]},{
     description: "TC_10 Possible Source Cell kpi cell_handover_success_rate should be greater than lb_threshold_for_cell_ho_succ_rate else performance and availability rule will screened out",
     TestCase: "TestLessThanConditions",
     data: [{
             "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {},
                     "settings": {
                        "lb_threshold_for_cell_ho_succ_rate": "7"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "kpis": {
                             "cell_handover_success_rate": "9",
                             "cell_availability": "11"
                         },
                         "settings": {}
                     },
                     {
                          "fdn": "003",
                          "ossId": 3,
                          "kpis": {
                              "cell_handover_success_rate": "9",
                              "cell_availability": "15"
                          },
                          "settings": {}
                     }
                 ]
             }
        ]},{
        description: "TC_11 Possible Source Cell kpi e_rab_retainability_percentage_lost should be less than lb_threshold_for_erab_percentage_lost else performance and availability rule will screened out",
        TestCase: "TestGreaterThanConditions",
        data: [{
          "sourceCell": {
                  "fdn": "001",
                  "ossId": 1,
                  "kpis": {},
                  "settings": {
                     "lb_threshold_for_erab_percentage_lost": "7"
                  }
              },
              "targetCells": [
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "e_rab_retainability_percentage_lost": "9",
                          "cell_availability": "11"
                      },
                      "settings": {}
                  },
                  {
                       "fdn": "003",
                       "ossId": 3,
                       "kpis": {
                           "e_rab_retainability_percentage_lost": "9",
                           "cell_availability": "15"
                       },
                       "settings": {}
                  }
              ]
          }
        ]},{
        description: "TC_12 Possible Source Cell kpi e_rab_retainability_percentage_lost_qci1 should be less than lb_threshold_for_erab_percentage_lost_for_qci1 else performance and availability rule will screened out",
        TestCase: "TestGreaterThanConditions",
        data: [{
           "sourceCell": {
                   "fdn": "001",
                   "ossId": 1,
                   "kpis": {},
                   "settings": {
                      "lb_threshold_for_erab_percentage_lost_for_qci1": "7"
                   }
               },
               "targetCells": [
                   {
                       "fdn": "002",
                       "ossId": 2,
                       "kpis": {
                           "e_rab_retainability_percentage_lost_qci1": "9",
                           "cell_availability": "11"
                       },
                       "settings": {}
                   },
                   {
                        "fdn": "003",
                        "ossId": 3,
                        "kpis": {
                            "e_rab_retainability_percentage_lost_qci1": "9",
                            "cell_availability": "15"
                        },
                        "settings": {}
                   }
               ]
           }
       ]
  }];
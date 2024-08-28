/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

var determineStepSizeAndOptimizationSpeedTest = [{
        description: "Consistent optimization speed, no missing KPIs, num_values_used_for_mcu_cdf_calculation_daily >= min_num_cell_for_cdf_calculation, small step size",
        result :
            {
                 "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {
                         "num_values_used_for_mcu_cdf_calculation_daily": "100"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "min_num_cell_for_cdf_calculation": "20"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "stepSize": "small",
                         "kpis": {
                             "max_connected_users_daily": "166",
                             "connected_users": "140",
                             "p_failing_r_mbps": "0.93"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     },
                     {
                         "fdn": "003",
                         "ossId": 3,
                         "stepSize": "small",
                         "kpis": {
                             "max_connected_users_daily": "177",
                             "connected_users": "160",
                             "p_failing_r_mbps": "0.93"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     }
                 ]
            },
        data:
              [
                  {
                      "sourceCell": {
                          "fdn": "001",
                          "ossId": 1,
                          "kpis": {
                              "num_values_used_for_mcu_cdf_calculation_daily": "100"
                          },
                          "cmAttributes": {},
                          "settings": {
                              "min_num_cell_for_cdf_calculation": "20"
                          }
                      },
                      "targetCells": [
                          {
                              "fdn": "002",
                              "ossId": 2,
                              "stepSize": "",
                              "kpis": {
                                  "max_connected_users_daily": "166",
                                  "connected_users": "140",
                                  "p_failing_r_mbps": "0.93"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "slow",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          },
                          {
                              "fdn": "003",
                              "ossId": 3,
                              "stepSize": "",
                              "kpis": {
                                  "max_connected_users_daily": "177",
                                  "connected_users": "160",
                                  "p_failing_r_mbps": "0.93"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "slow",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          }
                      ]
                  }
              ],
        optimizationCells:
              [
                  {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "num_values_used_for_mcu_cdf_calculation_daily": "100"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "min_num_cell_for_cdf_calculation": "20"
                      }
                  },
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "max_connected_users_daily": "166",
                          "connected_users": "140",
                          "p_failing_r_mbps": "0.93"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "slow",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  },
                  {
                      "fdn": "003",
                      "ossId": 3,
                      "kpis": {
                          "max_connected_users_daily": "177",
                          "connected_users": "160",
                          "p_failing_r_mbps": "0.93"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "slow",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  }
              ]
    },
    {
        description: "Consistent optimization speed, no missing KPIs, num_values_used_for_mcu_cdf_calculation_daily >= min_num_cell_for_cdf_calculation, large step size",
        result :
            {
                 "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {
                         "num_values_used_for_mcu_cdf_calculation_daily": "100"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "min_num_cell_for_cdf_calculation": "20"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "stepSize": "large",
                         "kpis": {
                             "max_connected_users_daily": "166",
                             "connected_users": "140",
                             "p_failing_r_mbps": "0.4"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     }
                 ]
             },
        data:
              [
                  {
                      "sourceCell": {
                          "fdn": "001",
                          "ossId": 1,
                          "kpis": {
                              "num_values_used_for_mcu_cdf_calculation_daily": "100"
                          },
                          "cmAttributes": {},
                          "settings": {
                              "min_num_cell_for_cdf_calculation": "20"
                          }
                      },
                      "targetCells": [
                          {
                              "fdn": "002",
                              "ossId": 2,
                              "stepSize": "",
                              "kpis": {
                                  "max_connected_users_daily": "166",
                                  "connected_users": "140",
                                  "p_failing_r_mbps": "0.4"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "slow",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          }
                      ]
                  }
              ],
        optimizationCells:
              [
                  {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "num_values_used_for_mcu_cdf_calculation_daily": "100"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "min_num_cell_for_cdf_calculation": "20"
                      }
                  },
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "max_connected_users_daily": "166",
                          "connected_users": "140",
                          "p_failing_r_mbps": "0.4"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "slow",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  }
              ]
    },
    {
        description: "Inconsistent optimization speed, small step size",
        result :
             {
                 "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {
                         "num_values_used_for_mcu_cdf_calculation_daily": "100"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "min_num_cell_for_cdf_calculation": "20"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "stepSize": "small",
                         "kpis": {
                             "max_connected_users_daily": "166",
                             "connected_users": "140",
                             "p_failing_r_mbps": "0.93"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     }
                 ]
             },
        data:
              [
                  {
                      "sourceCell": {
                          "fdn": "001",
                          "ossId": 1,
                          "kpis": {
                              "num_values_used_for_mcu_cdf_calculation_daily": "100"
                          },
                          "cmAttributes": {},
                          "settings": {
                              "min_num_cell_for_cdf_calculation": "20"
                          }
                      },
                      "targetCells": [
                          {
                              "fdn": "002",
                              "ossId": 2,
                              "stepSize": "",
                              "kpis": {
                                  "max_connected_users_daily": "166",
                                  "connected_users": "140",
                                  "p_failing_r_mbps": "0.93"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "normal",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          }
                      ]
                  }
              ],
        optimizationCells:
              [
                  {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "num_values_used_for_mcu_cdf_calculation_daily": "100"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "min_num_cell_for_cdf_calculation": "20"
                      }
                  },
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "max_connected_users_daily": "166",
                          "connected_users": "140",
                          "p_failing_r_mbps": "0.93"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "normal",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  }
              ]
    },
    {
        description: "Missing num_values_used_for_mcu_cdf_calculation_daily KPI, small step size",
        result :
             {
                 "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {},
                     "cmAttributes": {},
                     "settings": {
                         "min_num_cell_for_cdf_calculation": "20"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "stepSize": "small",
                         "kpis": {
                             "max_connected_users_daily": "166",
                             "connected_users": "140",
                             "p_failing_r_mbps": "0.93"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     }
                 ]
             },
        data:
              [
                  {
                      "sourceCell": {
                          "fdn": "001",
                          "ossId": 1,
                          "kpis": {},
                          "cmAttributes": {},
                          "settings": {
                              "min_num_cell_for_cdf_calculation": "20"
                          }
                      },
                      "targetCells": [
                          {
                              "fdn": "002",
                              "ossId": 2,
                              "stepSize": "",
                              "kpis": {
                                  "max_connected_users_daily": "166",
                                  "connected_users": "140",
                                  "p_failing_r_mbps": "0.93"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "slow",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          }
                      ]
                  }
              ],
        optimizationCells:
              [
                  {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {},
                      "cmAttributes": {},
                      "settings": {
                          "min_num_cell_for_cdf_calculation": "20"
                      }
                  },
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "max_connected_users_daily": "166",
                          "connected_users": "140",
                          "p_failing_r_mbps": "0.93"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "slow",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  }
              ]
    },
    {
        description: "num_values_used_for_mcu_cdf_calculation_daily KPI is \"null\", small step size",
        result :
             {
                 "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {
                         "num_values_used_for_mcu_cdf_calculation_daily": "null"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "min_num_cell_for_cdf_calculation": "20"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "stepSize": "small",
                         "kpis": {
                             "max_connected_users_daily": "166",
                             "connected_users": "140",
                             "p_failing_r_mbps": "0.93"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     }
                 ]
             },
        data:
              [
                  {
                      "sourceCell": {
                          "fdn": "001",
                          "ossId": 1,
                          "kpis": {
                              "num_values_used_for_mcu_cdf_calculation_daily": "null"
                          },
                          "cmAttributes": {},
                          "settings": {
                              "min_num_cell_for_cdf_calculation": "20"
                          }
                      },
                      "targetCells": [
                          {
                              "fdn": "002",
                              "ossId": 2,
                              "stepSize": "",
                              "kpis": {
                                  "max_connected_users_daily": "166",
                                  "connected_users": "140",
                                  "p_failing_r_mbps": "0.93"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "slow",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          }
                      ]
                  }
              ],
        optimizationCells:
              [
                  {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "num_values_used_for_mcu_cdf_calculation_daily": "null"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "min_num_cell_for_cdf_calculation": "20"
                      }
                  },
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "max_connected_users_daily": "166",
                          "connected_users": "140",
                          "p_failing_r_mbps": "0.93"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "slow",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  }
              ]
    },
    {
        description: "num_values_used_for_mcu_cdf_calculation_daily < min_num_cell_for_cdf_calculation, small step size",
        result :
             {
                 "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {
                         "num_values_used_for_mcu_cdf_calculation_daily": "15"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "min_num_cell_for_cdf_calculation": "20"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "stepSize": "small",
                         "kpis": {
                             "max_connected_users_daily": "166",
                             "connected_users": "140",
                             "p_failing_r_mbps": "0.4"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     }
                 ]
             },
        data:
              [
                  {
                      "sourceCell": {
                          "fdn": "001",
                          "ossId": 1,
                          "kpis": {
                              "num_values_used_for_mcu_cdf_calculation_daily": "15"
                          },
                          "cmAttributes": {},
                          "settings": {
                              "min_num_cell_for_cdf_calculation": "20"
                          }
                      },
                      "targetCells": [
                          {
                              "fdn": "002",
                              "ossId": 2,
                              "stepSize": "",
                              "kpis": {
                                  "max_connected_users_daily": "166",
                                  "connected_users": "140",
                                  "p_failing_r_mbps": "0.4"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "slow",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          }
                      ]
                  }
              ],
        optimizationCells:
              [
                  {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "num_values_used_for_mcu_cdf_calculation_daily": "15"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "min_num_cell_for_cdf_calculation": "20"
                      }
                  },
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "max_connected_users_daily": "166",
                          "connected_users": "140",
                          "p_failing_r_mbps": "0.4"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "slow",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  }
              ]
    },
    {
        description: "Missing max_connected_users_daily KPI, small step size",
        result :
             {
                 "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {
                         "num_values_used_for_mcu_cdf_calculation_daily": "100"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "min_num_cell_for_cdf_calculation": "20"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "stepSize": "small",
                         "kpis": {
                             "connected_users": "140",
                             "p_failing_r_mbps": "0.93"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     }
                 ]
             },
        data:
              [
                  {
                      "sourceCell": {
                          "fdn": "001",
                          "ossId": 1,
                          "kpis": {
                              "num_values_used_for_mcu_cdf_calculation_daily": "100"
                          },
                          "cmAttributes": {},
                          "settings": {
                              "min_num_cell_for_cdf_calculation": "20"
                          }
                      },
                      "targetCells": [
                          {
                              "fdn": "002",
                              "ossId": 2,
                              "stepSize": "",
                              "kpis": {
                                  "connected_users": "140",
                                  "p_failing_r_mbps": "0.93"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "slow",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          }
                      ]
                  }
              ],
        optimizationCells:
              [
                  {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "num_values_used_for_mcu_cdf_calculation_daily": "100"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "min_num_cell_for_cdf_calculation": "20"
                      }
                  },
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "connected_users": "140",
                          "p_failing_r_mbps": "0.93"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "slow",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  }
              ]
    },
    {
        description: "max_connected_users_daily KPI is \"null\", small step size",
        result :
             {
                 "sourceCell": {
                     "fdn": "001",
                     "ossId": 1,
                     "kpis": {
                         "num_values_used_for_mcu_cdf_calculation_daily": "100"
                     },
                     "cmAttributes": {},
                     "settings": {
                         "min_num_cell_for_cdf_calculation": "20"
                     }
                 },
                 "targetCells": [
                     {
                         "fdn": "002",
                         "ossId": 2,
                         "stepSize": "small",
                         "kpis": {
                             "max_connected_users_daily": "null",
                             "connected_users": "140",
                             "p_failing_r_mbps": "0.93"
                         },
                         "cmAttributes": {},
                         "settings": {
                             "optimization_speed": "slow",
                             "qos_for_capacity_estimation": "0.5",
                             "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                         }
                     }
                 ]
             },
        data:
              [
                  {
                      "sourceCell": {
                          "fdn": "001",
                          "ossId": 1,
                          "kpis": {
                              "num_values_used_for_mcu_cdf_calculation_daily": "100"
                          },
                          "cmAttributes": {},
                          "settings": {
                              "min_num_cell_for_cdf_calculation": "20"
                          }
                      },
                      "targetCells": [
                          {
                              "fdn": "002",
                              "ossId": 2,
                              "stepSize": "",
                              "kpis": {
                                  "max_connected_users_daily": "null",
                                  "connected_users": "140",
                                  "p_failing_r_mbps": "0.93"
                              },
                              "cmAttributes": {},
                              "settings": {
                                  "optimization_speed": "slow",
                                  "qos_for_capacity_estimation": "0.5",
                                  "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                              }
                          }
                      ]
                  }
              ],
        optimizationCells:
              [
                  {
                      "fdn": "001",
                      "ossId": 1,
                      "kpis": {
                          "num_values_used_for_mcu_cdf_calculation_daily": "100"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "min_num_cell_for_cdf_calculation": "20"
                      }
                  },
                  {
                      "fdn": "002",
                      "ossId": 2,
                      "kpis": {
                          "max_connected_users_daily": "null",
                          "connected_users": "140",
                          "p_failing_r_mbps": "0.93"
                      },
                      "cmAttributes": {},
                      "settings": {
                          "optimization_speed": "slow",
                          "qos_for_capacity_estimation": "0.5",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2"
                      }
                  }
              ]
    }
];

#*------------------------------------------------------------------------------
#******************************************************************************
# COPYRIGHT Ericsson 2021
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#******************************************************************************
#------------------------------------------------------------------------------
@RunAllTests

Feature: FLM_MaxSourceUsersMove
  FLM_MaxSourceUsersMove step does the following:
  Calculate Max Source user from source cell and distribute to 3 target cells based on delta value of source and target cells p_failing_r_mbps value.

  The kpi used:
  p_failing_r_mbps
  connected_users

  The settings used:
  N/A

  The CM attributed:
  N/A

  Please note the (num_values_used_for_mcu_cdf_calculation_daily kpi, target_cell_capacity kpi, optimization_speed setting, bandwidth setting)
  are not used directly in this state, but they are needed for the DetermineStepSizeAndOptimizationSpeed and
  NumericStepSizeAndDistributeUsers states in order to increase the number of users we are moving to the target cells,
  so we can validate the calculated max source users move.

  Scenario: TC1 - Calculate and verifying number of users to move from source cell to 3 target cells.
  Using p_failing_r_mbps kpi delta value of source and highest p_failing_r_mbps value of target cells.
  In Below scenario source has 40 connected user and p_failing_r_mbps delta 0.6 , total calculated max source users move is 24.

      Given Create Default Optimization Cells
        | 054444_2   |
        | 054444_1_2 |
        | 054444_1_4 |
        | 054444_1_9 |


      And Policy Input Event
        | sectorId | executionId |
        | 032021   | TC_1_MaxSourceUsersMove      |

      And Set Optimization Cells Data
        | dataType     | dataName                                      | dataValue         | fdn        |
        | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_2   |
        | KPI          | p_failing_r_mbps                              | 0.9               | 054444_2   |
        | KPI          | connected_users                               | 40                | 054444_2   |
        | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_2   |
        | KPI          | target_cell_capacity                          | 100               | 054444_2   |
        | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_2   |
        | SETTING      | optimization_speed                            | normal            | 054444_2   |
        | KPI          | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
        | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
        | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
        | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
        | KPI          | target_cell_capacity                          | 100               | 054444_1_2 |
        | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
        | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |
        | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_4 |
        | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
        | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_4 |
        | KPI          | max_connected_users_daily                     | 31                | 054444_1_4 |
        | KPI          | target_cell_capacity                          | 100               | 054444_1_4 |
        | SETTING      | optimization_speed                            | normal            | 054444_1_4 |
        | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_4 |
        | KPI          | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_9 |
        | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_9 |
        | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_9 |
        | KPI          | max_connected_users_daily                     | 31                | 054444_1_9 |
        | KPI          | target_cell_capacity                          | 100               | 054444_1_9 |
        | SETTING      | optimization_speed                            | normal            | 054444_1_9 |
        | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_9 |

      When Putting Policy Input Event onto Kafka Topic

      Then Optimization proceeds with the following target cells
        | targetCellFdn | targetCellOssId | targetUsersMove |
        | 054444_1_2    | 1               | 8               |
        | 054444_1_4    | 1               | 8               |
        | 054444_1_9    | 1               | 8               |

      Then Optimization proceeds with the following source cells
        | sourceCellFdn | sourceCellOssId | sourceUsersMove |
        | 054444_2      | 1               | 24              |

  Scenario: TC2 - Calculate and verifying number of users to move from source cell to 1 target cell.
  Using p_failing_r_mbps kpi delta value of source and highest p_failing_r_mbps value of target cells.
  In Below scenario source has 40 connected user and p_failing_r_mbps delta 0.8 (0.9 - 0.1 = 0.8),
  total calculated max source users move is 40 * 0.8 =  32.

    Given Create Default Optimization Cells
      | 10032021_1 |
      | 10032021_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_2_MaxSourceUsersMove      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.545855356858811 | 10032021_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 10032021_1 |
      | KPI          | connected_users                               | 40                | 10032021_1 |
      | KPI          | unhappy_users                                 | 0.645855356858811 | 10032021_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 10032021_1 |
      | SETTING      | optimization_speed                            | normal            | 10032021_1 |
      | KPI          | goal_function_resource_efficiency             | 0.97165574442653  | 10032021_2 |
      | KPI          | p_failing_r_mbps                              | 0.1               | 10032021_2 |
      | KPI          | max_connected_users_daily                     | 31                | 10032021_2 |
      | KPI          | target_cell_capacity                          | 128               | 10032021_2 |
      | SETTING      | optimization_speed                            | normal            | 10032021_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 10032021_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 10032021_2    | 1               | 32              |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 10032021_1    | 1               | 32              |

  Scenario: TC3 - Calculate and verifying number of users to move from source cell to 1 target cell.
  Using p_failing_r_mbps kpi delta value of source and highest p_failing_r_mbps value of target cells.
  In Below scenario source has 39 connected user and p_failing_r_mbps delta 0.7 (0.9 - 0.2 = 0.7) , total calculated max source users move is 39 * 0.8 =  27.3.
  The targetUsersMove is 27 because it gets a floor of max source user move in the NumericStepSizeAndDistributeUsers state.

    Given Create Default Optimization Cells
       | 10032021_1 |
       | 10032021_2 |

    And Policy Input Event
       | sectorId | executionId |
       | 032021   | TC_3_MaxSourceUsersMove      |

    And Set Optimization Cells Data
       | dataType     | dataName                                      | dataValue         | fdn        |
       | KPI          | goal_function_resource_efficiency             | 0.545855356858811 | 10032021_1 |
       | KPI          | p_failing_r_mbps                              | 0.9               | 10032021_1 |
       | KPI          | connected_users                               | 39                | 10032021_1 |
       | KPI          | unhappy_users                                 | 0.645855356858811 | 10032021_1 |
       | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 10032021_1 |
       | SETTING      | optimization_speed                            | normal            | 10032021_1 |
       | KPI          | goal_function_resource_efficiency             | 0.97165574442653  | 10032021_2 |
       | KPI          | p_failing_r_mbps                              | 0.2               | 10032021_2 |
       | KPI          | max_connected_users_daily                     | 31                | 10032021_2 |
       | KPI          | target_cell_capacity                          | 110               | 10032021_2 |
       | SETTING      | optimization_speed                            | normal            | 10032021_2 |
       | CM_ATTRIBUTE | bandwidth                                     | 20000             | 10032021_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
       | targetCellFdn | targetCellOssId | targetUsersMove |
       | 10032021_2    | 1               | 27              |

    And Optimization proceeds with the following source cells
       | sourceCellFdn | sourceCellOssId | sourceUsersMove |
       | 10032021_1    | 1               | 27              |

  Scenario: TC4 - Calculate and verifying number of users to move from source cell to 1 target cell.
  Using p_failing_r_mbps kpi delta value of source and highest p_failing_r_mbps value of target cells.
  In Below scenario source has 39 connected user and p_failing_r_mbps delta 0.0 (0.9- 0.9 = 0) , total calculated users to move is 39 * 0 =  0,
  As maxUserToMove is calculated as 0, the sector excluded due to Source Cell maxUserToMove < 0.5.

    Given Create Default Optimization Cells
       | 10032021_1 |
       | 10032021_2 |

    And Policy Input Event
       | sectorId           | executionId |
       | 10032021           | TC_4_MaxSourceUsersMove      |

    And Set Optimization Cells Data
       | dataType | dataName                          | dataValue         | fdn        |
       | KPI      | goal_function_resource_efficiency | 0.545855356858811 | 10032021_1 |
       | KPI      | p_failing_r_mbps                  | 0.9               | 10032021_1 |
       | KPI      | connected_users                   | 39                | 10032021_1 |
       | KPI      | unhappy_users                     | 0.645855356858811 | 10032021_1 |
       | KPI      | goal_function_resource_efficiency | 0.97165574442653  | 10032021_2 |
       | KPI      | p_failing_r_mbps                  | 0.9               | 10032021_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC5 - Calculate and verify number of users to move from source cell to 3 target cells.
  Using p_failing_r_mbps kpi delta value of source and highest p_failing_r_mbps value of target cells.
  In the scenario below, source cell has 40 connected_users, 0.6 delta p_failing_r_mbps and 20 endc_spid115_ues,
  total calculated max source users move is 12.

    Given Create Default Optimization Cells
      | 054444_2   |
      | 054444_1_2 |
      | 054444_1_4 |
      | 054444_1_9 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_5_MaxSourceUsersMove      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_2   |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_2   |
      | KPI          | connected_users                               | 40                | 054444_2   |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_2   |
      | KPI          | target_cell_capacity                          | 100               | 054444_2   |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_2   |
      | KPI          | endc_spid115_ues                              | 20                | 054444_2   |
      | SETTING      | optimization_speed                            | normal            | 054444_2   |
      | KPI          | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_4 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_4 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_4 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_4 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_4 |
      | KPI          | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_9 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_9 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_9 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_9 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_9 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_9 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 4               |
      | 054444_1_4    | 1               | 4               |
      | 054444_1_9    | 1               | 4               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_2      | 1               | 12              |

  Scenario: TC6 - Calculate and verify number of users to move from source cell to 3 target cells.
  Using p_failing_r_mbps kpi delta value of source and highest p_failing_r_mbps value of target cells.
  In the scenario below, source cell has 40 connected_users, 0.6 delta p_failing_r_mbps and 0 endc_spid115_ues,
  total calculated max source users move is 24.

    Given Create Default Optimization Cells
      | 054444_2   |
      | 054444_1_2 |
      | 054444_1_4 |
      | 054444_1_9 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_6_MaxSourceUsersMove      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_2   |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_2   |
      | KPI          | connected_users                               | 40                | 054444_2   |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_2   |
      | KPI          | target_cell_capacity                          | 100               | 054444_2   |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_2   |
      | KPI          | endc_spid115_ues                              | 0                 | 054444_2   |
      | SETTING      | optimization_speed                            | normal            | 054444_2   |
      | KPI          | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_4 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_4 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_4 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_4 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_4 |
      | KPI          | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_9 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_9 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_9 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_9 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_9 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_9 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 8               |
      | 054444_1_4    | 1               | 8               |
      | 054444_1_9    | 1               | 8               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_2      | 1               | 24              |

  Scenario: TC7 - Calculate and verify number of users to move from source cell to 3 target cells.
  Using p_failing_r_mbps kpi delta value of source and highest p_failing_r_mbps value of target cells.
  In the scenario below, source cell has 40 connected_users, 0.6 delta p_failing_r_mbps and 40 endc_spid115_ues,
  total calculated max source users move is 0.

    Given Create Default Optimization Cells
      | 054444_2   |
      | 054444_1_2 |
      | 054444_1_4 |
      | 054444_1_9 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_7_MaxSourceUsersMove     |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_2   |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_2   |
      | KPI          | connected_users                               | 40                | 054444_2   |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_2   |
      | KPI          | target_cell_capacity                          | 100               | 054444_2   |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_2   |
      | KPI          | endc_spid115_ues                              | 40                | 054444_2   |
      | SETTING      | optimization_speed                            | normal            | 054444_2   |
      | KPI          | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_4 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_4 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_4 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_4 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_4 |
      | KPI          | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_9 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_9 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_9 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_9 |
      | KPI          | target_cell_capacity                          | 100               | 054444_1_9 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_9 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty
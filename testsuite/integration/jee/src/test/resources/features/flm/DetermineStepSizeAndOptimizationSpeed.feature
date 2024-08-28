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
Feature: FLM_Determine_Step_Size_And_Optimization_Speed
  The purpose of this policy state is to determine the step size that is used in the next state, and to apply the
  consistency rule for the optimization_speed setting in case it is not consistent across the cells in the sector.

  Input to this state:
  Ranked list of potential Source cells and their possible target cells.

  Output from this state:
  The output from this state will be the top ranked source cell and its associated list of target cells.
  For each of the target cells the determined step size is set and if the optimization_speed was inconsistent
  across the cells in the sector, it will be defaulted to 'slow' for each of the cells.

  The kpi used:
  num_values_used_for_mcu_cdf_calculation_daily
  connected_users
  max_connected_users_daily
  p_failing_r_mbps

  The settings used:
  optimization_speed
  min_num_cell_for_cdf_calculation
  qos_for_capacity_estimation
  optimization_speed_factor_table

  The cm attributes used:
  N/A

  Please note the bandwidth and bandwidth_to_step_size_table is not used directly in this state, but it is needed for the next state
  in order to validate the step size through the number of users that we are moving. In the case step size is 'small'
  we will use the corresponding value from the bandwidth_to_step_size_table for the cell's CM bandwidth.
  It might be also used when the step size is 'large' depending on the values of usersToMove and maxUserToMove.

  Scenario: TC1 - Inconsistent Optimization Speed across cells in the sector
  The optimization_speed setting is set to 'slow' for one of the cells in the sector (054444_1_1), while for other cells
  it is set to 'normal'. Therefore, the optimization_speed setting will be set to 'slow' for all of the cells in the sector.
  In this scenario a large step size is used so target_cell_capacity/optimizationSpeed (100 / 6 = 16.6) which is 17.
  17 is greater than targetCellStepSize which is 2 and 17 is less than maxUserMove which is 160.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |
      | 054444_1_4 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_1_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 400               | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | slow              | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 100               | 054444_1_2 |
      | SETTING  | optimization_speed                            | slow              | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.5               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 100               | 054444_1_3 |
      | SETTING  | optimization_speed                            | slow              | 054444_1_3 |
      | KPI      | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_4 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_4 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_4 |
      | KPI      | target_cell_capacity                          | 100               | 054444_1_4 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 17               |
      | 054444_1_3    | 1               | 17               |
      | 054444_1_4    | 1               | 17               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 51             |




  Scenario: TC2 - Max Connected users > Actual Connected users and Target Probability < QoS for Capacity Estimation
  The condition in the scenario name evaluates to true since target cells max_connected_users KPI (31) is greater than the connected_users
  KPI (21), as well as the p_failing_r_mbps KPI (0.5) is less than qos_for_capacity_estimation setting (0.7).
  Therefore the step size gets set to 'large' and the number of users that have been moved reflects that.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_2_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI          | connected_users                               | 400               | 054444_1_1 |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI          | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 13              |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 13              |




  Scenario: TC3 - Max Connected users <= Actual Connected users and Target Probability < QoS for Capacity Estimation
  Since target cells max_connected_users KPI (19) is less than connected_users KPI (21), and p_failing_r_mbps KPI (0.5)
  is less than qos_for_capacity_estimation setting (0.7), the step size gets set to 'large',
  so the corresponding value from the bandwidth_to_step_size_table for the cell's CM bandwidth is the number of users we are moving.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_3_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI          | connected_users                               | 400               | 054444_1_1 |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI          | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 19                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 10              |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 10              |




  Scenario: TC4 - Max Connected users > Actual Connected users and Target Probability > equal QoS for Capacity Estimation
  Since target cells max_connected_users KPI (31) is greater than connected_users KPI (21), and p_failing_r_mbps KPI (0.5)
  is greater than qos_for_capacity_estimation setting (0.7), the step size gets set to 'small',
  so the corresponding value from the bandwidth_to_step_size_table for the cell's CM bandwidth is the number of users we are moving.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_4_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI          | connected_users                               | 400               | 054444_1_1 |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI          | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 12                | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | SETTING      | qos_for_capacity_estimation                   | 0.49              | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 10              |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 10              |




  Scenario: TC5 - Max Connected Users <= Actual Connected Users and Target Probability >= Qos for Capacity Estimation
  Since max_connected_users KPI (21, 20, 19) is less than or equal to connected_users KPI (21), and p_failing_r_mbps KPI (0.3, 0.3, 0.5)
  is greater than or equal to qos_for_capacity_estimation setting (0.29, 0.5), the step size gets set to 'small',
  so the corresponding value from the bandwidth_to_step_size_table for the cell's CM bandwidth is the number of users we are moving.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |
      | 054444_1_4 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_5_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI          | connected_users                               | 400               | 054444_1_1 |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI          | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 21                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | SETTING      | qos_for_capacity_estimation                   | 0.29              | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_3 |
      | KPI          | max_connected_users_daily                     | 20                | 054444_1_3 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_3 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_3 |
      | SETTING      | qos_for_capacity_estimation                   | 0.5               | 054444_1_3 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_3 |
      | KPI          | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_4 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_4 |
      | KPI          | max_connected_users_daily                     | 19                | 054444_1_4 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_4 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 10              |
      | 054444_1_3    | 1               | 10              |
      | 054444_1_4    | 1               | 10              |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 30              |




  Scenario: TC6 - num_values_used_for_mcu_cdf_calculation KPI < min_num_cell_for_cdf_calculation_lbq setting
  The value of the KPI num_values_used_for_mcu_cdf_calculation is less than the value of numCellsUsedForMCUCdfCalculation setting,
  so the step size is small regardless of the Target Cell values of Connected users and Probability
  In this case we are not confident of the Target Cell capacity so no 'large' step behaviour is allowed.
  Therefore the number of users we are moving is the corresponding value from the bandwidth_to_step_size_table for the cell's CM bandwidth (10)
  which is lower than round(target_cell_capacity (31) / optimization_speed's value from the OSF table (4)) which would be round(12.5) = 13.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_6_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI          | connected_users                               | 400               | 054444_1_1 |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI          | target_cell_capacity                          | 1000              | 054444_1_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 5                 | 054444_1_1 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 10              |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 10              |




  Scenario: TC7 - Missing or NULL max_connected_users_daily KPI
  The max_connected_users_daily KPI is either "null" or missing for the target cells, therefore, 'small' step size is chosen.
  Due to the aforementioned reason the corresponding value from the bandwidth_to_step_size_table for the cell's CM bandwidth
  is the number of users we are moving.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_7_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI          | connected_users                               | 400               | 054444_1_1 |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI          | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |
      | KPI          | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI          | p_failing_r_mbps                              | 0.5               | 054444_1_3 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_3 |
      | KPI          | max_connected_users_daily                     | null              | 054444_1_3 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_3 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_3 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 10              |
      | 054444_1_3    | 1               | 10              |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 20              |




  Scenario: TC8 - Missing num_values_used_for_mcu_cdf_calculation_daily KPI
  The num_values_used_for_mcu_cdf_calculation_daily KPI is missing for the source cell, therefore, 'small' step size is chosen.
  Due to the aforementioned reason the corresponding value from the bandwidth_to_step_size_table for the cell's CM bandwidth
  is the number of users we are moving.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_8_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType     | dataName                          | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                  | 0.9               | 054444_1_1 |
      | KPI          | connected_users                   | 400               | 054444_1_1 |
      | KPI          | unhappy_users                     | 0.592511239070919 | 054444_1_1 |
      | KPI          | target_cell_capacity              | 1000              | 054444_1_1 |
      | SETTING      | optimization_speed                | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                         | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency | 0.602045667098208 | 054444_1_2 |
      | KPI          | unhappy_users                     | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                  | 0.3               | 054444_1_2 |
      | KPI          | max_connected_users_daily         | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity              | 50                | 054444_1_2 |
      | SETTING      | optimization_speed                | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                         | 20000             | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 10              |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 10              |




  Scenario: TC9 - null num_values_used_for_mcu_cdf_calculation_daily KPI
  The num_values_used_for_mcu_cdf_calculation_daily KPI is set to "null" for the source cell, therefore, 'small' step size is chosen.
  Due to the aforementioned reason the corresponding value from the bandwidth_to_step_size_table for the cell's CM bandwidth
  is the number of users we are moving.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_9_Determine_Step_Size_And_Optimization_Speed      |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI          | connected_users                               | 400               | 054444_1_1 |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI          | target_cell_capacity                          | 1000              | 054444_1_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | null              | 054444_1_1 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 50                | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 10              |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 10              |
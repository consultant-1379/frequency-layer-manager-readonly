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

Feature: FLM_Numeric_Step_Size_And_Distribute_Users
  The purpose of this policy state is to determine the number of users that are going to be moved from source cell
  to each of the target cells that are associated with this source cell. The bandwidth cm attribute and bandwidth_to_step_size_table
  may affect the number of users we are moving (no matter whether the step size is 'small' or 'large'),
  so it was also added in some of the test cases in the MaxSourceUsersMove and DetermineStepSizeAndOptimizationSpeed feature files.

  Input to this state:
  Top ranked source cell and its associated list of target cells and MaxSourceUserMove.

  Output from this state:
  The output from this state will be the top ranked source cell and its associated list of target cells.
  For each of the target cells the numUsersToMove is calculated and set.

  The kpi used:
  target_cell_capacity
  p_failing_r_mbps

  The settings used:
  bandwidth_to_step_size_table
  optimization_speed_factor_table
  optimization_speed

  The cm attributes used:
  bandwidth

  Scenario: TC1 - Small step size, Target cells = 1 and Target Step Sizes > Max Source User Move
  MaxSourceUsersMove is (6 * 0.2) = 1.2 and bandwidth is 5000 which resolves to 2. Therefore, targetCellStepSize is 2
  and numUsersToMove becomes 1 (round(MaxSourceUsersMove)) since step size is 'small' and MaxSourceUsersMove < targetCellStepSize.

    Given Create Default Optimization Cells
      | 10032021_1 |
      | 10032021_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_1_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.545855356858811 | 10032021_1 |
      | KPI      | p_failing_r_mbps                  | 0.9               | 10032021_1 |
      | KPI      | connected_users                   | 6                 | 10032021_1 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 10032021_1 |
      | KPI      | target_cell_capacity              | 1000              | 10032021_1 |
      | SETTING  | optimization_speed                | normal            | 10032021_1 |
      | KPI      | goal_function_resource_efficiency | 0.97165574442653  | 10032021_2 |
      | KPI      | p_failing_r_mbps                  | 0.7               | 10032021_2 |
      | KPI      | max_connected_users_daily         | 20                | 10032021_2 |
      | KPI      | target_cell_capacity              | 12                | 10032021_2 |
      | SETTING  | optimization_speed                | normal            | 10032021_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 10032021_2    | 1               | 1               |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 10032021_1    | 1               | 1               |




  Scenario: TC2 - Small step size, Target cells = 1 and Target Step Sizes < Max Source User Move
  MaxSourceUsersMove is (39 * 0.7) = 28, bandwidth is 5000 which resolves to 2. Therefore, targetCellStepSize is 2
  and numUsersToMove becomes 2 since step size is 'small'.

    Given Create Default Optimization Cells
      | 10032021_1 |
      | 10032021_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_2_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.545855356858811 | 10032021_1 |
      | KPI      | p_failing_r_mbps                  | 0.9               | 10032021_1 |
      | KPI      | connected_users                   | 39                | 10032021_1 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 10032021_1 |
      | KPI      | target_cell_capacity              | 1000              | 10032021_1 |
      | SETTING  | optimization_speed                | normal            | 10032021_1 |
      | KPI      | goal_function_resource_efficiency | 0.97165574442653  | 10032021_2 |
      | KPI      | p_failing_r_mbps                  | 0.2               | 10032021_2 |
      | KPI      | max_connected_users_daily         | 20                | 10032021_2 |
      | KPI      | target_cell_capacity              | 12                | 10032021_2 |
      | SETTING  | optimization_speed                | normal            | 10032021_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 10032021_2    | 1               | 2               |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 10032021_1    | 1               | 2               |




  Scenario: TC3 - Small step size, Target cells > 1 and Target Step Sizes < Max Source User Move
  MaxSourceUsersMove is (40 * 0.6) = 24, bandwidth is 5000 which resolves to 2. Therefore, targetCellStepSize is 2
  and numUsersToMove becomes 2 since step size is 'small' for all of the target cells. Now since sum(numUsersToMove) for all
  target cells is < MaxSourceUsersMove, its not redistributed.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |
      | 054444_1_4 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_3_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 40                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 11                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.5               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 11                | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_3 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_3 |
      | KPI      | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_4 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_4 |
      | KPI      | max_connected_users_daily                     | 11                | 054444_1_4 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_4 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 2               |
      | 054444_1_3    | 1               | 2               |
      | 054444_1_4    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 6               |




  Scenario: TC4 - Small step size, Target cells > 1 and Target Step Sizes > Max Source User Move
  MaxSourceUsersMove is (42 * 0.6) = 25.2, bandwidth is 20000 which resolves to 10. Therefore, targetCellStepSize is 10
  and numUsersToMove becomes 10 since step size is 'small' for all of the target cells.
  Total number of users (30) is greater than max source user move, so redistribute users based on their numUsersToMove ratio.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |
      | 054444_1_4 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_4_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 42                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.4               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_3 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_3 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_3 |
      | KPI      | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_4 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI      | p_failing_r_mbps                              | 0.6               | 054444_1_4 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_4 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_4 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 8               |
      | 054444_1_3    | 1               | 8               |
      | 054444_1_4    | 1               | 8               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 24              |




  Scenario: TC5 - Large step size, Target cells = 1, calculated userToMove < Bandwidth table value and Target Step Size < Max Source User Move
  MaxSourceUsersMove is (40 * 0.4) = 16, bandwidth is 5000 which resolves to 2 so targetCellStepSize is 2.
  The value of usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (4 / 4) = 1,
  therefore, the total number users to move is 2. No redistribution because only 1 target cell.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_5_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 40                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.5               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 4                 | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 2               |




  Scenario: TC6 - Large step size, Target cells = 1, calculated userToMove < Bandwidth table value and Target Step Size > Max Source User Move
  MaxSourceUsersMove is (15 * (0.9 - 0.35)) = 8.25, bandwidth is 20000 which resolves to 10 so targetCellStepSize is 10.
  Since usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (32 / 4) = 8
  and it is less than the targetCellStepSize, number of users to be moved becomes 10.
  No redistribution because only 1 target cell.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_6_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType     | dataName                                      | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI          | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI          | connected_users                               | 15                | 054444_1_1 |
      | KPI          | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI          | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI          | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_1 |
      | KPI          | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI          | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI          | p_failing_r_mbps                              | 0.35              | 054444_1_2 |
      | KPI          | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI          | target_cell_capacity                          | 32                | 054444_1_2 |
      | SETTING      | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                     | 20000             | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 8               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 8               |




  Scenario: TC7 - Large step size, Target cells = 1, calculated userToMove > Bandwidth table value and Target Step Size < Max Source User Move
  MaxSourceUsersMove is (40 * 0.4) = 16, bandwidth is 5000 which resolves to 2 so targetCellStepSize is 2.
  Since usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (12 / 4) = 3
  greater than value of bandwidth, the total number of users that are going to be moved is 3.  No redistribution because only 1 target cell.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_7_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 40                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.5               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 3               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 3               |




  Scenario: TC8 - Large step size, Target cells = 1, calculated userToMove > Bandwidth table value and Target Step Size > Max Source User Move
  MaxSourceUsersMove is (3 * 0.4) = 1.2, bandwidth is 5000 which resolves to 2 so targetCellStepSize is 2.
  Since usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (12 / 4) = 3
  and its greater than targetCellStepSize, therefore numUserMove is floor(1.2) = 1
  As there is only 1 target cell, it does not proceed to the logic where it redistributes the users and makes sure its <= Max Source User Move.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_8_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 3                 | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.5               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 1               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 1               |




  Scenario: TC9 - Large step size, Target cells > 1, Target Step Sizes < Max Source User Move
  MaxSourceUsersMove is (40 * 0.6) = 24, bandwidth is 5000 which resolves to a value of 2, therefore, targetCellStepSize is 2 for both target cells.
  For target cell 054444_1_2: calculated usersToMove > Bandwidth table value
  usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (12 / 4) = 3
  Since usersToMove is greater than targetCellStepSize, the numUserMove is 3.
  For target cell 054444_1_3: calculated usersToMove < Bandwidth table value
  usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (8 / 4) = 2
  Since usersToMove is less or equal to targetCellStepSize, the final value of users to be moved to this target cell is the targetCellStepSize which is 2.
  No redistribution because totalNumOfUsersToMove < MaxSourceUsersMove.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_9_Numeric_Step_Size_And_Distribute_Users     |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 40                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.5               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 8                 | 054444_1_3 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_3 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 3               |
      | 054444_1_3    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 5               |




  Scenario: TC10 - Large step size, Target cells > 1, Target Step Sizes > Max Source User Move
  MaxSourceUsersMove is (20 * 0.6) = 12, bandwidth is 20000 which resolves to a value of 10, therefore, targetCellStepSize is 10 for both target cells.
  For target cell 054444_1_2: calculated usersToMove > Bandwidth table value
  usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (80 / 4) = 20
  Since usersToMove is greater than targetCellStepSize, value of numUsersToMove is 12.
  For target cell 054444_1_3: calculated usersToMove < Bandwidth table value
  usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (6 / 4) = round(1.5) = 2
  Since usersToMove is not greater than targetCellStepSize the numUsersToMove value gets set to the value of targetCellStepSize which is 10.

  However, since we have more than one target cell, it proceeds to the logic where it redistributes the users to move
  based on the numUsersToMove ratio, therefore the final number of users to move to target cell 054444_1_2 becomes 6 and for 054444_1_3 6.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_10_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 20                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.6               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 80                | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 6                 | 054444_1_3 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_3 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_3 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 8               |
      | 054444_1_3    | 1               | 4               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 12              |




  Scenario: TC11 - Small step size, Target cells = 1 and Target Step Sizes > Max Source User Move and Max Source User Move >= 0.5 and < 1
  MaxSourceUsersMove is (7 * 0.1) = 0.7, bandwidth is 5000 which resolves to 2. Therefore, targetCellStepSize is 2
  and numUsersToMove becomes 1 since step size is 'small'.

    Given Create Default Optimization Cells
      | 10032021_1 |
      | 10032021_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_11_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.545855356858811 | 10032021_1 |
      | KPI      | p_failing_r_mbps                  | 0.9               | 10032021_1 |
      | KPI      | connected_users                   | 7                 | 10032021_1 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 10032021_1 |
      | KPI      | target_cell_capacity              | 1000              | 10032021_1 |
      | SETTING  | optimization_speed                | normal            | 10032021_1 |
      | KPI      | goal_function_resource_efficiency | 0.97165574442653  | 10032021_2 |
      | KPI      | p_failing_r_mbps                  | 0.8               | 10032021_2 |
      | KPI      | max_connected_users_daily         | 20                | 10032021_2 |
      | KPI      | target_cell_capacity              | 12                | 10032021_2 |
      | SETTING  | optimization_speed                | normal            | 10032021_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 10032021_2    | 1               | 1               |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 10032021_1    | 1               | 1               |




  Scenario: TC12 - Small step size, Target cells = 1 and Target Step Sizes < Max Source User Move and Max Source User Move < 5.5
  MaxSourceUsersMove is (7 * 0.7) = 4.9, bandwidth is 5000 which resolves to 2. Therefore, targetCellStepSize is 2
  and numUsersToMove becomes 2 since step size is 'small'.

    Given Create Default Optimization Cells
      | 10032021_1 |
      | 10032021_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_12_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.545855356858811 | 10032021_1 |
      | KPI      | p_failing_r_mbps                  | 0.9               | 10032021_1 |
      | KPI      | connected_users                   | 7                 | 10032021_1 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 10032021_1 |
      | KPI      | target_cell_capacity              | 1000              | 10032021_1 |
      | SETTING  | optimization_speed                | normal            | 10032021_1 |
      | KPI      | goal_function_resource_efficiency | 0.97165574442653  | 10032021_2 |
      | KPI      | p_failing_r_mbps                  | 0.2               | 10032021_2 |
      | KPI      | max_connected_users_daily         | 20                | 10032021_2 |
      | KPI      | target_cell_capacity              | 12                | 10032021_2 |
      | SETTING  | optimization_speed                | normal            | 10032021_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 10032021_2    | 1               | 2               |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 10032021_1    | 1               | 2               |




  Scenario: TC13 - Small step size, Target cells = 1 and Target Step Sizes > Max Source User Move and Max Source User Move > 4.5
  MaxSourceUsersMove is (39 * 0.2) = 7.8, bandwidth is 20000 which resolves to 10. Therefore, targetCellStepSize is 10
  and numUsersToMove becomes round(7.8)= 8 since step size is 'small'.

    Given Create Default Optimization Cells
      | 10032021_1 |
      | 10032021_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_13_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType     | dataName                          | dataValue         | fdn        |
      | KPI          | goal_function_resource_efficiency | 0.545855356858811 | 10032021_1 |
      | KPI          | p_failing_r_mbps                  | 0.9               | 10032021_1 |
      | KPI          | connected_users                   | 39                | 10032021_1 |
      | KPI          | unhappy_users                     | 0.645855356858811 | 10032021_1 |
      | KPI          | target_cell_capacity              | 1000              | 10032021_1 |
      | SETTING      | optimization_speed                | normal            | 10032021_1 |
      | CM_ATTRIBUTE | bandwidth                         | 20000             | 10032021_1 |
      | KPI          | goal_function_resource_efficiency | 0.97165574442653  | 10032021_2 |
      | KPI          | p_failing_r_mbps                  | 0.7               | 10032021_2 |
      | KPI          | max_connected_users_daily         | 20                | 10032021_2 |
      | KPI          | target_cell_capacity              | 12                | 10032021_2 |
      | SETTING      | optimization_speed                | normal            | 10032021_2 |
      | CM_ATTRIBUTE | bandwidth                         | 20000             | 10032021_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 10032021_2    | 1               | 8               |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 10032021_1    | 1               | 8               |




  Scenario: TC14 - Large step size, optimization_speed = fast, Target cells = 1, calculated userToMove > Bandwidth table value and Target Step Size < Max Source User Move
  MaxSourceUsersMove is (40 * 0.4) = 16, bandwidth is 5000 which resolves to 2 so targetCellStepSize is 2.
  Since usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (12 / 2) = 6
  greater than value of targetCellStepSize, the total number of users that are going to be moved is 6.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_14_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 40                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | fast              | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.5               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 31                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_2 |
      | SETTING  | optimization_speed                            | fast              | 054444_1_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 6               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 6               |


  Scenario: TC15 - Mix of step sizes, Target cells > 1 and, 054444_1_2 and 054444_1_3 have large step sizes and 054444_1_4 has small step size.
  MaxSourceUsersMove is (42 * 0.6) = 25.2. Bandwidth is 20000 which resolves to 10, therefore, targetCellStepSize is 10.
  For target cell 054444_1_2: usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (12 / 2) = 6
  Since usersToMove is less than targetCellStepSize, the value of numUsersToMove is 10.
  For target cell 054444_1_3: usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (10 / 2) = 5
  Since usersToMove is less than targetCellStepSize, the value of numUsersToMove is 10.
  For target cell 054444_1_4 usersToMove will be 10.

  Total number of users (30) is greater than max source user move, so redistribute users based on their numUsersToMove ratio.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |
      | 054444_1_4 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_15_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 42                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.4               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 26                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 26                | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 10                | 054444_1_3 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_3 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_3 |
      | KPI      | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_4 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI      | p_failing_r_mbps                              | 0.8               | 054444_1_4 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_4 |
      | KPI      | target_cell_capacity                          | 5                 | 054444_1_4 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 8               |
      | 054444_1_3    | 1               | 8               |
      | 054444_1_4    | 1               | 8               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 24              |


  Scenario: TC16 - Mix of step sizes, Target cells > 1 and, 054444_1_2 and 054444_1_3 have small step sizes and 054444_1_4 has large step size.
  MaxSourceUsersMove is (42 * 0.6) = 25.2. Bandwidth is 20000 which resolves to 10, therefore, targetCellStepSize is 10.
  For target cell 054444_1_2: usersToMove will be 10.
  For target cell 054444_1_3: usersToMove will be 10.
  For target cell 054444_1_4 usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (24 / 2) = 12
  Since usersToMove is greater than targetCellStepSize, the value of numUsersToMove is 12.

  Total number of users (32) is greater than max source user move, so redistribute users based on their numUsersToMove ratio.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |
      | 054444_1_4 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_16_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 42                | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | fast              | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.8               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 10                | 054444_1_2 |
      | SETTING  | optimization_speed                            | fast              | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.8               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 5                 | 054444_1_3 |
      | SETTING  | optimization_speed                            | fast              | 054444_1_3 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_3 |
      | KPI      | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_4 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI      | p_failing_r_mbps                              | 0.3               | 054444_1_4 |
      | KPI      | max_connected_users_daily                     | 40                | 054444_1_4 |
      | KPI      | target_cell_capacity                          | 24                | 054444_1_4 |
      | SETTING  | optimization_speed                            | fast              | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 8               |
      | 054444_1_3    | 1               | 8               |
      | 054444_1_4    | 1               | 9               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 25              |


  Scenario: TC17 - small step sizes, Target cells > 1
  MaxSourceUsersMove is (7 * 0.1) = 0.7. Bandwidth is 10000 which resolves to 4, therefore, targetCellStepSize is 4.
  For target cell 054444_1_2: usersToMove will be 4
  For target cell 054444_1_3: usersToMove will be 4
  For target cell 054444_1_4: usersToMove will be 4

  Total number of users (3) is greater than max source user move, so redistribute users based on their numUsersToMove ratio.The output contains a single target cell
  as after redistribution, the totalUsersToMove is 0 and all users go to one target cell with highest numUsersToMove before redistribution.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |
      | 054444_1_4 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_17_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.9               | 054444_1_1 |
      | KPI      | connected_users                               | 7                 | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.8               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 5                 | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 10                | 054444_1_2 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                 | 10000             | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.8               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 5                 | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 5                 | 054444_1_3 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_3 |
      | CM_ATTRIBUTE | bandwidth                                 | 10000             | 054444_1_3 |
      | KPI      | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_4 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI      | p_failing_r_mbps                              | 0.8               | 054444_1_4 |
      | KPI      | max_connected_users_daily                     | 6                 | 054444_1_4 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_4 |
      | SETTING  | optimization_speed                            | normal            | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                 | 10000             | 054444_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 1               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 1               |

  Scenario: TC18 - large step sizes, Target cells > 1
  MaxSourceUsersMove is (7 * 0.1) = 0.7. Bandwidth is 20000 which resolves to 10, therefore, targetCellStepSize is 10.
  For target cell 054444_1_2: usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (10 / 6) = 2
  Since usersToMove is less than targetCellStepSize,so value of numUsersToMove is 10.
  For target cell 054444_1_3:  usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (12 / 6) = 2
  Since usersToMove is less than targetCellStepSize,so value of numUsersToMove is 10.
  For target cell 054444_1_4:  usersToMove is (target_cell_capacity / corresponding optimization_speed value from the OSF table) = (10 / 6) = 2
  Since usersToMove is less than targetCellStepSize,so value of numUsersToMove is 10.

  Total number of users (3) is greater than max source user move, so redistribute users based on their numUsersToMove ratio. The output contains a single target cell
  as after redistribution, the totalUsersToMove is 0 and all users go to one target cell with highest numUsersToMove before redistribution.

    Given Create Default Optimization Cells
      | 054444_1_1 |
      | 054444_1_2 |
      | 054444_1_3 |
      | 054444_1_4 |

    And Policy Input Event
      | sectorId | executionId |
      | 032021   | TC_18_Numeric_Step_Size_And_Distribute_Users    |

    And Set Optimization Cells Data
      | dataType | dataName                                      | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency             | 0.224184080627752 | 054444_1_1 |
      | KPI      | p_failing_r_mbps                              | 0.7               | 054444_1_1 |
      | KPI      | connected_users                               | 7                 | 054444_1_1 |
      | KPI      | unhappy_users                                 | 0.592511239070919 | 054444_1_1 |
      | KPI      | num_values_used_for_mcu_cdf_calculation_daily | 100               | 054444_1_1 |
      | KPI      | target_cell_capacity                          | 1000              | 054444_1_1 |
      | SETTING  | optimization_speed                            | slow              | 054444_1_1 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_1 |
      | KPI      | goal_function_resource_efficiency             | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_2 |
      | KPI      | p_failing_r_mbps                              | 0.6               | 054444_1_2 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_2 |
      | KPI      | target_cell_capacity                          | 10                | 054444_1_2 |
      | SETTING  | optimization_speed                            | slow              | 054444_1_2 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency             | 0.734909938121602 | 054444_1_3 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_3 |
      | KPI      | p_failing_r_mbps                              | 0.6               | 054444_1_3 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_3 |
      | KPI      | target_cell_capacity                          | 12                | 054444_1_3 |
      | SETTING  | optimization_speed                            | slow              | 054444_1_3 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_3 |
      | KPI      | goal_function_resource_efficiency             | 0.758874105661037 | 054444_1_4 |
      | KPI      | unhappy_users                                 | 13.4191211495669  | 054444_1_4 |
      | KPI      | p_failing_r_mbps                              | 0.6               | 054444_1_4 |
      | KPI      | max_connected_users_daily                     | 20                | 054444_1_4 |
      | KPI      | target_cell_capacity                          | 10                | 054444_1_4 |
      | SETTING  | optimization_speed                            | slow              | 054444_1_4 |
      | CM_ATTRIBUTE | bandwidth                                 | 20000             | 054444_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_2    | 1               | 1               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_1_1    | 1               | 1               |
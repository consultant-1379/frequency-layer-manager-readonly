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

Feature: FLM_CoverageBalanceRatioDistance_Screener
  The purpose of this policy state is to screen out target cells where the ratio
  Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance is less than
  the setting "target_source_coverage_balance_ratio_threshold" and the
  ST Samples Overlap % < ST Samples Overlap threshold.

  The ratio of Target Coverage Balance Distance/Source Coverage Balance Distance is compared the
  Target/Source Coverage Balance Ratio threshold.
  If the ratio is greater than the threshold then the source and target cells proceed for optimization.
  If the ratio is less than the threshold then the UE TA distribution KPIs need to be considered to determine
  if enough of the users in the Source cell have overlapping coverage from the Target cell.
  The steps are
  1.	Compare Target distance_q4 with Source Quarter distances.
  2.	Identify which Source distance TA Quarter the target falls in.
  3.	Interpolate the percentage of samples from this TA Quarter and add the percentage of samples from the previous TA Quarters.
  This is the "ST Samples Overlap %”.
  4.	IF ST Samples Overlap % < ST Samples Overlap threshold THEN exclude the target cell.

  Input to this state:
  Ranked list of potential Source cells and their possible target cells

  Output from this state:
  The output from this state will be the input ranked list of potential source cells and their associated target cells
  with the target cells not meeting the thresholds screened out.
  If all target cells for all potential source cells are screened out then the sector is removed from optimization
  and an empty LBQ is returned.

  The kpi used:
  coverage_balance_ratio_distance
  distance_q1
  distance_q2
  distance_q3
  distance_q4

  The settings used:
  target_source_coverage_balance_ratio_threshold
  ST Samples Overlap threshold

  Scenario: TC1 - Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio
  less than threshold for 1 target cell in the sector.
  Target Coverage Area less than Source Coverage Area 'distance_q1' and
  ST Samples Overlap % > ST Samples Overlap threshold.
  Target Cells Coverage Area have sufficient overlap with Source Cells Coverage area.
  Source and Targets proceed to next state for optimization.

  The Sector in this test had 1 possible source cells,
  - cell 054902_2_2 with potential target cells 054902_1_4 and 054902_1_9

  Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio is less than threshold for
  Cell 054902_1_4.
  Target Coverage Area 'distance_q4' falls in Source Coverage Area 'distance_q1' and
  ST Samples Overlap % > ST Samples Overlap threshold.

  Source and All target Cells proceed to next state for optimization.

    Given Create Default Optimization Cells
      | 054902_2   |
      | 054902_2_2 |
      | 054902_1_4 |
      | 054902_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_1_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.744184080627752   | 054902_2   |
      | KPI      | unhappy_users                     | 90.592511239070919  | 054902_2   |
      | KPI      | goal_function_resource_efficiency | 0.447884075944469   | 054902_2_2 |
      | KPI      | unhappy_users                     | 12.6403873501825    | 054902_2_2 |
      | KPI      | distance_q1                       | 1186.98666666667    | 054902_2_2 |
      | KPI      | ue_percentage_q1                  | 81.8897637795276    | 054902_2_2 |
      | KPI      | goal_function_resource_efficiency | 0.980834728689175   | 054902_1_4 |
      | KPI      | unhappy_users                     | 1.53093251968166    | 054902_1_4 |
      | KPI      | coverage_balance_ratio_distance   | 60.6123420083598532 | 054902_1_4 |
      | KPI      | distance_q4                       | 1051.04             | 054902_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.978888304995677   | 054902_1_9 |
      | KPI      | unhappy_users                     | 0.747764508333663   | 054902_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.9                 | 054902_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054902_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.2                 | 054902_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054902_1_4    | 1               | 2               |
      | 054902_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054902_2_2    | 1               | 4               |

  Scenario: TC2 - Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio
  less than threshold for some target cells in the sector.
  Target Coverage Area less than Source Coverage Area 'distance_q1' and
  ST Samples Overlap % < ST Samples Overlap threshold.

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_2_2
  - source cell 054448_1 with target cell 054448_2_2
  and
  ST Samples Overlap % < ST Samples Overlap threshold for target cells
  - target cell 054448_2_2 for source cell 054448_3

  Target cell 054448_2_2 is excluded for source cell 054448_3.

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_2_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                  | 100                 | 054448_1   |
      | KPI      | distance_q1                       | 107                 | 054448_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 3.33333333333333    | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 81.8897637795276    | 054448_3   |
      | KPI      | distance_q1                       | 110                 | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                       | 109.99              | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 81                  | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.7                 | 054448_3   |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054448_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_3      | 1               | 2               |

  Scenario: TC3 - Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio
  less than threshold for some target cells in the sector.
  Target Coverage Area less than Source Coverage Area 'distance_q1' and
  ST Samples Overlap % < ST Samples Overlap threshold.
  All target cells are excluded for a Source cell, the source cell is excluded.

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cells, 054448_1_9, 054448_2_2
  - source cell 054448_2 with target cell 054448_1_9
  and
  ST Samples Overlap % < ST Samples Overlap threshold for target cells
  - target cell 054448_2_2 for source cell 054448_3
  - target cell 054448_1_9 for source cell 054448_3
  - target cell 054448_1_9 for source cell 054448_2

  Source cells 054448_2 and 054448_3 are excluded as all targets are excluded.

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_3_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                  | 100                 | 054448_1   |
      | KPI      | distance_q1                       | 30.416              | 054448_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 33.33333333333333   | 054448_2   |
      | KPI      | distance_q1                       | 78                  | 054448_2   |
      | KPI      | ue_percentage_q1                  | 34.1463414634146    | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 40.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 81.8897637795276    | 054448_3   |
      | KPI      | distance_q1                       | 110                 | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                       | 23.40               | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 28.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 81                  | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.9                 | 054448_1   |
      | KPI      | p_failing_r_mbps                  | 0.2                 | 054448_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054448_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_1_9    | 1               | 2               |
      | 054448_2_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_1      | 1               | 4               |


  Scenario: TC4 - Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio
  less than threshold for 1 target cell in the sector.
  Target Coverage Area less than Source Coverage Area 'distance_q4' and
  ST Samples Overlap %> ST Samples Overlap threshold.
  Target Cells Coverage Area have sufficient overlap with Source Cells Coverage area.
  Source and Targets proceed to next state for optimization.

  The Sector in this test had 1 possible source cells,
  - cell 054902_2_2 with potential target cells 054902_1_4 and 054902_1_9

  Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio is less than threshold for
  Cell 054902_1_4.
  Target Coverage Area 'distance_q4' falls in Source Coverage Area 'distance_q4' and
  ST Samples Overlap % > ST Samples Overlap threshold.

  Source and All target Cells proceed to next state for optimization.

    Given Create Default Optimization Cells
      | 054902_2   |
      | 054902_2_2 |
      | 054902_1_4 |
      | 054902_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_4_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.744184080627752   | 054902_2   |
      | KPI      | unhappy_users                     | 90.592511239070919  | 054902_2   |
      | KPI      | goal_function_resource_efficiency | 0.447884075944469   | 054902_2_2 |
      | KPI      | unhappy_users                     | 89.592511239070919  | 054902_2_2 |
      | KPI      | distance_q1                       | 324.8               | 054902_2_2 |
      | KPI      | distance_q2                       | 649.6               | 054902_2_2 |
      | KPI      | distance_q3                       | 974.400000000001    | 054902_2_2 |
      | KPI      | distance_q4                       | 1299.2              | 054902_2_2 |
      | KPI      | ue_percentage_q1                  | 64.2042621722846    | 054902_2_2 |
      | KPI      | ue_percentage_q2                  | 2.8464419475655     | 054902_2_2 |
      | KPI      | ue_percentage_q3                  | 0.0                 | 054902_2_2 |
      | KPI      | ue_percentage_q4                  | 9.36329588014981    | 054902_2_2 |
      | KPI      | goal_function_resource_efficiency | 0.980834728689175   | 054902_1_4 |
      | KPI      | unhappy_users                     | 13.4191211495669    | 054902_1_4 |
      | KPI      | coverage_balance_ratio_distance   | 60.6123420083598532 | 054902_1_4 |
      | KPI      | distance_q4                       | 1196.8              | 054902_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.978888304995677   | 054902_1_9 |
      | KPI      | unhappy_users                     | 20.743089638662777  | 054902_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.9                 | 054902_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.2                 | 054902_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054902_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054902_1_4    | 1               | 2               |
      | 054902_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054902_2_2    | 1               | 4               |


  Scenario: TC5 - Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio
  less than threshold for some target cells in the sector.
  Target Coverage Area less than Source Coverage Area 'distance_q4' and
  ST Samples Overlap % < ST Samples Overlap threshold.

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_2_2
  - source cell 054448_1 with target cell 054448_2_2
  and
  ST Samples Overlap % < ST Samples Overlap threshold for target cells
  - target cell 054448_2_2 for source cell 054448_3

  Target cell 054448_2_2 is excluded for source cell 054448_3.

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_5_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                  | 6                   | 054448_1   |
      | KPI      | ue_percentage_q2                  | 26                  | 054448_1   |
      | KPI      | ue_percentage_q3                  | 62                  | 054448_1   |
      | KPI      | ue_percentage_q4                  | 6                   | 054448_1   |
      | KPI      | distance_q1                       | 156                 | 054448_1   |
      | KPI      | distance_q2                       | 312                 | 054448_1   |
      | KPI      | distance_q3                       | 468                 | 054448_1   |
      | KPI      | distance_q4                       | 624                 | 054448_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 3.33333333333333    | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 10.6382978723404    | 054448_3   |
      | KPI      | ue_percentage_q2                  | 3.54609929078014    | 054448_3   |
      | KPI      | ue_percentage_q3                  | 26.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                  | 58.8652482269504    | 054448_3   |
      | KPI      | distance_q1                       | 182                 | 054448_3   |
      | KPI      | distance_q2                       | 364                 | 054448_3   |
      | KPI      | distance_q3                       | 546                 | 054448_3   |
      | KPI      | distance_q4                       | 728                 | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                       | 109.99              | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 600                 | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054448_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.7                 | 054448_3   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_3      | 1               | 2               |


  Scenario: TC6 - Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio
  less than threshold for some target cells in the sector.
  Target Coverage Area less than Source Coverage Area 'distance_q4' and
  ST Samples Overlap % < ST Samples Overlap threshold.
  All target cells are excluded for a Source cell, the source cell is excluded.

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_1_9
  - source cell 054448_3 with target cell 054448_2_2
  - source cell 054448_1 with target cell 054448_2_2
  and
  ST Samples Overlap % < ST Samples Overlap threshold for target cells
  - target cell 054448_1_9 for source cell 054448_3
  - target cell 054448_2_2 for source cell 054448_3
  - target cell 054448_2_2 for source cell 054448_1
  - target cell 054448_1_9 for source cell 054448_1

  Source cells 054448_1, 054448_3 are excluded as all target cells are excluded.
  Source Cell list returned is:
  1. cell 054448_2 with potential target cells 054448_1_9

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_6_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                  | 6                   | 054448_1   |
      | KPI      | ue_percentage_q2                  | 26                  | 054448_1   |
      | KPI      | ue_percentage_q3                  | 32                  | 054448_1   |
      | KPI      | ue_percentage_q4                  | 6                   | 054448_1   |
      | KPI      | distance_q1                       | 156                 | 054448_1   |
      | KPI      | distance_q2                       | 312                 | 054448_1   |
      | KPI      | distance_q3                       | 468                 | 054448_1   |
      | KPI      | distance_q4                       | 624                 | 054448_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 33.33333333333333   | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 10.6382978723404    | 054448_3   |
      | KPI      | ue_percentage_q2                  | 3.54609929078014    | 054448_3   |
      | KPI      | ue_percentage_q3                  | 26.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                  | 58.8652482269504    | 054448_3   |
      | KPI      | distance_q1                       | 182                 | 054448_3   |
      | KPI      | distance_q2                       | 364                 | 054448_3   |
      | KPI      | distance_q3                       | 546                 | 054448_3   |
      | KPI      | distance_q4                       | 728                 | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                       | 547                 | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 600                 | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054448_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.7                 | 054448_2   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_2      | 1               | 2               |


  Scenario: TC7 - Target distances have a value of zero, distance_q1, distance_q2, distance_q3, distance_q4 are all equal to 0.
  All target cells are excluded for a Source cell, the source cell is excluded.

  The Sector in this test has 2 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_1_9, 054448_2_2
  - source cell 054448_2 with target cell 054448_1_9
  and
  Target cell distances are all zero for target cell 054448_1_9

  Source Cell list returned is:
  - cell 054448_3 with potential target cells 054448_2_2

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_7_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 33.33333333333333   | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 10.6382978723404    | 054448_3   |
      | KPI      | ue_percentage_q2                  | 33.54609929078014   | 054448_3   |
      | KPI      | ue_percentage_q3                  | 26.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                  | 58.8652482269504    | 054448_3   |
      | KPI      | distance_q1                       | 182                 | 054448_3   |
      | KPI      | distance_q2                       | 364                 | 054448_3   |
      | KPI      | distance_q3                       | 546                 | 054448_3   |
      | KPI      | distance_q4                       | 728                 | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q1                       | 0                   | 054448_1_9 |
      | KPI      | distance_q2                       | 0                   | 054448_1_9 |
      | KPI      | distance_q3                       | 0                   | 054448_1_9 |
      | KPI      | distance_q4                       | 0                   | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 600                 | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.7                 | 054448_3   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_2_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_3      | 1               | 2               |

  Scenario: TC7A - Target distances have a value of zero, distance_q1, distance_q2, distance_q3,distance_q4 are all equal to 0.
  All target cells are excluded for a Source cell, the source cell is excluded.

  The Sector in this test has 2 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_1_9, 054448_2_2
  - source cell 054448_2 with target cell 054448_1_9
  and
  Target cell distances are all zero for all target cells .

  All Source Cells are excluded so Sector is excluded.

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_7A_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 33.33333333333333   | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 10.6382978723404    | 054448_3   |
      | KPI      | ue_percentage_q2                  | 33.54609929078014   | 054448_3   |
      | KPI      | ue_percentage_q3                  | 26.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                  | 58.8652482269504    | 054448_3   |
      | KPI      | distance_q1                       | 182                 | 054448_3   |
      | KPI      | distance_q2                       | 364                 | 054448_3   |
      | KPI      | distance_q3                       | 546                 | 054448_3   |
      | KPI      | distance_q4                       | 728                 | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q1                       | 0                   | 054448_1_9 |
      | KPI      | distance_q2                       | 0                   | 054448_1_9 |
      | KPI      | distance_q3                       | 0                   | 054448_1_9 |
      | KPI      | distance_q4                       | 0                   | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q1                       | 0                   | 054448_2_2 |
      | KPI      | distance_q2                       | 0                   | 054448_2_2 |
      | KPI      | distance_q3                       | 0                   | 054448_2_2 |
      | KPI      | distance_q4                       | 0                   | 054448_2_2 |


    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC8 - Source Cell distances distance_q1, distance_q2, distance_q3, distance_q4 are all equal to 0.
  The source cell is excluded.

  The Sector in this test has 2 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_1_9, 054448_2_2
  - source cell 054448_2 with target cell 054448_1_9
  and
  Source cell distances are all zero for source cell 054448_3

  Source Cell list returned is:
  - cell 054448_2 with potential target cells 054448_1_9

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_8_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 33.33333333333333   | 054448_2   |
      | KPI      | ue_percentage_q1                  | 10.6382978723404    | 054448_2   |
      | KPI      | ue_percentage_q2                  | 3.54609929078014    | 054448_2   |
      | KPI      | ue_percentage_q3                  | 26.9503546099291    | 054448_2   |
      | KPI      | ue_percentage_q4                  | 58.8652482269504    | 054448_2   |
      | KPI      | distance_q1                       | 152.5               | 054448_2   |
      | KPI      | distance_q2                       | 305                 | 054448_2   |
      | KPI      | distance_q3                       | 457.5               | 054448_2   |
      | KPI      | distance_q4                       | 610                 | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 0                   | 054448_3   |
      | KPI      | ue_percentage_q2                  | 0                   | 054448_3   |
      | KPI      | ue_percentage_q3                  | 0                   | 054448_3   |
      | KPI      | ue_percentage_q4                  | 0                   | 054448_3   |
      | KPI      | distance_q1                       | 0                   | 054448_3   |
      | KPI      | distance_q2                       | 0                   | 054448_3   |
      | KPI      | distance_q3                       | 0                   | 054448_3   |
      | KPI      | distance_q4                       | 0                   | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                       | 609                 | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 600                 | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054448_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.7                 | 054448_2   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_2      | 1               | 2               |


  Scenario: TC8A - Source Cell distances distance_q1, distance_q2, distance_q3,distance_q4 are all equal to 0.
  All source cells are excluded.

  The Sector in this test has 2 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_1_9, 054448_2_2
  - source cell 054448_2 with target cell 054448_1_9
  and
  Source cell distances are all zero for all source cells.
  Sector is excluded.

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_8A_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 33.33333333333333   | 054448_2   |
      | KPI      | ue_percentage_q1                  | 0                   | 054448_2   |
      | KPI      | ue_percentage_q2                  | 0                   | 054448_2   |
      | KPI      | ue_percentage_q3                  | 0                   | 054448_2   |
      | KPI      | ue_percentage_q4                  | 0                   | 054448_2   |
      | KPI      | distance_q1                       | 0                   | 054448_2   |
      | KPI      | distance_q2                       | 0                   | 054448_2   |
      | KPI      | distance_q3                       | 0                   | 054448_2   |
      | KPI      | distance_q4                       | 0                   | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 0                   | 054448_3   |
      | KPI      | ue_percentage_q2                  | 0                   | 054448_3   |
      | KPI      | ue_percentage_q3                  | 0                   | 054448_3   |
      | KPI      | ue_percentage_q4                  | 0                   | 054448_3   |
      | KPI      | distance_q1                       | 0                   | 054448_3   |
      | KPI      | distance_q2                       | 0                   | 054448_3   |
      | KPI      | distance_q3                       | 0                   | 054448_3   |
      | KPI      | distance_q4                       | 0                   | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                       | 609                 | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 600                 | 054448_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC9 - Sector is excluded as not enough overlapping coverage for all Source Cells and their target cells.

  Target Coverage Area less than Source Coverage Area 'distance_q4' and
  ST Samples Overlap % < ST Samples Overlap threshold.
  All target cells are excluded for a Source cell, the source cell is excluded.
  All Source cells are excluded, sector is excluded and an empty LBQ returned

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for all target cells.
  ST Samples Overlap % < ST Samples Overlap threshold for all target cells

  All Source cells are excluded so sector is excluded.

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_9_CoverageBalanceRatioDistance_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                  | 6                   | 054448_1   |
      | KPI      | ue_percentage_q2                  | 26                  | 054448_1   |
      | KPI      | ue_percentage_q3                  | 32                  | 054448_1   |
      | KPI      | ue_percentage_q4                  | 6                   | 054448_1   |
      | KPI      | distance_q1                       | 156                 | 054448_1   |
      | KPI      | distance_q2                       | 312                 | 054448_1   |
      | KPI      | distance_q3                       | 468                 | 054448_1   |
      | KPI      | distance_q4                       | 624                 | 054448_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 33.33333333333333   | 054448_2   |
      | KPI      | ue_percentage_q1                  | 6                   | 054448_2   |
      | KPI      | ue_percentage_q2                  | 26                  | 054448_2   |
      | KPI      | ue_percentage_q3                  | 32                  | 054448_2   |
      | KPI      | ue_percentage_q4                  | 6                   | 054448_2   |
      | KPI      | distance_q1                       | 156                 | 054448_2   |
      | KPI      | distance_q2                       | 312                 | 054448_2   |
      | KPI      | distance_q3                       | 468                 | 054448_2   |
      | KPI      | distance_q4                       | 624                 | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 10.6382978723404    | 054448_3   |
      | KPI      | ue_percentage_q2                  | 3.54609929078014    | 054448_3   |
      | KPI      | ue_percentage_q3                  | 10.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                  | 48.8652482269504    | 054448_3   |
      | KPI      | distance_q1                       | 157                 | 054448_3   |
      | KPI      | distance_q2                       | 154.9               | 054448_3   |
      | KPI      | distance_q3                       | 471                 | 054448_3   |
      | KPI      | distance_q4                       | 628                 | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                       | 590                 | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 600                 | 054448_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC10 - target cell is correctly excluded when there are different thresholds set for the Cells
  - Targets Cells only excluded.
  Different thresholds on the different Source Cells
  If there are different thresholds on the target and Source cell the setting on the source cell is applied.

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_1_9
  - source cell 054448_3 with target cell 054448_2_2
  - source cell 054448_1 with target cell 054448_2_2
  and
  ST Samples Overlap % < ST Samples Overlap threshold for target cells
  - target cell 054448_2_2 for source cell 054448_3
  - target cell 054448_2_2 for source cell 054448_1

  Target cell 054448_2_2 is excluded for source cell 054448_3.
  Target cell 054448_2_2 is excluded for source cell 054448_1.

  Source Cell list returned is:
  1. cell 054448_3 with potential target cells 054448_1_9
  2. cell 054448_2 with potential target cells 054448_1_9
  2. cell 054448_1 with potential target cells 054448_1_9

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_10_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency       | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                           | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance         | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                        | 10                  | 054448_1   |
      | KPI      | ue_percentage_q2                        | 26                  | 054448_1   |
      | KPI      | ue_percentage_q3                        | 32                  | 054448_1   |
      | KPI      | ue_percentage_q4                        | 18                  | 054448_1   |
      | KPI      | distance_q1                             | 182                 | 054448_1   |
      | KPI      | distance_q2                             | 364                 | 054448_1   |
      | KPI      | distance_q3                             | 546                 | 054448_1   |
      | KPI      | distance_q4                             | 728                 | 054448_1   |
      | SETTING  | source_target_samples_overlap_threshold | 82                  | 054448_1   |
      | KPI      | goal_function_resource_efficiency       | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                           | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance         | 3.33333333333333    | 054448_2   |
      | SETTING  | source_target_samples_overlap_threshold | 55                  | 054448_2   |
      | KPI      | goal_function_resource_efficiency       | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                           | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance         | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                        | 1.6382978723404     | 054448_3   |
      | KPI      | ue_percentage_q2                        | 58.8652482269504    | 054448_3   |
      | KPI      | ue_percentage_q3                        | 16.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                        | 10                  | 054448_3   |
      | KPI      | distance_q1                             | 182                 | 054448_3   |
      | KPI      | distance_q2                             | 364                 | 054448_3   |
      | KPI      | distance_q3                             | 546                 | 054448_3   |
      | KPI      | distance_q4                             | 728                 | 054448_3   |
      | SETTING  | source_target_samples_overlap_threshold | 85                  | 054448_3   |
      | KPI      | goal_function_resource_efficiency       | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                           | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance         | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                             | 700                 | 054448_1_9 |
      | SETTING  | source_target_samples_overlap_threshold | 50                  | 054448_2_2 |
      | KPI      | goal_function_resource_efficiency       | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                           | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance         | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                             | 680                 | 054448_2_2 |
      | SETTING  | source_target_samples_overlap_threshold | 80                  | 054448_2_2 |
      | KPI      | p_failing_r_mbps                        | 0.3                 | 054448_1_9 |
      | KPI      | p_failing_r_mbps                        | 0.7                 | 054448_3   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_3      | 1               | 2               |

  Scenario: TC11 - target cell is correctly excluded when there are different thresholds set for the different Source Cells
  Source Cell is excluded.
  Different thresholds on the different Source Cells
  If there are different thresholds on the target and Source cell the setting on the source cell is applied.

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_1_9
  - source cell 054448_3 with target cell 054448_2_2
  - source cell 054448_1 with target cell 054448_2_2
  and
  ST Samples Overlap % < ST Samples Overlap threshold for target cells
  - target cell 054448_1_9 for source cell 054448_3
  - target cell 054448_2_2 for source cell 054448_3
  - target cell 054448_2_2 for source cell 054448_1

  Source cell 054448_3 is excluded as all targets are excluded.

  Source Cell list returned is:
  1. cell 054448_2 with potential target cells 054448_1_9
  2. cell 054448_1 with potential target cells 054448_1_9

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_11_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency       | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                           | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance         | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                        | 10                  | 054448_1   |
      | KPI      | ue_percentage_q2                        | 26                  | 054448_1   |
      | KPI      | ue_percentage_q3                        | 32                  | 054448_1   |
      | KPI      | ue_percentage_q4                        | 18                  | 054448_1   |
      | KPI      | distance_q1                             | 182                 | 054448_1   |
      | KPI      | distance_q2                             | 364                 | 054448_1   |
      | KPI      | distance_q3                             | 546                 | 054448_1   |
      | KPI      | distance_q4                             | 728                 | 054448_1   |
      | SETTING  | source_target_samples_overlap_threshold | 82                  | 054448_1   |
      | KPI      | goal_function_resource_efficiency       | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                           | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance         | 3.33333333333333    | 054448_2   |
      | SETTING  | source_target_samples_overlap_threshold | 55                  | 054448_2   |
      | KPI      | goal_function_resource_efficiency       | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                           | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance         | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                        | 1.6382978723404     | 054448_3   |
      | KPI      | ue_percentage_q2                        | 58.8652482269504    | 054448_3   |
      | KPI      | ue_percentage_q3                        | 16.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                        | 10                  | 054448_3   |
      | KPI      | distance_q1                             | 182                 | 054448_3   |
      | KPI      | distance_q2                             | 364                 | 054448_3   |
      | KPI      | distance_q3                             | 546                 | 054448_3   |
      | KPI      | distance_q4                             | 728                 | 054448_3   |
      | SETTING  | source_target_samples_overlap_threshold | 86                  | 054448_3   |
      | KPI      | goal_function_resource_efficiency       | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                           | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance         | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                             | 700                 | 054448_1_9 |
      | SETTING  | source_target_samples_overlap_threshold | 50                  | 054448_2_2 |
      | KPI      | goal_function_resource_efficiency       | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                           | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance         | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                             | 680                 | 054448_2_2 |
      | SETTING  | source_target_samples_overlap_threshold | 80                  | 054448_2_2 |
      | KPI      | p_failing_r_mbps                        | 0.3                 | 054448_1_9 |
      | KPI      | p_failing_r_mbps                        | 0.7                 | 054448_2   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_2      | 1               | 2               |


  Scenario: TC12 - target cell is correctly excluded when there are different thresholds set for the different Source Cells
  All Source Cells excluded so Sector is excluded
  Different thresholds on the different Source Cells
  If there are different thresholds on the target and Source cell the setting on the source cell is applied.

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_1_9
  - source cell 054448_3 with target cell 054448_2_2
  - source cell 054448_1 with target cell 054448_2_2
  and
  ST Samples Overlap % < ST Samples Overlap threshold for all target cells
  for all Source cells

  All Source Cells are excluded so Sector is excluded.

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_12_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency       | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                           | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance         | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                        | 10                  | 054448_1   |
      | KPI      | ue_percentage_q2                        | 26                  | 054448_1   |
      | KPI      | ue_percentage_q3                        | 32                  | 054448_1   |
      | KPI      | ue_percentage_q4                        | 18                  | 054448_1   |
      | KPI      | distance_q1                             | 182                 | 054448_1   |
      | KPI      | distance_q2                             | 364                 | 054448_1   |
      | KPI      | distance_q3                             | 546                 | 054448_1   |
      | KPI      | distance_q4                             | 728                 | 054448_1   |
      | SETTING  | source_target_samples_overlap_threshold | 84                  | 054448_1   |
      | KPI      | goal_function_resource_efficiency       | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                           | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance         | 33.33333333333333   | 054448_2   |
      | KPI      | ue_percentage_q1                        | 10.6382978723404    | 054448_2   |
      | KPI      | ue_percentage_q2                        | 8.8652482269504     | 054448_2   |
      | KPI      | ue_percentage_q3                        | 16.9503546099291    | 054448_2   |
      | KPI      | ue_percentage_q4                        | 10                  | 054448_2   |
      | KPI      | distance_q1                             | 182                 | 054448_2   |
      | KPI      | distance_q2                             | 364                 | 054448_2   |
      | KPI      | distance_q3                             | 546                 | 054448_2   |
      | KPI      | distance_q4                             | 728                 | 054448_2   |
      | SETTING  | source_target_samples_overlap_threshold | 55                  | 054448_2   |
      | KPI      | goal_function_resource_efficiency       | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                           | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance         | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                        | 1.6382978723404     | 054448_3   |
      | KPI      | ue_percentage_q2                        | 58.8652482269504    | 054448_3   |
      | KPI      | ue_percentage_q3                        | 16.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                        | 10                  | 054448_3   |
      | KPI      | distance_q1                             | 182                 | 054448_3   |
      | KPI      | distance_q2                             | 364                 | 054448_3   |
      | KPI      | distance_q3                             | 546                 | 054448_3   |
      | KPI      | distance_q4                             | 728                 | 054448_3   |
      | SETTING  | source_target_samples_overlap_threshold | 86                  | 054448_3   |
      | KPI      | goal_function_resource_efficiency       | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                           | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance         | 13.4050611054374507 | 054448_1_9 |
      | KPI      | distance_q4                             | 700                 | 054448_1_9 |
      | SETTING  | source_target_samples_overlap_threshold | 50                  | 054448_2_2 |
      | KPI      | goal_function_resource_efficiency       | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                           | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance         | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                             | 680                 | 054448_2_2 |
      | SETTING  | source_target_samples_overlap_threshold | 80                  | 054448_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC13 - Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio
  less than threshold for some target cells in the sector.
  Distance_q4 falls outside Source cell distance_q4 for all target cells for one of the Source Cells.

  The Sector in this test has 3 possible source cells ranked as follows,
  1. cell 054448_3 with potential target cells 054448_1_9, 054448_2_2
  2. cell 054448_2 with potential target cells 054448_1_9
  3. cell 054448_1 with potential target cells 054448_1_9, 054448_2_2

  Target/Source coverage_balance_ratio_distance is less than the threshold for
  - source cell 054448_3 with target cell 054448_2_2
  - source cell 054448_1 with target cell 054448_2_2
  - source cell 054448_2 with target cell 054448_1_9
  and
  Distance_q4 falls outside distance_q4 for all the target cells identified for source cell 054448_3.

    Given Create Default Optimization Cells
      | 054448_2   |
      | 054448_3   |
      | 054448_1   |
      | 054448_1_9 |
      | 054448_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102600 | TC_13_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 054448_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 054448_1   |
      | KPI      | coverage_balance_ratio_distance   | 23.4050611054374507 | 054448_1   |
      | KPI      | ue_percentage_q1                  | 6                   | 054448_1   |
      | KPI      | ue_percentage_q2                  | 26                  | 054448_1   |
      | KPI      | ue_percentage_q3                  | 62                  | 054448_1   |
      | KPI      | ue_percentage_q4                  | 6                   | 054448_1   |
      | KPI      | distance_q1                       | 110                 | 054448_1   |
      | KPI      | distance_q2                       | 220                 | 054448_1   |
      | KPI      | distance_q3                       | 330                 | 054448_1   |
      | KPI      | distance_q4                       | 440                 | 054448_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 054448_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 054448_2   |
      | KPI      | coverage_balance_ratio_distance   | 3.33333333333333    | 054448_2   |
      | KPI      | distance_q1                       | 156                 | 054448_2   |
      | KPI      | distance_q2                       | 312                 | 054448_2   |
      | KPI      | distance_q3                       | 468                 | 054448_2   |
      | KPI      | distance_q4                       | 624                 | 054448_2   |
      | KPI      | ue_percentage_q1                  | 6                   | 054448_2   |
      | KPI      | ue_percentage_q2                  | 26                  | 054448_2   |
      | KPI      | ue_percentage_q3                  | 62                  | 054448_2   |
      | KPI      | ue_percentage_q4                  | 6                   | 054448_2   |
      | KPI      | goal_function_resource_efficiency | 0.558874105661037   | 054448_3   |
      | KPI      | unhappy_users                     | 40.092511239070919  | 054448_3   |
      | KPI      | coverage_balance_ratio_distance   | 20.6123420083598532 | 054448_3   |
      | KPI      | ue_percentage_q1                  | 10.6382978723404    | 054448_3   |
      | KPI      | ue_percentage_q2                  | 3.54609929078014    | 054448_3   |
      | KPI      | ue_percentage_q3                  | 26.9503546099291    | 054448_3   |
      | KPI      | ue_percentage_q4                  | 58.8652482269504    | 054448_3   |
      | KPI      | distance_q1                       | 25                  | 054448_3   |
      | KPI      | distance_q2                       | 50                  | 054448_3   |
      | KPI      | distance_q3                       | 75                  | 054448_3   |
      | KPI      | distance_q4                       | 100                 | 054448_3   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 054448_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 054448_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 2.4050611054374507  | 054448_1_9 |
      | KPI      | distance_q4                       | 712                 | 054448_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 054448_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 054448_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 18.4098331507027    | 054448_2_2 |
      | KPI      | distance_q4                       | 600                 | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054448_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.2                 | 054448_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.9                 | 054448_3   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054448_1_9    | 1               | 2               |
      | 054448_2_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054448_3      | 1               | 4               |

  Scenario: TC14 - Source Coverage Balance Ratio Distance is zero for one of the source cells in the sector.
  The source cell is removed from the potential source cell list.

  The Sector in this test had 3 source cells ranked as follows:
  1. Cell 054850_3_4 with potential target cells 054850_3, 054850_3_9
  2. Cell 054850_3_2 with potential target cell 054850_3_9.
  3. Cell 054850_3_6 with potential target cells 054850_3, 054850_3_9
  Highest ranked source cell is 054850_3_4.
  The coverage_balance_ratio_distance has a value zero for source cell 054850_3_4.
  The source cell is excluded.
  No thresholds are breached, the next ranked source cell 054850_3_2 is returned as source fdn in
  Proposed LoadBalancingQuanta with target cell 054850_3_9.

    Given Create Default Optimization Cells
      | 054850_3_6 |
      | 054850_3   |
      | 054850_3_2 |
      | 054850_3_4 |
      | 054850_3_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812850 | TC_14_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.900317058100273  | 054850_3   |
      | KPI      | goal_function_resource_efficiency | 0.629314794871641  | 054850_3_2 |
      | KPI      | unhappy_users                     | 11.54536953213271  | 054850_3_2 |
      | KPI      | goal_function_resource_efficiency | 0.529314794871641  | 054850_3_4 |
      | KPI      | unhappy_users                     | 20.743089638662777 | 054850_3_4 |
      | KPI      | coverage_balance_ratio_distance   | 0                  | 054850_3_4 |
      | KPI      | goal_function_resource_efficiency | 0.529314794871641  | 054850_3_6 |
      | KPI      | unhappy_users                     | 0.7555555511222    | 054850_3_6 |
      | KPI      | goal_function_resource_efficiency | 0.92965574442653   | 054850_3_9 |
      | KPI      | p_failing_r_mbps                  | 0.3                | 054850_3_9 |
      | KPI      | p_failing_r_mbps                  | 0.7                | 054850_3_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054850_3_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054850_3_2    | 1               | 2               |

  Scenario: TC15 - Source Coverage Balance Ratio Distance kpi is zero for all of the source cells in the sector.
  No Optimization is possible for the sector.
  An empty LBQ is returned.

  The Sector in this test had 1 possible source cell,
  - cell 054968_2_2 with potential target cell 054968_2.

  Cell 054968_2_2 has a value of zero for coverage_balance_ratio_distance so is removed as a possible source cell.
  All possible source cells have been removed so the sector is removed from optimization and an empty LBQ is returned.

    Given Create Default Optimization Cells
      | 054968_2   |
      | 054968_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290088340418968 | TC_15_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.94501303383472  | 054968_2   |
      | KPI      | goal_function_resource_efficiency | 0.640501303383472 | 054968_2_2 |
      | KPI      | coverage_balance_ratio_distance   | 0                 | 054968_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  #Below tests cover scenarios in which the synthetic counters are considered unrelaible
  Scenario: TC16 - synthetic_counters_cell_reliability_daily for source < synthetic_counters_cell_reliability_threshold_in_rops
  for source cell, No optimization is possible for sector

  The Sector in this test had 1 possible source cell,
  - cell 054902_2_2 with potential target cell - cell 054902_1_4

  All Source Cells are excluded so Sector is excluded.

    Given Create Default Optimization Cells
      | 054902_2_2 |
      | 054902_1_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_16_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType    | dataName                                              | dataValue           | fdn        |
      | KPI         | goal_function_resource_efficiency                     | 0.447884075944469   | 054902_2_2 |
      | KPI         | unhappy_users                                         | 12.6403873501825    | 054902_2_2 |
      | KPI         | goal_function_resource_efficiency                     | 0.980834728689175   | 054902_1_4 |
      | KPI         | unhappy_users                                         | 1.53093251968166    | 054902_1_4 |
      | KPI         | p_failing_r_mbps                                      | 0.9                 | 054902_2_2 |
      | KPI         | p_failing_r_mbps                                      | 0.3                 | 054902_1_4 |
      | KPI         | synthetic_counter_cell_reliability_daily              | 1                   | 054902_2_2 |
      | SETTING     | synthetic_counters_cell_reliability_threshold_in_rops | 5                   | 054902_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC17 - synthetic_counters_cell_reliability_daily for target < synthetic_counters_cell_reliability_threshold_in_rops
  for only target cell, No optimization is possible for sector

  The Sector in this test had 1 possible source cell,
  - cell 054902_2_2 with potential target cell - cell 054902_1_4

  All target Cells are excluded so Sector is excluded.

    Given Create Default Optimization Cells
      | 054902_2_2 |
      | 054902_1_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_17_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType    | dataName                                              | dataValue           | fdn        |
      | KPI         | goal_function_resource_efficiency                     | 0.447884075944469   | 054902_2_2 |
      | KPI         | unhappy_users                                         | 12.6403873501825    | 054902_2_2 |
      | KPI         | goal_function_resource_efficiency                     | 0.980834728689175   | 054902_1_4 |
      | KPI         | unhappy_users                                         | 1.53093251968166    | 054902_1_4 |
      | KPI         | p_failing_r_mbps                                      | 0.9                 | 054902_2_2 |
      | KPI         | p_failing_r_mbps                                      | 0.3                 | 054902_1_4 |
      | KPI         | synthetic_counter_cell_reliability_daily              | 1                   | 054902_1_4 |
      | SETTING     | synthetic_counters_cell_reliability_threshold_in_rops | 5                   | 054902_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC18 - synthetic_counters_cell_reliability_daily for source and target < synthetic_counters_cell_reliability_threshold_in_rops
  for both source cell and only target cell, No optimization is possible for sector

  The Sector in this test had 1 possible source cell,
  - cell 054902_2_2 with potential target cell - cell 054902_1_4

  All Source Cells are excluded so Sector is excluded.

    Given Create Default Optimization Cells
      | 054902_2_2 |
      | 054902_1_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_18_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType    | dataName                                              | dataValue           | fdn        |
      | KPI         | goal_function_resource_efficiency                     | 0.447884075944469   | 054902_2_2 |
      | KPI         | unhappy_users                                         | 12.6403873501825    | 054902_2_2 |
      | KPI         | goal_function_resource_efficiency                     | 0.980834728689175   | 054902_1_4 |
      | KPI         | unhappy_users                                         | 1.53093251968166    | 054902_1_4 |
      | KPI         | p_failing_r_mbps                                      | 0.9                 | 054902_2_2 |
      | KPI         | p_failing_r_mbps                                      | 0.3                 | 054902_1_4 |
      | KPI         | synthetic_counter_cell_reliability_daily              | 1                   | 054902_1_4 |
      | SETTING     | synthetic_counters_cell_reliability_threshold_in_rops | 5                   | 054902_1_4 |
      | KPI         | synthetic_counter_cell_reliability_daily              | 1                   | 054902_2_2 |
      | SETTING     | synthetic_counters_cell_reliability_threshold_in_rops | 5                   | 054902_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC19 - synthetic_counters_cell_reliability_daily for target < synthetic_counters_cell_reliability_threshold_in_rops
  for one target cell, optimization continues with the source cell and the other target cell

  The Sector in this test had 1 possible source cells,
  - cell 054902_2_2 with potential target cells 054902_1_4 and 054902_1_9

  synthetic_counters_cell_reliability_daily for source < synthetic_counters_cell_reliability_threshold_in_rops for
  Cell 054902_1_4.

  Source and All target Cells proceed to next state for optimization.

    Given Create Default Optimization Cells
      | 054902_2_2 |
      | 054902_1_4 |
      | 054902_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_19_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                              | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency                     | 0.447884075944469   | 054902_2_2 |
      | KPI      | unhappy_users                                         | 12.6403873501825    | 054902_2_2 |
      | KPI      | goal_function_resource_efficiency                     | 0.980834728689175   | 054902_1_4 |
      | KPI      | unhappy_users                                         | 1.53093251968166    | 054902_1_4 |
      | KPI      | goal_function_resource_efficiency                     | 0.978888304995677   | 054902_1_9 |
      | KPI      | unhappy_users                                         | 0.747764508333663   | 054902_1_9 |
      | KPI      | p_failing_r_mbps                                      | 0.9                 | 054902_2_2 |
      | KPI      | p_failing_r_mbps                                      | 0.3                 | 054902_1_4 |
      | KPI      | p_failing_r_mbps                                      | 0.2                 | 054902_1_9 |
      | KPI      | synthetic_counter_cell_reliability_daily              | 1                   | 054902_1_4 |
      | SETTING  | synthetic_counters_cell_reliability_threshold_in_rops | 5                   | 054902_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054902_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054902_2_2    | 1               | 2               |

  Scenario: TC20 - synthetic_counters_cell_reliability_daily for source < synthetic_counters_cell_reliability_threshold_in_rops
  for source cell, both target cells are reliable, No optimization is possible for sector

  The Sector in this test had 1 possible source cells,
  - cell 054902_2_2 with potential target cells 054902_1_4 and 054902_1_9

  synthetic_counters_cell_reliability_daily for source < synthetic_counters_cell_reliability_threshold_in_rops for
  Cell 054902_2_2.

  All Source Cells are excluded so Sector is excluded.

    Given Create Default Optimization Cells
      | 054902_2_2 |
      | 054902_1_4 |
      | 054902_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_20_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                              | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency                     | 0.447884075944469   | 054902_2_2 |
      | KPI      | unhappy_users                                         | 12.6403873501825    | 054902_2_2 |
      | KPI      | goal_function_resource_efficiency                     | 0.980834728689175   | 054902_1_4 |
      | KPI      | unhappy_users                                         | 1.53093251968166    | 054902_1_4 |
      | KPI      | goal_function_resource_efficiency                     | 0.978888304995677   | 054902_1_9 |
      | KPI      | unhappy_users                                         | 0.747764508333663   | 054902_1_9 |
      | KPI      | p_failing_r_mbps                                      | 0.9                 | 054902_2_2 |
      | KPI      | p_failing_r_mbps                                      | 0.3                 | 054902_1_4 |
      | KPI      | p_failing_r_mbps                                      | 0.2                 | 054902_1_9 |
      | KPI      | synthetic_counter_cell_reliability_daily              | 1                   | 054902_2_2 |
      | SETTING  | synthetic_counters_cell_reliability_threshold_in_rops | 5                   | 054902_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC21 - synthetic_counters_cell_reliability_daily for source and target > synthetic_counters_cell_reliability_threshold_in_rops
  for source and target. Sector busy hour data unavailable for source cell, no optimization can occur

  The Sector in this test had 1 possible source cells,
  - cell 054902_2_2 with 1 potential target cell - cell 054902_1_4

  Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio is less than threshold for
  Cell 054902_1_4.
  distance_q1 is null for the source cell as no pm event data was available for the sector busy hour

  All Source Cells are excluded so Sector is excluded.

    Given Create Default Optimization Cells
      | 054902_2_2 |
      | 054902_1_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_21_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.447884075944469   | 054902_2_2 |
      | KPI      | unhappy_users                     | 12.6403873501825    | 054902_2_2 |
      | KPI      | distance_q1                       | null                | 054902_2_2 |
      | KPI      | ue_percentage_q1                  | 81.8897637795276    | 054902_2_2 |
      | KPI      | goal_function_resource_efficiency | 0.980834728689175   | 054902_1_4 |
      | KPI      | unhappy_users                     | 1.53093251968166    | 054902_1_4 |
      | KPI      | coverage_balance_ratio_distance   | 60.6123420083598532 | 054902_1_4 |
      | KPI      | distance_q4                       | 1051.04             | 054902_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.9                 | 054902_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054902_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC22 - synthetic_counters_cell_reliability_daily for source and target > synthetic_counters_cell_reliability_threshold_in_rops
  for source and target. Sector busy hour data unavailable for only target cell, no optimization can occur

  The Sector in this test had 1 possible source cells,
  - cell 054902_2_2 with 1 potential target cell - cell 054902_1_4

  Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio is less than threshold for
  Cell 054902_1_4.
  distance_q4 is null for the target cell as no pm event data was available for the sector busy hour

  All target Cells are excluded so Sector is excluded.

    Given Create Default Optimization Cells
      | 054902_2_2 |
      | 054902_1_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_22_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.447884075944469   | 054902_2_2 |
      | KPI      | unhappy_users                     | 12.6403873501825    | 054902_2_2 |
      | KPI      | distance_q1                       | 1186.98666666667    | 054902_2_2 |
      | KPI      | ue_percentage_q1                  | 81.8897637795276    | 054902_2_2 |
      | KPI      | goal_function_resource_efficiency | 0.980834728689175   | 054902_1_4 |
      | KPI      | unhappy_users                     | 1.53093251968166    | 054902_1_4 |
      | KPI      | coverage_balance_ratio_distance   | 60.6123420083598532 | 054902_1_4 |
      | KPI      | distance_q4                       | null                | 054902_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.9                 | 054902_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054902_1_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC23 - synthetic_counters_cell_reliability_daily for source and targets > synthetic_counters_cell_reliability_threshold_in_rops
  for source and both targets. Sector busy hour data unavailable for one target cell,
  optimization continues with the source cell and the other target cell

  The Sector in this test had 1 possible source cells,
    - cell 054902_2_2 with potential target cells 054902_1_4 and 054902_1_9

  Target Coverage Balance Ratio Distance/Source Coverage Balance Ratio Distance ratio is less than threshold for
  Cells 054902_1_4 and 054902_1_9.
  distance_q4 is null for the target cell 054902_1_9 as no pm event data was available for the sector busy hour
  Target Coverage Area 'distance_q4' falls in Source Coverage Area 'distance_q1' and
  ST Samples Overlap % > ST Samples Overlap threshold for target cell 054902_1_4.

  Source and target Cell 054902_1_4 proceed to next state for optimization.

    Given Create Default Optimization Cells
      | 054902_2_2 |
      | 054902_1_4 |
      | 054902_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102902 | TC_23_CoverageBalanceRatioDistance_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.447884075944469   | 054902_2_2 |
      | KPI      | unhappy_users                     | 12.6403873501825    | 054902_2_2 |
      | KPI      | distance_q1                       | 1186.98666666667    | 054902_2_2 |
      | KPI      | ue_percentage_q1                  | 81.8897637795276    | 054902_2_2 |
      | KPI      | goal_function_resource_efficiency | 0.980834728689175   | 054902_1_4 |
      | KPI      | unhappy_users                     | 1.53093251968166    | 054902_1_4 |
      | KPI      | coverage_balance_ratio_distance   | 60.6123420083598532 | 054902_1_4 |
      | KPI      | distance_q4                       | 1051.04             | 054902_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.978888304995677   | 054902_1_9 |
      | KPI      | unhappy_users                     | 0.747764508333663   | 054902_1_9 |
      | KPI      | coverage_balance_ratio_distance   | 60.6123420083598532 | 054902_1_9 |
      | KPI      | distance_q4                       | null                | 054902_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.9                 | 054902_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 054902_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.2                 | 054902_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054902_1_4    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054902_2_2    | 1               | 2               |
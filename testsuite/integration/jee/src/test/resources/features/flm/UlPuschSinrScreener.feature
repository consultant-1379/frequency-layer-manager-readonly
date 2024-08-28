#*------------------------------------------------------------------------------
#******************************************************************************
# COPYRIGHT Ericsson 2022
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#******************************************************************************
#------------------------------------------------------------------------------
@RunAllTests

Feature: FLM_UlPuschSinrScreener
  The purpose of this policy state is to ensure that UL PUSCH SINR is not degraded during optimization

  Input to this state:
  Ranked list of potential Source cells and their possible target cells

  Output from this state:
  The output from this state will be the input ranked list of potential source cells and their associated target cells
  with the target cells not meeting the threshold screened out.
  If all target cells for all potential source cells are screened out then the sector is removed from optimization
  and an empty LBQ is returned.

  The kpi used:
  ul_pusch_sinr_hourly

  The settings used:
  minTargetUlPuschSinr (Default value: 5)
  ulPuschSinrRatioThreshold (Default value: 0.8)


  Scenario: TC1 - Target UL PUSCH SINR < min_target_uplink_pusch_sinr for 1 target cell in the sector.
  The settings are the same for all cells in the sector.
  The target cell (055550_1_9) is excluded as a possible target for that source cell (055550_2).
  Source cell (055550_2) has no possible targets remaining and is excluded too.

  The Sector in this test has 2 possible source cells ranked as follows,
  1. cell 055550_1 with potential target cells 055550_1_9, 055550_2_2
  2. cell 055550_2 with potential target cells 055550_1_9
  Target UL PUSCH SINR is lower than the min_target for target cells 055550_1_9 so it is removed from optimization.
  As source cell 055550_2 has no remaining possible target cells, it is also removed from optimization.

    Given Create Default Optimization Cells
      | 055550_2   |
      | 055550_1   |
      | 055550_1_9 |
      | 055550_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173123459656102600 | TC_1_UlPuschSinrScreener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 055550_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 055550_1   |
      | KPI      | ul_pusch_sinr_hourly              | 10                  | 055550_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 055550_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 055550_2   |
      | KPI      | ul_pusch_sinr_hourly              | 10                  | 055550_2   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 055550_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 055550_1_9 |
      | KPI      | ul_pusch_sinr_hourly              | 4                   | 055550_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 055550_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 055550_2_2 |
      | KPI      | ul_pusch_sinr_hourly              | 10                  | 055550_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 055550_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.7                 | 055550_1   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055550_2_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055550_1      | 1               | 2               |


    Scenario: TC2 - Target UL PUSCH SINR < Src UL PUSCH SINR * ulPuschSinrRatioThreshold for 1 target cell in the sector.
    The settings are the same for all cells in the sector.
    The target cell (003) is excluded as a possible target for that source cell (002).
    Source cell (002) has no possible targets remaining and is excluded too.

    The Sector in this test has 2 possible source cells ranked as follows,
    1. cell 001 with potential target cells 003, 004
    2. cell 002 with potential target cells 003
    Target UL PUSCH SINR is lower than the min_target for target cells 055550_1_9 so it is removed from optimization.
    As source cell 055550_2 has no remaining possible target cells, it is also removed from optimization.

      Given Create Default Optimization Cells
        | 001   |
        | 002   |
        | 003   |
        | 004   |

      And Policy Input Event
        | sectorId           | executionId |
        | 173123459656102601 | TC_2_UlPuschSinrScreener     |

      And Set Optimization Cells Data
        | dataType | dataName                          | dataValue           | fdn        |
        | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 001        |
        | KPI      | unhappy_users                     | 0.592511239070919   | 001        |
        | KPI      | ul_pusch_sinr_hourly              | 16                  | 001        |
        | KPI      | p_failing_r_mbps                  | 0.7                 | 001        |
        | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 002        |
        | KPI      | unhappy_users                     | 25.052511239070919  | 002        |
        | KPI      | ul_pusch_sinr_hourly              | 16                  | 002        |
        | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 003        |
        | KPI      | unhappy_users                     | 76.0995926937517    | 003        |
        | KPI      | ul_pusch_sinr_hourly              | 9                   | 003        |
        | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 004        |
        | KPI      | unhappy_users                     | 78.24862155388474   | 004        |
        | KPI      | ul_pusch_sinr_hourly              | 16                  | 004        |
        | KPI      | p_failing_r_mbps                  | 0.3                 | 004        |

      When Putting Policy Input Event onto Kafka Topic

      Then Optimization proceeds with the following target cells
        | targetCellFdn | targetCellOssId | targetUsersMove |
        | 004           | 1               | 2               |

      Then Optimization proceeds with the following source cells
        | sourceCellFdn | sourceCellOssId | sourceUsersMove |
        | 001           | 1               | 2               |

  Scenario: TC3 - UL PUSCH SINR is satisfied for all target cells in the sector.
  No target cells are removed. All source and target cells are selected for optimization.

  The Sector in this test has 1 possible source cell,
  - cell 051235_2 with potential target cells 051235_1_4 and 051235_1_9

    Given Create Default Optimization Cells
      | 051235_2   |
      | 051235_1_4 |
      | 051235_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173291189656102501 | TC_3_UlPuschSinrScreener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.364909938121602 | 051235_2   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 051235_2   |
      | KPI      | percentage_endc_users             | 80.0              | 051235_2   |
      | KPI      | p_failing_r_mbps                  | 0.9               | 051235_2   |
      | KPI      | ul_pusch_sinr_hourly              | 10                | 051235_2   |
      | KPI      | goal_function_resource_efficiency | 0.667027043555072 | 051235_1_4 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 051235_1_4 |
      | KPI      | percentage_endc_users             | 44.7196261682243  | 051235_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 051235_1_4 |
      | KPI      | ul_pusch_sinr_hourly              | 10                | 051235_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 051235_1_9 |
      | KPI      | unhappy_users                     | 1.54536953213271  | 051235_1_9 |
      | KPI      | percentage_endc_users             | 39.8121212        | 051235_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.2               | 051235_1_9 |
      | KPI      | ul_pusch_sinr_hourly              | 10                | 051235_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 051235_1_4    | 1               | 2               |
      | 051235_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 051235_2      | 1               | 4               |


  Scenario: TC4 - UL PUSCH SINR Thresholds not saitsfied for all of target cells in the sector.
  The sector is moved from optimization and an empty LBQ is returned.

  The Sector in this test has 2 possible source cells,
  - cell 051234_2 with potential target cells 051234_1_4 and 051234_1_9
  - cell 051234_2_2 with potential target cells 051234_1_4 and 051234_1_9

  Cell 051234_1_4 is removed as Target UL PUSCH SINR < min_target_uplink_pusch_sinr.
  Cell 051234_1_9 is removed as Target UL PUSCH SINR < Src UL PUSCH SINR * ulPuschSinrRatioThreshold.
  Source cells are removed as they dont have any potential target cells.
  All possible source cell have been removed so the sector is removed from optimization and an empty LBQ is returned.

    Given Create Default Optimization Cells
      | 051234_2   |
      | 051234_1_4 |
      | 051234_1_9 |
      | 051234_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173291189656102503 | TC_4_UlPuschSinrScreener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.358874105661037 | 051234_2   |
      | KPI      | unhappy_users                     | 90.59251123907091 | 051234_2   |
      | KPI      | ul_pusch_sinr_hourly              | 20                | 051234_2   |
      | KPI      | goal_function_resource_efficiency | 0.867027043555072 | 051234_1_4 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 051234_1_4 |
      | KPI      | ul_pusch_sinr_hourly              | 4                 | 051234_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 051234_1_9 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 051234_1_9 |
      | KPI      | percentage_endc_users             | 59.8121212        | 051234_1_9 |
      | KPI      | ul_pusch_sinr_hourly              | 10                | 051234_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.534909938121602 | 051234_2_2 |
      | KPI      | unhappy_users                     | 89.59251123907091 | 051234_2_2 |
      | KPI      | ul_pusch_sinr_hourly              | 10                | 051234_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC5 - Target UL PUSCH SINR is missing in a target cell.
  Screening is skipped for that target cell.

  The Sector in this test had 1 possible source cell,
  - cell 051238_2 with potential target cells 051238_1_4 and 051238_1_9

    Given Create Default Optimization Cells
      | 051238_2   |
      | 051238_1_4 |
      | 051238_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173277554656102501 | TC_5_UlPuschSinrScreener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.364909938121602 | 051238_2   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 051238_2   |
      | KPI      | p_failing_r_mbps                  | 0.9               | 051238_2   |
      | KPI      | ul_pusch_sinr_hourly              | 10                | 051238_2   |
      | KPI      | goal_function_resource_efficiency | 0.667027043555072 | 051238_1_4 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 051238_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 051238_1_4 |
      | KPI      | ul_pusch_sinr_hourly              | null              | 051238_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 051238_1_9 |
      | KPI      | unhappy_users                     | 1.54536953213271  | 051238_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.2               | 051238_1_9 |
      | KPI      | ul_pusch_sinr_hourly              | null              | 051238_1_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 051238_1_4    | 1               | 2               |
      | 051238_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 051238_2      | 1               | 4               |
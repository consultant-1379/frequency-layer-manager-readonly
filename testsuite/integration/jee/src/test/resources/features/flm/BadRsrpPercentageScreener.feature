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

Feature: FLM_BadRsrpPercentageScreener
  The purpose of this policy state is to screen out target cells where the ratio
  Target Bad RSRP % / Source Bad RSRP % is greater than
  the setting "percentage_bad_rsrp_ratio_threshold"

  Input to this state:
  Ranked list of potential Source cells and their possible target cells

  Output from this state:
  The output from this state will be the input ranked list of potential source cells and their associated target cells
  with the target cells not meeting the threshold screened out.
  If all target cells for all potential source cells are screened out then the sector is removed from optimization
  and an empty LBQ is returned.

  The kpi used:
  num_samples_rsrp_ta_q1
  num_samples_rsrp_ta_q2
  num_samples_rsrp_ta_q3
  num_samples_rsrp_ta_q4
  num_bad_samples_rsrp_ta_q1
  num_bad_samples_rsrp_ta_q2
  num_bad_samples_rsrp_ta_q3
  num_bad_samples_rsrp_ta_q4
  distance_q1
  distance_q2
  distance_q3
  distance_q4

  The settings used:
  percentageBadRsrpRatioThreshold (Default value: 1.2)


  Scenario: TC1 - Target Bad RSRP % / Source Bad RSRP % > threshold for 1 target cell in the sector therefore is screened out.
  The settings are the same for all cells in the sector.
  The target cell (055550_1_9) is excluded as a possible target for that source cell (055550_2).
  Source cell (055550_2) has no possible targets remaining and is excluded too.

  The Sector in this test has 2 possible source cells ranked as follows,
  1. cell 055550_1 with potential target cells 055550_1_9, 055550_2_2
  2. cell 055550_2 with potential target cells 055550_1_9

    Given Create Default Optimization Cells
      | 055550_2   |
      | 055550_1   |
      | 055550_1_9 |
      | 055550_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173123459656102600 | TC_1_BadRsrpPercentageScreener    |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 055550_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 055550_1   |
      | KPI      | p_failing_r_mbps                  | 0.7                 | 055550_1   |
      | KPI      | ul_pusch_sinr_hourly              | 10                  | 055550_1   |
      | KPI      | num_samples_rsrp_ta_q1            | 100                 | 055550_1   |
      | KPI      | num_samples_rsrp_ta_q2            | 100                 | 055550_1   |
      | KPI      | num_samples_rsrp_ta_q3            | 100                 | 055550_1   |
      | KPI      | num_samples_rsrp_ta_q4            | 100                 | 055550_1   |
      | KPI      | num_bad_samples_rsrp_ta_q1        | 9                   | 055550_1   |
      | KPI      | num_bad_samples_rsrp_ta_q2        | 37                  | 055550_1   |
      | KPI      | num_bad_samples_rsrp_ta_q3        | 39                  | 055550_1   |
      | KPI      | num_bad_samples_rsrp_ta_q4        | 10                  | 055550_1   |
      | KPI      | distance_q1                       | 208                 | 055550_1   |
      | KPI      | distance_q2                       | 417                 | 055550_1   |
      | KPI      | distance_q3                       | 626                 | 055550_1   |
      | KPI      | distance_q4                       | 834                 | 055550_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 055550_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 055550_2   |
      | KPI      | ul_pusch_sinr_hourly              | 10                  | 055550_2   |
      | KPI      | num_samples_rsrp_ta_q1            | 100                 | 055550_2   |
      | KPI      | num_samples_rsrp_ta_q2            | 100                 | 055550_2   |
      | KPI      | num_samples_rsrp_ta_q3            | 100                 | 055550_2   |
      | KPI      | num_samples_rsrp_ta_q4            | 100                 | 055550_2   |
      | KPI      | num_bad_samples_rsrp_ta_q1        | 9                   | 055550_2   |
      | KPI      | num_bad_samples_rsrp_ta_q2        | 37                  | 055550_2   |
      | KPI      | num_bad_samples_rsrp_ta_q3        | 39                  | 055550_2   |
      | KPI      | num_bad_samples_rsrp_ta_q4        | 10                  | 055550_2   |
      | KPI      | distance_q1                       | 208                 | 055550_2   |
      | KPI      | distance_q2                       | 417                 | 055550_2   |
      | KPI      | distance_q3                       | 626                 | 055550_2   |
      | KPI      | distance_q4                       | 834                 | 055550_2   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 055550_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 055550_1_9 |
      | KPI      | ul_pusch_sinr_hourly              | 4                   | 055550_1_9 |
      | KPI      | num_samples_rsrp_ta_q1            | 100                 | 055550_1_9 |
      | KPI      | num_samples_rsrp_ta_q2            | 100                 | 055550_1_9 |
      | KPI      | num_samples_rsrp_ta_q3            | 100                 | 055550_1_9 |
      | KPI      | num_samples_rsrp_ta_q4            | 100                 | 055550_1_9 |
      | KPI      | num_bad_samples_rsrp_ta_q1        | 90                  | 055550_1_9 |
      | KPI      | num_bad_samples_rsrp_ta_q2        | 80                  | 055550_1_9 |
      | KPI      | num_bad_samples_rsrp_ta_q3        | 69                  | 055550_1_9 |
      | KPI      | num_bad_samples_rsrp_ta_q4        | 80                  | 055550_1_9 |
      | KPI      | distance_q1                       | 208                 | 055550_1_9 |
      | KPI      | distance_q2                       | 417                 | 055550_1_9 |
      | KPI      | distance_q3                       | 626                 | 055550_1_9 |
      | KPI      | distance_q4                       | 834                 | 055550_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 055550_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 055550_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 055550_2_2 |
      | KPI      | ul_pusch_sinr_hourly              | 10                  | 055550_2_2 |
      | KPI      | num_samples_rsrp_ta_q1            | 100                 | 055550_2_2 |
      | KPI      | num_samples_rsrp_ta_q2            | 100                 | 055550_2_2 |
      | KPI      | num_samples_rsrp_ta_q3            | 100                 | 055550_2_2 |
      | KPI      | num_samples_rsrp_ta_q4            | 100                 | 055550_2_2 |
      | KPI      | num_bad_samples_rsrp_ta_q1        | 9                   | 055550_2_2 |
      | KPI      | num_bad_samples_rsrp_ta_q2        | 37                  | 055550_2_2 |
      | KPI      | num_bad_samples_rsrp_ta_q3        | 39                  | 055550_2_2 |
      | KPI      | num_bad_samples_rsrp_ta_q4        | 10                  | 055550_2_2 |
      | KPI      | distance_q1                       | 208                 | 055550_2_2 |
      | KPI      | distance_q2                       | 417                 | 055550_2_2 |
      | KPI      | distance_q3                       | 626                 | 055550_2_2 |
      | KPI      | distance_q4                       | 830                 | 055550_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055550_2_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055550_1      | 1               | 2               |
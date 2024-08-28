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

Feature: FLM_IdentifySourceCells
  The purpose of this policy state is to identify a list of potential Source Cells
  in a sector and their possible target cells.
  The delta GFS is the difference in the goal_function_resource_efficiency values for 2 cells.
  All cells in the sector where the delta GFS with another cell is greater than the threshold
  are identified as potential source cells

  All cells in the sector where the delta GFS with the source cell is greater than the threshold
  are identified as potential targets for that source cell.

  All potential source cells shall be ranked based on their Unhappy Users kpi.
  In the event that two potential source cells have the same Unhappy Users score
  then the cell with the lowest value of Goal Function Score will be given the higher ranking.

  The output from this state will be a list ranked source cells and their associated potential target cells
  for the sector.

  The kpi's used are:
  goal_function_resource_efficiency
  unhappy_users

  The settings used are:
  goal_function_score_delta_threshold


  Scenario: TC1 - Identify Source and Target Cells when only 1 cell in a sector.
  The cell for which the delta GFS exceeds the threshold is identified as the only potential
  source cell for the sector.
  The other cell in the sector is identified as the target cell regardless of it's unhappy_users score.

    Given Create Default Optimization Cells
      | 054234_2   |
      | 054234_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290088340418268 | TC_1_IdentifySourceCells      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.545855356858811 | 054234_2   |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054234_2   |
      | KPI      | goal_function_resource_efficiency | 0.97165574442653  | 054234_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 054234_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.7               | 054234_2   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054234_2_2    | 1               | 2               |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054234_2      | 1               | 2               |


  Scenario: TC2 - Identify Ranked Source and Target Cells when more than 1 possible source cell in the sector.
  The cells for which the delta GFS exceeds the threshold are identified as potential source cells for the sector.
  All cells in the sector where the delta GFS with the source cell is greater than the threshold
  are identified as potential targets for that source cell.
  The identified source cells are ranked based on their unhappy_users score, highest score is ranked highest.
  The highest ranked source cell is output as 'sourceCellFdn' in the "proposedLoadBalancingQuanta".
  The identified possible targetCells' are output in 'targetCells' list in the "proposedLoadBalancingQuanta".
  The Sector in this test had 2 possible source cells, cell 054343_3_2 with potential target cell 054343_3_9
  and cell 054343_3_4 with potential target cells 054343_3 and 054343_3_9.
  Highest ranked source cell is 054343_3_4.


    Given Create Default Optimization Cells
      | 054343_3_2 |
      | 054343_3_4 |
      | 054343_3_9 |
      | 054343_3   |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812150 | TC_2_IdentifySourceCells      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.800317058100273 | 054343_3   |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054343_3   |
      | KPI      | goal_function_resource_efficiency | 0.529314794871641 | 054343_3_2 |
      | KPI      | unhappy_users                     | 1.54536953213271  | 054343_3_2 |
      | KPI      | goal_function_resource_efficiency | 0.406027043555072 | 054343_3_4 |
      | KPI      | unhappy_users                     | 20.74308963866277 | 054343_3_4 |
      | KPI      | goal_function_resource_efficiency | 0.838850312445342 | 054343_3_9 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054343_3_9 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 054343_3_9 |
      | KPI      | p_failing_r_mbps                  | 0.2               | 054343_3   |
      | KPI      | p_failing_r_mbps                  | 0.9               | 054343_3_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054343_3_9    | 1               | 2               |
      | 054343_3      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054343_3_4    | 1               | 4               |

  Scenario: TC3 - Regression Test - Delta GFS threshold is not exceeded for any cells in the sector.
  The sector is already well balanced and no optimization will occur.

    Given Create Default Optimization Cells
      | 054145_3   |
      | 054147_4   |
      | 054145_3_2 |
      | 054147_4_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290409770210514 | TC_3_IdentifySourceCells      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.911262002743484 | 054145_3   |
      | KPI      | unhappy_users                     | 0.041724473953688 | 054145_3   |
      | KPI      | goal_function_resource_efficiency | 0.974807751461089 | 054147_4   |
      | KPI      | unhappy_users                     | 0.040329522910603 | 054147_4   |
      | KPI      | goal_function_resource_efficiency | 0.994125354670572 | 054145_3_2 |
      | KPI      | unhappy_users                     | 0.002364969966736 | 054145_3_2 |
      | KPI      | goal_function_resource_efficiency | 0.997014575894164 | 054147_4_2 |
      | KPI      | unhappy_users                     | 0.135612962188048 | 054147_4_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC4 - Identify Ranked Source and target cells when 2 possible source Cells have same unhappy_users score.
  The cells for which the delta GFS exceeds the threshold are identified as potential source cells for the sector.
  All cells in the sector where the delta GFS with the source cell is greater than the threshold
  are identified as potential targets for that source cell.
  The identified source cells are ranked based on their unhappy_users score, lowest score is ranked highest.
  The identified source cells have the same unhappy_users score so the cell with the lowest goal_function_resource_efficiency
  is given the higher ranking.
  The highest ranked source cell is output as 'sourceCellFdn' in the "proposedLoadBalancingQuanta".
  The identified possible targetCells' are output in 'targetCells' list in the "proposedLoadBalancingQuanta".
  The Sector in this test had 3 possible source cells,
  - cell 054444_1 with potential target cells 054444_1_2, 054444_1_4 and 054444_1_9
  - cell 054444_2 with potential target cells 054444_1_2, 054444_1_4 and 054444_1_9
  - cell 054444_2_2 with potential target cells 054444_1_4 and 054444_1_9
  All source cells have the same unhappy_users score so cell 054444_2 has the lowest goal_function_resource_efficiency
  and is ranked highest, cell 054444_1 is ranked second highest.

    Given Create Default Optimization Cells
      | 054444_1   |
      | 054444_2   |
      | 054444_1_2 |
      | 054444_1_4 |
      | 054444_1_9 |
      | 054444_2_2 |


    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102500 | TC_4_IdentifySourceCells      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.233248616471435 | 054444_1   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 054444_1   |
      | KPI      | goal_function_resource_efficiency | 0.224184080627752 | 054444_2   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 054444_2   |
      | KPI      | goal_function_resource_efficiency | 0.602045667098208 | 054444_1_2 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 054444_1_2 |
      | KPI      | goal_function_resource_efficiency | 0.758874105661037 | 054444_1_9 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 054444_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.734909938121602 | 054444_1_4 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 054444_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.430501303383472 | 054444_2_2 |
      | KPI      | unhappy_users                     | 0.592511239070919 | 054444_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 054444_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 054444_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 054444_1_2 |
      | KPI      | p_failing_r_mbps                  | 0.9               | 054444_2   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054444_1_4    | 1               | 2               |
      | 054444_1_9    | 1               | 2               |
      | 054444_1_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054444_2      | 1               | 6               |
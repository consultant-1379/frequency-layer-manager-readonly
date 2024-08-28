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

Feature: FLM_Contiguity_Kpi_Screener
  The purpose of this policy state is to screen out target cells where the ratio
  Target Contiguity/Source Contiguity is less than the setting "Target/Source Contiguity Ratio threshold".

  Input to this state:
  Ranked list of potential Source cells and their possible target cells

  Output from this state:
  The output from this state will be the input ranked list of potential source cells and their associated target cells
  with the target cells not meeting the threshold screened out.
  If all target cells for all potential source cells are screened out then the sector is removed from optimization
  and an empty LBQ is returned.

  The kpi used:
  contiguity

  The settings used:
  target_source_contiguity_ratio_threshold


  Scenario: TC1 - Target/Source Contiguity ratio less than threshold for 1 target cell in the sector.
  The threshold setting is the same for all cells in the sector.
  The target cell is excluded as a possible target for that source cell.

  The Sector in this test had 2 possible source cells, cell 054950_3_2 with potential target cell 054950_3_9
  and cell 054950_3_4 with potential target cells 054950_3 and 054950_3_9.
  Highest ranked source cell is 054950_3_4.
  Target/Source Contiguity is less than the threshold for target cell 054950_3 so it is removed as a possible target.

    Given Create Default Optimization Cells
      | 054950_3   |
      | 054950_3_9 |
      | 054950_3_2 |
      | 054950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC1_Contiguity_Kpi_Screener       |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.829850312445342 | 054950_3   |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054950_3   |
      | KPI      | contiguity                        | 3.33333333333333  | 054950_3   |
      | KPI      | goal_function_resource_efficiency | 0.536027043555072 | 054950_3_2 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054950_3_2 |
      | KPI      | contiguity                        | 35.4609929078014  | 054950_3_2 |
      | KPI      | goal_function_resource_efficiency | 0.529314794871641 | 054950_3_4 |
      | KPI      | unhappy_users                     | 1.54536953213271  | 054950_3_4 |
      | KPI      | contiguity                        | 23.5632183908046  | 054950_3_4 |
      | KPI      | goal_function_resource_efficiency | 0.838850312445342 | 054950_3_9 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054950_3_9 |
      | KPI      | contiguity                        | 38.3333333333333  | 054950_3_9 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 054950_3_9 |
      | KPI      | p_failing_r_mbps                  | 0.7               | 054950_3_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054950_3_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054950_3_4    | 1               | 2               |


  Scenario: TC2_2 - Target/Source Contiguity ratio less than threshold for more than 1 target cell in the sector.
  Target/Source Contiguity ratio less than threshold for all target cells for 1 source cell in the sector.
  The source cell is removed as all targets are removed.
  The target cells are excluded as a possible targets for any other source cell.
  The next ranked source cell is returned with its modified target cell list.

  The threshold setting is the same for all cells in the sector.

  The Sector in this test had 3 possible source cells,
  - cell 054900_1 with potential target cells 054900_1_2, 054900_1_4 and 054900_1_9
  - cell 054900_2 with potential target cells 054900_1_2, 054900_1_4 and 054900_1_9
  - cell 054900_2_2 with potential target cells 054900_1_2, 054900_1_4 and 054900_1_9
  All source cells have the same unhappy_users score so cell 054900_2_2 has the lowest goal_function_resource_efficiency
  and is ranked highest from previous state.
  Target/Source Contiguity is less than the threshold for source cell 054900_2_2 and
  target cell 054900_1_4 and 054900_1_9 so they are removed as possible targets.
  Cell 054900_2_2 is removed as a possible source cell as it has no possible target cells.
  Cell 054900_2 is returned as source Cell in the Proposed LoadBalancingQuanta with cell 054900_1_2 as possible target cell, (target cells
  054900_1_4 and 054900_1_9 having been removed due to Contiguity Rule).

    Given Create Default Optimization Cells
      | 054900_1   |
      | 054900_1_2 |
      | 054900_1_4 |
      | 054900_1_9 |
      | 054900_2   |
      | 054900_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_2_Contiguity_Kpi_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.364909938121602 | 054900_1   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 054900_1   |
      | KPI      | contiguity                        | 100.0             | 054900_1   |
      | KPI      | goal_function_resource_efficiency | 0.667027043555072 | 054900_1_2 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054900_1_2 |
      | KPI      | contiguity                        | 87.3209936678032  | 054900_1_2 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 054900_1_4 |
      | KPI      | unhappy_users                     | 1.54536953213271  | 054900_1_4 |
      | KPI      | contiguity                        | 14.7196261682243  | 054900_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.838850312445342 | 054900_1_9 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054900_1_9 |
      | KPI      | contiguity                        | 38.8121212        | 054900_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.362045667098208 | 054900_2   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 054900_2   |
      | KPI      | contiguity                        | 80.0              | 054900_2   |
      | KPI      | goal_function_resource_efficiency | 0.361874105661037 | 054900_2_2 |
      | KPI      | unhappy_users                     | 0.592511239070919 | 054900_2_2 |
      | KPI      | contiguity                        | 100.0             | 054900_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 054900_1_2 |
      | KPI      | p_failing_r_mbps                  | 0.7               | 054900_2   |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102900 | TC_2_Contiguity_Kpi_Screener      |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054900_1_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054900_2      | 1               | 2               |


  Scenario: TC3 - Target/Source Contiguity ratio less than threshold for all target cells for all source cells in the sector.
  More than 1 source cell in the sector. The sector is removed from optimization and an empty LBQ is returned.

  The Sector in this test had 2 possible source cells,
  - cell 054445_2 with potential target cells 054445_1_4 and 054445_1_9
  - cell 054445_2_2 with potential target cells 054445_1_4 and 054445_1_9

  Target/Source Contiguity is less than the threshold for target cells 054445_1_4 and 054445_1_9 so they are removed as possible targets.
  Cell 054445_2_2 is removed as a possible source cell as it has no possible target cells.
  Cell 054445_2 is removed as a possible source cell as it has no possible target cells.
  All identified source cells have been removed so the sector is removed from optimization and an empty LBQ is returned.

    Given Create Default Optimization Cells
      | 054445_2   |
      | 054445_1_4 |
      | 054445_1_9 |
      | 054445_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102501 | TC_3_Contiguity_Kpi_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.364909938121602 | 054445_2   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 054445_2   |
      | KPI      | contiguity                        | 80.0              | 054445_2   |
      | KPI      | goal_function_resource_efficiency | 0.667027043555072 | 054445_1_4 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054445_1_4 |
      | KPI      | contiguity                        | 14.7196261682243  | 054445_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 054445_1_9 |
      | KPI      | unhappy_users                     | 1.54536953213271  | 054445_1_9 |
      | KPI      | contiguity                        | 39.8121212        | 054445_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.361874105661037 | 054900_2_2 |
      | KPI      | unhappy_users                     | 0.592511239070919 | 054900_2_2 |
      | KPI      | contiguity                        | 44.3181818181818  | 054900_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC4 - Source Contiguity kpi is zero for one of the source cells in the sector.
  The source cell is removed from the potential source cell list.

  The Sector in this test had 2 possible source cells,
  - cell 054447_2 with potential target cells 054447_1_4 and 054447_1_9
  - cell 054447_2_2 with potential target cells 054447_1_4 and 054447_1_9

  Cell 054447_2 is removed as a possible source cell as its contiguity kpi has a value of 0.0.
  Cell 054447_2_2 returned as the source cell in the Proposed LoadBalancingQuanta with cell 054447_1_4
  and 054447_1_9 as possible target cells.

    Given Create Default Optimization Cells
      | 054447_2   |
      | 054447_1_4 |
      | 054447_1_9 |
      | 054447_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102502 | TC_4_Contiguity_Kpi_Screener        |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.358874105661037 | 054447_2   |
      | KPI      | unhappy_users                     | 90.59251123907091 | 054447_2   |
      | KPI      | contiguity                        | 0.0               | 054447_2   |
      | KPI      | goal_function_resource_efficiency | 0.867027043555072 | 054447_1_4 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 054447_1_4 |
      | KPI      | contiguity                        | 84.7196261682243  | 054447_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.839314794871641 | 054447_1_9 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 054447_1_9 |
      | KPI      | contiguity                        | 89.8121212        | 054447_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.534909938121602 | 054447_2_2 |
      | KPI      | unhappy_users                     | 89.59251123907091 | 054447_2_2 |
      | KPI      | contiguity                        | 80.0              | 054447_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 054447_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.2               | 054447_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.9               | 054447_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054447_1_4    | 1               | 2               |
      | 054447_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054447_2_2    | 1               | 4               |


  Scenario: TC5 - Source Contiguity kpi is zero for all of the source cells in the sector.
  The sector is moved from optimization and an empty LBQ is returned.

  The Sector in this test had 2 possible source cells,
  - cell 054447_2 with potential target cells 054447_1_4 and 054447_1_9
  - cell 054447_2_2 with potential target cells 054447_1_4 and 054447_1_9

  Cell 054447_2_2 is removed as contiguity kpi has a value 0.0.
  Cell 054447_2 is removed as contiguity kpi has a value 0.0.
  All possible source cell have been removed so the sector is removed from optimization and an empty LBQ is returned.

    Given Create Default Optimization Cells
      | 054447_2   |
      | 054447_1_4 |
      | 054447_1_9 |
      | 054447_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290089656102503 | TC_5_Contiguity_Kpi_Screener        |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.358874105661037 | 054447_2   |
      | KPI      | unhappy_users                     | 90.59251123907091 | 054447_2   |
      | KPI      | contiguity                        | 0.0               | 054447_2   |
      | KPI      | goal_function_resource_efficiency | 0.867027043555072 | 054447_1_4 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 054447_1_4 |
      | KPI      | contiguity                        | 84.7196261682243  | 054447_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 054447_1_9 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 054447_1_9 |
      | KPI      | contiguity                        | 89.8121212        | 054447_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.534909938121602 | 054447_2_2 |
      | KPI      | unhappy_users                     | 89.59251123907091 | 054447_2_2 |
      | KPI      | contiguity                        | 0.0               | 054447_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

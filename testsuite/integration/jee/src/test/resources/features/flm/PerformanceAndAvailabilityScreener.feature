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

Feature: FLM_Performance_and_Availability_Screener
  The purpose of this policy state is to ensure that UEs are not directed to cells which are
  already performing poorly and where the user may have a negative experience.
  The performance of each target cell should be evaluated by comparing the cell values for each
  of the RAN KPIs against configurable thresholds.
  All target cells not meeting the Load-Balancing Performance Thresholds should be excluded,
  the breaching of a single threshold is sufficient to exclude the cell.

  If kpi cell_availability is is missing or null the target should be excluded.
  Targets cells for which the other Kpi's are missing or NULL should NOT be excluded.

  Input to this state:
  Ranked list of potential Source cells and their possible target cells

  Output from this state:
  The output from this state will be the input ranked list of potential source cells and their associated target cells
  with the target cells not meeting the threshols screened out.
  If all target cells for all potential source cells are screened out then the sector is removed from optimization
  and an empty LBQ is returned.

  The kpi's used in this state are:

  e_rab_retainability_percentage_lost
  e_rab_retainability_percentage_lost_qci1
  initial_and_added_e_rab_establishment_sr
  initial_and_added_e_rab_establishment_sr_for_qci1
  cell_availability
  cell_handover_success_rate

  The settings used:

  loadBalancingThresholdForInitialAndAddedErabEstabSuccRate (Default value: 98.0)
  loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1 (Default value: 98.5)
  loadBalancingThresholdForErabPercentageLost (Default value: 2.0)
  loadBalancingThresholdForErabPercentageLostForQci1 (Default value: 1.5)
  loadBalancingThresholdForCellHoSuccRate (Default value: 70.0)
  loadBalancingThresholdForCellAvailability (Default value: 70.0)


  Scenario: TC1 - All KPi's available, no thresholds breached.
  Valid values are received for all The RAN KPI's for all target cells in the sector.
  None of RAN KPI thresholds are breached.

  The Sector in this test has 2 possible source cells, cell 055950_3_2 with potential target cell 055950_3_9
  and cell 055950_3_4 with potential target cells 055950_3_9 and 055950_3
  Highest ranked source cell is 055950_3_4.
  Target/Source Contiguity is less than the threshold for target cell 055950_3 so it is removed as a possible target.
  The RAN KPI thresholds are not breached.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_1_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.900317058100273  | 055950_3_9 |
      | KPI      | unhappy_users                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | contiguity                        | 38.3333333333333   | 055950_3_9 |
      | KPI      | goal_function_resource_efficiency | 0.739314794871641  | 055950_3   |
      | KPI      | unhappy_users                     | 0.645855356858811  | 055950_3   |
      | KPI      | contiguity                        | 3.33333333333333   | 055950_3   |
      | KPI      | goal_function_resource_efficiency | 0.528850312445342  | 055950_3_2 |
      | KPI      | unhappy_users                     | 1.54536953213271   | 055950_3_2 |
      | KPI      | contiguity                        | 35.4609929078014   | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency | 0.406027043555072  | 055950_3_4 |
      | KPI      | unhappy_users                     | 20.743089638662777 | 055950_3_4 |
      | KPI      | contiguity                        | 23.5632183908046   | 055950_3_4 |
      | KPI      | p_failing_r_mbps                  | 0.3                | 055950_3_9 |
      | KPI      | p_failing_r_mbps                  | 0.7                | 055950_3_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055950_3_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055950_3_4    | 1               | 2               |

  Scenario: TC2 - The KPI's are missing for the target cells.
  The Sector in this test has 2 possible source cells, cell 055950_3_2 with potential target cell 055950_3_9
  and cell 055950_3_4 with potential target cells 055950_3 and 055950_3_9.
  Highest ranked source cell is 055950_3_4.
  Cell_Availability is missing for target cell 055950_3_9 so it is excluded.
  RAN KPI's are missing for the other target cells, the cells are not excluded.
  Optimization proceeds to the next state.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_2_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.900317058100273  | 055950_3_9 |
      | KPI      | unhappy_users                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | goal_function_resource_efficiency | 0.739314794871641  | 055950_3   |
      | KPI      | unhappy_users                     | 0.645855356858811  | 055950_3   |
      | KPI      | goal_function_resource_efficiency | 0.528850312445342  | 055950_3_2 |
      | KPI      | unhappy_users                     | 1.54536953213271   | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency | 0.406027043555072  | 055950_3_4 |
      | KPI      | unhappy_users                     | 20.743089638662777 | 055950_3_4 |
      | KPI      | contiguity                        | 23.5632183908046   | 055950_3_4 |
      | KPI      | p_failing_r_mbps                  | 0.3                | 055950_3   |
      | KPI      | p_failing_r_mbps                  | 0.7                | 055950_3_4 |

    And Missing Mandatory Optimization Cells Data
      | dataType | dataName                                          | fdn        |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr          | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1 | 055950_3_9 |
      | KPI      | cell_availability                                 | 055950_3_9 |
      | KPI      | cell_handover_success_rate                        | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost               | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | 055950_3   |
      | KPI      | initial_and_added_e_rab_establishment_sr          | 055950_3   |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1 | 055950_3   |
      | KPI      | cell_handover_success_rate                        | 055950_3   |
      | KPI      | e_rab_retainability_percentage_lost               | 055950_3   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055950_3      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055950_3_4    | 1               | 2               |

  Scenario: TC2A - The KPI's are missing for the target cells.
  The Sector in this test has 2 possible source cells, cell 055950_3_2 with potential target cell 055950_3_9
  and cell 055950_3_4 with potential target cells 055950_3 and 055950_3_9.
  Highest ranked source cell is 055950_3_4.
  Cell_Availability is missing for target cell 055950_3 and null for cell 055950_3_9 so they are excluded.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_2A_Performance_and_Availability_Screener    |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.900317058100273  | 055950_3_9 |
      | KPI      | unhappy_users                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | goal_function_resource_efficiency | 0.739314794871641  | 055950_3   |
      | KPI      | unhappy_users                     | 0.645855356858811  | 055950_3   |
      | KPI      | goal_function_resource_efficiency | 0.528850312445342  | 055950_3_2 |
      | KPI      | unhappy_users                     | 1.54536953213271   | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency | 0.406027043555072  | 055950_3_4 |
      | KPI      | unhappy_users                     | 20.743089638662777 | 055950_3_4 |
      | KPI      | contiguity                        | 23.5632183908046   | 055950_3_4 |
      | KPI      | cell_availability                 | null               | 055950_3_9 |

    And Missing Mandatory Optimization Cells Data
      | dataType | dataName          | fdn      |
      | KPI      | cell_availability | 055950_3 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC3 - The KPI's are NULL for all cells.
  The KPI's are NULL for all target cells,(except cell_availability), the target cells are NOT excluded.
  The Sector in this test has 2 possible source cells, cell 055950_3_2 with potential target cell 055950_3_9
  and cell 055950_3_4 with potential target cells 055950_3 and 055950_3_9.
  Highest ranked source cell is 055950_3_4.
  RAN KPI's are missing for the target cells, the cells are not excluded.
  Optimization proceeds to the next state.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_3_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency                 | 0.900317058100273  | 055950_3_9 |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | null               | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr          | null               | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1 | null               | 055950_3_9 |
      | KPI      | cell_handover_success_rate                        | null               | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost               | null               | 055950_3_9 |
      | KPI      | goal_function_resource_efficiency                 | 0.739314794871641  | 055950_3   |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3   |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | null               | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr          | null               | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1 | null               | 055950_3_9 |
      | KPI      | cell_handover_success_rate                        | null               | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost               | null               | 055950_3_9 |
      | KPI      | goal_function_resource_efficiency                 | 0.528850312445342  | 055950_3_2 |
      | KPI      | unhappy_users                                     | 1.54536953213271   | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency                 | 0.406027043555072  | 055950_3_4 |
      | KPI      | unhappy_users                                     | 20.743089638662777 | 055950_3_4 |
      | KPI      | p_failing_r_mbps                                  | 0.2                | 055950_3_9 |
      | KPI      | p_failing_r_mbps                                  | 0.3                | 055950_3   |
      | KPI      | p_failing_r_mbps                                  | 0.9                | 055950_3_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055950_3_9    | 1               | 2               |
      | 055950_3      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055950_3_4    | 1               | 4               |

  Scenario: TC4 - Some of the KPI's are NULL for all target cells, thresholds are not breached, optimization proceeds.
  The KPI's :
  'initial_and_added_e_rab_establishment_sr_for_qci1'
  'cell_handover_success_rate'
  are 'NULL' for All target cells.
  The Sector in this test has 2 possible source cells, cell 055950_3_2 with potential target cells 055950_3_9
  and cell 055950_3_4 with potential target cells 055950_3 and 055950_3_9.
  Highest ranked source cell is 055950_3_4.
  The thresholds are not breached for any of the other RAN KPIs.
  Optimization proceeds to the next state.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_4_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency                 | 0.900317058100273  | 055950_3_9 |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | null               | 055950_3_9 |
      | KPI      | cell_handover_success_rate                        | null               | 055950_3_9 |
      | KPI      | goal_function_resource_efficiency                 | 0.739314794871641  | 055950_3   |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3   |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1 | null               | 055950_3_9 |
      | KPI      | cell_handover_success_rate                        | null               | 055950_3_9 |
      | KPI      | goal_function_resource_efficiency                 | 0.528850312445342  | 055950_3_2 |
      | KPI      | unhappy_users                                     | 1.54536953213271   | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency                 | 0.406027043555072  | 055950_3_4 |
      | KPI      | unhappy_users                                     | 20.743089638662777 | 055950_3_4 |
      | KPI      | p_failing_r_mbps                                  | 0.2                | 055950_3_9 |
      | KPI      | p_failing_r_mbps                                  | 0.3                | 055950_3   |
      | KPI      | p_failing_r_mbps                                  | 0.9                | 055950_3_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055950_3_9    | 1               | 2               |
      | 055950_3      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055950_3_4    | 1               | 4               |

  Scenario: TC5 - Some of the KPI's are NULL for all target cells, a threshold is breached for some targets cells, optimization proceeds.
  The Sector in this test has 2 possible source cells:
  - cell 055950_3_2 with potential target cells 055950_3_6 and 055950_3_9
  - cell 055950_3_4 with potential target cells 055950_3, 055950_3_6 and 055950_3_9
  Highest ranked source cell is 055950_3_2.
  A breach of any 1 threshold is enough to exclude the cell as target.
  Target cell 055950_3 is excluded as:
  initial_and_added_e_rab_establishment_sr < loadBalancingThresholdForInitialAndAddedErabEstabSuccRate

  Target Cell 055950_3_6 is excluded as :
  e_rab_retainability_percentage_lost > loadBalancingThresholdForErabPercentageLost

  Optimization proceeds to the next state with remaining ranked Source/Target cells.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |
      | 055950_3_6 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_5_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency                 | 0.828850312445342  | 055950_3_9 |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | null               | 055950_3_9 |
      | KPI      | cell_handover_success_rate                        | null               | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr          | 90.531719838650532 | 055950_3   |
      | KPI      | goal_function_resource_efficiency                 | 0.629314794871641  | 055950_3   |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3   |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1 | null               | 055950_3   |
      | KPI      | cell_handover_success_rate                        | null               | 055950_3   |
      | KPI      | goal_function_resource_efficiency                 | 0.406027043555072  | 055950_3_2 |
      | KPI      | unhappy_users                                     | 11.54536953213271  | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency                 | 0.300317058100273  | 055950_3_4 |
      | KPI      | unhappy_users                                     | 2.743089638662777  | 055950_3_4 |
      | KPI      | goal_function_resource_efficiency                 | 0.900317058100273  | 055950_3_6 |
      | KPI      | unhappy_users                                     | 20.743089638662777 | 055950_3_6 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | null               | 055950_3_6 |
      | KPI      | cell_handover_success_rate                        | null               | 055950_3_6 |
      | KPI      | e_rab_retainability_percentage_lost               | 2.259968382223784  | 055950_3_6 |
      | KPI      | p_failing_r_mbps                                  | 0.3                | 055950_3_9 |
      | KPI      | p_failing_r_mbps                                  | 0.7                | 055950_3_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055950_3_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055950_3_2    | 1               | 2               |

  Scenario: TC6 - Different thresholds are breached for all targets cells for a Source cell. All target cells are excluded, optimization proceeds.
  All the target cells are excluded so the Source Cell is also excluded.
  The Sector in this test has 2 possible source cells:
  - cell 055950_3_2 with potential target cells 055950_3_6 and 055950_3_9
  - cell 055950_3_4 with potential target cells 055950_3, 055950_3_6 and 055950_3_9
  Highest ranked source cell is 055950_3_2.

  Target Cell 055950_3_6 is excluded as :
  e_rab_retainability_percentage_lost > loadBalancingThresholdForErabPercentageLost

  Target Cell 055950_3_9 is excluded as :
  e_rab_retainability_percentage_lost_qci1 > loadBalancingThresholdForErabPercentageLostForQci1

  All target cells are excluded so Source Cell 055950_3_2 is excluded.
  Optimization proceeds to the next state with remaining ranked Source/Target cells.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |
      | 055950_3_6 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_6_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency                 | 0.828850312445342  | 055950_3_9 |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | 1.595855356858811  | 055950_3_9 |
      | KPI      | cell_handover_success_rate                        | 70.531719838650532 | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr          | 98.531719838650532 | 055950_3   |
      | KPI      | goal_function_resource_efficiency                 | 0.629314794871641  | 055950_3   |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3   |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1 | 98.645855356858811 | 055950_3   |
      | KPI      | cell_handover_success_rate                        | 79.531719838650532 | 055950_3   |
      | KPI      | goal_function_resource_efficiency                 | 0.406027043555072  | 055950_3_2 |
      | KPI      | unhappy_users                                     | 11.54536953213271  | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency                 | 0.300317058100273  | 055950_3_4 |
      | KPI      | unhappy_users                                     | 7.43089638662777   | 055950_3_4 |
      | KPI      | goal_function_resource_efficiency                 | 0.900317058100273  | 055950_3_6 |
      | KPI      | unhappy_users                                     | 20.743089638662777 | 055950_3_6 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | 2.0                | 055950_3_6 |
      | KPI      | cell_handover_success_rate                        | 70.0               | 055950_3_6 |
      | KPI      | e_rab_retainability_percentage_lost               | 2.259968382223784  | 055950_3_6 |
      | KPI      | p_failing_r_mbps                                  | 0.3                | 055950_3   |
      | KPI      | p_failing_r_mbps                                  | 0.7                | 055950_3_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055950_3      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055950_3_4    | 1               | 2               |

  Scenario: TC7 - Different thresholds are breached for all targets cells for all Source cells in the Sector.
  All Source/Targets in the Sector are excluded.
  Sector is excluded, no optimization is possible.

  The Sector in this test has 2 possible source cells:
  - cell 055950_3_2 with potential target cells 055950_3_6 and 055950_3_9
  - cell 055950_3_4 with potential target cells 055950_3, 055950_3_6 and 055950_3_9
  Highest ranked source cell is 055950_3_2.

  Target cell 055950_3 is excluded as:
  initial_and_added_e_rab_establishment_sr < loadBalancingThresholdForInitialAndAddedErabEstabSuccRate
  initial_and_added_e_rab_establishment_sr_for_qci1 < loadBalancingThresholdForErabPercentageLostForQci1
  cell_handover_success_rate < loadBalancingThresholdForCellHoSuccRate

  Target Cell 055950_3_6 is excluded as :
  e_rab_retainability_percentage_lost > loadBalancingThresholdForErabPercentageLost

  Target Cell 055950_3_9 is excluded as :
  e_rab_retainability_percentage_lost_qci1 > loadBalancingThresholdForErabPercentageLostForQci1
  cell_handover_success_rate < loadBalancingThresholdForCellHoSuccRate

  All target cells are excluded so no optimization is possible, Sector is excluded.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |
      | 055950_3_6 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_7_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency                 | 0.828850312445342  | 055950_3_9 |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | 1.505855356858811  | 055950_3_9 |
      | KPI      | cell_handover_success_rate                        | 69.531719838650532 | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr          | 90.531719838650532 | 055950_3   |
      | KPI      | goal_function_resource_efficiency                 | 0.629314794871641  | 055950_3   |
      | KPI      | unhappy_users                                     | 0.645855356858811  | 055950_3   |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1 | 97.645855356858811 | 055950_3   |
      | KPI      | cell_handover_success_rate                        | 69.531719838650532 | 055950_3   |
      | KPI      | goal_function_resource_efficiency                 | 0.406027043555072  | 055950_3_2 |
      | KPI      | unhappy_users                                     | 11.54536953213271  | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency                 | 0.300317058100273  | 055950_3_4 |
      | KPI      | unhappy_users                                     | 2.743089638662777  | 055950_3_4 |
      | KPI      | goal_function_resource_efficiency                 | 0.900317058100273  | 055950_3_6 |
      | KPI      | unhappy_users                                     | 20.743089638662777 | 055950_3_6 |
      | KPI      | e_rab_retainability_percentage_lost_qci1          | 2.0                | 055950_3_6 |
      | KPI      | cell_handover_success_rate                        | 70.0               | 055950_3_6 |
      | KPI      | e_rab_retainability_percentage_lost               | 2.259968382223784  | 055950_3_6 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC8 - Verify target cell exclusion when different thresholds set on different Source cells in the sector.
  Verify that targets are excluded correctly when the values for the threshold settings
  are different for the Source cells in the sector and some of the thresholds are breached

  The Sector in this test has 2 possible source cells:
  - cell 055950_3_2 with potential target cells 055950_3_6 and 055950_3_9
  - cell 055950_3_4 with potential target cells 055950_3, 055950_3_6 and 055950_3_9
  Highest ranked source cell is 055950_3_4.

  Target Cells 055950_3_6 and 055950_3_9 are excluded for Source cell 055950_3_4 as :
  e_rab_retainability_percentage_lost_qci1 > loadBalancingThresholdForErabPercentageLostForQci1

  Target Cell 055950_3_6 is excluded for Source cell 055950_3_2 as :
  cell_handover_success_rate < loadBalancingThresholdForCellHoSuccRate

  Optimization proceeds to the next state with remaining ranked Source/Target cells.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |
      | 055950_3_6 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_8_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                                           | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency                  | 0.828850312445342  | 055950_3_9 |
      | KPI      | unhappy_users                                      | 0.645855356858811  | 055950_3_9 |
      | KPI      | e_rab_retainability_percentage_lost_qci1           | 1.595855356858811  | 055950_3_9 |
      | KPI      | cell_handover_success_rate                         | 70.531719838650532 | 055950_3_9 |
      | KPI      | initial_and_added_e_rab_establishment_sr           | 98.531719838650532 | 055950_3   |
      | KPI      | goal_function_resource_efficiency                  | 0.629314794871641  | 055950_3   |
      | KPI      | unhappy_users                                      | 0.645855356858811  | 055950_3   |
      | KPI      | initial_and_added_e_rab_establishment_sr_for_qci1  | 98.645855356858811 | 055950_3   |
      | KPI      | cell_handover_success_rate                         | 79.531719838650532 | 055950_3   |
      | KPI      | goal_function_resource_efficiency                  | 0.406027043555072  | 055950_3_2 |
      | KPI      | unhappy_users                                      | 11.54536953213271  | 055950_3_2 |
      | SETTING  | loadBalancingThresholdForCellHoSuccRate            | 65.9               | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency                  | 0.300317058100273  | 055950_3_4 |
      | KPI      | unhappy_users                                      | 27.43089638662777  | 055950_3_4 |
      | SETTING  | loadBalancingThresholdForErabPercentageLostForQci1 | 1.59               | 055950_3_4 |
      | KPI      | goal_function_resource_efficiency                  | 0.900317058100273  | 055950_3_6 |
      | KPI      | unhappy_users                                      | 20.743089638662777 | 055950_3_6 |
      | KPI      | e_rab_retainability_percentage_lost_qci1           | 2.0                | 055950_3_6 |
      | KPI      | cell_handover_success_rate                         | 65.0               | 055950_3_6 |
      | KPI      | e_rab_retainability_percentage_lost                | 2.259968382223784  | 055950_3_6 |
      | KPI      | p_failing_r_mbps                                   | 0.3                | 055950_3   |
      | KPI      | p_failing_r_mbps                                   | 0.7                | 055950_3_4 |


    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055950_3      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055950_3_4    | 1               | 2               |

  Scenario: TC9 - Verify target cell exclusion when different thresholds set on different Source cells in the sector.
  Verify that targets are excluded correctly when the values for the threshold settings
  are different for the Source cells in the sector and some of the thresholds are breached

  The Sector in this test has 2 possible source cells:
  - cell 055950_3_2 with potential target cells 055950_3_6 and 055950_3_9
  - cell 055950_3_4 with potential target cells 055950_3, 055950_3_6 and 055950_3_9
  Highest ranked source cell is 055950_3_4.

  Target Cell 055950_3 is excluded for Source cell 055950_3_4 as :
  cell_availability < loadBalancingThresholdForCellAvailability

  Optimization proceeds to the next state with remaining ranked Source/Target cells.

    Given Create Default Optimization Cells
      | 055950_3_9 |
      | 055950_3   |
      | 055950_3_2 |
      | 055950_3_4 |
      | 055950_3_6 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_9_Performance_and_Availability_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue          | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.828850312445342  | 055950_3_9 |
      | KPI      | unhappy_users                     | 0.645855356858811  | 055950_3_9 |
      | KPI      | goal_function_resource_efficiency | 0.629314794871641  | 055950_3   |
      | KPI      | unhappy_users                     | 0.645855356858811  | 055950_3   |
      | KPI      | cell_availability                 | 69.531719838650532 | 055950_3   |
      | KPI      | goal_function_resource_efficiency | 0.406027043555072  | 055950_3_2 |
      | KPI      | unhappy_users                     | 11.54536953213271  | 055950_3_2 |
      | KPI      | goal_function_resource_efficiency | 0.300317058100273  | 055950_3_4 |
      | KPI      | unhappy_users                     | 27.43089638662777  | 055950_3_4 |
      | KPI      | goal_function_resource_efficiency | 0.900317058100273  | 055950_3_6 |
      | KPI      | unhappy_users                     | 20.743089638662777 | 055950_3_6 |
      | KPI      | p_failing_r_mbps                  | 0.2                | 055950_3_6 |
      | KPI      | p_failing_r_mbps                  | 0.3                | 055950_3_9 |
      | KPI      | p_failing_r_mbps                  | 0.9                | 055950_3_4 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055950_3_6    | 1               | 2               |
      | 055950_3_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055950_3_4    | 1               | 4               |
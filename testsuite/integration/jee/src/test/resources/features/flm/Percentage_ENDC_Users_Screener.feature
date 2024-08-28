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

Feature: FLM_PercentageENDCUsersScreener
  The purpose of this policy state is to screen out target cells where
  Target's Percentage ENDC Users is less than the setting "settings/lb_threshold_for_endc_users".

  Input to this state:
  Ranked list of potential Source cells and their possible target cells

  Output from this state:
  The output from this state will be the input ranked list of potential source cells and their associated target cells
  with the target cells not meeting the threshold screened out.
  If all target cells for all potential source cells are screened out then the sector is removed from optimization
  and an empty LBQ is returned.

  The kpi used:
  percentage_endc_users

  The settings used:
  lb_threshold_for_endc_users


  Scenario: TC1 - Target Percentage ENDC Users higher than threshold for 1 target cell in the sector.
  The threshold setting is the same for all cells in the sector.
  The target cell is excluded as a possible target for that source cell.
  Source cell has no possible targets remaining and is excluded too.

  The Sector in this test has 2 possible source cells ranked as follows,
  1. cell 055550_1 with potential target cells 055550_1_9, 055550_2_2
  2. cell 055550_2 with potential target cells 055550_1_9
  Target Percentage ENDC Users is higher than the threshold for target cells 055550_1_9 so it is removed from optimization.
  As source cell 055550_2 has no remaining possible target cells, it is also removed from optimization.

    Given Create Default Optimization Cells
      | 055550_2   |
      | 055550_1   |
      | 055550_1_9 |
      | 055550_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173123459656102600 | TC_1_PercentageENDCUsersScreener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue           | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.458874105661037   | 055550_1   |
      | KPI      | unhappy_users                     | 0.592511239070919   | 055550_1   |
      | KPI      | percentage_endc_users             | 23.405064374507     | 055550_1   |
      | KPI      | goal_function_resource_efficiency | 0.658874105661037   | 055550_2   |
      | KPI      | unhappy_users                     | 25.052511239070919  | 055550_2   |
      | KPI      | percentage_endc_users             | 3.33333333333333    | 055550_2   |
      | KPI      | goal_function_resource_efficiency | 0.966352774049607   | 055550_1_9 |
      | KPI      | unhappy_users                     | 76.0995926937517    | 055550_1_9 |
      | KPI      | percentage_endc_users             | 53.40506114507      | 055550_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.946352774049607   | 055550_2_2 |
      | KPI      | unhappy_users                     | 78.24862155388474   | 055550_2_2 |
      | KPI      | percentage_endc_users             | 18.4098331507027    | 055550_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.3                 | 055550_2_2 |
      | KPI      | p_failing_r_mbps                  | 0.7                 | 055550_1   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 055550_2_2    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 055550_1      | 1               | 2               |

  Scenario: TC2 - Target Percentage ENDC Users less than threshold for all target cells in the sector.
  No target cells are removed. All source and target cells are selected for optimization.

  The Sector in this test had 1 possible source cell,
  - cell 051235_2 with potential target cells 051235_1_4 and 051235_1_9

    Given Create Default Optimization Cells
      | 051235_2   |
      | 051235_1_4 |
      | 051235_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173291189656102501 | TC_2_PercentageENDCUsersScreener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.364909938121602 | 051235_2   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 051235_2   |
      | KPI      | percentage_endc_users             | 80.0              | 051235_2   |
      | KPI      | goal_function_resource_efficiency | 0.667027043555072 | 051235_1_4 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 051235_1_4 |
      | KPI      | percentage_endc_users             | 14.7196261682243  | 051235_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 051235_1_9 |
      | KPI      | unhappy_users                     | 1.54536953213271  | 051235_1_9 |
      | KPI      | percentage_endc_users             | 39.8121212        | 051235_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 051235_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.2               | 051235_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.9               | 051235_2   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 051235_1_4    | 1               | 2               |
      | 051235_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 051235_2      | 1               | 4               |


  Scenario: TC3 - Target Percentage ENDC Users higher than threshold for all of the target cells in the sector.
  The sector is moved from optimization and an empty LBQ is returned.

  The Sector in this test had 2 possible source cells,
  - cell 051234_2 with potential target cells 051234_1_4 and 051234_1_9
  - cell 051234_2_2 with potential target cells 051234_1_4 and 051234_1_9

  Cell 051234_1_4 is removed as percentage_endc_users kpi has a value 0.0.
  Cell 051234_1_9 is removed as percentage_endc_users kpi has a value 0.0.
  Source cells are removed as they dont have any potential target cells.
  All possible source cell have been removed so the sector is removed from optimization and an empty LBQ is returned.

    Given Create Default Optimization Cells
      | 051234_2   |
      | 051234_1_4 |
      | 051234_1_9 |
      | 051234_2_2 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173291189656102503 | TC_3_PercentageENDCUsersScreener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.358874105661037 | 051234_2   |
      | KPI      | unhappy_users                     | 90.59251123907091 | 051234_2   |
      | KPI      | percentage_endc_users             | 49.0              | 051234_2   |
      | KPI      | goal_function_resource_efficiency | 0.867027043555072 | 051234_1_4 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 051234_1_4 |
      | KPI      | percentage_endc_users             | 84.7196261682243  | 051234_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 051234_1_9 |
      | KPI      | unhappy_users                     | 13.4191211495669  | 051234_1_9 |
      | KPI      | percentage_endc_users             | 59.8121212        | 051234_1_9 |
      | KPI      | goal_function_resource_efficiency | 0.534909938121602 | 051234_2_2 |
      | KPI      | unhappy_users                     | 89.59251123907091 | 051234_2_2 |
      | KPI      | percentage_endc_users             | 31.0              | 051234_2_2 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC4 - Target Percentage ENDC Users is missing in a target cell.
  Screening is skipped for that target cell.

  The Sector in this test had 1 possible source cell,
  - cell 051238_2 with potential target cells 051238_1_4 and 051238_1_9

    Given Create Default Optimization Cells
      | 051238_2   |
      | 051238_1_4 |
      | 051238_1_9 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173277554656102501 | TC_4_PercentageENDCUsersScreener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.364909938121602 | 051238_2   |
      | KPI      | unhappy_users                     | 0.592511239070919 | 051238_2   |
      | KPI      | percentage_endc_users             | 80.0              | 051238_2   |
      | KPI      | goal_function_resource_efficiency | 0.667027043555072 | 051238_1_4 |
      | KPI      | unhappy_users                     | 0.645855356858811 | 051238_1_4 |
      | KPI      | percentage_endc_users             | 14.7196261682243  | 051238_1_4 |
      | KPI      | goal_function_resource_efficiency | 0.729314794871641 | 051238_1_9 |
      | KPI      | unhappy_users                     | 1.54536953213271  | 051238_1_9 |
      | KPI      | percentage_endc_users             | null              | 051238_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.3               | 051238_1_4 |
      | KPI      | p_failing_r_mbps                  | 0.2               | 051238_1_9 |
      | KPI      | p_failing_r_mbps                  | 0.9               | 051238_2   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 051238_1_4    | 1               | 2               |
      | 051238_1_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 051238_2      | 1               | 4               |
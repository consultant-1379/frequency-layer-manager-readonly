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

Feature: FLM_ESS_Screener
  The purpose of this policy state is to screen out target cells that are identified as
  ESS cells(lteNrSpectrumShared) when the boolean setting ess_enabled is true

  Input to this state:
  Ranked list of potential Source cells and their possible target cells

  Output from this state:
  The output from this state will be the input ranked list of potential source cells and their associated target cells
  with the target cells not meeting the threshold screened out.
  If all target cells for all potential source cells are screened out then the sector is removed from optimization
  and an empty LBQ is returned.

  The cmAttribute used:
  lteNrSpectrumShared

  The settings used:
  ess_enabled


  Scenario: TC1 - Target lteNrSpectrumShared is set to yes for 1 target cell in the sector.
  The threshold setting is the same for all cells in the sector.
  The target cell is excluded as a possible target for that source cell.

  The Sector in this test has 2 possible source cells ranked as follows,
  1. cell 040740_1 with potential target cells 040740_1_9, 040740_2_2
  2. cell 040740_2 with potential target cells 040740_1_9
  Target lteNrSpectrumShared is yes for target cells 040740_1_9 so it is removed from optimization.
  As source cell 040740_2 has no remaining possible target cells, it is also removed from optimization.

     Given Create Default Optimization Cells
       | 040740_2   |
       | 040740_1   |
       | 040740_1_9 |
       | 040740_2_2 |

     And Policy Input Event
       | sectorId           | executionId |
       | 173123459656102600 | TC_1_ESS_Screener      |

     And Set Optimization Cells Data
       | dataType      | dataName                          | dataValue           | fdn        |
       | KPI           | goal_function_resource_efficiency | 0.458874105661037   | 040740_1   |
       | KPI           | unhappy_users                     | 0.592511239070919   | 040740_1   |
       | CM_ATTRIBUTE  | lteNrSpectrumShared               | no                  | 040740_1   |
       | SETTING       | ess_enabled                       | t                   | 040740_1   |
       | KPI           | goal_function_resource_efficiency | 0.658874105661037   | 040740_2   |
       | KPI           | unhappy_users                     | 25.052511239070919  | 040740_2   |
       | CM_ATTRIBUTE  | lteNrSpectrumShared               | no                  | 040740_2   |
       | SETTING       | ess_enabled                       | t                   | 040740_2   |
       | KPI           | goal_function_resource_efficiency | 0.966352774049607   | 040740_1_9 |
       | KPI           | unhappy_users                     | 76.0995926937517    | 040740_1_9 |
       | CM_ATTRIBUTE  | lteNrSpectrumShared               | yes                 | 040740_1_9 |
       | SETTING       | ess_enabled                       | t                   | 040740_1_9 |
       | KPI           | goal_function_resource_efficiency | 0.946352774049607   | 040740_2_2 |
       | KPI           | unhappy_users                     | 78.24862155388474   | 040740_2_2 |
       | CM_ATTRIBUTE  | lteNrSpectrumShared               | no                  | 040740_2_2 |
       | SETTING       | ess_enabled                       | t                   | 040740_2_2 |
       | KPI           | p_failing_r_mbps                  | 0.3                 | 040740_2_2 |
       | KPI           | p_failing_r_mbps                  | 0.7                 | 040740_1   |

     When Putting Policy Input Event onto Kafka Topic

     Then Optimization proceeds with the following target cells
       | targetCellFdn | targetCellOssId | targetUsersMove |
       | 040740_2_2    | 1               | 2               |

     Then Optimization proceeds with the following source cells
       | sourceCellFdn | sourceCellOssId | sourceUsersMove |
       | 040740_1      | 1               | 2               |

  Scenario: TC2 - Target lteNrSpectrumShared is no for all target cells for all source cells in the sector.
  The Sector in this test had 1 possible source cell,
  - cell 043840_2 with potential target cells 043840_1_4 and 043840_1_9

  No target cells are removed. All source and target cells are selected for optimization.

     Given Create Default Optimization Cells
       | 043840_2   |
       | 043840_1_4 |
       | 043840_1_9 |

     And Policy Input Event
       | sectorId           | executionId |
       | 173291196656102521 | TC_2_ESS_Screener      |

     And Set Optimization Cells Data
       | dataType     | dataName                          | dataValue         | fdn        |
       | KPI          | goal_function_resource_efficiency | 0.364909938121602 | 043840_2   |
       | KPI          | unhappy_users                     | 0.592511239070919 | 043840_2   |
       | CM_ATTRIBUTE | lteNrSpectrumShared               | no                | 043840_2   |
       | SETTING      | ess_enabled                       | t                 | 043840_2   |
       | KPI          | goal_function_resource_efficiency | 0.667027043555072 | 043840_1_4 |
       | KPI          | unhappy_users                     | 0.645855356858811 | 043840_1_4 |
       | CM_ATTRIBUTE | lteNrSpectrumShared               | no                | 043840_1_4 |
       | SETTING      | ess_enabled                       | t                 | 043840_1_4 |
       | KPI          | goal_function_resource_efficiency | 0.729314794871641 | 043840_1_9 |
       | KPI          | unhappy_users                     | 1.54536953213271  | 043840_1_9 |
       | CM_ATTRIBUTE | lteNrSpectrumShared               | no                | 043840_1_9 |
       | SETTING      | ess_enabled                       | t                 | 043840_1_9 |
       | KPI          | p_failing_r_mbps                  | 0.3               | 043840_1_4 |
       | KPI          | p_failing_r_mbps                  | 0.2               | 043840_1_9 |
       | KPI          | p_failing_r_mbps                  | 0.9               | 043840_2   |

     When Putting Policy Input Event onto Kafka Topic

     Then Optimization proceeds with the following target cells
       | targetCellFdn | targetCellOssId | targetUsersMove |
       | 043840_1_4    | 1               | 2               |
       | 043840_1_9    | 1               | 2               |

     Then Optimization proceeds with the following source cells
       | sourceCellFdn | sourceCellOssId | sourceUsersMove |
       | 043840_2      | 1               | 4               |

  Scenario: TC3 - Target lteNrSpectrumShared is yes for all of the target cells in the sector.
  The sector is moved from optimization and an empty LBQ is returned.

  The Sector in this test had 2 possible source cells,
  - cell 039930_2 with potential target cells 039930_1_4 and 039930_1_9
  - cell 039930_2_2 with potential target cells 039930_1_4 and 039930_1_9

  Cell 039930_1_4 is removed as cmAttribute lteNrSpectrumShared has a value yes
  Cell 039930_1_9 is removed as cmAttribute lteNrSpectrumShared has a value yes
  Source cells are removed as they dont have any potential target cells.
  All possible source cell have been removed so the sector is removed from optimization and an empty LBQ is returned.

     Given Create Default Optimization Cells
       | 039930_2   |
       | 039930_1_4 |
       | 039930_1_9 |
       | 039930_2_2 |

     And Policy Input Event
       | sectorId           | executionId |
       | 173291177656102563 | TC_3_ESS_Screener      |

     And Set Optimization Cells Data
       | dataType      | dataName                          | dataValue         | fdn        |
       | KPI           | goal_function_resource_efficiency | 0.358874105661037 | 039930_2   |
       | KPI           | unhappy_users                     | 90.59251123907091 | 039930_2   |
       | CM_ATTRIBUTE  | lteNrSpectrumShared               | no                | 039930_2   |
       | SETTING       | ess_enabled                       | t                 | 039930_2   |
       | KPI           | goal_function_resource_efficiency | 0.867027043555072 | 039930_1_4 |
       | KPI           | unhappy_users                     | 13.4191211495669  | 039930_1_4 |
       | CM_ATTRIBUTE  | lteNrSpectrumShared               | yes               | 039930_1_4 |
       | SETTING       | ess_enabled                       | t                 | 039930_1_4 |
       | KPI           | goal_function_resource_efficiency | 0.729314794871641 | 039930_1_9 |
       | KPI           | unhappy_users                     | 13.4191211495669  | 039930_1_9 |
       | CM_ATTRIBUTE  | lteNrSpectrumShared               | yes               | 039930_1_9 |
       | SETTING       | ess_enabled                       | t                 | 039930_1_9 |
       | KPI           | goal_function_resource_efficiency | 0.534909938121602 | 039930_2_2 |
       | KPI           | unhappy_users                     | 89.59251123907091 | 039930_2_2 |
       | CM_ATTRIBUTE  | lteNrSpectrumShared               | no                | 039930_2_2 |
       | SETTING       | ess_enabled                       | t                 | 039930_2_2 |

     When Putting Policy Input Event onto Kafka Topic

     Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty
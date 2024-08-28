#*------------------------------------------------------------------------------
#******************************************************************************
# COPYRIGHT Ericsson 2020 - 2021
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#******************************************************************************
#------------------------------------------------------------------------------
@RunAllTests
Feature: FLM_GFS_Screener
  The Goal Function Delta Sector Screen policy step does the following:
  Sectors that have an inconsistent value of R across cells are screened out
  Sectors that have an inconsistent value of Goal functions Score Delta Optimization Threshold across cells are screened out
  The Delta GFS for each sector, which will be the difference between the maximum and minimum values of cell GFS found in the sector,
  will be compared with a configurable threshold and those sectors where the value exceeds the threshold will proceed to optimization.

  The kpi used:
  goal_function_resource_efficiency

  The settings used:
  target_throughput_r
  delta_gfs_optimization_threshold


  Scenario: TC_1 - Optimization Cells with One cell
  One cell should not pass the Delta GF Screening as difference between the min and max GF values never exceeds the GF
  threshold.

    Given Optimization Cells sector_with_one_cell

    And Policy Input Event
      | sectorId | executionId |
      | 101      | TC_1_GFS_Screener       |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC_2 - Optimization Cells with GF difference less than GF Threshold
  Two or more cells when the GF difference is less than GF Threshold the sector should be screened out and an empty
  LBQ should be returned in the output.

    Given Optimization Cells delta_gfs_difference_below_threshold

    And Policy Input Event
      | sectorId | executionId |
      | 101      | TC_2_GFS_Screener       |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC_3 - Optimization Cells with GF difference greater than GF Threshold
  Two or more cells when the GF difference is above than GF Threshold the sector should be processed and an LBQ should
  be returned fully populated.

    Given Create Default Optimization Cells
      | 001 |
      | 002 |

    And Policy Input Event
      | sectorId | executionId |
      | 101      | TC_3_GFS_Screener      |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue        | fdn |
      | KPI      | goal_function_resource_efficiency | 0.24018373040423 | 001 |
      | KPI      | goal_function_resource_efficiency | 0.64018373040423 | 002 |
      | KPI      | p_failing_r_mbps                  | 0.3              | 002 |
      | KPI      | p_failing_r_mbps                  | 0.7              | 001 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 002           | 1               | 2               |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 001           | 1               | 2               |


  Scenario: TC_4 - Optimization Cells with GF difference equal to GF Threshold
  Two or more cells when the GF difference is equal to than GF Threshold the sector should be processed and an LBQ
  should be returned fully populated.

    Given Create Default Optimization Cells
      | 001 |
      | 002 |

    And Policy Input Event
      | sectorId | executionId |
      | 101      | TC_4_GFS_Screener       |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue        | fdn |
      | KPI      | goal_function_resource_efficiency | 0.34018373040423 | 001 |
      | KPI      | goal_function_resource_efficiency | 0.64018373040423 | 002 |
      | KPI      | p_failing_r_mbps                  | 0.3              | 002 |
      | KPI      | p_failing_r_mbps                  | 0.7              | 001 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 002           | 1               | 2               |

    And Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 001           | 1               | 2               |


  Scenario: TC_5 - Optimization Cells with inconsistent target_throughput_r value
  The target_throughput_r setting is not the same for all cells the sector will be screened out and an empty LBQ
  will be returned.

    Given Optimization Cells delta_gfs_inconsistent_target_throughput_r

    And Policy Input Event
      | sectorId | executionId |
      | 101      | TC_5_GFS_Screener       |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC_6 - Optimization Cells with inconsistent delta_gfs_optimization_threshold value
  The delta_gfs_optimization_threshold setting is not the same for all cells the sector will be screened out and an
  empty LBQ will be returned.

    Given Optimization Cells delta_gfs_inconsistent_threshold

    And Policy Input Event
      | sectorId | executionId |
      | 101      | TC_6_GFS_Screener       |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC_7 - Optimization Cells with inconsistent target_throughput_r and delta_gfs_optimization_threshold values
  The delta_gfs_optimization_threshold and delta_gfs_optimization_threshold setting is not the same for all
  cells the sector will be screened out and an empty LBQ will be returned.

    Given Optimization Cells delta_gfs_and_target_throughput_r_inconsistent

    And Policy Input Event
      | sectorId | executionId |
      | 101      | TC_7_GFS_Screener       |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty
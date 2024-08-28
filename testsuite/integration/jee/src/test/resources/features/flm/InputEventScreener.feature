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

Feature: FLM_InputEventScreener
  The Input Event Screener policy step does the following:
  Screens out any cells with missing or have "null" values for the mandatory KPI, settings or CM attributes.
  A message is logged indicating the cell is excluded and the cell is not passed on to the next stage for optimization.


  The kpi used:
  p_failing_r_mbps
  goal_function_resource_efficiency
  unhappy_users
  contiguity
  coverage_balance_ratio_distance
  distance_q1
  distance_q2
  distance_q3
  distance_q4
  connected_users
  app_coverage_reliability
  kpi_cell_reliability_daily

  The settings used:
  target_throughput_r
  delta_gfs_optimization_threshold
  percentile_for_max_connected_user
  min_num_cell_for_cdf_calculation
  qos_for_capacity_estimation
  num_calls_cell_hourly_reliability_threshold_in_hours

  The CM attributed:
  bandwidth

  Scenario: TC1 - Cell excluded from further optimization if mandatory KPI Missing.
  For each of the mandatory KPIs remove the KPI and verify the cell is excluded.  The KPI can removed by deleting the kpi row for the cell.
  For each of the mandatory KPIs set the KPI value to "null" and verify the cell is excluded.

    Given Create Default Optimization Cells
      | cellFdn   |
      | 001       |
      | 002       |

    And Policy Input Event
      | sectorId           | executionId |
      | 123456789012345678 | TC1_InputEventScreener       |

    And Missing Mandatory Optimization Cells Data
      | dataType | dataName         | fdn |
      | KPI      | p_failing_r_mbps | 001 |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue | fdn |
      | KPI      | goal_function_resource_efficiency | null      | 002 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC2 - Cell excluded due to missing or empty setting.
  For each of the settings remove the setting and verify the cell is excluded.
  For each of the settings set the setting value to "null" and verify the cell is excluded.
  Both of these scenarios should not occur in an FLM execution so will be covered by cucumber tests only

    Given Create Default Optimization Cells
      | cellFdn   |
      | 001       |
      | 002       |

    And Policy Input Event
      | sectorId           | executionId |
      | 123456789012345678 | TC2_InputEventScreener       |

    And Missing Mandatory Optimization Cells Data
      | dataType | dataName            | fdn |
      | SETTING  | target_throughput_r | 001 |

    And Set Optimization Cells Data
      | dataType | dataName                         | dataValue | fdn |
      | SETTING  | delta_gfs_optimization_threshold | null      | 002 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC3 - Cell excluded due to missing or empty cm data
  For each of the cm data remove the setting and verify the cell is excluded.
  For each of the cm data set the setting value to "null" and verify the cell is excluded.
  Both of these scenarios should not occur in an FLM execution so will be covered by cucumber tests only

    Given Create Default Optimization Cells
      | cellFdn   |
      | 001       |
      | 002       |

    And Policy Input Event
      | sectorId           | executionId |
      | 123456789012345678 | TC3_InputEventScreener       |

    And Missing Mandatory Optimization Cells Data
      | dataType     | dataName  | fdn |
      | CM_ATTRIBUTE | bandwidth | 001 |

    And Set Optimization Cells Data
      | dataType     | dataName  | dataValue | fdn |
      | CM_ATTRIBUTE | bandwidth | null      | 002 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC4 - Sector excluded from further optimization if all cells are excluded.
  Each of the 6 cells in the sector is missing or has an empty KPI, Setting or CM Attribute.
  As all cells are excluded then we get an empty LBQ

    Given Create Default Optimization Cells
      | cellFdn                    |
      | cell_missing_kpi           |
      | cell_missing_setting       |
      | cell_missing_cm_data       |
      | cell_empty_kpi             |
      | cell_empty_setting         |
      | cell_missing_setting       |
      | cell_empty_cm_data         |

    And Policy Input Event
      | sectorId           | executionId |
      | 123456789012345678 | TC4_InputEventScreener       |

    And Missing Mandatory Optimization Cells Data
      | dataType | dataName            | fdn                  |
      | KPI      | p_failing_r_mbps    | cell_missing_kpi     |
      | SETTING  | target_throughput_r | cell_missing_setting |
      | SETTING  | bandwidth           | cell_missing_cm_data |

    And Set Optimization Cells Data
      | dataType | dataName            | dataValue | fdn                  |
      | KPI      | p_failing_r_mbps    | null      | cell_empty_kpi       |
      | SETTING  | target_throughput_r | null      | cell_missing_setting |
      | SETTING  | bandwidth           | null      | cell_missing_cm_data |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC5 - one Cell excluded due to cell reliability is less than threshold value

    Given Create Default Optimization Cells
      | 001       |
      | 002       |

    And Policy Input Event
      | sectorId           | executionId |
      | 123456789012345678 | TC5_InputEventScreener       |

    And Set Optimization Cells Data
      | dataType | dataName                                               | dataValue | fdn |
      | SETTING  | num_calls_cell_hourly_reliability_threshold_in_hours   | 20        | 002 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC6 - Two Cell excluded due to cell reliability is less than threshold value

    Given Create Default Optimization Cells
      | 001       |
      | 002       |

    And Policy Input Event
      | sectorId           | executionId |
      | 346592843081761069 | TC6_InputEventScreener       |

    And Set Optimization Cells Data
      | dataType | dataName                                               | dataValue | fdn |
      | SETTING  | num_calls_cell_hourly_reliability_threshold_in_hours   | 20        | 001 |
      | SETTING  | num_calls_cell_hourly_reliability_threshold_in_hours   | 20        | 002 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

    And Sector excluded due to cell reliability threshold not met
      | sectorId           | executionId |
      | 346592843081761069 | TC6_InputEventScreener      |
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

Feature: FLM_PA_determineDegradationStatus
  The performance assurance determine degradations status state does the following:
  Determines if any sector level KPI has violated the provided threshold provided the KPI is enabled.
  Determines if any cell level KPI has violated the provided threshold provided the relevance threshold is also violated
  and the KPI is enabled.
  A degradation status is returned providing a final verdict as well as any degraded KPIs.

  Scenario: IT_TC_01 whenInputEventProvided_andSectorLevelKpisHaveDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput
  The output verdict is "DEGRADED". Degraded sector level KPIs and timestamps are listed in the degradation status

    Given The Cells In The Sector
      | fdn      | kpiName            | relevanceThresholdType | relevanceThreshold | enabled | value | timestamp             | threshold  |
      | 054950_1 | test_cell_kpi_name | MIN                    | 95                 | true    | 97    | 1970-01-01 00:00:00.0 | 96         |
      | 054950_2 | test_cell_kpi_name | MIN                    | 95                 | true    | 97    | 1970-01-01 00:01:00.0 | 96         |

    And The Sector For The PA Input Event
      | sectorId           | settingValue | kpiName              | enabled | value | timestamp             | threshold |
      | 173290459927812150 | 1            | test_sector_kpi_name | true    | 0.5   | 1970-01-01 00:00:00.0 | 0.6       |

    And The PA Policy Input Event
      | executionId | paExecutionId |
      | PA_TC_1     | PA_TC_1_1     |

    When The PA Policy Input event Is Published To Kafka

    Then The PA Policy Output Event Shows The KPIs That Have Degraded
      | kpiLevel | kpiName              | topologyId         | timestamp             |
      | sector   | test_sector_kpi_name | 173290459927812150 | 1970-01-01 00:00:00.0 |

  Scenario: IT_TC_02 whenInputEventProvided_andCellLevelKpisHaveDegraded_thenSectorIsMarkedAsDegraded_andKpisAreAddedToOutput
  The output verdict is "DEGRADED". Degraded cell level KPIs and timestamps are listed in the degradation status

    Given The Cells In The Sector
      | fdn      | kpiName            | relevanceThresholdType | relevanceThreshold  | enabled | value | timestamp             | threshold  |
      | 054950_1 | test_cell_kpi_name | MIN                    | 100                 | true    | 97    | 1970-01-01 00:00:00.0 | 98         |
      | 054950_2 | test_cell_kpi_name | MIN                    | 95                  | true    | 97    | 1970-01-01 00:01:00.0 | 96         |

    And The Sector For The PA Input Event
      | sectorId           | settingValue | kpiName              | enabled | value | timestamp             | threshold |
      | 173290459927812150 | 1            | test_sector_kpi_name | true    | 0.6   | 1970-01-01 00:00:00.0 | 0.5       |

    And The PA Policy Input Event
      | executionId | paExecutionId |
      | PA_TC_2     | PA_TC_2_1     |

    When The PA Policy Input event Is Published To Kafka

    Then The PA Policy Output Event Shows The KPIs That Have Degraded
      | kpiLevel  | kpiName              | topologyId         | timestamp             |
      | cell      | test_cell_kpi_name   | 054950_1           | 1970-01-01 00:00:00.0 |

  Scenario: IT_TC_03 whenInputEventProvided_andNoKpisHaveDegraded_thenSectorIsMarkedAsNotDegraded
  The output verdict is "NOT DEGRADED".

    Given The Cells In The Sector
      | fdn      | kpiName            | relevanceThresholdType | relevanceThreshold | enabled | value | timestamp             | threshold  |
      | 054950_1 | test_cell_kpi_name | MIN                    | 95                 | true    | 97    | 1970-01-01 00:00:00.0 | 96         |
      | 054950_2 | test_cell_kpi_name | MIN                    | 95                 | true    | 97    | 1970-01-01 00:01:00.0 | 96         |

    And The Sector For The PA Input Event
      | sectorId           | settingValue | kpiName              | enabled | value | timestamp             | threshold |
      | 173290459927812150 | 1            | test_sector_kpi_name | true    | 0.6   | 1970-01-01 00:00:00.0 | 0.5       |

    And The PA Policy Input Event
      | executionId | paExecutionId |
      | PA_TC_3     | PA_TC_3_1     |

    When The PA Policy Input event Is Published To Kafka

    Then The PA Policy Output Event Shows No KPIs Have Degraded


  Scenario: IT_TC_04 whenInputEventProvided_andDisabledCellLevelKpisHaveDegraded_thenTheyAreNotAddedToTheOutputEvent
  The output verdict is "NOT DEGRADED".

    Given The Cells In The Sector
      | fdn      | kpiName            | relevanceThresholdType | relevanceThreshold | enabled  | value | timestamp             | threshold  |
      | 054950_1 | test_cell_kpi_name | MIN                    | 100                | false    | 95    | 1970-01-01 00:00:00.0 | 96         |
      | 054950_2 | test_cell_kpi_name | MIN                    | 100                | false    | 95    | 1970-01-01 00:01:00.0 | 96         |

    And The Sector For The PA Input Event
      | sectorId           | settingValue | kpiName              | enabled | value | timestamp              | threshold |
      | 173290459927812150 | 1            | test_sector_kpi_name | false   | 0.5    | 1970-01-01 00:00:00.0 | 0.6       |

    And The PA Policy Input Event
      | executionId | paExecutionId |
      | PA_TC_4     | PA_TC_4_1     |

    When The PA Policy Input event Is Published To Kafka

    Then The PA Policy Output Event Shows No KPIs Have Degraded


  Scenario: IT_TC_05 whenInputEventProvided_andlKpiValuesAreNull_thenSectorIsMarkedAsNotDegraded
  The output verdict is "NOT DEGRADED".

    Given The Cells In The Sector
      | fdn      | kpiName            | relevanceThresholdType | relevanceThreshold | enabled | value   | timestamp             | threshold  |
      | 054950_1 | test_cell_kpi_name | MIN                    | 100                | true    | null    | 1970-01-01 00:00:00.0 | 96         |
      | 054950_2 | test_cell_kpi_name | MIN                    | 100                | true    | null    | 1970-01-01 00:01:00.0 | 96         |

    And The Sector For The PA Input Event
      | sectorId           | settingValue | kpiName              | enabled | value | timestamp               | threshold |
      | 173290459927812150 | 1            | test_sector_kpi_name | true    | null    | 1970-01-01 00:00:00.0 | 0.5       |

    And The PA Policy Input Event
      | executionId | paExecutionId |
      | PA_TC_5     | PA_TC_5_1     |

    When The PA Policy Input event Is Published To Kafka

    Then The PA Policy Output Event Shows No KPIs Have Degraded
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

Feature: FLM_AppCoverageReliabilityScreener
  The App Coverage Reliability Screener policy step does the following:
  Screens out any cells where the 'app_coverage_reliability' KPI is not true.
  A message is logged indicating the cell is excluded and the cell is not passed on to the next stage for optimization.

  Scenario: TC1 Imbalanced Sector with 3 Cells Reliable for App Coverage
  All cells should pass through the App Coverage Reliability Screener and proceed with optimization

    Given Create Default Optimization Cells
      | fdn        |
      | 054950_1   |
      | 054950_2   |
      | 054950_3   |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC1_AppCoverageReliabilityScreener       |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue          | fdn      |
      | KPI      | goal_function_resource_efficiency | 0.843248616471435  | 054950_1 |
      | KPI      | goal_function_resource_efficiency | 0.933248616471435  | 054950_2 |
      | KPI      | goal_function_resource_efficiency | 0.453248616471435  | 054950_3 |
      | KPI      | p_failing_r_mbps | 0.3  | 054950_1 |
      | KPI      | p_failing_r_mbps | 0.2  | 054950_2 |
      | KPI      | p_failing_r_mbps | 0.9| 054950_3 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054950_1      | 1               | 2               |
      | 054950_2      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054950_3      | 1               | 4               |

  Scenario: TC2 Imbalanced Sector with all 3 Cells Unreliable for App Coverage
  No cells should pass through the App Coverage Reliability Screener and skip optimization

    Given Create Default Optimization Cells
      | fdn        |
      | 054951_1   |
      | 054951_2   |
      | 054951_3   |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812951 | TC2_AppCoverageReliabilityScreener       |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue          | fdn      |
      | KPI      | goal_function_resource_efficiency | 0.843248616471435  | 054951_1 |
      | KPI      | app_coverage_reliability          | false              | 054951_1 |
      | KPI      | goal_function_resource_efficiency | 0.933248616471435  | 054951_2 |
      | KPI      | app_coverage_reliability          | false              | 054951_2 |
      | KPI      | goal_function_resource_efficiency | 0.453248616471435  | 054951_3 |
      | KPI      | app_coverage_reliability          | false              | 054951_3 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC3 Imbalanced Sector with 2 Cells Reliable for App Coverage and 1 Cell Unreliable for App Coverage
  Cell 054952_1 should be screened in App Coverage Reliability Screener.
  Cells 054952_2 and 054952_3 should pass through the App Coverage Reliability Screener and proceed with optimization

    Given Create Default Optimization Cells
      | fdn        |
      | 054952_1   |
      | 054952_2   |
      | 054952_3   |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812952 | TC3_AppCoverageReliabilityScreener       |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue          | fdn      |
      | KPI      | goal_function_resource_efficiency | 0.843248616471435  | 054952_1 |
      | KPI      | app_coverage_reliability          | false              | 054952_1 |
      | KPI      | goal_function_resource_efficiency | 0.933248616471435  | 054952_2 |
      | KPI      | goal_function_resource_efficiency | 0.453248616471435  | 054952_3 |
      | KPI      | p_failing_r_mbps                  | 0.3                | 054952_2 |
      | KPI      | p_failing_r_mbps                  | 0.7                | 054952_3 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054952_2      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054952_3      | 1               | 2               |

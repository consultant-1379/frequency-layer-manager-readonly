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

Feature: FLM_LowConnectedUsersScreener
  The purpose of this policy state is to filter out cells based on the number of connected users.
  If the number of connected users in the cell is lower than min_connected_users setting,
  the source cell and its corresponding target cells with be filtered out.

  The kpi used:
  connected_users

  The settings used:
  min_connected_users

  Scenario: TC1 - The number of connected users in the source cell is lower than the minimum connected users setting.
  All cells are filtered out.

    Given Create Default Optimization Cells
      | 001 |
      | 002 |

    And Policy Input Event
      | sectorId       | executionId                   |
      | 17329045992781 | TC1_LowConnectedUsersScreener |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue        | fdn |
      | KPI      | connected_users                   | 8                | 001 |
      | SETTING  | min_connected_users               | 10               | 001 |
      | KPI      | goal_function_resource_efficiency | 0.34018373040423 | 001 |
      | KPI      | goal_function_resource_efficiency | 0.64018373040423 | 002 |
      | KPI      | p_failing_r_mbps                  | 0.3              | 002 |
      | KPI      | p_failing_r_mbps                  | 0.7              | 001 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty

  Scenario: TC2 - The number of connected users in the source cell exceeds the minimum connected users setting.
  No cells are filtered out.

    Given Create Default Optimization Cells
      | 001 |
      | 002 |

    And Policy Input Event
      | sectorId        | executionId |
      | 173290459927812 | TC2_LowConnectedUsersScreener |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue        | fdn |
      | KPI      | connected_users                   | 15               | 001 |
      | SETTING  | min_connected_users               | 10               | 001 |
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
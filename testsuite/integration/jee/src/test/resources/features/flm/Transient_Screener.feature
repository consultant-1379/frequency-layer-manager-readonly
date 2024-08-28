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

Feature: FLM_Transience_Kpi_Screener
  The purpose of this policy state is to screen out source and target cells based on transience.

  Input to this state:
  Ranked list of potential Source cells and their possible target cells

  Output from this state:
  The output from this state will be the input ranked list of potential source cells and their associated target cells
  with the target cells not meeting the threshold screened out.
  If all target cells for all potential source cells are screened out then the sector is removed from optimization
  and an empty LBQ is returned.

  The kpi used:
  lower_threshold_for_transient
  upper_threshold_for_transient
  p_failing_r_mbps_detrended

  Scenario: TC1 - Source Cell where p_failing_r_mbps_detrended less than upper_threshold_for_transient for 1 source cell in the sector.
  The source cell proceeds for optimization.

  The Sector in this test had 2 possible source cells, cell 054950_3_2 with potential target cell 054950_3_9
  and cell 054950_3_4 with potential target cells 054950_3 and 054950_3_9.
  Highest ranked source cell is 054950_3_4.
  p_failing_r_mbps_detrended greater than upper_threshold_for_transient for Source Cell: 054950_3_2 so is excluded.

    Given Create Default Optimization Cells
      | 054950_3   |
      | 054950_3_9 |
      | 054950_3_2 |
      | 054950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_1_Transience_Kpi_Screener        |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.829850312445342 | 054950_3   |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054950_3   |
      | KPI      | contiguity                        | 39.3333333333333  | 054950_3   |
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
      | KPI      | upper_threshold_for_transient     | 0.777665189       | 054950_3_9 |
      | KPI      | lower_threshold_for_transient     | 0.066516851       | 054950_3_9 |
      | KPI      | p_failing_r_mbps_detrended        | 0.16952695        | 054950_3_9 |
      | KPI      | upper_threshold_for_transient     | 0.9829778         | 054950_3_4 |
      | KPI      | lower_threshold_for_transient     | 0.7889982         | 054950_3_4 |
      | KPI      | p_failing_r_mbps_detrended        | 0.86952695        | 054950_3_4 |
      | KPI      | upper_threshold_for_transient     | 0.329314794871641 | 054950_3_2 |
      | KPI      | lower_threshold_for_transient     | 0.931479487164174 | 054950_3_2 |
      | KPI      | p_failing_r_mbps_detrended        | 0.96952695        | 054950_3_2 |
      | KPI      | upper_threshold_for_transient     | 0.768912652656568 | 054950_3   |
      | KPI      | lower_threshold_for_transient     | 0.342314794871641 | 054950_3   |
      | KPI      | p_failing_r_mbps_detrended        | 0.412412          | 054950_3   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054950_3      | 1               | 2               |
      | 054950_3_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054950_3_4    | 1               | 4               |


  Scenario: TC2 - Source Cell where p_failing_r_mbps_detrended less than upper_threshold_for_transient for 1 source cells in the sector.
  Target cell where p_failing_r_mbps_detrended greater than lower_threshold_for_transient for 1 target cells in the sector.
  One source and target cell proceeds for optimization.

  The Sector in this test had 2 possible source cells, cell 054950_3_2 with potential target cell 054950_3_9
  and cell 054950_3_4 with potential target cells 054950_3 and 054950_3_9.
  Highest ranked source cell is 054950_3_4.
  p_failing_r_mbps_detrended less than lower_threshold_for_transient Target Cell: 054950_3_9.
  Source Cell 054950_3_2 no target cells remaining so source cell is excluded.

    Given Create Default Optimization Cells
      | 054950_3   |
      | 054950_3_9 |
      | 054950_3_2 |
      | 054950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_2_Transience_Kpi_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.829850312445342 | 054950_3   |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054950_3   |
      | KPI      | contiguity                        | 39.3333333333333  | 054950_3   |
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
      | KPI      | lower_threshold_for_transient     | 0.666516851       | 054950_3_9 |
      | KPI      | upper_threshold_for_transient     | 0.966526525       | 054950_3_9 |
      | KPI      | p_failing_r_mbps_detrended        | 0.21412           | 054950_3_9 |
      | KPI      | upper_threshold_for_transient     | 0.9829778         | 054950_3_4 |
      | KPI      | lower_threshold_for_transient     | 0.7595855         | 054950_3_4 |
      | KPI      | p_failing_r_mbps_detrended        | 0.86952695        | 054950_3_4 |
      | KPI      | upper_threshold_for_transient     | 0.929314794871641 | 054950_3_2 |
      | KPI      | lower_threshold_for_transient     | 0.856955654158485 | 054950_3_2 |
      | KPI      | p_failing_r_mbps_detrended        | 0.358165161       | 054950_3_2 |
      | KPI      | upper_threshold_for_transient     | 0.569955968565658 | 054950_3   |
      | KPI      | lower_threshold_for_transient     | 0.342314794871641 | 054950_3   |
      | KPI      | p_failing_r_mbps_detrended        | 0.388165161       | 054950_3   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054950_3      | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054950_3_4    | 1               | 2               |


  Scenario: TC3 - Target cell where p_failing_r_mbps_detrended less than lower_threshold_for_transient for all target cells in the sector.
  All Target cells are excluded therefore all source cells and sector is excluded

  The Sector in this test had 2 possible source cells, cell 054950_3_2 with potential target cell 054950_3_9
  and cell 054950_3_4 with potential target cells 054950_3 and 054950_3_9.
  Highest ranked source cell is 054950_3_4.

    Given Create Default Optimization Cells
      | 054950_3   |
      | 054950_3_9 |
      | 054950_3_2 |
      | 054950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_3_Transience_Kpi_Screener     |

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
      | KPI      | lower_threshold_for_transient     | 0.666516851       | 054950_3_9 |
      | KPI      | upper_threshold_for_transient     | 0.785656541       | 054950_3_9 |
      | KPI      | p_failing_r_mbps_detrended        | 0.16952695        | 054950_3_9 |
      | KPI      | lower_threshold_for_transient     | 0.342314794871641 | 054950_3   |
      | KPI      | upper_threshold_for_transient     | 0.556656546516656 | 054950_3   |
      | KPI      | p_failing_r_mbps_detrended        | 0.328165161       | 054950_3   |
      | KPI      | lower_threshold_for_transient     | 0.3254915         | 054950_3_4 |
      | KPI      | upper_threshold_for_transient     | 0.9829778         | 054950_3_4 |
      | KPI      | p_failing_r_mbps_detrended        | 0.358165161       | 054950_3_4 |
      | KPI      | lower_threshold_for_transient     | 1.235487164154816 | 054950_3_2 |
      | KPI      | upper_threshold_for_transient     | 1.329314794871641 | 054950_3_2 |
      | KPI      | p_failing_r_mbps_detrended        | 0.96952695        | 054950_3_2 |


    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC4 - Source Cell where p_failing_r_mbps_detrended greater than upper_threshold_for_transient for all source cells in the sector.
  All source cells are excluded therefore sector is excluded

  The Sector in this test had 2 possible source cells, cell 054950_3_2 with potential target cell 054950_3_9
  and cell 054950_3_4 with potential target cells 054950_3 and 054950_3_9.
  Highest ranked source cell is 054950_3_4.

    Given Create Default Optimization Cells
      | 054950_3   |
      | 054950_3_9 |
      | 054950_3_2 |
      | 054950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_4_Transience_Kpi_Screener     |

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
      | KPI      | lower_threshold_for_transient     | 0.366516851       | 054950_3_9 |
      | KPI      | upper_threshold_for_transient     | 0.965651618       | 054950_3_9 |
      | KPI      | p_failing_r_mbps_detrended        | 0.46952695        | 054950_3_9 |
      | KPI      | lower_threshold_for_transient     | 0.586516518       | 054950_3_4 |
      | KPI      | upper_threshold_for_transient     | 0.7829778         | 054950_3_4 |
      | KPI      | p_failing_r_mbps_detrended        | 0.86952695        | 054950_3_4 |
      | KPI      | lower_threshold_for_transient     | 0.516516516871616 | 054950_3_2 |
      | KPI      | upper_threshold_for_transient     | 1.329314794871641 | 054950_3_2 |
      | KPI      | p_failing_r_mbps_detrended        | 1.96952695        | 054950_3_2 |
      | KPI      | lower_threshold_for_transient     | 0.242314794871641 | 054950_3   |
      | KPI      | upper_threshold_for_transient     | 0.423147948759762 | 054950_3   |
      | KPI      | p_failing_r_mbps_detrended        | 0.512412          | 054950_3   |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization is skipped and Expected Proposed Load Balancing Quanta is empty


  Scenario: TC5 - No value for transience for source or target cells.
  Source and target cells are sent on for optimization

  The Sector in this test had 2 possible source cells, cell 054950_3_2 with potential target cell 054950_3_9
  and cell 054950_3_4 with potential target cells 054950_3 and 054950_3_9.
  Highest ranked source cell is 054950_3_4.

    Given Create Default Optimization Cells
      | 054950_3   |
      | 054950_3_9 |
      | 054950_3_2 |
      | 054950_3_4 |

    And Policy Input Event
      | sectorId           | executionId |
      | 173290459927812950 | TC_5_Transience_Kpi_Screener     |

    And Set Optimization Cells Data
      | dataType | dataName                          | dataValue         | fdn        |
      | KPI      | goal_function_resource_efficiency | 0.829850312445342 | 054950_3   |
      | KPI      | unhappy_users                     | 0.645855356858811 | 054950_3   |
      | KPI      | contiguity                        | 39.3333333333333  | 054950_3   |
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
      | KPI      | lower_threshold_for_transient     | 0.066516851       | 054950_3_9 |

    When Putting Policy Input Event onto Kafka Topic

    Then Optimization proceeds with the following target cells
      | targetCellFdn | targetCellOssId | targetUsersMove |
      | 054950_3      | 1               | 2               |
      | 054950_3_9    | 1               | 2               |

    Then Optimization proceeds with the following source cells
      | sourceCellFdn | sourceCellOssId | sourceUsersMove |
      | 054950_3_4    | 1               | 4               |
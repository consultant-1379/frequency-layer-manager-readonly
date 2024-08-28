/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

var reliabilityScreenerTestData = [
    {
        description: "Check if Cell Reliability is less then Cell Reliability threshold value",
        result: false,
        data: [
          {
              "fdn": "001",
              "ossId": 1,
              "kpis": {
                  "kpi_cell_reliability_daily": "7"
              },
              "settings": {
                  "num_calls_cell_hourly_reliability_threshold_in_hours": "20"
              }
          }
        ]
    },
    {
        description: "Check if Cell Reliability is greater then Cell Reliability threshold value",
        result: true,
        data: [
          {
              "fdn": "001",
              "ossId": 1,
              "kpis": {
                  "kpi_cell_reliability_daily": "24"
              },
              "settings": {
                  "num_calls_cell_hourly_reliability_threshold_in_hours": "20"
              }
          }
        ]
     },
     {
        description: "Check if Cell Reliability is equal to Cell Reliability threshold value",
        result: true,
        data: [
          {
              "fdn": "001",
              "ossId": 1,
              "kpis": {
                  "kpi_cell_reliability_daily": "20"
              },
              "settings": {
                  "num_calls_cell_hourly_reliability_threshold_in_hours": "20"
              }
          }
        ]

     }
];

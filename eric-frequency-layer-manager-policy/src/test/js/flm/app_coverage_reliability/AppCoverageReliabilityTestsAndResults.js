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

var cellThatIsReliable = {
    "fdn": "SubNetwork=ONRM_ROOT_MO,SubNetwork=SubNet1,MeContext=Athlone1,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=001",
    "ossId": 1,
    "kpis": {
        "app_coverage_reliability": "true",
    },
    "cmAttributes": {},
    "settings": {}
};

var cellThatIsNotReliable = {
    "fdn": "SubNetwork=ONRM_ROOT_MO,SubNetwork=SubNet1,MeContext=Athlone1,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=002",
    "ossId": 1,
    "kpis": {
        "app_coverage_reliability": "false",
    },
    "cmAttributes": {},
    "settings": {}
};

var testAppCoverageReliabilityValues = [
    {
        description: "Optimization cells with one reliable cell",
        result: [
            cellThatIsReliable
        ],
        data: [
            cellThatIsReliable
        ]
    },
    {
        description: "Optimization cells with one non reliable cell",
        result: [],
        data: [
            cellThatIsNotReliable
        ]
    },
    {
        description: "Optimization cells with one reliable and one non reliable cell",
        result: [
            cellThatIsReliable
        ],
        data: [
            cellThatIsReliable,
            cellThatIsNotReliable
        ]
    }
];

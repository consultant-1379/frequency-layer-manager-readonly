/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

var addLbqTestCases = [
    {
        description: "Proposed LBQ with source cell and 1 target cell",
        result: '{"sourceCellFdn":"001","sourceCellOssId":11,"targetCells":[{"targetCellFdn":"002","targetCellOssId":22,"targetUsersMove":"2"}],"sourceUsersMove":"2"}',
        proceedWithOptimization: true,
        data:
            {
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 11
                },
                "targetCells": [
                    {
                        "fdn": "002",
                        "ossId": 22,
                        "numUsersToMove": "2"
                    }
                ]
            }
    },
    {
        description: "Proposed LBQ with source cell and 3 target cells",
        result: '{"sourceCellFdn":"001","sourceCellOssId":11,"targetCells":[{"targetCellFdn":"002","targetCellOssId":22,"targetUsersMove":"2"},{"targetCellFdn":"003","targetCellOssId":33,"targetUsersMove":"2"},{"targetCellFdn":"004","targetCellOssId":44,"targetUsersMove":"2"}],"sourceUsersMove":"6"}',
        proceedWithOptimization: true,
        data:
            {
                "sourceCell": {
                    "fdn": "001",
                    "ossId": 11
                },
                "targetCells": [
                    {
                        "fdn": "002",
                        "ossId": 22,
                        "numUsersToMove": "2"
                    },
                    {
                        "fdn": "003",
                        "ossId": 33,
                        "numUsersToMove": "2"
                    },
                    {
                        "fdn": "004",
                        "ossId": 44,
                        "numUsersToMove": "2"
                    }
                ]
            }
    },
    {
        description: "Not proceeding with optimization, returned proposed lbq is empty",
        result: '{"sourceCellFdn":"","sourceCellOssId":-1,"sourceUsersMove":"","targetCells":[{"targetCellFdn":"","targetCellOssId":-1,"targetUsersMove":""}]}',
        proceedWithOptimization: false,
        data: null
    }
];
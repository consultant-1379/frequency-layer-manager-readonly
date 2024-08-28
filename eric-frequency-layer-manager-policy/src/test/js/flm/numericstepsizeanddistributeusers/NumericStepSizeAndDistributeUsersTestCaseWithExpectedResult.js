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

var numericStepSizeAndDistributeUsersTest = [
    {
        description: "TC_1: Small step size, Target cells = 1 and Target Step Sizes > Max Source User Move",
        maxUserToMoveFloat: 9,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "9"
                }
            ]
        }
    },
    {
        description: "TC_2: Small step size, Target cells = 1 and Target Step Sizes < Max Source User Move",
        maxUserToMoveFloat: 10,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "5000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "5000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "2"
                }
            ]
        }
    },
    {
        description: "TC_3: Small step size, Target cells > 1 and Target Step Sizes < Max Source User Move",
        maxUserToMoveFloat: 10,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "5000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "5000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "5000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "2"
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "5000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "2"
                }
            ]
        }
    },
    {
        description: "TC_4: Small step size, Target cells > 1 and Target Step Sizes > Max Source User Move",
        maxUserToMoveFloat: 9,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.8",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.8",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "6"
                },
                {
                    "fdn": "003",
                    "ossId": 3,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "3"
                }
            ]
        }
    },
    {
        description: "TC_5: Large step size, Target cells = 1, calculated userToMove < Bandwidth table value and Target Step Size < Max Source User Move",
        maxUserToMoveFloat: 9,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "9"
                }
            ]
        }
    },
    {
        description: "TC_6: Large step size, Target cells = 1, calculated userToMove < Bandwidth table value and Target Step Size > Max Source User Move",
        maxUserToMoveFloat: 10,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "66"
                    },
                    "cmAttributes": {
                        "bandwidth": "10000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "66"
                    },
                    "cmAttributes": {
                        "bandwidth": "10000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "10"
                }
            ]
        }
    },
    {
        description: "TC_7: Large step size, Target cells = 1, calculated userToMove > Bandwidth table value and Target Step Size < Max Source User Move",
        maxUserToMoveFloat: 10,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "6"
                }
            ]
        }
    },
    {
        description: "TC_8: Large step size, Target cells = 1, calculated userToMove > Bandwidth table value and Target Step Size > Max Source User Move",
        maxUserToMoveFloat: 1,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "num_values_used_for_mcu_cdf_calculation_daily": "100",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {
                    "min_num_cell_for_cdf_calculation": "20"
                }
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 2,
                    "stepSize": "large",
                    "kpis": {
                        "max_connected_users_daily": "166",
                        "connected_users": "140",
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "num_values_used_for_mcu_cdf_calculation_daily": "100",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {
                    "min_num_cell_for_cdf_calculation": "20"
                }
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 2,
                    "stepSize": "large",
                    "kpis": {
                        "max_connected_users_daily": "166",
                        "connected_users": "140",
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "1"
                }
            ]
        }
    },
    {
        description: "TC_9: Large step size, calculated userToMove < Bandwidth table value, Target cells = 1 and Target Step Sizes > Max Source User Move",
        maxUserToMoveFloat: 6,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "numUsersToMove": "6",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        }
    },
    {
        description: "TC_10: Large step size, Target cells > 1 and Target Step Sizes > Max Source User Move",
        maxUserToMoveFloat: 6,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.6",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "3"
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.6",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "3"
                }
            ]
        }
    },
    {
        description: "TC_10_2: Small step size, Target cells > 1 and Target Step Sizes < Max Source User Move, exceeds capacity",
        maxUserToMoveFloat: 25.2,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.1",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "8",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "8",
                    "kpis": {
                        "p_failing_r_mbps": "0.1",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "8",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        }
    },
    {
        description: "TC_11: Small step size, Target cells = 1 and Target Step Sizes > Max Source User Move and Max Source User Move >= 0.5 and < 1",
        maxUserToMoveFloat: 0.75,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "max_connected_users_daily": "166",
                        "connected_users": "140",
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "max_connected_users_daily": "166",
                        "connected_users": "140",
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "1"
                }
            ]
        }
    },
    {
        description: "TC_12: Small step size, Target cells = 1 and Target Step Sizes < Max Source User Move and Max Source User Move < 5.5",
        maxUserToMoveFloat: 5.25,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "max_connected_users_daily": "166",
                        "connected_users": "140",
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "max_connected_users_daily": "166",
                        "connected_users": "140",
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "5"
                }
            ]
        }
    },
    {
        description: "TC_13: Small step size, Target cells = 1 and Target Step Sizes > Max Source User Move and Max Source User Move > 4.5",
        maxUserToMoveFloat: 4.75,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "max_connected_users_daily": "166",
                        "connected_users": "140",
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "max_connected_users_daily": "166",
                        "connected_users": "140",
                        "p_failing_r_mbps": "0.93"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "5"
                }
            ]
        }
    },
    {
         description: "TC_14: small step size, Target cells = 2 test with 5.97 users, should be distributed 3 rounded (2.98) and 3 (rounded 2.98) target cells",
         maxUserToMoveFloat: 5.9714208089553455,
         data:
         {
             "sourceCell": {
                 "fdn": "001",
                 "ossId": 1,
                 "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                 "cmAttributes": {},
                 "settings": {}
             },
             "targetCells": [
                 {
                     "fdn": "002",
                     "ossId": 1,
                     "stepSize": "small",
                     "kpis": {
                         "p_failing_r_mbps": "0.3",
                         "target_cell_capacity": "10"
                     },
                     "cmAttributes": {
                         "bandwidth": "20000"
                     },
                     "settings": {
                         "optimization_speed": "fast",
                         "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                         "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                     }
                 },
                 {
                     "fdn": "003",
                     "ossId": 1,
                     "stepSize": "small",
                     "kpis": {
                         "p_failing_r_mbps": "0.1",
                         "target_cell_capacity": "12"
                     },
                     "cmAttributes": {
                         "bandwidth": "20000"
                     },
                     "settings": {
                         "optimization_speed": "fast",
                         "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                         "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                     }
                 }
             ]
         },
         result :
         {
             "sourceCell": {
                 "fdn": "001",
                 "ossId": 1,
                 "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "21"
                },
                 "cmAttributes": {},
                 "settings": {}
             },
             "targetCells": [
                 {
                      "fdn": "002",
                      "ossId": 1,
                      "stepSize": "small",
                      "numUsersToMove": "3",
                      "kpis": {
                          "p_failing_r_mbps": "0.3",
                          "target_cell_capacity": "10"
                      },
                      "cmAttributes": {
                          "bandwidth": "20000"
                      },
                      "settings": {
                          "optimization_speed": "fast",
                          "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                          "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                      }
                 },
                 {
                     "fdn": "003",
                     "ossId": 1,
                     "stepSize": "small",
                     "numUsersToMove": "3",
                     "kpis": {
                         "p_failing_r_mbps": "0.1",
                         "target_cell_capacity": "12"
                     },
                     "cmAttributes": {
                         "bandwidth": "20000"
                     },
                     "settings": {
                         "optimization_speed": "fast",
                         "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                         "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                     }
                 }
             ]
         }
    },
    {
        description: "TC_15: Large step size, Target cells = 3 and Target Step Sizes > Max Source User Move and Max Source User Move = 1.12 which goes to Single target",
        maxUserToMoveFloat: 1.1243517601668356,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        "result":
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "numUsersToMove": "1",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        }
    },
    {
        description: "TC_16: Small step size, Target cells = 1, Target Step Sizes > Max Source User Move and Target Step Sizes > source connected user; output numUsersToMove = maxUserToMoveFloat",
        maxUserToMoveFloat: 4,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "5"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        result :
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.95",
                    "connected_users": "5"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.93",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "slow",
                        "qos_for_capacity_estimation": "0.5",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    },
                    "numUsersToMove": "4"
                }
            ]
        }
    },
    {
        description: "TC_17: Mix of step sizes, Target cells = 3, 2 target cells with large step size and 1 target cell with small step size",
        maxUserToMoveFloat: 9,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        "result":
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "numUsersToMove": "3",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "3",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "large",
                    "numUsersToMove": "3",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        }
    },
    {
        description: "TC_18: Mix of step sizes, 3 Target cells: 2 of which have large stepSize and 1 has small stepSize. After redistribution, totalNumUsersToMove is 0 so the users are moved to the target cell with largest usersToMove (calculated before redistribution)",
        maxUserToMoveFloat: 1.12,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "15"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        "result":
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "large",
                    "numUsersToMove": "1",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "large",
                    "numUsersToMove": "1",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "15"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        }
    },
    {
        description: "TC_19: Mix of step sizes, Target cells = 3, 2 target cells with small step size and 1 target cell with large step size",
        maxUserToMoveFloat: 10,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "6"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        "result":
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "3",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "6"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "3",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "large",
                    "numUsersToMove": "3",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "20000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        }
    },
    {
        description: "TC_20: All small step sizes, 3 Target cells  and totalNumUsersToMove is 0 after redistribution so the users are moved to the target cell with largest usersToMove (before redistribution)",
        maxUserToMoveFloat: 1.12,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.3",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "p_failing_r_mbps": "0.4",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        "result":
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.6",
                    "connected_users": "21"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "1",
                    "kpis": {
                        "p_failing_r_mbps": "0.2",
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "1400"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        }
    },
    {
        description: "TC_21: Mix of step sizes, Target cells = 3, 2 target cells with small step size and 1 target cell with large step size",
        maxUserToMoveFloat: 7.8,
        data:
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "26"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "kpis": {
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "large",
                    "kpis": {
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        },
        "result":
        {
            "sourceCell": {
                "fdn": "001",
                "ossId": 1,
                "kpis": {
                    "p_failing_r_mbps": "0.9",
                    "connected_users": "26"
                },
                "cmAttributes": {},
                "settings": {}
            },
            "targetCells": [
                {
                    "fdn": "002",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "2",
                    "kpis": {
                        "target_cell_capacity": "10"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "003",
                    "ossId": 1,
                    "stepSize": "small",
                    "numUsersToMove": "2",
                    "kpis": {
                        "target_cell_capacity": "5"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                },
                {
                    "fdn": "004",
                    "ossId": 1,
                    "stepSize": "large",
                    "numUsersToMove": "3",
                    "kpis": {
                        "target_cell_capacity": "12"
                    },
                    "cmAttributes": {
                        "bandwidth": "15000"
                    },
                    "settings": {
                        "optimization_speed": "fast",
                        "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
                        "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10"
                    }
                }
            ]
        }
    }
];

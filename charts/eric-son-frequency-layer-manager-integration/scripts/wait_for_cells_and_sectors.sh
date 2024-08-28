#!/usr/bin/env bash

#To count number of all the columns from sql file
sql_file_name="/scripts/test-values-kpi-service.sql";
total_columns_kpi_cell_guid_table=($(grep "INSERT INTO kpi.${KPI_CELL_GUID_TABLE}" $sql_file_name | head -1 | sed 's/^.*INSERT INTO kpi.${KPI_CELL_GUID_TABLE} //; s/VALUES.*$//' | sed 's/^.*(//; s/).*$//' | sed -e 's/,/\n/g'));
echo "Total Number of columns in $sql_file_name file - ${#total_columns_kpi_cell_guid_table[@]}"

waitTimeout=10
while [[ $waitTimeout -gt 0 ]]; do
    cellGuidColumnCount=$( psql -t -c "select count(*) FROM information_schema.columns WHERE table_name = '${KPI_CELL_GUID_TABLE}'")
    cellSectorColumnCount=$( psql -t -c "select count(*) FROM information_schema.columns WHERE table_name = '${KPI_CELL_SECTOR_TABLE}'")
    echo "cell Guid count : $cellGuidColumnCount"
    echo "cell sector count : $cellSectorColumnCount"

    if [[ $cellGuidColumnCount -gt ${#total_columns_kpi_cell_guid_table[@]} ]] && [[ $cellSectorColumnCount -gt ${EXPECTED_DEFAULT_CELL_SECTOR_COLUMN_COUNT} ]]; then
        exit 0
    fi
    echo "Waiting for columns to be generated"
    sleep 30s
    waitTimeout=$waitTimeout-1
done


if [[ $waitTimeout -le 0 ]]; then
        exit 1
fi

#!/usr/bin/env bash

address="35.276.222.44"
http_port=""

counter=0
while [[ $(curl -s -X GET "http://${address}:8081/actuator/health" -H "accept: */*" | jq -r .status) != "UP" ]]; do
  if (( $count == 20 )); then
    echo "Waited too long!"
    exit -1
  fi
  echo "Waiting for service to start"
  sleep 5
  let counter+=1
done

# Add two
echo "Add first ticket"
ticket1=$(curl -s -X POST "http://${address}${http_port}/ticket" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"date\": \"2019-03-25T14:50:30.855Z\", \"description\": \"Nothing here\", \"id\": 0, \"state\": \"OPEN\", \"title\": \"Some Title\", \"user\": \"Jens\"}")
echo $ticket1 | jq .
echo "Add second ticket"
ticket2=$(curl -s -X POST "http://${address}${http_port}/ticket" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"date\": \"2019-03-25T14:50:30.855Z\", \"description\": \"Beschreibung\", \"id\": 0, \"state\": \"SOLVED\", \"title\": \"And again\", \"user\": \"Jens\"}")
echo $ticket2 | jq .

id1=$(echo $ticket1 | jq .id)
id2=$(echo $ticket2 | jq .id)

# Show all
echo -e "\nShow all tickets"
curl -s -X GET "http://${address}${http_port}/ticket" -H "accept: */*" | jq .

# Show one
echo -e "\nShow ticket with id $id1"
curl -s -X GET "http://${address}${http_port}/ticket/$id1" -H "accept: */*" | jq .

# Show stats
echo -e "\nShow currents stats"
curl -s -X GET "http://${address}${http_port}/ticket/stats" -H "accept: */*" | jq .

# Replace 2
echo -e "\nReplace state of ticket with id $id2 to WAITING"
curl -s -X PUT "http://${address}${http_port}/ticket/$id2" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"id\": 2, \"user\": \"Jens\", \"title\": \"And again\", \"description\": \"Beschreibung\", \"date\": \"2019-03-25T14:50:30.855\", \"state\": \"WAITING\" }" | jq .

# Show stats
echo -e "\nShow stats again"
result=$(curl -s -X GET "http://${address}${http_port}/ticket/stats" -H "accept: */*")
echo $result | jq .

# Delete two
echo -e "\nRemove ticket with id $id1"
curl -s -X DELETE "http://${address}${http_port}/ticket/$id1" -H "accept: */*"
echo "Remove ticket with id $id2"
curl -s -X DELETE "http://${address}${http_port}/ticket/$id2" -H "accept: */*"

# Check result
if (( $(echo $result | jq length) != 2 )) || (( $(echo $result | jq .[0].count) != 1 )) || (( $(echo $result | jq .[1].count) != 1 )); then
  echo "Wrong result"
  exit -1
fi
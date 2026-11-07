# mc-simple-payment-notice-service
A simple payment notice through kafka schema
## Setup
    Docker Instances
        + cp kafka image 
        + cp kafka schema regisrtry 
        + kafka ui

## Build
        + maven build

## Test

    ```script
    # String/Plain message
    curl -vX POST "localhost:8090/api/payments/string?key=acct-001" \
    -H "Content-Type: text/plain" -d "Welcome to kafka schema registry ...
    
    #* schema message
    curl -X POST localhost:8090/api/payments/json-schema \
    -H "Content-Type: application/json" \
    -d '{"name":"alice","alias":"bob","amount":125.50,"currency":"USD","note":"Mark Das"}'
    ```
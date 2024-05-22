#!/usr/bin/sh

tickers="https://api.binance.com/api/v3/ticker/price"

curl -X GET $tickers > './subscriptions/tickers.txt'
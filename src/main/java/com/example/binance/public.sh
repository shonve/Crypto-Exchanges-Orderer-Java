#!/usr/bin/sh

tickers="https://api.binance.com/api/v3/ticker/24hr"
curl -X GET $tickers > ./publicInfo/tickers.txt
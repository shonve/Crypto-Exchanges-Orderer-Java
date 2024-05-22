#!/usr/bin/sh
tickers="https://api.bitget.com/api/v2/spot/market/tickers"
curl -X GET $tickers > './publicInfo/tickers.txt'
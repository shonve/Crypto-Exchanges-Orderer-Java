#!/usr/bin/sh

base_uri="https://api.gateio.ws/api/v4"
tickers_end_point="/spot/tickers"
tickers=$base_uri$tickers_end_point
cat ./publicInfo/tickers.txt > ./publicInfo/last_tickers.txt
curl -X GET $tickers > ./publicInfo/tickers.txt
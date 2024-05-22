#!/usr/bin/sh

#instruments_info="https://api.bybit.com/v5/market/instruments-info?category=spot"
#curl $instruments_info >> ./publicInfo/instruments.txt
tickers="https://api.bybit.com/v5/market/tickers?category=spot"
curl -X GET $tickers > ./publicInfo/tickers.txt
#!/usr/bin/sh
api_time="www.okx.com/api/v5/public/time"
instruments="www.okx.com/api/v5/public/instruments?instType=SPOT"
#curl $instruments >> ./publicInfo/instruments.txt
curl $api_time
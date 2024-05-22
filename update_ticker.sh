#!/usr/bin/sh
root_dir=$PWD
base_dir="$root_dir/src/main/java/com/example"
exchanges=("binance" "bybit" "gateio" "bitget")
cd $base_dir
date="2024-03-17"
for exchange in "${exchanges[@]}"; do
    echo $exchange && cd $exchange
    if [ ! -d "change" ]; then 
        mkdir "change"
    fi
    cd ./change
    if [ ! -d "$date" ]; then
        mkdir $date && cd $date && touch change.txt && cd ..
    fi
    cd .. && bash public.sh && cd ..
done
cd $root_dir
bash run.sh

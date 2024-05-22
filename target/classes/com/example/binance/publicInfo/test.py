import json
from typing import List

base_dir: str = "/home/rabindar/web/my-binance-connector/src/main/java/com/example/binance"

def get_today_prices(symbol: str):
    with open(f"{base_dir}/change/2024-01-30/change.txt", "r") as f:
        for line in f:
            res: dict = json.loads(line)
            if (len(res['prices']) > 1):
                print(res['symbol'])
                print(res['prices'])
                #print(res['prices'][0])
                #print(f"{res['symbol']}: {res['prices'][len(res['prices'])-1]}")

def get_prices(date: str, symbol: str):
    with open(f"{base_dir}/change/{date}/change.txt", "r") as f:
        for line in f:
            res: dict = json.loads(line)
            if (res['symbol'] == symbol):
                f.close()
                return res['prices']
        f.close()
    
    return None

def get_tickers(date: str):
    tickers: dict = {}
    with open(f"{base_dir}/change/{date}/change.txt", "r") as f:
        for line in f:
            res: dict = json.loads(line)
            prices = res['prices']
            item: dict = {}
            for item in prices:
                if (item.__contains__("min-threshold")):
                    tickers[res['symbol']] = item
        f.close()
        #for symbol in tickers:
            #print(f"{symbol}: {tickers[symbol]}")
    return tickers

def get_symbols(date: str, change: float):
    tickers: dict = get_tickers(date)
    for symbol in tickers:
        prices = get_prices(date, symbol)
        print(f"{symbol}: {prices}")
        #last_index: int = len(prices) - 1
        #if (prices[last_index]['change'] > change):
        #    print(f"{symbol}: {prices[last_index-1]}, {prices[last_index]}\r\n")

#get_symbols("2024-01-31", 0.02)
#get_symbols("2024-02-02", 0.05)

#get_prices("2024-01-31", -0.05)

#get_today_prices("BARUSDT")

#get_tickers("2024-01-31")         
tickers_yesterday = get_tickers("2024-02-07")
tickers_today = get_tickers("2024-02-08")
base_volume: float = 1000000

'''
for symbol in tickers_yesterday:
    ticker: dict = tickers_yesterday[symbol]
    if (ticker['volume'] > base_volume):
        print(f"{symbol}: {ticker}")
print(f"\r\n")
'''
symbols: List[str] = []
for symbol in tickers_today:
    #if (tickers_yesterday.__contains__(symbol)):
    #    print(f"{symbol}: {tickers_yesterday[symbol]}")
    #    print(f"{symbol}: {tickers_today[symbol]}")
    ticker: dict = tickers_today[symbol]
    if (ticker['volume'] > base_volume):
        if (symbol.find('USDT') > -1):
            symbols.append(symbol)
print(symbols)

# symbols: XCADUSDT, IRLUSDT, VVUSDT, XRP3SUSDT
# to be tested: EVERUSDT, ORTUSDT, ADA2SUSDT, MAGICUSDT, CHRPUSDT, PLYUSDT, ECOXUSDT, HVHUSDT,
#               DZOOUSDT, PYTHUSDT, DFIUSDT, GMT2USDT, CULTUSDT, FTM2USDT, 1SOLUSDT,
#get_prices("2024-02-01", "VVUSDT")

# get all symbols with 0.09-0.1 change, expect 1.2-1.4% rise in most of them
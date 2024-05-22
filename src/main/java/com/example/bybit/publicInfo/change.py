import json
from typing import List

base_dir: str = "/home/rabindar/web/my-binance-connector/src/main/java/com/example/bybit"

base_volume: float = 1000000

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

def get_prices(index: int, date: str):
    result: dict = {}
    with open(f"{base_dir}/change/{date}/change.txt", "r") as f:
        for line in f:
            res: dict = json.loads(line)
            prices = res['prices']
            if (len(prices) >= index+1):
                result[res['symbol']] = prices[index]
            '''
            if (len(prices) == 1):
                item: dict = prices[0]
                if (item['volume'] > base_volume):
                    result[res['symbol']] = prices[0]
                continue
            item1: dict = prices[len(prices) - 1]
            item2: dict = prices[len(prices) - 2]
            if (item2['volume'] > base_volume and item2['volume'] > item1['volume']):
                result[res['symbol']] = item2
            '''
        f.close()

    return result

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
#tickers_yesterday = get_tickers("2024-02-07")
#tickers_today = get_tickers("2024-02-08")

'''
for symbol in tickers_yesterday:
    ticker: dict = tickers_yesterday[symbol]
    if (ticker['volume'] > base_volume):
        print(f"{symbol}: {ticker}")
print(f"\r\n")
'''





'''
for symbol in tickers_today:
    #if (tickers_yesterday.__contains__(symbol)):
    #    print(f"{symbol}: {tickers_yesterday[symbol]}")
    #    print(f"{symbol}: {tickers_today[symbol]}")
    ticker: dict = tickers_today[symbol]
    if (ticker['volume'] > base_volume):
        symbols.append(symbol)
print(symbols)
'''

#prices = get_prices("2024-02-08", "ONDOUSDT")
#print(prices)


# symbols: XCADUSDT, IRLUSDT, VVUSDT, XRP3SUSDT
# to be tested: EVERUSDT, ORTUSDT, ADA2SUSDT, MAGICUSDT, CHRPUSDT, PLYUSDT, ECOXUSDT, HVHUSDT,
#               DZOOUSDT, PYTHUSDT, DFIUSDT, GMT2USDT, CULTUSDT, FTM2USDT, 1SOLUSDT,
#get_prices("2024-02-01", "VVUSDT")

# get all symbols with 0.09-0.1 change, expect 1.2-1.4% rise in most of them

prices0: dict = get_prices(0, '2024-02-07')
prices1: dict = get_prices(1, '2024-02-07')
symbols: List[str] = []
for symbol in prices1:
    #print(result[symbol])
    volume0: str = format(prices0[symbol]['volume']/base_volume, ".2f")
    volume1: str = format(prices1[symbol]['volume']/base_volume, ".2f")
    if (float(volume1) > float(volume0)):
        symbols.append(symbol)
    print(f"{symbol}: {volume0}m, {volume1}m")

print(symbols)
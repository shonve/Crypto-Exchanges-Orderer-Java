import json
from typing import List

# {"op": "subscribe", "args": [{"instType":"SPOT","channel":"ticker","instId":"BTCUSDT"}]}
# {"method": "SUBSCRIBE","params": ["btcusdt@ticker"],"id":1}

def build_common_symbols(exchange: str, symbols_path: str):
    common_symbols = []
    with open(symbols_path, "r") as f:
        res: dict = json.loads(f.read())
        exchange_symbols = res['symbols']
        f.close()
    with open("./symbols.txt", "r") as f:
        res: dict = json.loads(f.read())
        symbols = res['symbols']
        f.close()
    
    for symbol in symbols:
        if (symbol in exchange_symbols):
            common_symbols.append(symbol)
    exchange_symbols = []
    symbols = []
    with open(f"../common/{exchange}/symbols.txt", "w") as f:
        f.write(json.dumps({
            "symbols": common_symbols
        }) + "\r\n")

def common_symbols_usdt(exchange: str):
    symbols_usdt = []
    with open(f"../common/{exchange}/symbols.txt", "r") as f:
        res: dict = json.loads(f.read())
        symbols = res['symbols']
        for symbol in symbols:
            if (symbol.find("USDT") > -1):
                symbols_usdt.append(symbol)
    
    return symbols_usdt


def ticker_exists(symbol: str):
    with open("./subscriptions/tickers.txt", "r") as f:
        for line in f:
            if (line.find(symbol) > -1):
                res: dict = json.loads(line)
                if (res.__contains__(symbol)):
                    print(line)
                    return True
        f.close()

def build_subscriptions(symbols):
    with open("./subscriptions/tickers.txt", "w") as f:
        for symbol in symbols:
            arg: str = f"\"{symbol.lower()}@ticker\""
            subscription: str = '{"method": "SUBSCRIBE", "params": [' + arg + ']}'
            f.write("{")
            f.write(f'\"{symbol}\": {subscription}')
            f.write("}\r\n")

        f.close()

def find_symbols() -> List[str]:
    symbols: list[str] = []
    with open("./tickers.txt", "r") as f:
        while True:
            content = f.read()
            if (content == ""):
                f.close()
                break
            while(content.find('\"symbol\":') > 0):
                index: int = content.find('\"symbol\":\"')
                content = content[index+10:]
                index = content.find('\"')
                symbol: str = content[0:index]
                symbols.append(symbol)
                #print(symbol)
    with open("./symbols.txt", "w") as f:
        f.write(json.dumps({
            "symbols": symbols
        }) + "\r\n")
        f.close()
    return symbols
    
symbols = find_symbols()
build_subscriptions(symbols)
ticker_exists("CKBUSDT")

#exchange: str = "binance"
#build_common_symbols(exchange, f"/home/rabindar/web/my-binance-connector/src/main/java/com/example/{exchange}/publicInfo/symbols.txt")
#print(len(common_symbols_usdt(exchange)))
'''
test_symbols: List[str] = ["VEXTUSDT", "LISUSDT", "XWGUSDT", "MAGICUSDT", "PENDLEUSDT", "CTTUSDT", "NEONUSDT", "SPARTAUSDT", "COMUSDT", "VVUSDT", "VINUUSDT", "ROSEUSDT", "AVAX2LUSDT", "APE2SUSDT", "FMBUSDT", "FLRUSDT", "NEXTUSDT", "ORTUSDT"]
result = f"["
for symbol in test_symbols:
    if ticker_exists(symbol):
        result += f"\"{symbol}\", "
result += "]"
print(result)
'''
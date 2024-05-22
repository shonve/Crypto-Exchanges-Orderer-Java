import json
from typing import List, Dict

# {"op": "subscribe", "args": [{"instType":"SPOT","channel":"ticker","instId":"BTCUSDT"}]}
# {"method": "SUBSCRIBE","params": ["btcusdt@ticker"],"id":1}

def build_common_symbols(exchange: str, symbols_path: str):
    common_symbols = []
    exchange_symbols = []
    with open(symbols_path, "r") as f:
        res: dict = json.loads(f.read())
        for symbol in res['symbols']:
            exchange_symbols.append(symbol.replace("_", ""))
        f.close()
    with open("./symbols.txt", "r") as f:
        res: dict = json.loads(f.read())
        symbols = res['symbols']
        f.close()
    
    for symbol in symbols:
        symbol = symbol.replace("_", "")
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

def ticker(symbol: str):
    with open("./subscriptions/tickers.txt", "r") as f:
        for line in f:
            if (line.find(symbol) > -1):
                res: dict = json.loads(line)
                if (res.__contains__(symbol)):
                    return res
        f.close()
    return None

def build_subscriptions(symbols):
    # {"BTCUSDT":{"event": "subscribe", "channel": "spot.tickers", "payload": ["BTC_USDT"]}}
    with open("./subscriptions/tickers.txt", "w") as f:
        subscription: dict = {}
        subscription["event"] = "subscribe"
        subscription["channel"] = "spot.tickers"
        for symbol in symbols:
            subscription["payload"] = [symbol]
            f.write(json.dumps({
                symbol.replace("_", ""): subscription
            }) + "\r\n")

def find_symbols() -> List[str]:
    symbols: list[str] = []
    with open("./tickers.txt", "r") as f:
        while True:
            content = f.read()
            if (content == ""):
                f.close()
                break
            substr: str = '\"currency_pair\":\"'
            while(content.find(substr) > -1):
                index: int = content.find(substr)
                content = content[index+len(substr):]
                index = content.find('\"')
                symbol: str = content[0:index]
                symbols.append(symbol)

    with open("./symbols.txt", "w") as f:
        f.write(json.dumps({
            "symbols": symbols
        }) + "\r\n")
        f.close()
    return symbols
    
symbols = find_symbols()
build_subscriptions(symbols)
#exchange: str = "bitget"
#build_common_symbols(exchange, f"/home/rabindar/web/my-binance-connector/src/main/java/com/example/{exchange}/publicInfo/symbols.txt")
#print(len(common_symbols_usdt(exchange)))


test_symbols: List[str] = ["GTCETH", "GALA5LUSDT", "FLOKICEOUSDT", "MESAETH", "GTCUSDT", "MESAUSDT", "AIEUSDT", "BABYBONKUSDT", "AICODEUSDT", "AGBUSDT", "POOHUSDT", "GALA3LUSDT", "GTCBTC", "GAYPEPEUSDT", "AVTUSDT", "LITHETH", "LITHUSDT", "LOOPUSDT"]
final_symbols: Dict[str, str] = {}
#result = f"["
for symbol in test_symbols:
    res: dict = ticker(symbol)
    if (res is None):
        continue
    raw_symbol: str = res[symbol]["payload"][0]
    final_symbols[symbol] = raw_symbol
#result += "]"
print(final_symbols)
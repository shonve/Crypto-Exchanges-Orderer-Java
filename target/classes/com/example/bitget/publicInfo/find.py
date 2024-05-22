import json
from typing import List, Dict

# {"op": "subscribe", "args": [{"instType":"SPOT","channel":"ticker","instId":"BTCUSDT"}]}

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

def ticker(symbol: str):
    with open("./subscriptions/tickers.txt", "r") as f:
        for line in f:
            if (line.find(symbol) > -1):
                res: dict = json.loads(line)
                if (res.__contains__(symbol)):
                    return res
        f.close()
    return None


def common_symbols_usdt(exchange: str):
    symbols_usdt = []
    with open(f"../common/{exchange}/symbols.txt", "r") as f:
        res: dict = json.loads(f.read())
        symbols = res['symbols']
        for symbol in symbols:
            if (symbol.find("USDT") > -1):
                symbols_usdt.append(symbol)
    
    return symbols_usdt

def usdt_eth_symbols(symbols_usdt: List[str]):
    symbols = []
    with open("./symbols.txt", "r") as f:
        res: dict = json.loads(f.read())
        for symbol in res['symbols']:
            if (symbol.find("ETH")):
                usdt_symbol = symbol.replace("ETH", "USDT")
                if (usdt_symbol in symbols_usdt):
                    symbols.append(usdt_symbol)

        f.close()
    
    return symbols


def usdt_symbols():
    symbols = []
    with open("./symbols.txt", "r") as f:
        res: dict = json.loads(f.read())
        for symbol in res['symbols']:
            if (symbol.find("USDT")):
                symbols.append(symbol)
        f.close()
    
    return symbols

def build_subscriptions(symbols):
    with open("./subscriptions/tickers.txt", "w") as f:
        for symbol in symbols:
            arg: str = json.dumps({"instType": "SPOT", "channel": "ticker", "instId": symbol})
            subscription: str = '{"op": "subscribe", "args": [' + arg + ']}'
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
    
#symbols = find_symbols()
#build_subscriptions(symbols)
#symbols_usdt = usdt_symbols()
#symbols_usdt_eth = usdt_eth_symbols(symbols_usdt)
test_symbols: List[str] = ["XETAUSDT", "TAMAUSDT", "IMGNAIUSDT", "BRISEUSDT", "AITECHUSDT", "AIMXUSDT", "BABYBONKUSDT", "BABYDRAGONUSDT"]
final_symbols: Dict[str, str] = {}
#result = f"["
for symbol in test_symbols:
    res: dict = ticker(symbol)
    if (res is None):
        continue
    raw_symbol: str = res[symbol]["args"][0]["instId"]
    final_symbols[symbol] = raw_symbol
#result += "]"
print(final_symbols)
#exchange: str = "bybit"
#build_common_symbols(exchange, f"/home/rabindar/web/my-binance-connector/src/main/java/com/example/{exchange}/publicInfo/symbols.txt")
#print(common_symbols_usdt(exchange))

#print(f"usdt symbols: {len(symbols_usdt)}, usdt_eth_symbols: {len(symbols_usdt_eth)}")
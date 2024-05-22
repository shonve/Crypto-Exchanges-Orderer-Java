import json
from typing import List


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
            #print(line)
            if (line.find(symbol) > -1):
                print(line)
                return True
        f.close()
    
    return False

def build_subscriptions(symbols):
    with open("./subscriptions/tickers.txt", "w") as f:
        for symbol in symbols:
            subscription: str = json.dumps({ "op": "subscribe", "args": [f"tickers.{symbol}"]})
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
    

#print(common_symbols_usdt("bitget"))

#exchange: str = "binance"
#build_common_symbols(exchange, f"/home/rabindar/web/my-binance-connector/src/main/java/com/example/{exchange}/publicInfo/symbols.txt")
#print(len(common_symbols_usdt(exchange)))

def filter_lists(all_symbols: List[list]):
    symbols: List[str] = []
    symbols_list: List[list] = []
    initial_count: int = 0
    for sub_symbols in all_symbols:
        initial_count += len(sub_symbols)
        new_symbols: List[str] = []
        for symbol in sub_symbols:
            if (symbol not in symbols):
                new_symbols.append(symbol)
            else:
                print(f"{symbol}")
        symbols = list(set(symbols+sub_symbols))
        symbols_list.append(new_symbols)
    
    print(f"initial symbols count: {initial_count}, final symbols count: {len(symbols)}")
    return symbols_list

'''
symbols: List[list] = filter_lists([
        ['3PUSDT', 'AGIUSDT', 'BOBUSDT', 'CAPOUSDT', 'CATUSDT', 'COMUSDT', 'COQUSDT', 'DUELUSDT', 'KOKUSDT', 'MCTUSDT', 'OMNUSDT', 'ONDOUSDT', 'POKTUSDT', 'SOSUSDT', 'TAPUSDT', 'TURBOSUSDT', 'XWGUSDT', 'BONKUSDT'], 
        ['BTCUSDT', 'ETHUSDT', 'SOLUSDT', 'DOGEUSDT', 'PEPEUSDT', 'WLDUSDT', 'ALGOUSDT', 'LUNCUSDT', 'NEARUSDT', 'OPUSDT', 'SHIBUSDT', 'ORDIUSDT', 'DOTUSDT', 'FTMUSDT'],
        ["TRVLUSDT", "ORTUSDT", "DOMEUSDT", "CWARUSDT", "RATSUSDT", "SHIBUSDT", "QTUMUSDT", "SAILUSDT", "AZYUSDT", "FITFIUSDT", "CULTUSDT", "XYMUSDT", "BTTUSDT", "BABYDOGEUSDT", "DOGE2LUSDT", "OMNIUSDT", "GSTUSDT", "DSRUNUSDT", "PSTAKEUSDT", "XECUSDT"],
        ['FLOKIUSDT', 'TVKUSDT', 'MTKUSDT', 'MYROUSDT', 'PIPUSDT', 'ORTUSDT', 'VANRYUSDT', 'OMNIUSDT', 'LINGUSDT', 'TRVLUSDT', 'MEMEUSDT', 'GPTUSDT', 'KMONUSDT', "TAMAUSDT"],
        ['QMALLUSDT', 'FTM2LUSDT', 'TELUSDT', 'KDAUSDT', 'ARTYUSDT', 'DECHATUSDT', 'FORTUSDT'],
        ['ARTYUSDT', 'BCHUSDT', 'BTGUSDT', 'DOGEEUR', 'DOGEUSDC', 'DOT3LUSDT', 'FETUSDT', 'FILUSDT', 'HOTUSDT', 'HVHUSDT', 'INJUSDT', 'KDAUSDT', 'LADYSUSDT', 'LTCUSDT', 'MCTUSDT', 'MEMEUSDT', 'MYROUSDT', 'NEONUSDT', 'OMGUSDT', 'PEOPLEUSDT', 'PSGUSDT', 'SALDUSDT', 'SATSUSDT', 'SHIBUSDC', 'SPELLUSDT', 'SRMUSDT', 'SUIAUSDT', 'TOKENUSDT', 'VANRYUSDT', 'XWGUSDT', 'ZIGUSDT']
        ])
print(symbols)
'''

check_symbols: List[str] = ['OPUSDT', 'NFTUSDT', 'OMNIUSDT', 'SHIBUSDC', 'FORTUSDT', 'NEONUSDT', 
'ONDOUSDT', 'DOGEUSDT', 'DZOOUSDT', 'QMALLUSDT', 'SUIAUSDT', 'QTUMUSDT', 'LUNCUSDT', 'TELUSDT', 
'ADAUSDT', 'ETHUSDT', 'VPADUSDT', 'MVLUSDT', '3PUSDT', 'FILUSDT', 'MCTUSDT', 'SOSUSDT', 'ORDIUSDT',
'XWGUSDT', 'COQUSDT', 'MATICUSDT', 'DOGE2LUSDT', 'FARUSDT', 'PIPUSDT', 'PEPEUSDT', 'FTM2LUSDT',
'SHIBUSDT', 'JASMYUSDT', 'BTCUSDT', 'FMCUSDT', 'FTMUSDT', 'POKTUSDT', 'DUELUSDT', 'APEUSDT', 
'LINGUSDT', 'ZIGUSDT', 'ARTYUSDT', 'CAPOUSDT', 'CATUSDT', 'TVKUSDT', 'GSTUSDT', 'GALAUSDT', 
'VANRYUSDT', 'THETAUSDT', 'SPELLUSDT', 'TOKENUSDT', 'OMGUSDT', 'WLDUSDT', 'BONKUSDT', 'QORPOUSDT',
'INJUSDT', 'BNBUSDT', '1SOLUSDT', 'ARBUSDT', 'XAIUSDT', 'DOT3LUSDT', 'SOLUSDT', 'OMNUSDT',
'KMONUSDT', 'ORTUSDT', 'LINKUSDT', 'STRKUSDT', 'DOGEUSDC', 'AGIXUSDT', 'LTCUSDT', 'MTKUSDT',
'VVUSDT', 'KOKUSDT', 'RACAUSDT', 'TAMAUSDT', 'SALDUSDT', 'BOBUSDT', 'PEOPLEUSDT', 'BBLUSDT',
'SRMUSDT', 'COMUSDT', 'ZKFUSDT', 'FLOKIUSDT', 'AXLUSDT', 'MEMEUSDT', 'PSGUSDT', 'TRVLUSDT',
'VRAUSDT', 'ERTHAUSDT', 'AFGUSDT', 'KDAUSDT', 'NEARUSDT', 'ALGOUSDT', 'AVAXUSDT', 'AGIUSDT', 
'SATSUSDT', 'TURBOSUSDT', 'BABYDOGEUSDT', 'BTGUSDT', 'UNIUSDT', 'RATSUSDT', 'XRPUSDT', 'DOGEEUR',
'GPTUSDT', 'DECHATUSDT', 'LADYSUSDT', 'BCHUSDT', 'TAPUSDT', 'PORTALUSDT', 'MYROUSDT', 'DOTUSDT',
'SLPUSDT', 'HOTUSDT', 'DEVTUSDT', 'FETUSDT', 'HVHUSDT', "TRVLUSDT", "ORTUSDT", "DOGEEUR", 
"DOMEUSDT", "CWARUSDT", "RATSUSDT", "SHIBUSDT", "QTUMUSDT", "SAILUSDT", "COMUSDT", "AZYUSDT", 
"FITFIUSDT", "DOGEUSDT", "CULTUSDT", "SHIBUSDC", "XYMUSDT", "DOGEUSDC", "BTTUSDT", "BONKUSDT",
"BABYDOGEUSDT", "DOGE2LUSDT", "OMNIUSDT", "GSTUSDT", "DSRUNUSDT", "PSTAKEUSDT", "XECUSDT"
]
new_symbols: List[str] =  ["APPUSDT", "CTCUSDT", "LADYSUSDT", "FLOKIUSDT", "TELUSDT", "PEPEUSDT", "RUNEUSDT", "REALUSDT", "TVKUSDT", "HVHUSDT", "LENDSUSDT", "EOS2LUSDT", "INSPUSDT", "AEGUSDT", "AGIUSDT", "KONUSDT", "MEEUSDT", "AIOZUSDT", "BOBAUSDT", "GPTUSDT", "SEILORUSDT", "NEONUSDT", "KMONUSDT", "ERTHAUSDT", "BTGUSDT"]


final_symbols: List[str] = []
for symbol in new_symbols:
    if (symbol not in check_symbols):
        final_symbols.append(symbol)
        #print(f"{symbol} exists")
        #new_symbols.remove(symbol)

#print(final_symbols)
check_symbols += final_symbols
#print(check_symbols)

def check_tickers(symbols: List[str]):
    count: int = 0
    for symbol in symbols:
        if (ticker_exists(symbol)):
            count += 1
    print(f"symbols: {len(symbols)}, tickers: {count}")


'''
final_symbols: set = set(check_symbols)
print(f"check symbols: {len(check_symbols)}, final symbols: {len(final_symbols)}")
count: int = 0
not_present: List[str] = []
#check_symbols: List[str] = ['FLOKIUSDT', 'TVKUSDT', 'MTKUSDT', 'LADYSUSDT', 'SRMUSDT', 'BABYDOGEUSDT', 'MYROUSDT', 'PIPUSDT', 'ORTUSDT', 'VANRYUSDT', 'OMNIUSDT', 'LINGUSDT', 'ERTHAUSDT', 'TRVLUSDT', 'DOGE2LUSDT', 'TOKENUSDT', 'MEMEUSDT', 'GPTUSDT', 'VPADUSDT', 'KMONUSDT']
for symbol in check_symbols:
    if (ticker_exists(symbol)):
        count += 1
    else:
        not_present.append(symbol)
print(f"symbols: {len(check_symbols)}, present: {count}, not present: {not_present}")
'''

#symbols: List[str] = ['APPUSDT', 'CTCUSDT', 'RUNEUSDT', 'REALUSDT', 'LENDSUSDT', 'EOS2LUSDT', 'INSPUSDT', 'AEGUSDT', 'KONUSDT', 'MEEUSDT', 'AIOZUSDT', 'BOBAUSDT', 'SEILORUSDT']
#symbols: List[str] = ["RAINUSDT", "SOSUSDT", "TAMAUSDT", "BTGUSDT", "CBXUSDT", "GPTUSDT"]
#check_tickers(symbols)

symbols = find_symbols()
build_subscriptions(symbols)
symbols: List[str] = ['NIBIUSDT']
for symbol in symbols:
    ticker_exists(symbol)
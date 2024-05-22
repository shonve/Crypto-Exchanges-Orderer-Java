import json

prices: dict = {}
with open("./tickers.txt", "r") as f:
    tickers: list = json.loads(f.read())['result']['list']
    for item in tickers:
        symbol: str = item['symbol']
        price: float = float(item['lastPrice'])
        print(f"symbol: {symbol}, price: {price}")
        prices[symbol] = str(price)
    
    f.close()

with open("./prices.txt", "a") as f:
    f.write(json.dumps(prices))
    f.close()
import json

def calc_change(p1: float, p2: float):
    return (p2-p1)/p1

def get_change(date: str, t1: str, t2: str):
    prices1: dict = None
    prices2: dict = None
    time1: str = ""
    time2: str = ""
    with open("../prices/prices.txt", "r") as f:
        for line in f:
            res: dict = json.loads(line)
            if (res['date'] != date):
                continue
            time: str = res['time']
            if (time.find(t1) > -1):
                prices1 = res['prices']
                time1 = time
            if (time.find(t2) > -1):
                prices2 = res['prices']
                time2 = time

        f.close()
    
    if (prices1 is None or prices2 is None):
        return
    
    for symbol in prices1:
        if (prices2.__contains__(symbol)):
            change: float = calc_change(prices1[symbol], prices2[symbol])
            print(f"date: {date}, t1: {time1}, t2: {time2}, symbol: {symbol}, change: {change}")

yesterday_prices: dict = {}
with open("./prices.txt", "r") as f:
    yesterday_prices = json.loads(f.read())
    f.close()


today_prices: dict = {}
with open("../prices/prices.txt", "r") as f:
    for line in f:
        res: dict = json.loads(line)
        today_prices = res['prices']
        time: str = res['time']
        date: str = res['date']
        res = {}
        print(f"date: {date}, time: {time}, prices: {today_prices}")
        print(f"\r\n\r\n")
    f.close()


symbols: str = ""
for symbol in yesterday_prices:
    if (today_prices.__contains__(symbol)):
        y_price: float = float(yesterday_prices[symbol])
        t_price: float = float(today_prices[symbol])
        change: float = (t_price-y_price)/y_price
        if (change < 0.05):
            print(f"symbol: {symbol}, yesterday-price: {y_price}, today-price: {t_price}, change: {change}")
            #symbols += f"\"{symbol}\" "

print(symbols)

#change("2024-02-15", "10", "12")
        



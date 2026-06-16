import './StockCard.css';

function StockCard({ stock, onClick }) {
    const isUp = stock.changePercent >= 0;

    return (
        <div className="stock-card" onClick={onClick}>
            <div className="stock-card-header">
                <div className="stock-symbol">{stock.symbol}</div>
                <div className={`stock-change ${isUp ? 'up' : 'down'}`}>
                    {isUp ? '▲' : '▼'} {Math.abs(stock.changePercent)?.toFixed(2)}%
                </div>
            </div>
            <div className="stock-name">{stock.name}</div>
            <div className="stock-price">
                {stock.currency} {stock.price?.toFixed(2)}
            </div>
            <div className="stock-footer">
                <span className={`stock-change-amount ${isUp ? 'up' : 'down'}`}>
                    {isUp ? '+' : ''}{stock.change?.toFixed(2)} today
                </span>
                <button className="btn-trade">Trade →</button>
            </div>
        </div>
    );
}

export default StockCard;
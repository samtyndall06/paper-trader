import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getStock } from '../services/api';
import './Trade.css';
import StockChart from '../components/StockChart';

function Trade() {
    const { symbol } = useParams();
    const navigate = useNavigate();
    const [stock, setStock] = useState(null);
    const [loading, setLoading] = useState(true);
    const [shares, setShares] = useState(1);
    const [tradeType, setTradeType] = useState('BUY');
    const [message, setMessage] = useState(null);

    useEffect(() => {
        getStock(symbol)
            .then(res => {
                setStock(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, [symbol]);

    const total = stock ? (stock.price * shares).toFixed(2) : 0;
    const isUp = stock?.changePercent >= 0;

    const handleTrade = () => {
        // We'll wire this to the backend later
        setMessage({
            type: tradeType === 'BUY' ? 'success' : 'danger',
            text: `${tradeType} order placed: ${shares} shares of ${symbol} at $${stock.price.toFixed(2)}`
        });
        setTimeout(() => setMessage(null), 3000);
    };

    if (loading) return <div className="loading">Loading stock data...</div>;
    if (!stock) return <div className="loading">Stock not found.</div>;

    return (
        <div className="trade-page">

            {/* BACK BUTTON */}
            <button className="btn btn-outline back-btn" onClick={() => navigate('/')}>
                ← Back
            </button>

            {/* STOCK HEADER */}
            <div className="trade-header card">
                <div className="trade-header-left">
                    <div className="trade-symbol">{stock.symbol}</div>
                    <div className="trade-name">{stock.name}</div>
                </div>
                <div className="trade-header-right">
                    <div className="trade-price">
                        {stock.currency} {stock.price?.toFixed(2)}
                    </div>
                    <div className={`trade-change ${isUp ? 'up' : 'down'}`}>
                        {isUp ? '▲' : '▼'} {Math.abs(stock.changePercent)?.toFixed(2)}%
                        ({isUp ? '+' : ''}{stock.change?.toFixed(2)} today)
                    </div>
                </div>
            </div>

            {/* CHART */}
            <StockChart symbol={symbol} />

            {/* CONTENT */}
            <div className="trade-content">

                {/* STOCK STATS */}
                <div className="stock-stats card">
                    <h2>Stock Details</h2>
                    <div className="stats-grid">
                        <div className="stat-item">
                            <span className="stat-label">Current Price</span>
                            <span className="stat-value">
                                {stock.currency} {stock.price?.toFixed(2)}
                            </span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-label">Change Today</span>
                            <span className={`stat-value ${isUp ? 'up' : 'down'}`}>
                                {isUp ? '+' : ''}{stock.change?.toFixed(2)}
                            </span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-label">Change %</span>
                            <span className={`stat-value ${isUp ? 'up' : 'down'}`}>
                                {isUp ? '+' : ''}{stock.changePercent?.toFixed(2)}%
                            </span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-label">Volume</span>
                            <span className="stat-value">
                                {stock.volume?.toLocaleString()}
                            </span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-label">Currency</span>
                            <span className="stat-value">{stock.currency}</span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-label">Market</span>
                            <span className="stat-value">
                                {symbol.includes('.NZ') ? 'NZX' :
                                 symbol.includes('.AX') ? 'ASX' :
                                 symbol.includes('.L') ? 'LSE' :
                                 symbol.includes('.T') ? 'TSE' :
                                 symbol.includes('.HK') ? 'HKEX' : 'NASDAQ/NYSE'}
                            </span>
                        </div>
                    </div>
                </div>

                {/* TRADE FORM */}
                <div className="trade-form card">
                    <h2>Place Order</h2>

                    {message && (
                        <div className={`trade-message ${message.type}`}>
                            {message.text}
                        </div>
                    )}

                    {/* BUY / SELL toggle */}
                    <div className="trade-toggle">
                        <button
                            className={`toggle-btn ${tradeType === 'BUY' ? 'active-buy' : ''}`}
                            onClick={() => setTradeType('BUY')}
                        >
                            Buy
                        </button>
                        <button
                            className={`toggle-btn ${tradeType === 'SELL' ? 'active-sell' : ''}`}
                            onClick={() => setTradeType('SELL')}
                        >
                            Sell
                        </button>
                    </div>

                    <div className="form-group">
                        <label>Number of Shares</label>
                        <input
                            type="number"
                            min="1"
                            value={shares}
                            onChange={e => setShares(Math.max(1, parseInt(e.target.value) || 1))}
                        />
                    </div>

                    <div className="trade-summary">
                        <div className="summary-row">
                            <span>Price per share</span>
                            <span>{stock.currency} {stock.price?.toFixed(2)}</span>
                        </div>
                        <div className="summary-row">
                            <span>Shares</span>
                            <span>{shares}</span>
                        </div>
                        <div className="summary-row summary-total">
                            <span>Total</span>
                            <span>{stock.currency} {total}</span>
                        </div>
                    </div>

                    <button
                        className={`btn trade-btn ${tradeType === 'BUY' ? 'btn-success' : 'btn-danger'}`}
                        onClick={handleTrade}
                    >
                        {tradeType === 'BUY' ? '▲ Buy' : '▼ Sell'} {shares} Share{shares > 1 ? 's' : ''}
                    </button>

                    <p className="trade-note">
                        Paper trading only — no real money involved
                    </p>
                </div>
            </div>
        </div>
    );
}

export default Trade;
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getStock, buyStock, sellStock } from '../services/api';
import { useAuth } from '../context/AuthContext';
import StockChart from '../components/StockChart';
import './Trade.css';

function Trade() {
    const { symbol } = useParams();
    const navigate = useNavigate();
    const { user, login } = useAuth();
    const [stock, setStock] = useState(null);
    const [loading, setLoading] = useState(true);
    const [shares, setShares] = useState(1);
    const [tradeType, setTradeType] = useState('BUY');
    const [message, setMessage] = useState(null);
    const [submitting, setSubmitting] = useState(false);

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

    const handleTrade = async () => {
        if (!user) {
            navigate('/login');
            return;
        }

        setSubmitting(true);
        setMessage(null);

        try {
            const action = tradeType === 'BUY' ? buyStock : sellStock;
            const res = await action(user.email, symbol, shares);

            setMessage({
                type: 'success',
                text: res.data.message
            });

            // Update the user's balance in context
            login({ ...user, balance: res.data.newBalance });

        } catch (err) {
            setMessage({
                type: 'danger',
                text: err.response?.data?.error || 'Trade failed'
            });
        }
        setSubmitting(false);
    };

    if (loading) return <div className="loading">Loading stock data...</div>;
    if (!stock) return <div className="loading">Stock not found.</div>;

    return (
        <div className="trade-page">

            <button className="btn btn-outline back-btn" onClick={() => navigate('/')}>
                ← Back
            </button>

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

            <StockChart symbol={symbol} />

            <div className="trade-content">

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
                    </div>
                </div>

                <div className="trade-form card">
                    <h2>Place Order</h2>

                    {!user && (
                        <div className="trade-message danger">
                            Please login to start trading
                        </div>
                    )}

                    {message && (
                        <div className={`trade-message ${message.type}`}>
                            {message.text}
                        </div>
                    )}

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
                        disabled={submitting}
                    >
                        {submitting ? 'Processing...' :
                            `${tradeType === 'BUY' ? '▲ Buy' : '▼ Sell'} ${shares} Share${shares > 1 ? 's' : ''}`
                        }
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
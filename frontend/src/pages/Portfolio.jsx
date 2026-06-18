import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPortfolio, getTradeHistory } from '../services/api';
import { useAuth } from '../context/AuthContext';
import './Portfolio.css';

function Portfolio() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [holdings, setHoldings] = useState([]);
    const [trades, setTrades] = useState([]);
    const [loading, setLoading] = useState(true);
    const [tab, setTab] = useState('holdings');

    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }
        Promise.all([
            getPortfolio(user.email),
            getTradeHistory(user.email)
        ]).then(([portfolioRes, tradesRes]) => {
            setHoldings(portfolioRes.data);
            setTrades(tradesRes.data);
            setLoading(false);
        }).catch(err => {
            console.error(err);
            setLoading(false);
        });
    }, [user, navigate]);

    if (!user) return null;
    if (loading) return <div className="loading">Loading portfolio...</div>;

    const totalValue = holdings.reduce((sum, h) => sum + h.currentValue, 0);
    const totalGainLoss = holdings.reduce((sum, h) => sum + h.gainLoss, 0);
    const totalCost = totalValue - totalGainLoss;
    const totalGainLossPercent = totalCost > 0 ? (totalGainLoss / totalCost) * 100 : 0;

    return (
        <div className="portfolio-page">
            <h1>My Portfolio</h1>

            {/* SUMMARY CARDS */}
            <div className="summary-cards">
                <div className="summary-card card">
                    <span className="summary-label">Cash Balance</span>
                    <span className="summary-value">
                        ${parseFloat(user.balance).toLocaleString('en-US', {minimumFractionDigits: 2})}
                    </span>
                </div>
                <div className="summary-card card">
                    <span className="summary-label">Portfolio Value</span>
                    <span className="summary-value">
                        ${totalValue.toLocaleString('en-US', {minimumFractionDigits: 2})}
                    </span>
                </div>
                <div className="summary-card card">
                    <span className="summary-label">Total Gain/Loss</span>
                    <span className={`summary-value ${totalGainLoss >= 0 ? 'up' : 'down'}`}>
                        {totalGainLoss >= 0 ? '+' : ''}${totalGainLoss.toLocaleString('en-US', {minimumFractionDigits: 2})}
                        {' '}({totalGainLossPercent >= 0 ? '+' : ''}{totalGainLossPercent.toFixed(2)}%)
                    </span>
                </div>
                <div className="summary-card card">
                    <span className="summary-label">Net Worth</span>
                    <span className="summary-value">
                        ${(parseFloat(user.balance) + totalValue).toLocaleString('en-US', {minimumFractionDigits: 2})}
                    </span>
                </div>
            </div>

            {/* TABS */}
            <div className="portfolio-tabs">
                <button
                    className={`tab-btn ${tab === 'holdings' ? 'active' : ''}`}
                    onClick={() => setTab('holdings')}
                >
                    Holdings
                </button>
                <button
                    className={`tab-btn ${tab === 'history' ? 'active' : ''}`}
                    onClick={() => setTab('history')}
                >
                    Trade History
                </button>
            </div>

            {/* HOLDINGS TAB */}
            {tab === 'holdings' && (
                holdings.length === 0 ? (
                    <div className="empty-state card">
                        <p>You don't own any stocks yet.</p>
                        <button className="btn btn-primary" onClick={() => navigate('/')}>
                            Browse Stocks
                        </button>
                    </div>
                ) : (
                    <div className="holdings-table card">
                        <div className="table-header">
                            <span>Symbol</span>
                            <span>Shares</span>
                            <span>Avg Buy Price</span>
                            <span>Current Price</span>
                            <span>Value</span>
                            <span>Gain/Loss</span>
                        </div>
                        {holdings.map(h => (
                            <div
                                key={h.symbol}
                                className="table-row"
                                onClick={() => navigate(`/trade/${h.symbol}`)}
                            >
                                <span className="row-symbol">{h.symbol}</span>
                                <span>{h.shares}</span>
                                <span>${h.avgBuyPrice?.toFixed(2)}</span>
                                <span>${h.currentPrice?.toFixed(2)}</span>
                                <span>${h.currentValue?.toFixed(2)}</span>
                                <span className={h.gainLoss >= 0 ? 'up' : 'down'}>
                                    {h.gainLoss >= 0 ? '+' : ''}${h.gainLoss?.toFixed(2)}
                                    {' '}({h.gainLossPercent >= 0 ? '+' : ''}{h.gainLossPercent?.toFixed(2)}%)
                                </span>
                            </div>
                        ))}
                    </div>
                )
            )}

            {/* HISTORY TAB */}
            {tab === 'history' && (
                trades.length === 0 ? (
                    <div className="empty-state card">
                        <p>No trades yet.</p>
                    </div>
                ) : (
                    <div className="history-table card">
                        <div className="table-header history-header">
                            <span>Type</span>
                            <span>Symbol</span>
                            <span>Shares</span>
                            <span>Price</span>
                            <span>Total</span>
                            <span>Date</span>
                        </div>
                        {trades.map(t => (
                            <div key={t.id} className="table-row history-row">
                                <span className={`trade-type ${t.type === 'BUY' ? 'buy' : 'sell'}`}>
                                    {t.type}
                                </span>
                                <span className="row-symbol">{t.symbol}</span>
                                <span>{t.shares}</span>
                                <span>${t.price?.toFixed(2)}</span>
                                <span>${t.total?.toFixed(2)}</span>
                                <span className="trade-date">
                                    {new Date(t.createdAt).toLocaleDateString()}
                                </span>
                            </div>
                        ))}
                    </div>
                )
            )}
        </div>
    );
}

export default Portfolio;
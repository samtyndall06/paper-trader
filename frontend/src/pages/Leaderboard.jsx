import { useState, useEffect } from 'react';
import { getLeaderboard } from '../services/api';
import { useAuth } from '../context/AuthContext';
import './Leaderboard.css';

function Leaderboard() {
    const [rankings, setRankings] = useState([]);
    const [loading, setLoading] = useState(true);
    const { user } = useAuth();

    useEffect(() => {
        getLeaderboard()
            .then(res => {
                setRankings(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, []);

    const medalEmoji = (rank) => {
        if (rank === 1) return '🥇';
        if (rank === 2) return '🥈';
        if (rank === 3) return '🥉';
        return null;
    };

    if (loading) return <div className="loading">Loading leaderboard...</div>;

    return (
        <div className="leaderboard-page">
            <h1>Leaderboard</h1>
            <p className="leaderboard-subtitle">
                Ranked by total net worth — starting balance was $10,000
            </p>

            <div className="leaderboard-table card">
                <div className="lb-header">
                    <span>Rank</span>
                    <span>Trader</span>
                    <span>Net Worth</span>
                    <span>Gain/Loss</span>
                </div>
                {rankings.map(r => (
                    <div
                        key={r.rank}
                        className={`lb-row ${user?.name === r.name ? 'lb-row-you' : ''}`}
                    >
                        <span className="lb-rank">
                            {medalEmoji(r.rank) || `#${r.rank}`}
                        </span>
                        <span className="lb-name">
                            {r.name} {user?.name === r.name && <span className="lb-you-tag">You</span>}
                        </span>
                        <span className="lb-networth">
                            ${r.netWorth.toLocaleString('en-US', {minimumFractionDigits: 2})}
                        </span>
                        <span className={`lb-gain ${r.gainLoss >= 0 ? 'up' : 'down'}`}>
                            {r.gainLoss >= 0 ? '+' : ''}${r.gainLoss.toLocaleString('en-US', {minimumFractionDigits: 2})}
                            {' '}({r.gainLossPercent >= 0 ? '+' : ''}{r.gainLossPercent.toFixed(2)}%)
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Leaderboard;
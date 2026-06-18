import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './AiSuggestions.css';

function AiSuggestions({ country }) {
    const [suggestions, setSuggestions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        setLoading(true);
        setError(false);
        axios.get(`/api/ai/suggestions?country=${country}`)
            .then(res => {
                setSuggestions(res.data.suggestions || []);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setError(true);
                setLoading(false);
            });
    }, [country]);

    const confidenceColor = (level) => {
        if (level === 'High') return 'high';
        if (level === 'Medium') return 'medium';
        return 'low';
    };

    if (loading) {
        return (
            <div className="ai-suggestions card">
                <div className="ai-header">
                    <span className="ai-icon">✨</span>
                    <h2>AI Suggestions</h2>
                </div>
                <div className="ai-loading">Analyzing market trends...</div>
            </div>
        );
    }

    if (error || suggestions.length === 0) {
        return null;
    }

    return (
        <div className="ai-suggestions card">
            <div className="ai-header">
                <span className="ai-icon">✨</span>
                <h2>AI Suggestions</h2>
                <span className="ai-badge">Powered by local AI</span>
            </div>
            <div className="ai-list">
                {suggestions.map((s, i) => (
                    <div
                        key={i}
                        className="ai-suggestion-item"
                        onClick={() => navigate(`/trade/${s.symbol}`)}
                    >
                        <div className="ai-suggestion-left">
                            <span className="ai-symbol">{s.symbol}</span>
                            <span className="ai-reason">{s.reason}</span>
                        </div>
                        <span className={`ai-confidence ${confidenceColor(s.confidence)}`}>
                            {s.confidence}
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default AiSuggestions;
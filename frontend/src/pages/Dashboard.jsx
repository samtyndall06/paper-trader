import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getStocks, getCountries } from '../services/api';
import StockCard from '../components/StockCard';
import './Dashboard.css';
import AiSuggestions from '../components/AiSuggestions';

function Dashboard() {
    const [stocks, setStocks] = useState([]);
    const [countries, setCountries] = useState({});
    const [selectedCountry, setSelectedCountry] = useState('GLOBAL');
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const navigate = useNavigate();

    // Load available countries on startup
    useEffect(() => {
        console.log('Fetching countries...');
        getCountries()
            .then(res => {
                console.log('Countries:', res.data);
                setCountries(res.data);
            })
            .catch(err => console.error('Countries error:', err));
    }, []);

    useEffect(() => {
        console.log('Fetching stocks for:', selectedCountry);
        setLoading(true);
        getStocks(selectedCountry)
            .then(res => {
                console.log('Stocks:', res.data);
                setStocks(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error('Stocks error:', err);
                setLoading(false);
            });
    }, [selectedCountry]);

    // Filter stocks by search
    const filteredStocks = stocks.filter(stock =>
        stock.symbol?.toLowerCase().includes(search.toLowerCase()) ||
        stock.name?.toLowerCase().includes(search.toLowerCase())
    );

    return (
        <div className="dashboard">

            {/* HEADER */}
            <div className="dashboard-header">
                <div>
                    <h1>Market Overview</h1>
                    <p className="dashboard-subtitle">
                        Real-time prices — updates every 30 seconds
                    </p>
                </div>
                <input
                    type="text"
                    placeholder="Search stocks..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    className="search-input"
                />
            </div>

            {/* COUNTRY FILTERS */}
            <div className="country-filters">
                {Object.entries(countries).map(([code, name]) => (
                    <button
                        key={code}
                        className={`filter-btn ${selectedCountry === code ? 'active' : ''}`}
                        onClick={() => setSelectedCountry(code)}
                    >
                        {name}
                    </button>
                ))}
            </div>
            
            {/* AI SUGGESTIONS */}
            <AiSuggestions country={selectedCountry} />

            {/* STOCK GRID */}
            {loading ? (
                <div className="loading">Loading stocks...</div>
            ) : (
                <>
                    <p className="stock-count">
                        {filteredStocks.length} stocks
                    </p>
                    <div className="stock-grid">
                        {filteredStocks.map(stock => (
                            <StockCard
                                key={stock.symbol}
                                stock={stock}
                                onClick={() => navigate(`/trade/${stock.symbol}`)}
                            />
                        ))}
                    </div>
                </>
            )}
        </div>
    );
}

export default Dashboard;
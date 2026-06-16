import { useState, useEffect } from 'react';
import {
    AreaChart, Area, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer
} from 'recharts';
import { getStockHistory } from '../services/api';
import './StockChart.css';

function StockChart({ symbol, color = '#0071e3' }) {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [range, setRange] = useState('30D');

    useEffect(() => {
        getStockHistory(symbol)
            .then(res => {
                setData(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, [symbol]);

    // Filter data based on selected range
    const filteredData = () => {
        switch (range) {
            case '7D': return data.slice(-7);
            case '14D': return data.slice(-14);
            case '30D': return data;
            default: return data;
        }
    };

    const chartData = filteredData();
    const firstPrice = chartData[0]?.price || 0;
    const lastPrice = chartData[chartData.length - 1]?.price || 0;
    const isUp = lastPrice >= firstPrice;
    const chartColor = isUp ? '#1a9e3f' : '#d93025';

    // Custom tooltip
    const CustomTooltip = ({ active, payload, label }) => {
        if (active && payload && payload.length) {
            return (
                <div className="chart-tooltip">
                    <p className="tooltip-date">{label}</p>
                    <p className="tooltip-price">
                        ${payload[0].value?.toFixed(2)}
                    </p>
                </div>
            );
        }
        return null;
    };

    if (loading) return <div className="loading">Loading chart...</div>;

    return (
        <div className="stock-chart">
            <div className="chart-header">
                <div className="chart-title">Price History</div>
                <div className="range-selector">
                    {['7D', '14D', '30D'].map(r => (
                        <button
                            key={r}
                            className={`range-btn ${range === r ? 'active' : ''}`}
                            onClick={() => setRange(r)}
                        >
                            {r}
                        </button>
                    ))}
                </div>
            </div>

            <ResponsiveContainer width="100%" height={280}>
                <AreaChart data={chartData}>
                    <defs>
                        <linearGradient id="colorGradient" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor={chartColor} stopOpacity={0.3}/>
                            <stop offset="95%" stopColor={chartColor} stopOpacity={0}/>
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e1e2e" />
                    <XAxis
                        dataKey="date"
                        tick={{ fill: '#6e6e73', fontSize: 11 }}
                        tickFormatter={val => val.slice(5)}
                        interval="preserveStartEnd"
                    />
                    <YAxis
                        tick={{ fill: '#6e6e73', fontSize: 11 }}
                        tickFormatter={val => `$${val.toFixed(0)}`}
                        domain={['auto', 'auto']}
                        width={60}
                    />
                    <Tooltip content={<CustomTooltip />} />
                    <Area
                        type="monotone"
                        dataKey="price"
                        stroke={chartColor}
                        strokeWidth={2}
                        fill="url(#colorGradient)"
                        dot={false}
                        activeDot={{ r: 5, fill: chartColor }}
                    />
                </AreaChart>
            </ResponsiveContainer>
        </div>
    );
}

export default StockChart;
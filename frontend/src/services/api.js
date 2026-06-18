import axios from 'axios';

const API_BASE = '/api';

const api = axios.create({
    baseURL: API_BASE,
    headers: { 'Content-Type': 'application/json' }
});

// ---- STOCK API CALLS ----
export const getStocks = (country = 'GLOBAL') =>
    api.get(`/stocks?country=${country}`);

export const getStock = (symbol) =>
    api.get(`/stocks/${symbol}`);

export const getCountries = () =>
    api.get('/stocks/countries');

export const searchStock = (symbol) =>
    api.get(`/stocks/search?symbol=${symbol}`);

// ---- AUTH API CALLS ----
export const register = (name, email, password) =>
    api.post('/auth/register', { name, email, password });

export const login = (email, password) =>
    api.post('/auth/login', { email, password });

// ---- TRADE API CALLS ----
export const buyStock = (email, symbol, shares) =>
    api.post('/trades/buy', { email, symbol, shares });

export const sellStock = (email, symbol, shares) =>
    api.post('/trades/sell', { email, symbol, shares });

export const getTradeHistory = (email) =>
    api.get(`/trades/history?email=${email}`);

export const getPortfolio = (email) =>
    api.get(`/portfolio?email=${email}`);

export const getPortfolioValue = (email) =>
    api.get(`/portfolio/value?email=${email}`);

export default api;

export const getStockHistory = (symbol) =>
    api.get(`/stocks/${symbol}/history`);

export const getLeaderboard = () =>
    api.get('/portfolio/leaderboard');
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
export const buyStock = (symbol, shares) =>
    api.post('/trades/buy', { symbol, shares });

export const sellStock = (symbol, shares) =>
    api.post('/trades/sell', { symbol, shares });

export const getTradeHistory = () =>
    api.get('/trades/history');

// ---- PORTFOLIO API CALLS ----
export const getPortfolio = () =>
    api.get('/portfolio');

export const getPortfolioValue = () =>
    api.get('/portfolio/value');

export default api;
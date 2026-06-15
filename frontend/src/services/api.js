import axios from 'axios';

// Base URL for our Spring Boot backend
const API_BASE = 'http://localhost:8082/api';

const api = axios.create({
    baseURL: API_BASE,
    headers: { 'Content-Type': 'application/json' }
});

// ---- STOCK API CALLS ----

// Get stocks by country e.g. 'GLOBAL', 'NZ', 'US'
export const getStocks = (country = 'GLOBAL') =>
    api.get(`/stocks?country=${country}`);

// Get info for a specific stock
export const getStock = (symbol) =>
    api.get(`/stocks/${symbol}`);

// Get available countries
export const getCountries = () =>
    api.get('/stocks/countries');

// Search for a stock
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
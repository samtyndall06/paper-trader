import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import Portfolio from './pages/Portfolio';
import Trade from './pages/Trade';
import Login from './pages/Login';
import Register from './pages/Register';
import './styles/global.css';
import Leaderboard from './pages/Leaderboard';


function App() {
    return (
        <Router>
            <div className="app">
                <Navbar />
                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<Dashboard />} />
                        <Route path="/portfolio" element={<Portfolio />} />
                        <Route path="/trade/:symbol" element={<Trade />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />
                        <Route path="/leaderboard" element={<Leaderboard />} />
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App;
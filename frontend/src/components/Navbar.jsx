import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

function Navbar() {
    const location = useLocation();
    const navigate = useNavigate();
    const { user, logout } = useAuth();
    const [menuOpen, setMenuOpen] = useState(false);

    const isActive = (path) => location.pathname === path ? 'active' : '';

    const handleLogout = () => {
        logout();
        navigate('/');
        setMenuOpen(false);
    };

    const closeMenu = () => setMenuOpen(false);

    return (
        <>
            <nav className="navbar">
                <Link to="/" className="nav-logo" onClick={closeMenu}>
                    paper<span>trader</span>
                </Link>
                <div className="nav-links">
                    <Link to="/" className={`nav-link ${isActive('/')}`}>Dashboard</Link>
                    <Link to="/portfolio" className={`nav-link ${isActive('/portfolio')}`}>Portfolio</Link>
                    <Link to="/leaderboard" className={`nav-link ${isActive('/leaderboard')}`}>Leaderboard</Link>
                </div>
                <div className="nav-actions">
                    {user ? (
                        <>
                            <div className="nav-balance">
                                💰 ${parseFloat(user.balance).toLocaleString('en-US', {minimumFractionDigits: 2})}
                            </div>
                            <span className="nav-username">{user.name}</span>
                            <button className="btn btn-outline" onClick={handleLogout}>
                                Logout
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="btn btn-outline">Login</Link>
                            <Link to="/register" className="btn btn-primary">Register</Link>
                        </>
                    )}
                </div>
                <button
                    type="button"
                    className={`hamburger ${menuOpen ? 'open' : ''}`}
                    onClick={() => setMenuOpen(!menuOpen)}
                    aria-label="Menu"
                >
                    <span></span>
                    <span></span>
                    <span></span>
                </button>
            </nav>

            <div className={`mobile-menu ${menuOpen ? 'open' : ''}`}>
                <Link to="/" onClick={closeMenu}>Dashboard</Link>
                <Link to="/portfolio" onClick={closeMenu}>Portfolio</Link>
                <Link to="/leaderboard" onClick={closeMenu}>Leaderboard</Link>
                {user ? (
                    <>
                        <div className="mobile-balance">
                            Balance: ${parseFloat(user.balance).toLocaleString('en-US', {minimumFractionDigits: 2})}
                        </div>
                        <button className="btn btn-outline mobile-logout" onClick={handleLogout}>
                            Logout
                        </button>
                    </>
                ) : (
                    <>
                        <Link to="/login" onClick={closeMenu}>Login</Link>
                        <Link to="/register" onClick={closeMenu}>Register</Link>
                    </>
                )}
            </div>
        </>
    );
}

export default Navbar;
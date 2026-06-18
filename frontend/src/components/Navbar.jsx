import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

function Navbar() {
    const location = useLocation();
    const navigate = useNavigate();
    const { user, logout } = useAuth();

    const isActive = (path) => location.pathname === path ? 'active' : '';

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <nav className="navbar">
            <Link to="/" className="nav-logo">
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
        </nav>
    );
}

export default Navbar;
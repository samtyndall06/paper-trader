import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

function Navbar() {
    const location = useLocation();

    const isActive = (path) => location.pathname === path ? 'active' : '';

    return (
        <nav className="navbar">
            <Link to="/" className="nav-logo">
                paper<span>trader</span>
            </Link>
            <div className="nav-links">
                <Link to="/" className={`nav-link ${isActive('/')}`}>Dashboard</Link>
                <Link to="/portfolio" className={`nav-link ${isActive('/portfolio')}`}>Portfolio</Link>
            </div>
            <div className="nav-actions">
                <Link to="/login" className="btn btn-outline">Login</Link>
                <Link to="/register" className="btn btn-primary">Register</Link>
            </div>
        </nav>
    );
}

export default Navbar;
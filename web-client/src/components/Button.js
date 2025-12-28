import React from 'react';
import { Link } from 'react-router-dom';
import '../styles/Button.css';

function Button({ onClick, children, type = "button", className = "", href, to, disabled }) {
    if (to) {
        return (
            <Link to={to} className={`button ${className}`}>
                {children}
            </Link>
        );
    }

    if (href) {
        return (
            <a href={href} className={`button ${className}`}>
                {children}
            </a>
        );
    }

    return (
        <button type={type} onClick={onClick} className={`button ${className}`} disabled={disabled}>
            {children}
        </button>
    );
}

export default Button;

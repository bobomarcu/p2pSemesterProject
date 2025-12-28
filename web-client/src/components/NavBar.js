import React, { useState } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { Link, useLocation } from 'react-router-dom';
import Button from './Button'; // Import the new Button component
import '../styles/Navbar.css';

function Navbar() {
    const { keycloak, initialized } = useKeycloak();
    const [isOpen, setIsOpen] = useState(false);
    const location = useLocation();

    if (!initialized) {
        return <div>Loading Navbar...</div>; // Or a loading spinner
    }

    const toggleMenu = () => {
        setIsOpen(!isOpen);
    };

    return (
        <nav className="navbar">
            <Link to="/" className="navbar-brand">
                <div style={{display:"flex",alignItems:"center"}}>
                    <img src="/cs-logo.png" alt="csubb" className="navbar-logo"/>
                    <p>p2pFileShare</p>
                </div>
            </Link>
            <div className="menu-icon" onClick={toggleMenu}>
                <div />
                <div />
                <div />
            </div>
            <div className={`navbar-links ${isOpen ? 'active' : ''}`}>
                <div className="close-icon" onClick={toggleMenu}>&times;</div>
                {!keycloak.authenticated && (
                    <Button onClick={() => keycloak.login()}>
                        Login
                    </Button>
                )}

                {keycloak.authenticated && (
                    <>
                        <Button onClick={() => keycloak.logout()}>
                            Logout
                        </Button>
                    </>
                )}
            </div>
        </nav>
    );
}

export default Navbar;

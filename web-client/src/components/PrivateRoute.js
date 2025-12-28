import React, { useState, useEffect } from 'react';
import { useKeycloak } from '@react-keycloak/web';

function PrivateRoute({ children }) {
    const { keycloak, initialized } = useKeycloak();
    const [userInfoLoaded, setUserInfoLoaded] = useState(false);

    useEffect(() => {
        if (initialized && keycloak.authenticated) {
            keycloak.loadUserInfo()
                .then(() => {
                    setUserInfoLoaded(true);
                })
                .catch((error) => {
                    console.error('Failed to load user info in PrivateRoute:', error);
                    // Optionally, log out or show an error message
                    keycloak.logout();
                });
        } else if (initialized && !keycloak.authenticated) {
            setUserInfoLoaded(false);
        }
    }, [keycloak, initialized]);

    if (!initialized || !userInfoLoaded) {
        return <div>Loading Keycloak user info...</div>; // Or a loading spinner
    }

    if (!keycloak.authenticated) {
        keycloak.login();
        return null;
    }

    return children;
}

export default PrivateRoute;
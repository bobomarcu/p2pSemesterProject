import React, { useState, useEffect } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import P2PService from '../services/P2PService';
import '../styles/NodeStatus.css';

const NodeStatus = () => {
    const { keycloak } = useKeycloak();
    const [count, setCount] = useState(0);

    useEffect(() => {
        const fetchCount = async () => {
            if (keycloak.token) {
                const c = await P2PService.getNetworkStatus(keycloak.token);
                setCount(c);
            }
        };

        if (keycloak.authenticated) {
            fetchCount();
            const interval = setInterval(fetchCount, 10000); // 10s update
            return () => clearInterval(interval);
        }
    }, [keycloak.token, keycloak.authenticated]);

    if (!keycloak.authenticated) return null;

    return (
        <div className="node-status-popup">
            <div className="status-indicator"></div>
            <span>{count} Nodes Online</span>
        </div>
    );
};

export default NodeStatus;

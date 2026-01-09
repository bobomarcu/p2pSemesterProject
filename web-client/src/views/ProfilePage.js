import React, { useEffect, useState } from "react";
import Button from "../components/Button";
import { useKeycloak } from "@react-keycloak/web";
import TrackerService from "../services/TrackerService";
import '../styles/ProfilePage.css';

export default function ProfilePage() {
    const { keycloak } = useKeycloak();
    const [stats, setStats] = useState({ uploadCount: 0, totalDownloadsReceived: 0 });

    const username = keycloak.tokenParsed?.preferred_username || "N/A";
    const email = keycloak.tokenParsed?.email || "N/A";
    const givenName = keycloak.tokenParsed?.given_name || "";
    const familyName = keycloak.tokenParsed?.family_name || "";
    const fullName = `${givenName} ${familyName}`.trim() || username || "User";

    const avatarInitial = (givenName ? givenName[0] : (username[0] || "?")).toUpperCase();

    const playerId = keycloak.tokenParsed?.sub;

    useEffect(() => {
        const fetchStats = async () => {
            if (playerId && keycloak.token) {
                try {
                    await keycloak.updateToken(30);
                    const data = await TrackerService.getUserStats(keycloak.token);
                    setStats(data);
                } catch (error) {
                    console.error("Failed to load user stats", error);
                }
            }
        };
        fetchStats();
    }, [playerId, keycloak, keycloak.token]);

    return (
        <section className="profile-page-container">
            <div className="back-button-wrapper">
                <Button to="/">Back to Home</Button>
            </div>
            
            <div className="profile-header">
                <div className="profile-avatar">
                    {avatarInitial}
                </div>
                <div className="profile-info">
                    <h1 className="profile-name">{fullName}</h1>
                    <h2 className="profile-username">@{username}</h2>
                    
                    <div className="profile-meta">
                        <div className="meta-item">
                            <span>{email}</span>
                        </div>
                    </div>
                    <Button onClick={() => keycloak.accountManagement()} className="edit-profile-button button-neutral">
                        Edit Profile
                    </Button>
                </div>
            </div>

            <div className="profile-content">
                <div className="stats-container">
                    <h3>Analytics</h3>
                    <div className="profile-stats-grid">
                        <div className="profile-stat-card">
                            <span className="stat-number">{stats.uploadCount}</span>
                            <span className="stat-label">Uploads</span>
                        </div>
                        <div className="profile-stat-card">
                            <span className="stat-number">{stats.totalDownloadsReceived}</span>
                            <span className="stat-label">Downloads Received</span>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}

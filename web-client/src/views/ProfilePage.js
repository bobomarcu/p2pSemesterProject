import React, { useEffect, useState } from "react";
import Button from "../components/Button";
import { useKeycloak } from "@react-keycloak/web";
import TrackerService from "../services/TrackerService";
import '../styles/ProfilePage.css';

export default function ProfilePage() {
    const { keycloak } = useKeycloak();
    const [stats, setStats] = useState({ uploadCount: 0, totalDownloadsReceived: 0 });
    const [myManifests, setMyManifests] = useState([]);

    const username = keycloak.tokenParsed?.preferred_username || "N/A";
    const email = keycloak.tokenParsed?.email || "N/A";
    const givenName = keycloak.tokenParsed?.given_name || "";
    const familyName = keycloak.tokenParsed?.family_name || "";
    const fullName = `${givenName} ${familyName}`.trim() || username || "User";

    const avatarInitial = (givenName ? givenName[0] : (username[0] || "?")).toUpperCase();

    const playerId = keycloak.tokenParsed?.sub;

    useEffect(() => {
        const fetchData = async () => {
            if (playerId && keycloak.token) {
                try {
                    await keycloak.updateToken(30);
                    const [statsData, manifestsData] = await Promise.all([
                        TrackerService.getUserStats(keycloak.token),
                        TrackerService.getUserManifests(keycloak.token)
                    ]);
                    setStats(statsData);
                    setMyManifests(manifestsData);
                } catch (error) {
                    console.error("Failed to load user data", error);
                }
            }
        };
        fetchData();
    }, [playerId, keycloak, keycloak.token]);

    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to delete this manifest?")) {
            try {
                await TrackerService.deleteManifest(id, keycloak.token);
                setMyManifests(myManifests.filter(m => m.id !== id));
                // Update stats locally
                setStats(prev => ({ ...prev, uploadCount: prev.uploadCount - 1 }));
            } catch (error) {
                alert("Failed to delete manifest");
            }
        }
    };

    return (
        <section className="profile-page-container">
            <div className="back-button-wrapper">
                <Button to="/">Back to Home</Button>
            </div>
            
            <div className="profile-section profile-header">
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
                <div className="profile-section">
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

                <div className="profile-section">
                    <h3>My Uploads</h3>
                    {myManifests.length === 0 ? (
                        <p style={{ color: '#7f8c8d', fontStyle: 'italic' }}>You haven't uploaded any files yet.</p>
                    ) : (
                        <div className="upload-list">
                            {myManifests.map((manifest) => (
                                <div key={manifest.id} className="upload-item">
                                    <div className="upload-info">
                                        <div className="upload-name">
                                            {manifest.name}
                                            <span style={{marginLeft: '10px'}} className={manifest.private ? "badge badge-private" : "badge badge-public"}>
                                                {manifest.private ? "Private" : "Public"}
                                            </span>
                                        </div>
                                        <div className="upload-meta">
                                            <span>{new Date(manifest.uploadedAt).toLocaleDateString('en-GB')}</span>
                                            <span>•</span>
                                            <span>{manifest.downloadCount || 0} downloads</span>
                                        </div>
                                    </div>
                                    <div className="upload-actions">
                                        <Button className="button-danger" onClick={() => handleDelete(manifest.id)}>
                                            Delete
                                        </Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </section>
    );
}

import React from "react";
import Button from "../components/Button";
import { useKeycloak } from "@react-keycloak/web";
import '../styles/ProfilePage.css';

export default function ProfilePage() {
    const { keycloak } = useKeycloak();

    const username = keycloak.tokenParsed?.preferred_username || "N/A";
    const email = keycloak.tokenParsed?.email || "N/A";
    const givenName = keycloak.tokenParsed?.given_name || "";
    const familyName = keycloak.tokenParsed?.family_name || "";
    const fullName = `${givenName} ${familyName}`.trim() || username || "User";

    const avatarInitial = (givenName ? givenName[0] : (username[0] || "?")).toUpperCase();

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
                <div style={{ padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px', border: '1px solid #eee' }}>
                    <h3 style={{ marginTop: 0 }}>Activity</h3>
                    <p style={{ color: '#666' }}>No recent activity to show.</p>
                </div>
            </div>
        </section>
    );
}

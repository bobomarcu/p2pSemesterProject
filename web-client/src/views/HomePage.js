import React from "react";
import '../styles/HomePage.css';
import Button from "../components/Button";

export default function HomePage(){
    return(
        <section className="home-page-grid-container">
            <div className="recent-manifests-column">
                <h3>Recent Manifests</h3>
                <div className="manifest-list-placeholder">
                    <p>Manifest 1</p>
                    <p>Manifest 2</p>
                    <p>Manifest 3</p>
                </div>
                <div className="button-center-container">
                    <Button to="/manifests">Browse Other Manifests</Button>
                </div>
            </div>
            <div className="second-column-container">
                <div className="second-column-row-2">
                    <div className="upload-manifest-card">
                        <h3>Create Manifest</h3>
                        <Button to="/upload">Upload File</Button>
                    </div>
                </div>
                <div className="second-column-row-1">
                    <div className="profile-stats-container">
                        <h3>Your Stats</h3>
                        <div className="stats-grid">
                            <div className="stat-item">
                                <div className="stat-value">12</div>
                                <div className="stat-label">Uploads</div>
                            </div>
                            <div className="stat-item">
                                <div className="stat-value">450</div>
                                <div className="stat-label">Downloads</div>
                            </div>
                        </div>
                        <Button to="/profile">View Profile</Button>
                    </div>
                </div>
            </div>
        </section>
    )
}
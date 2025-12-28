import React from "react";
import '../styles/HomePage.css'; // Import the new CSS file
import Button from "../components/Button"; // Import Button component

export default function HomePage(){
    return(
        <section className="home-page-grid-container">
            <div className="recent-manifests-column">
                <h3>Recent Manifests</h3>
                {/* Placeholder for recent manifests list */}
                <div className="manifest-list-placeholder">
                    {/* Manifest items will go here */}
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
                        <h3>Upload Manifest</h3>
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
                                <div className="stat-label">Views</div>
                            </div>
                        </div>
                        <Button to="/profile">View Profile</Button>
                    </div>
                </div>
            </div>
        </section>
    )
}
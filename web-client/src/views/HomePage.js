import React, { useEffect, useState } from "react";
import '../styles/HomePage.css';
import Button from "../components/Button";
import TrackerService from "../services/TrackerService";
import { useKeycloak } from "@react-keycloak/web";

export default function HomePage(){
    const { keycloak } = useKeycloak();
    const [popularManifests, setPopularManifests] = useState([]);
    const [userStats, setUserStats] = useState({ uploadCount: 0, totalDownloadsReceived: 0 });
    const [loadingPopular, setLoadingPopular] = useState(true);

    useEffect(() => {
        if (keycloak.token) {
            const fetchPopular = async () => {
                try {
                    const data = await TrackerService.getTop5Manifests(keycloak.token);
                    setPopularManifests(data);
                } catch (error) {
                    console.error("Failed to fetch popular manifests", error);
                } finally {
                    setLoadingPopular(false);
                }
            };

            const fetchStats = async () => {
                try {
                    const data = await TrackerService.getUserStats(keycloak.token);
                    setUserStats(data);
                } catch (error) {
                    console.error("Failed to fetch user stats", error);
                }
            };

            fetchPopular();
            fetchStats();
        }
    }, [keycloak.token]);

    return(
        <section className="home-page-grid-container">
            <div className="recent-manifests-column">
                <h3>Popular Manifests</h3>
                <div className="manifest-list-container">
                    {loadingPopular ? (
                        <p>Loading...</p>
                    ) : popularManifests.length > 0 ? (
                        popularManifests.map((manifest) => (
                            <div key={manifest.id} className="mini-manifest-card">
                                <h4>{manifest.name}</h4>
                                <span className="download-count">{manifest.downloadCount} downloads</span>
                            </div>
                        ))
                    ) : (
                        <p>No popular manifests yet.</p>
                    )}
                </div>
                <div className="button-center-container">
                    <Button to="/browse">Browse All Manifests</Button>
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
                                <div className="stat-value">{userStats.uploadCount}</div>
                                <div className="stat-label">Uploads</div>
                            </div>
                            <div className="stat-item">
                                <div className="stat-value">{userStats.totalDownloadsReceived}</div>
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
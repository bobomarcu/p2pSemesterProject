import React, { useEffect, useState } from "react";
import Button from "../components/Button";
import TrackerService from "../services/TrackerService";
import P2PService from "../services/P2PService";
import { useKeycloak } from "@react-keycloak/web";
import "../styles/BrowseManifestsPage.css"; 

export default function BrowseManifestsPage() {
    const { keycloak } = useKeycloak();
    const [manifests, setManifests] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchManifests = async () => {
            try {
                const data = await TrackerService.getAllManifests(keycloak.token);
                setManifests(data);
            } catch (err) {
                setError("Failed to load manifests. Please try again later.");
            } finally {
                setLoading(false);
            }
        };

        if (keycloak.token) {
            fetchManifests();
        }
    }, [keycloak.token]);

    const handleDownload = async (manifest) => {
        try {
            const blob = await P2PService.downloadFile(manifest.id, manifest.owner, keycloak.token);
            
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', manifest.name);
            document.body.appendChild(link);
            link.click();
            
            link.parentNode.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (e) {
            console.error(e);
            alert("Download failed: " + e.message);
        }
    };

    const filteredManifests = manifests.filter(manifest => 
        manifest.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        manifest.owner.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <section className="browse-page-container">
            <div className="browse-header">
                <Button to="/">Back</Button>
                <h3>Browse Manifests</h3>
            </div>

            <div className="search-container">
                <input 
                    type="text" 
                    className="search-input"
                    placeholder="Search by name or owner..." 
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>
            
            {loading && <p>Loading...</p>}
            {error && <p className="error-message">{error}</p>}
            
            {!loading && !error && filteredManifests.length === 0 && (
                <p>No manifests found matching your search.</p>
            )}

            <div className="manifest-grid">
                {filteredManifests.map((manifest) => (
                    <div key={manifest.id} className="manifest-card">
                        <div className="card-header">
                            <h4>{manifest.name}</h4>
                            <span className="badge-public">Public</span>
                        </div>
                        <p className="manifest-desc">{manifest.description}</p>
                        <div className="manifest-meta">
                            <span className="meta-owner">@{manifest.owner}</span>
                            <span className="meta-date">{new Date(manifest.uploadedAt).toLocaleDateString()}</span>
                        </div>
                        <div className="manifest-actions">
                            <span className="download-info">
                                <strong>{manifest.downloadCount || 0}</strong> downloads
                            </span>
                            <Button className="download-button" onClick={() => handleDownload(manifest)}>Download</Button>
                        </div>
                    </div>
                ))}
            </div>
        </section>
    );
}

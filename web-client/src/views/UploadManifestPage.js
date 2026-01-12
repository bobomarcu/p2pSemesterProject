import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";
import '../styles/UploadManifestPage.css';
import Button from "../components/Button";
import TrackerService from "../services/TrackerService";

export default function UploadManifestPage() {
    const navigate = useNavigate();
    const { keycloak } = useKeycloak();
    const [file, setFile] = useState(null);
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [isPrivate, setIsPrivate] = useState(false);
    const [uploading, setUploading] = useState(false);

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            setFile(e.target.files[0]);
            if (!name) {
                setName(e.target.files[0].name);
            }
        }
    };

    const handleUpload = async () => {
        if (!file || !name) {
            alert("Please select a file and provide a name.");
            return;
        }

        setUploading(true);
        try {
            const metadata = {
                name,
                description,
                owner: keycloak.tokenParsed?.preferred_username || "unknown",
                userId: keycloak.tokenParsed?.sub,
                isPrivate
            };
            await TrackerService.uploadManifest(file, metadata, keycloak.token);
            alert("Upload successful!");
            navigate("/browse");
        } catch (error) {
            alert("Upload failed: " + error.message);
        } finally {
            setUploading(false);
        }
    };

    return (
        <section className="upload-page-container">
            <Button to="/">Back to Home</Button>
            <h3>Upload New Manifest</h3>
            
            <div className="upload-box" onClick={() => document.getElementById('file-upload').click()}>
                <input 
                    type="file" 
                    id="file-upload" 
                    style={{ display: 'none' }} 
                    onChange={handleFileChange}
                />
                <p>{file ? `Selected: ${file.name}` : "Click or Drag to Upload File"}</p>
            </div>

            <div className="form-group">
                <label className="form-label">Name</label>
                <input 
                    type="text" 
                    className="form-input" 
                    placeholder="Enter manifest name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
            </div>

            <div className="form-group">
                <label className="form-label">Description</label>
                <textarea 
                    className="form-textarea" 
                    placeholder="Enter description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                ></textarea>
            </div>

            <div className="form-group">
                <label className="form-label">Visibility</label>
                <div className="privacy-options">
                    <label className="privacy-option">
                        <input 
                            type="radio" 
                            name="privacy" 
                            checked={!isPrivate} 
                            onChange={() => setIsPrivate(false)} 
                        />
                        Public
                    </label>
                    <label className="privacy-option">
                        <input 
                            type="radio" 
                            name="privacy" 
                            checked={isPrivate} 
                            onChange={() => setIsPrivate(true)} 
                        />
                        Private
                    </label>
                </div>
            </div>

            <div className="submit-button-container">
                <Button onClick={handleUpload} disabled={uploading}>
                    {uploading ? "Uploading..." : "Create Manifest"}
                </Button>
            </div>
        </section>
    );
}

import React, { useState } from "react";
import '../styles/UploadManifestPage.css';
import Button from "../components/Button";

export default function UploadManifestPage() {
    const [fileName, setFileName] = useState("");
    const [isPrivate, setIsPrivate] = useState(false);

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            setFileName(e.target.files[0].name);
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
                <p>{fileName ? `Selected: ${fileName}` : "Click or Drag to Upload File"}</p>
            </div>

            <div className="form-group">
                <label className="form-label">Name</label>
                <input type="text" className="form-input" placeholder="Enter manifest name" />
            </div>

            <div className="form-group">
                <label className="form-label">Description</label>
                <textarea className="form-textarea" placeholder="Enter description"></textarea>
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
                <Button onClick={() => alert("Mock upload submitted!")}>Create Manifest</Button>
            </div>
        </section>
    );
}

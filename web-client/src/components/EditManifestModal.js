import React, { useState } from 'react';
import Button from './Button';
import '../styles/EditManifestModal.css';

const EditManifestModal = ({ manifest, isOpen, onClose, onSave }) => {
    const [name, setName] = useState(manifest ? manifest.name : '');
    const [description, setDescription] = useState(manifest ? manifest.description : '');
    const [isPrivate, setIsPrivate] = useState(manifest ? manifest.private : false);

    React.useEffect(() => {
        if (manifest) {
            setName(manifest.name);
            setDescription(manifest.description);
            setIsPrivate(manifest.private);
        }
    }, [manifest]);

    if (!isOpen) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        onSave(manifest.id, {
            name,
            description,
            isPrivate
        });
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h3>Edit Manifest</h3>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Name</label>
                        <input 
                            type="text" 
                            className="form-input"
                            value={name} 
                            onChange={(e) => setName(e.target.value)} 
                            required 
                        />
                    </div>
                    <div className="form-group">
                        <label>Description</label>
                        <textarea 
                            className="form-textarea"
                            value={description} 
                            onChange={(e) => setDescription(e.target.value)}
                        />
                    </div>
                    <div className="form-group">
                        <label>Visibility</label>
                        <div className="privacy-options">
                            <label className="privacy-option">
                                <input 
                                    type="radio" 
                                    name="edit-privacy"
                                    checked={!isPrivate} 
                                    onChange={() => setIsPrivate(false)} 
                                /> Public
                            </label>
                            <label className="privacy-option">
                                <input 
                                    type="radio" 
                                    name="edit-privacy"
                                    checked={isPrivate} 
                                    onChange={() => setIsPrivate(true)} 
                                /> Private
                            </label>
                        </div>
                    </div>
                    <div className="modal-actions">
                        <Button className="button-neutral" onClick={onClose}>Cancel</Button>
                        <Button type="submit">Save Changes</Button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditManifestModal;

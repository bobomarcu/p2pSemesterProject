const API_BASE_URL = "http://localhost:8282/api/tracker";

class TrackerService {
    
    async getAllManifests(token) {
        try {
            const response = await fetch(`${API_BASE_URL}/manifests`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error("Error fetching manifests:", error);
            throw error;
        }
    }

    async getTop5Manifests(token) {
        try {
            const response = await fetch(`${API_BASE_URL}/manifests/top5`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error("Error fetching top 5 manifests:", error);
            throw error;
        }
    }

    async getUserStats(token) {
        try {
            const response = await fetch(`${API_BASE_URL}/stats`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error("Error fetching user stats:", error);
            throw error;
        }
    }

    async uploadManifest(file, metadata, token) {
        // Convert file to Base64
        const toBase64 = file => new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result.split(',')[1]); // Remove the data url prefix
            reader.onerror = error => reject(error);
        });

        try {
            const base64Content = await toBase64(file);
            
            const payload = {
                metadata: {
                    name: metadata.name,
                    description: metadata.description,
                    owner: metadata.owner, 
                    userId: metadata.userId,
                    isPrivate: metadata.isPrivate
                },
                fileContentBase64: base64Content
            };

            const response = await fetch(`${API_BASE_URL}/upload`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error("Error uploading manifest:", error);
            throw error;
        }
    }
}

export default new TrackerService();

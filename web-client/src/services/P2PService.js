const BOOTSTRAP_URL = "http://localhost:5555"; // Default bootstrap

class P2PService {
    constructor() {
        this.cookieName = "p2p_host";
    }

    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }

    setCookie(name, value, days) {
        let expires = "";
        if (days) {
            const date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + (value || "") + expires + "; path=/";
    }

    async getActivePeer(token) {
        let peerHost = this.getCookie(this.cookieName);
        
        // If no cookie, fetch from bootstrap and set it
        if (!peerHost) {
            peerHost = await this.discoverAndSelectPeer(token);
        }
        return peerHost;
    }

    async discoverAndSelectPeer(token) {
        try {
            // Use bootstrap node to find peers
            const response = await fetch(`${BOOTSTRAP_URL}/cluster/peers`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (!response.ok) throw new Error("Failed to fetch peers");
            
            const peers = await response.json();
            if (peers.length === 0) throw new Error("No peers available");

            // Select random peer
            const selected = peers[Math.floor(Math.random() * peers.length)];
            const hostUrl = `http://${selected.host}:${selected.port}`;
            
            this.setCookie(this.cookieName, hostUrl, 7); // Save for 7 days
            return hostUrl;
        } catch (error) {
            console.error("Peer discovery failed:", error);
            // Fallback to bootstrap if discovery fails
            return BOOTSTRAP_URL;
        }
    }

    async downloadFile(fileId, ownerId, token) {
        try {
            const peerUrl = await this.getActivePeer(token);
            console.log(`Downloading file ${fileId} from ${peerUrl}`);

            const response = await fetch(`${peerUrl}/node/files/${fileId}?ownerId=${ownerId}`, {
                 headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                 // If 404 or connection refused/error, maybe clear cookie and retry next time
                 if(response.status !== 401 && response.status !== 403) {
                     this.setCookie(this.cookieName, "", -1); // Delete cookie
                 }
                 throw new Error(`Download failed from ${peerUrl}: ${response.statusText}`);
            }
            return await response.blob();
        } catch (error) {
            console.error("Download error:", error);
            // Clear cookie on error to force rediscovery next time
            this.setCookie(this.cookieName, "", -1);
            throw error;
        }
    }

    async getNetworkStatus(token) {
        try {
            const response = await fetch(`${BOOTSTRAP_URL}/cluster/peers`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (!response.ok) return 0;
            const peers = await response.json();
            return peers.length;
        } catch (error) {
            return 0;
        }
    }
}

export default new P2PService();

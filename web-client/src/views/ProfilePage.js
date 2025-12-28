import React from "react";
import Button from "../components/Button";

export default function ProfilePage() {
    return (
        <section style={{ padding: '20px' }}>
            <Button to="/">Back to Home</Button>
            <h3>User Profile</h3>
            <p>Welcome to your profile page.</p>
            {/* Mock Profile Details */}
            <div>
                <p><strong>Username:</strong> mock_user</p>
                <p><strong>Email:</strong> user@example.com</p>
            </div>
        </section>
    );
}

"use client";
import { useEffect, useState } from "react";

export default function Dashboard() {
  const [data, setData] = useState(null);

  useEffect(() => {
    // Fetch data from our API every 5 seconds
    const interval = setInterval(() => {
      fetch("/api/status")
        .then((res) => res.json())
        .then((data) => setData(data))
        .catch((err) => console.error("Error fetching data:", err));
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  return (
    <main style={{ maxWidth: "800px", margin: "0 auto", padding: "20px" }}>
      <h1 style={{ color: "#1e3a8a" }}>👶 SAGE Parent Dashboard</h1>
      
      <div style={{ background: "white", padding: "20px", borderRadius: "12px", marginBottom: "20px", boxShadow: "0 4px 6px rgba(0,0,0,0.1)" }}>
        <h2>📍 Live Location</h2>
        {data && data.lat !== 0 ? (
          <>
            <p><strong>Latitude:</strong> {data.lat}</p>
            <p><strong>Longitude:</strong> {data.lng}</p>
            <p><strong>Last Updated:</strong> {data.timestamp ? new Date(data.timestamp).toLocaleString() : 'Unknown'}</p>
            <a 
              href={`https://www.google.com/maps?q=${data.lat},${data.lng}`} 
              target="_blank" 
              rel="noopener noreferrer"
              style={{ color: "#2563eb", textDecoration: "underline" }}
            >
              View on Google Maps
            </a>
          </>
        ) : (
          <p>Waiting for child's device to send location...</p>
        )}
      </div>

      <div style={{ background: "white", padding: "20px", borderRadius: "12px", boxShadow: "0 4px 6px rgba(0,0,0,0.1)" }}>
        <h2>⏰ App Usage Alerts</h2>
        {data && data.usage && data.usage !== "Waiting for data..." && data.usage !== "No data yet" ? (
          <pre style={{ whiteSpace: "pre-wrap", fontFamily: "inherit", fontSize: "16px" }}>{data.usage}</pre>
        ) : (
          <p>No usage data reported yet.</p>
        )}
      </div>
    </main>
  );
}
